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
        UserLocal::class,
        TimetableTask::class,
        SubjectProgress::class,
        WeeklyPlan::class,
        ProgrammingTracker::class,
        SelfImprovementTracker::class,
        Habit::class,
        HabitLog::class,
        NoteItem::class,
        GoalMetric::class,
        StudySession::class,
        ChapterStatus::class,
        JournalEntry::class,
        Friend::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun timetableDao(): TimetableDao
    abstract fun subjectDao(): SubjectDao
    abstract fun weeklyDao(): WeeklyDao
    abstract fun programmingDao(): ProgrammingDao
    abstract fun selfImprovementDao(): SelfImprovementDao
    abstract fun habitDao(): HabitDao
    abstract fun notesDao(): NotesDao
    abstract fun goalsDao(): GoalsDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun chapterStatusDao(): ChapterStatusDao
    abstract fun journalDao(): JournalDao
    abstract fun friendDao(): FriendDao

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
                .fallbackToDestructiveMigration()
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
                    seedDatabaseForUser(database, "default")
                }
            }
        }
    }
}

suspend fun seedDatabaseForUser(db: AppDatabase, username: String) {
    // 1. Seed Default Timetable Tasks
    val defaultTasks = listOf(
        TimetableTask(name = "Wake Up", timeRange = "5:00 AM", type = "OTHER", status = "PENDING", username = username),
        TimetableTask(name = "Exercise", timeRange = "5:20 AM - 6:00 AM", type = "OTHER", status = "PENDING", username = username),
        TimetableTask(name = "Meditation", timeRange = "6:00 AM - 6:30 AM", type = "OTHER", status = "PENDING", username = username),
        TimetableTask(name = "Deep Study Block", timeRange = "6:30 AM - 8:00 AM", type = "STUDY", status = "PENDING", username = username),
        TimetableTask(name = "Classes", timeRange = "9:00 AM - 11:15 AM", type = "STUDY", status = "PENDING", username = username),
        TimetableTask(name = "Programming", timeRange = "12:00 PM - 2:00 PM", type = "PROGRAMMING", status = "PENDING", username = username),
        TimetableTask(name = "Study Block", timeRange = "2:30 PM - 3:30 PM", type = "STUDY", status = "PENDING", username = username),
        TimetableTask(name = "Library Study", timeRange = "4:00 PM - 7:00 PM", type = "STUDY", status = "PENDING", username = username),
        TimetableTask(name = "Temple/Puja", timeRange = "7:00 PM - 8:00 PM", type = "OTHER", status = "PENDING", username = username),
        TimetableTask(name = "Programming / Self Improvement", timeRange = "8:45 PM - 10:00 PM", type = "PROGRAMMING", status = "PENDING", username = username),
        TimetableTask(name = "Sleep", timeRange = "10:30 PM", type = "OTHER", status = "PENDING", username = username)
    )
    db.timetableDao().insertTasks(defaultTasks)

    // 2. Seed Commerce Subjects
    val subjects = listOf("Accounts", "Mathematics", "Economics", "OCM", "Secretarial Practice", "English", "Hindi")
    val defaultSubjects = subjects.map {
        SubjectProgress(subjectName = it, currentChapter = 0, totalChapters = 12, notes = "Preparation for boards.", completionStatus = "IN_PROGRESS", revisionStatus = "NOT_REVISED", username = username)
    }
    db.subjectDao().insertSubjects(defaultSubjects)

    // 2.1 Seed Chapter status
    val defaultChapters = mapOf(
        "Accounts" to listOf("Introduction to Partnership", "Admission of Partner", "Retirement of Partner", "Death of Partner", "Dissolution of Partnership"),
        "Mathematics" to listOf("Mathematical Logic", "Matrices", "Differentiation", "Applications of Derivatives", "Integration"),
        "Economics" to listOf("Introduction to Micro and Macro", "Utility Analysis", "Demand Analysis", "Elasticity of Demand", "Forms of Market"),
        "OCM" to listOf("Principles of Management", "Functions of Management", "Entrepreneurship Development", "Business Services"),
        "Secretarial Practice" to listOf("Sources of Corporate Finance", "Capital Structure", "Issue of Shares", "Issue of Debentures"),
        "English" to listOf("An Astrologer's Day", "The Die-Hard Hobby", "The Cop and the Anthem", "Big Data - Big Insights"),
        "Hindi" to listOf("Prerna", "Surya Dev", "Swaroop", "Ujaala", "Samay")
    )
    defaultChapters.forEach { (sub, chs) ->
        chs.forEach { ch ->
            db.chapterStatusDao().insertChapterStatus(ChapterStatus(username = username, subject = sub, chapterName = ch, status = "NOT_STARTED"))
        }
    }

    // 3. Seed Weekly Planner (Mon-Sun)
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val defaultPlans = days.map {
        WeeklyPlan(dayOfWeek = it, topicsCompleted = "No topics completed yet.", chaptersCompleted = 0, testsGiven = 0, weakAreas = "None", username = username)
    }
    db.weeklyDao().insertPlans(defaultPlans)

    // 4. Seed Programming Languages Tracks
    val tracks = listOf("Python", "Web Development", "AI", "Projects")
    val defaultTracks = tracks.map {
        ProgrammingTracker(trackName = it, learningHours = 0.0, projectsCompleted = 0, conceptsLearned = "To learn.", notes = "Goal: Learn core concepts", username = username)
    }
    db.programmingDao().insertTrackers(defaultTracks)

    // 5. Seed Self Improvement Tracker
    val improvements = listOf("Handwriting", "English Communication", "Reading", "Typing Speed", "Discipline")
    val defaultImprovements = improvements.map {
        SelfImprovementTracker(topicName = it, focusText = "Daily deliberate practice", levelPercent = 30, notes = "", username = username)
    }
    db.selfImprovementDao().insertImprovements(defaultImprovements)

    // 6. Seed Habits Catalog
    val habits = listOf("Wake up on time", "Exercise", "Meditation", "Study", "Programming", "Temple/Puja", "Reading", "Sleep before 10:30 PM")
    val defaultHabits = habits.map {
        Habit(name = it, streak = 0, monthlyStreak = 0, username = username)
    }
    db.habitDao().insertHabits(defaultHabits)

    // 7. Seed Initial Goals
    val defaultGoals = listOf(
        GoalMetric(name = "Score 85%+ in Class 12 Boards", targetValue = 100.0, currentValue = 0.0, unit = "%", category = "MAIN", username = username),
        GoalMetric(name = "Learn Programming (Hands-On Coding)", targetValue = 100.0, currentValue = 20.0, unit = "hrs", category = "SECONDARY", username = username),
        GoalMetric(name = "Improve Handwriting (Daily page style)", targetValue = 10.0, currentValue = 4.0, unit = "/ 10 Score", category = "SECONDARY", username = username),
        GoalMetric(name = "Build Discipline (Habits Track)", targetValue = 100.0, currentValue = 15.0, unit = "%", category = "SECONDARY", username = username)
    )
    db.goalsDao().insertGoals(defaultGoals)

    // 8. Seed Default Notes
    db.notesDao().insertNote(
        NoteItem(
            title = "Board Exams Prep Strategy",
            content = "Keep Accounts and Mathematics on high priority! Solve previous year's papers for Economics and OCM. Revise Secretarial Practice definitions weekly.",
            category = "STUDY",
            tags = "boards,accounts,strategy",
            username = username
        )
    )
    db.notesDao().insertNote(
        NoteItem(
            title = "Python and AI Goals",
            content = "1. Learn basics of Pandas/NumPy.\n2. Complete a small web layout projects.\n3. Integrate Gemini LLM APIs into dynamic Python scripts.",
            category = "PROGRAMMING",
            tags = "python,ai",
            username = username
        )
    )

    // 9. Seed some simulated friends with diverse performance profiles
    val defaultFriends = listOf(
        Friend(friendUsername = "Aarav Gupta", xp = 450, streak = 5, chaptersCompleted = 6, weeklyHours = 12.5),
        Friend(friendUsername = "Priya Sharma", xp = 820, streak = 14, chaptersCompleted = 11, weeklyHours = 24.0),
        Friend(friendUsername = "Kabir Deshmukh", xp = 210, streak = 2, chaptersCompleted = 3, weeklyHours = 8.2),
        Friend(friendUsername = "Neha Patel", xp = 620, streak = 8, chaptersCompleted = 8, weeklyHours = 18.5)
    )
    defaultFriends.forEach {
        db.friendDao().insertFriend(it)
    }
}
