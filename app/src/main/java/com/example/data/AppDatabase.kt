package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TimetableTask::class,
        SubjectProgress::class,
        WeeklyPlan::class,
        ProgrammingTracker::class,
        SelfImprovementTracker::class,
        Habit::class,
        HabitLog::class,
        NoteItem::class,
        GoalMetric::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timetableDao(): TimetableDao
    abstract fun subjectDao(): SubjectDao
    abstract fun weeklyDao(): WeeklyDao
    abstract fun programmingDao(): ProgrammingDao
    abstract fun selfImprovementDao(): SelfImprovementDao
    abstract fun habitDao(): HabitDao
    abstract fun notesDao(): NotesDao
    abstract fun goalsDao(): GoalsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_life_os_db"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }
    }
}

suspend fun seedDatabase(db: AppDatabase) {
    // 1. Seed Default Timetable Tasks
    val defaultTasks = listOf(
        TimetableTask(name = "Wake Up", timeRange = "5:00 AM", type = "OTHER", status = "PENDING"),
        TimetableTask(name = "Exercise", timeRange = "5:20 AM - 6:00 AM", type = "OTHER", status = "PENDING"),
        TimetableTask(name = "Meditation", timeRange = "6:00 AM - 6:30 AM", type = "OTHER", status = "PENDING"),
        TimetableTask(name = "Deep Study Block", timeRange = "6:30 AM - 8:00 AM", type = "STUDY", status = "PENDING"),
        TimetableTask(name = "Classes", timeRange = "9:00 AM - 11:15 AM", type = "STUDY", status = "PENDING"),
        TimetableTask(name = "Programming", timeRange = "12:00 PM - 2:00 PM", type = "PROGRAMMING", status = "PENDING"),
        TimetableTask(name = "Study Block", timeRange = "2:30 PM - 3:30 PM", type = "STUDY", status = "PENDING"),
        TimetableTask(name = "Library Study", timeRange = "4:00 PM - 7:00 PM", type = "STUDY", status = "PENDING"),
        TimetableTask(name = "Temple/Puja", timeRange = "7:00 PM - 8:00 PM", type = "OTHER", status = "PENDING"),
        TimetableTask(name = "Programming / Self Improvement", timeRange = "8:45 PM - 10:00 PM", type = "PROGRAMMING", status = "PENDING"),
        TimetableTask(name = "Sleep", timeRange = "10:30 PM", type = "OTHER", status = "PENDING")
    )
    db.timetableDao().insertTasks(defaultTasks)

    // 2. Seed Commerce Subjects
    val subjects = listOf("Accounts", "Mathematics", "Economics", "OCM", "Secretarial Practice", "English", "Hindi")
    val defaultSubjects = subjects.map {
        SubjectProgress(subjectName = it, currentChapter = 0, totalChapters = 12, notes = "Preparation for Boards", completionStatus = "IN_PROGRESS", revisionStatus = "NOT_REVISED")
    }
    db.subjectDao().insertSubjects(defaultSubjects)

    // 3. Seed Weekly Planner (Mon-Sun)
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val defaultPlans = days.map {
        WeeklyPlan(dayOfWeek = it, topicsCompleted = "No topics completed yet.", chaptersCompleted = 0, testsGiven = 0, weakAreas = "None")
    }
    db.weeklyDao().insertPlans(defaultPlans)

    // 4. Seed Programming Languages Tracks
    val tracks = listOf("Python", "Web Development", "AI", "Projects")
    val defaultTracks = tracks.map {
        ProgrammingTracker(trackName = it, learningHours = 0.0, projectsCompleted = 0, conceptsLearned = "To be learned", notes = "Goal: Learn core concepts")
    }
    db.programmingDao().insertTrackers(defaultTracks)

    // 5. Seed Self Improvement Tracker
    val improvements = listOf("Handwriting", "English Communication", "Reading", "Typing Speed", "Discipline")
    val defaultImprovements = improvements.map {
        SelfImprovementTracker(topicName = it, focusText = "Daily deliberate practice", levelPercent = 30, notes = "")
    }
    db.selfImprovementDao().insertImprovements(defaultImprovements)

    // 6. Seed Habits Catalog
    val habits = listOf("Wake up on time", "Exercise", "Meditation", "Study", "Programming", "Temple/Puja", "Reading", "Sleep before 10:30 PM")
    val defaultHabits = habits.map {
        Habit(name = it, streak = 0, monthlyStreak = 0)
    }
    db.habitDao().insertHabits(defaultHabits)

    // 7. Seed Initial Goals
    val defaultGoals = listOf(
        GoalMetric(name = "Score 85%+ in Class 12 Boards", targetValue = 100.0, currentValue = 0.0, unit = "%", category = "MAIN"),
        GoalMetric(name = "Learn Programming (Hands-On Coding)", targetValue = 100.0, currentValue = 20.0, unit = "hrs", category = "SECONDARY"),
        GoalMetric(name = "Improve Handwriting (Daily page style)", targetValue = 10.0, currentValue = 4.0, unit = "/ 10 Score", category = "SECONDARY"),
        GoalMetric(name = "Build Discipline (Habits Track)", targetValue = 100.0, currentValue = 15.0, unit = "%", category = "SECONDARY")
    )
    db.goalsDao().insertGoals(defaultGoals)

    // 8. Seed Default Notes
    db.notesDao().insertNote(
        NoteItem(
            title = "Board Exams Prep Strategy",
            content = "Keep Accounts and Mathematics on high priority! Solve previous year's papers for Economics and OCM. Revise Secretarial Practice definitions weekly.",
            category = "STUDY"
        )
    )
    db.notesDao().insertNote(
        NoteItem(
            title = "Python and AI Goals",
            content = "1. Learn basics of Pandas/NumPy.\n2. Complete a small web layout projects.\n3. Integrate Gemini LLM APIs into dynamic Python scripts.",
            category = "PROGRAMMING"
        )
    )
}
