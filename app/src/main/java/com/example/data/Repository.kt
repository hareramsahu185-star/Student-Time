package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {

    // DAOs
    private val timetableDao = db.timetableDao()
    private val subjectDao = db.subjectDao()
    private val weeklyDao = db.weeklyDao()
    private val programmingDao = db.programmingDao()
    private val selfImprovementDao = db.selfImprovementDao()
    private val habitDao = db.habitDao()
    private val notesDao = db.notesDao()
    private val goalsDao = db.goalsDao()

    // Flows
    val allTasks: Flow<List<TimetableTask>> = timetableDao.getAllTasks()
    val allSubjects: Flow<List<SubjectProgress>> = subjectDao.getAllSubjects()
    val weeklyPlans: Flow<List<WeeklyPlan>> = weeklyDao.getWeeklyPlans()
    val programmingTrackers: Flow<List<ProgrammingTracker>> = programmingDao.getTrackers()
    val selfImprovements: Flow<List<SelfImprovementTracker>> = selfImprovementDao.getTrackers()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val habitLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()
    val allNotes: Flow<List<NoteItem>> = notesDao.getAllNotes()
    val allGoals: Flow<List<GoalMetric>> = goalsDao.getGoals()

    // Timetable operations
    suspend fun insertTask(task: TimetableTask) = timetableDao.insertTask(task)
    suspend fun updateTask(task: TimetableTask) = timetableDao.updateTask(task)
    suspend fun deleteTask(task: TimetableTask) = timetableDao.deleteTask(task)
    suspend fun clearTimetable() = timetableDao.clearAll()
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
    suspend fun deleteHabitLog(habitName: String, dateStr: String) = habitDao.deleteLog(habitName, dateStr)
    suspend fun clearHabits() {
        habitDao.clearAll()
        habitDao.clearAllLogs()
    }

    // Notes operations
    suspend fun insertNote(note: NoteItem) = notesDao.insertNote(note)
    suspend fun deleteNote(note: NoteItem) = notesDao.deleteNote(note)

    // Goal operations
    suspend fun insertGoal(goal: GoalMetric) = goalsDao.insertGoal(goal)

    // Reset database to initial template
    suspend fun resetDatabase() {
        db.runInTransaction {
            // Since we can't run suspend calls directly inside runInTransaction, 
            // we will run them sequentially in background context or just call clear operations.
        }
        timetableDao.clearAll()
        subjectDao.clearAll()
        weeklyDao.clearAll()
        programmingDao.clearAll()
        selfImprovementDao.clearAll()
        habitDao.clearAll()
        habitDao.clearAllLogs()
        notesDao.clearAll()
        goalsDao.clearAll()

        seedDatabase(db)
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
        val goals: List<GoalMetric>
    )

    // Export all data as JSON
    suspend fun exportAsJson(): String {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(BackupData::class.java)

        val backup = BackupData(
            tasks = allTasks.first(),
            subjects = allSubjects.first(),
            weeklyPlans = weeklyPlans.first(),
            programmingTracks = programmingTrackers.first(),
            selfImprovements = selfImprovements.first(),
            habits = allHabits.first(),
            habitLogs = habitLogs.first(),
            notes = allNotes.first(),
            goals = allGoals.first()
        )
        return adapter.toJson(backup)
    }

    // Import all data from JSON
    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(BackupData::class.java)
            val backup = adapter.fromJson(jsonString) ?: return false

            timetableDao.clearAll()
            timetableDao.insertTasks(backup.tasks)

            subjectDao.clearAll()
            subjectDao.insertSubjects(backup.subjects)

            weeklyDao.clearAll()
            weeklyDao.insertPlans(backup.weeklyPlans)

            programmingDao.clearAll()
            programmingDao.insertTrackers(backup.programmingTracks)

            selfImprovementDao.clearAll()
            selfImprovementDao.insertImprovements(backup.selfImprovements)

            habitDao.clearAll()
            habitDao.insertHabits(backup.habits)

            habitDao.clearAllLogs()
            backup.habitLogs.forEach { log ->
                habitDao.insertLog(log)
            }

            notesDao.clearAll()
            backup.notes.forEach { note ->
                notesDao.insertNote(note)
            }

            goalsDao.clearAll()
            goalsDao.insertGoals(backup.goals)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
