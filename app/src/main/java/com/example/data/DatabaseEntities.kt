package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserLocal(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val securityAnswer: String = "",
    val isVerified: Boolean = true,
    val xp: Int = 100,
    val coins: Int = 10,
    val level: Int = 1,
    val streak: Int = 0,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "timetable_tasks")
data class TimetableTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String = "default", // Support multi-user separation
    val name: String,
    val timeRange: String,
    val type: String, // e.g., "STUDY", "PROGRAMMING", "HABIT", "OTHER"
    val status: String, // "PENDING", "COMPLETED", "SKIPPED", "RESCHEDULED", "MOVED_TOMORROW"
    val notes: String = "",
    val dateStr: String = "Default" // "Default" for default template, or dynamic "yyyy-MM-dd"
)

@Entity(tableName = "subject_progress")
data class SubjectProgress(
    @PrimaryKey val subjectName: String,
    val currentChapter: Int = 0,
    val totalChapters: Int = 10,
    val notes: String = "",
    val completionStatus: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val revisionStatus: String = "NOT_REVISED", // "NOT_REVISED", "REVISED_ONCE", "REVISED_MULTIPLE"
    val username: String = "default" // Partition by user
)

@Entity(tableName = "weekly_planner")
data class WeeklyPlan(
    @PrimaryKey val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val topicsCompleted: String = "",
    val chaptersCompleted: Int = 0,
    val testsGiven: Int = 0,
    val weakAreas: String = "",
    val username: String = "default"
)

@Entity(tableName = "programming_tracker")
data class ProgrammingTracker(
    @PrimaryKey val trackName: String, // "Python", "Web Development", "AI", "Projects"
    val learningHours: Double = 0.0,
    val projectsCompleted: Int = 0,
    val conceptsLearned: String = "",
    val notes: String = "",
    val username: String = "default"
)

@Entity(tableName = "self_improvement")
data class SelfImprovementTracker(
    @PrimaryKey val topicName: String, // "Handwriting", "English Communication", "Reading", "Typing Speed", "Discipline"
    val focusText: String = "",
    val levelPercent: Int = 20, // progress %
    val notes: String = "",
    val username: String = "default"
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val name: String, // "Wake up on time", "Exercise", "Meditation", "Study", "Programming", "Temple/Puja", "Reading", "Sleep before 10:30 PM"
    val streak: Int = 0,
    val monthlyStreak: Int = 0,
    val username: String = "default"
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitName: String,
    val dateStr: String, // "yyyy-MM-dd"
    val status: Boolean,
    val username: String = "default"
)

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "STUDY", "PROGRAMMING", "PERSONAL"
    val lastUpdated: Long = System.currentTimeMillis(),
    val tags: String = "", // comma-separated strings
    val username: String = "default"
)

@Entity(tableName = "goals")
data class GoalMetric(
    @PrimaryKey val name: String, // e.g., "Score 85%+ in Class 12", "Learn Programming", etc.
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val unit: String = "%",
    val category: String = "SECONDARY", // "MAIN", "SECONDARY"
    val username: String = "default"
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val subject: String,
    val chapter: String,
    val startEndMinutes: String = "30",
    val durationHours: Double,
    val confidence: Int, // 1 to 5
    val testScore: Double? = null,
    val isRevision: Boolean = false,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapter_status")
data class ChapterStatus(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val subject: String,
    val chapterName: String,
    val status: String // "NOT_STARTED", "LEARNING", "PRACTICING", "REVISION", "MASTERED"
)

@Entity(tableName = "learning_journal")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val dateStr: String, // "yyyy-MM-dd"
    val learned: String,
    val mistakes: String,
    val confused: String,
    val revise: String,
    val win: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val friendUsername: String,
    val xp: Int,
    val streak: Int,
    val chaptersCompleted: Int,
    val weeklyHours: Double,
    val isPending: Boolean = false,
    val privacySetting: String = "FULL" // "PRIVATE", "FULL"
)
