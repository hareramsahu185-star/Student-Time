package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_tasks ORDER BY id ASC")
    fun getAllTasks(): Flow<List<TimetableTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TimetableTask)

    @Update
    suspend fun updateTask(task: TimetableTask)

    @Delete
    suspend fun deleteTask(task: TimetableTask)

    @Query("DELETE FROM timetable_tasks")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TimetableTask>)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject_progress")
    fun getAllSubjects(): Flow<List<SubjectProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(sub: SubjectProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subs: List<SubjectProgress>)

    @Query("DELETE FROM subject_progress")
    suspend fun clearAll()
}

@Dao
interface WeeklyDao {
    @Query("SELECT * FROM weekly_planner")
    fun getWeeklyPlans(): Flow<List<WeeklyPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WeeklyPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<WeeklyPlan>)

    @Query("DELETE FROM weekly_planner")
    suspend fun clearAll()
}

@Dao
interface ProgrammingDao {
    @Query("SELECT * FROM programming_tracker")
    fun getTrackers(): Flow<List<ProgrammingTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(track: ProgrammingTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackers(tracks: List<ProgrammingTracker>)

    @Query("DELETE FROM programming_tracker")
    suspend fun clearAll()
}

@Dao
interface SelfImprovementDao {
    @Query("SELECT * FROM self_improvement")
    fun getTrackers(): Flow<List<SelfImprovementTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImprovement(item: SelfImprovementTracker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImprovements(items: List<SelfImprovementTracker>)

    @Query("DELETE FROM self_improvement")
    suspend fun clearAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Query("SELECT * FROM habit_logs WHERE dateStr = :dateStr")
    fun getLogsForDate(dateStr: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs")
    fun getAllLogs(): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitName = :habitName AND dateStr = :dateStr")
    suspend fun deleteLog(habitName: String, dateStr: String)

    @Query("DELETE FROM habits")
    suspend fun clearAll()

    @Query("DELETE FROM habit_logs")
    suspend fun clearAllLogs()
}

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY lastUpdated DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

@Dao
interface GoalsDao {
    @Query("SELECT * FROM goals")
    fun getGoals(): Flow<List<GoalMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalMetric)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalMetric>)

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}
