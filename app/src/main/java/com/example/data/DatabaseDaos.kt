package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserLocal)

    @Update
    suspend fun updateUser(user: UserLocal)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserLocal>>
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_tasks WHERE username = :username ORDER BY id ASC")
    fun getAllTasks(username: String): Flow<List<TimetableTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TimetableTask)

    @Update
    suspend fun updateTask(task: TimetableTask)

    @Delete
    suspend fun deleteTask(task: TimetableTask)

    @Query("DELETE FROM timetable_tasks WHERE username = :username")
    suspend fun clearAll(username: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TimetableTask>)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject_progress WHERE username = :username")
    fun getAllSubjects(username: String): Flow<List<SubjectProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(sub: SubjectProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subs: List<SubjectProgress>)

    @Query("DELETE FROM subject_progress WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface WeeklyDao {
    @Query("SELECT * FROM weekly_planner WHERE username = :username")
    fun getWeeklyPlans(username: String): Flow<List<WeeklyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WeeklyPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<WeeklyPlan>)

    @Query("DELETE FROM weekly_planner WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface ProgrammingDao {
    @Query("SELECT * FROM programming_tracker WHERE username = :username")
    fun getTrackers(username: String): Flow<List<ProgrammingTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(track: ProgrammingTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackers(tracks: List<ProgrammingTracker>)

    @Query("DELETE FROM programming_tracker WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface SelfImprovementDao {
    @Query("SELECT * FROM self_improvement WHERE username = :username")
    fun getTrackers(username: String): Flow<List<SelfImprovementTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImprovement(item: SelfImprovementTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImprovements(items: List<SelfImprovementTracker>)

    @Query("DELETE FROM self_improvement WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE username = :username")
    fun getAllHabits(username: String): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Query("SELECT * FROM habit_logs WHERE username = :username AND dateStr = :dateStr")
    fun getLogsForDate(username: String, dateStr: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE username = :username")
    fun getAllLogs(username: String): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE username = :username AND habitName = :habitName AND dateStr = :dateStr")
    suspend fun deleteLog(username: String, habitName: String, dateStr: String)

    @Query("DELETE FROM habits WHERE username = :username")
    suspend fun clearAll(username: String)

    @Query("DELETE FROM habit_logs WHERE username = :username")
    suspend fun clearAllLogs(username: String)
}

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE username = :username ORDER BY lastUpdated DESC")
    fun getAllNotes(username: String): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("DELETE FROM notes WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface GoalsDao {
    @Query("SELECT * FROM goals WHERE username = :username")
    fun getGoals(username: String): Flow<List<GoalMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalMetric)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalMetric>)

    @Query("DELETE FROM goals WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE username = :username ORDER BY timestamp DESC")
    fun getStudySessions(username: String): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface ChapterStatusDao {
    @Query("SELECT * FROM chapter_status WHERE username = :username")
    fun getChapterStatuses(username: String): Flow<List<ChapterStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterStatus(cs: ChapterStatus)

    @Query("DELETE FROM chapter_status WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM learning_journal WHERE username = :username ORDER BY timestamp DESC")
    fun getJournalEntries(username: String): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(entry: JournalEntry)

    @Query("DELETE FROM learning_journal WHERE username = :username")
    suspend fun clearAll(username: String)
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY xp DESC")
    fun getFriends(): Flow<List<Friend>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Update
    suspend fun updateFriend(friend: Friend)

    @Query("DELETE FROM friends WHERE friendUsername = :name")
    suspend fun deleteFriend(name: String)

    @Query("DELETE FROM friends")
    suspend fun clearAll()
}
