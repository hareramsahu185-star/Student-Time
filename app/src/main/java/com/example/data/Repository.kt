package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    // DAOs
    val userDao = db.userDao()
    val timetableDao = db.timetableDao()
    val subjectDao = db.subjectDao()
    val weeklyDao = db.weeklyDao()
    val programmingDao = db.programmingDao()
    val selfImprovementDao = db.selfImprovementDao()
    val habitDao = db.habitDao()
    val notesDao = db.notesDao()
    val goalsDao = db.goalsDao()
    val studySessionDao = db.studySessionDao()
    val chapterStatusDao = db.chapterStatusDao()
    val journalDao = db.journalDao()
    val friendDao = db.friendDao()

    // Scoped Flows based on Username
    fun allTasks(username: String): Flow<List<TimetableTask>> = timetableDao.getAllTasks(username)
    fun allSubjects(username: String): Flow<List<SubjectProgress>> = subjectDao.getAllSubjects(username)
    fun weeklyPlans(username: String): Flow<List<WeeklyPlan>> = weeklyDao.getWeeklyPlans(username)
    fun programmingTrackers(username: String): Flow<List<ProgrammingTracker>> = programmingDao.getTrackers(username)
    fun selfImprovements(username: String): Flow<List<SelfImprovementTracker>> = selfImprovementDao.getTrackers(username)
    fun allHabits(username: String): Flow<List<Habit>> = habitDao.getAllHabits(username)
    fun habitLogs(username: String): Flow<List<HabitLog>> = habitDao.getAllLogs(username)
    fun allNotes(username: String): Flow<List<NoteItem>> = notesDao.getAllNotes(username)
    fun allGoals(username: String): Flow<List<GoalMetric>> = goalsDao.getGoals(username)
    fun studySessions(username: String): Flow<List<StudySession>> = studySessionDao.getStudySessions(username)
    fun chapterStatuses(username: String): Flow<List<ChapterStatus>> = chapterStatusDao.getChapterStatuses(username)
    fun journalEntries(username: String): Flow<List<JournalEntry>> = journalDao.getJournalEntries(username)
    val allFriends: Flow<List<Friend>> = friendDao.getFriends()

    // Timetable operations
    suspend fun insertTask(task: TimetableTask) = timetableDao.insertTask(task)
    suspend fun updateTask(task: TimetableTask) = timetableDao.updateTask(task)
    suspend fun deleteTask(task: TimetableTask) = timetableDao.deleteTask(task)
    suspend fun clearTimetable(username: String) = timetableDao.clearAll(username)
    suspend fun insertTasks(tasks: List<TimetableTask>) = timetableDao.insertTasks(tasks)

    // Subject operations
    suspend fun insertSubject(sub: SubjectProgress) = subjectDao.insertSubject(sub)
    suspend fun insertSubjects(subs: List<SubjectProgress>) = subjectDao.insertSubjects(subs)

    // Weekly operations
    suspend fun insertPlan(plan: WeeklyPlan) = weeklyDao.insertPlan(plan)

    // Programming operations
    suspend fun insertProgrammingTrack(track: ProgrammingTracker) = programmingDao.insertTracker(track)

    // Self Improvement operations
    suspend fun insertSelfImprovement(item: SelfImprovementTracker) = selfImprovementDao.insertImprovement(item)

    // Habit operations
    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun insertHabitLog(log: HabitLog) = habitDao.insertLog(log)
    suspend fun deleteHabitLog(username: String, habitName: String, dateStr: String) = habitDao.deleteLog(username, habitName, dateStr)
    suspend fun getLogsForDate(username: String, dateStr: String): Flow<List<HabitLog>> = habitDao.getLogsForDate(username, dateStr)
    suspend fun clearHabits(username: String) {
        habitDao.clearAll(username)
        habitDao.clearAllLogs(username)
    }

    // Notes operations
    suspend fun insertNote(note: NoteItem) = notesDao.insertNote(note)
    suspend fun deleteNote(note: NoteItem) = notesDao.deleteNote(note)

    // Goal operations
    suspend fun insertGoal(goal: GoalMetric) = goalsDao.insertGoal(goal)

    // Study Sessions
    suspend fun insertStudySession(session: StudySession) = studySessionDao.insertSession(session)
    suspend fun clearSessions(username: String) = studySessionDao.clearAll(username)

    // Chapter Status
    suspend fun insertChapterStatus(cs: ChapterStatus) = chapterStatusDao.insertChapterStatus(cs)
    suspend fun clearChapterStatuses(username: String) = chapterStatusDao.clearAll(username)

    // Journal
    suspend fun insertJournal(entry: JournalEntry) = journalDao.insertJournal(entry)
    suspend fun clearJournal(username: String) = journalDao.clearAll(username)

    // Friends
    suspend fun insertFriend(friend: Friend) = friendDao.insertFriend(friend)
    suspend fun updateFriend(friend: Friend) = friendDao.updateFriend(friend)
    suspend fun deleteFriend(name: String) = friendDao.deleteFriend(name)
    suspend fun clearFriends() = friendDao.clearAll()

    // Authentication local helpers
    suspend fun registerUser(username: String, passwordHash: String, securityAnswer: String): Boolean {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) return false
        val newUser = UserLocal(
            username = username,
            passwordHash = passwordHash,
            securityAnswer = securityAnswer,
            xp = 100,
            coins = 10,
            level = 1,
            streak = 1,
            isVerified = true
        )
        userDao.insertUser(newUser)
        seedDatabaseForUser(db, username)
        return true
    }

    suspend fun loginUser(username: String, passwordHash: String): UserLocal? {
        val user = userDao.getUserByUsername(username) ?: return null
        return if (user.passwordHash == passwordHash) user else null
    }

    suspend fun verifyReset(username: String, answer: String, newPasswordHash: String): Boolean {
        val user = userDao.getUserByUsername(username) ?: return false
        if (user.securityAnswer.trim().equals(answer.trim(), ignoreCase = true)) {
            userDao.insertUser(user.copy(passwordHash = newPasswordHash))
            return true
        }
        return false
    }

    suspend fun updateUserProfile(user: UserLocal) {
        userDao.updateUser(user)
    }

    // Reset database to initial template
    suspend fun resetDatabase(username: String) {
        timetableDao.clearAll(username)
        subjectDao.clearAll(username)
        weeklyDao.clearAll(username)
        programmingDao.clearAll(username)
        selfImprovementDao.clearAll(username)
        habitDao.clearAll(username)
        habitDao.clearAllLogs(username)
        notesDao.clearAll(username)
        goalsDao.clearAll(username)
        studySessionDao.clearAll(username)
        chapterStatusDao.clearAll(username)
        journalDao.clearAll(username)

        seedDatabaseForUser(db, username)
    }

    // JSON Backup structure helper
    data class BackupData(
        val tasks: List<TimetableTask>,
        val subjects: List<SubjectProgress>,
        val weeklyPlans: List<WeeklyPlan>,
        val programmingTracks: List<ProgrammingTracker>,
        val selfImprovements: List<SelfImprovementTracker>,
        val habits: List<Habit>,
        val habitLogs: List<HabitLog>,
        val notes: List<NoteItem>,
        val goals: List<GoalMetric>,
        val studySessions: List<StudySession>,
        val chapterStatuses: List<ChapterStatus>,
        val journalEntries: List<JournalEntry>
    )

    // Export user data as JSON
    suspend fun exportAsJson(username: String): String {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(BackupData::class.java)

        val backup = BackupData(
            tasks = allTasks(username).first(),
            subjects = allSubjects(username).first(),
            weeklyPlans = weeklyPlans(username).first(),
            programmingTracks = programmingTrackers(username).first(),
            selfImprovements = selfImprovements(username).first(),
            habits = allHabits(username).first(),
            habitLogs = habitLogs(username).first(),
            notes = allNotes(username).first(),
            goals = allGoals(username).first(),
            studySessions = studySessions(username).first(),
            chapterStatuses = chapterStatuses(username).first(),
            journalEntries = journalEntries(username).first()
        )
        return adapter.toJson(backup)
    }

    // Import user data from JSON
    suspend fun importFromJson(username: String, jsonString: String): Boolean {
        return try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(BackupData::class.java)
            val backup = adapter.fromJson(jsonString) ?: return false

            timetableDao.clearAll(username)
            timetableDao.insertTasks(backup.tasks.map { it.copy(username = username) })

            subjectDao.clearAll(username)
            subjectDao.insertSubjects(backup.subjects.map { it.copy(username = username) })

            weeklyDao.clearAll(username)
            weeklyDao.insertPlans(backup.weeklyPlans.map { it.copy(username = username) })

            programmingDao.clearAll(username)
            programmingDao.insertTrackers(backup.programmingTracks.map { it.copy(username = username) })

            selfImprovementDao.clearAll(username)
            selfImprovementDao.insertImprovements(backup.selfImprovements.map { it.copy(username = username) })

            habitDao.clearAll(username)
            habitDao.insertHabits(backup.habits.map { it.copy(username = username) })

            habitDao.clearAllLogs(username)
            backup.habitLogs.forEach { log ->
                habitDao.insertLog(log.copy(username = username))
            }

            notesDao.clearAll(username)
            backup.notes.forEach { note ->
                notesDao.insertNote(note.copy(username = username))
            }

            goalsDao.clearAll(username)
            goalsDao.insertGoals(backup.goals.map { it.copy(username = username) })

            studySessionDao.clearAll(username)
            backup.studySessions.forEach {
                studySessionDao.insertSession(it.copy(username = username))
            }

            chapterStatusDao.clearAll(username)
            backup.chapterStatuses.forEach {
                chapterStatusDao.insertChapterStatus(it.copy(username = username))
            }

            journalDao.clearAll(username)
            backup.journalEntries.forEach {
                journalDao.insertJournal(it.copy(username = username))
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
