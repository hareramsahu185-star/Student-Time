package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetable_tasks")
data class TimetableTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
    val revisionStatus: String = "NOT_REVISED" // "NOT_REVISED", "REVISED_ONCE", "REVISED_MULTIPLE"
)

@Entity(tableName = "weekly_planner")
data class WeeklyPlan(
    @PrimaryKey val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val topicsCompleted: String = "",
    val chaptersCompleted: Int = 0,
    val testsGiven: Int = 0,
    val weakAreas: String = ""
)

@Entity(tableName = "programming_tracker")
data class ProgrammingTracker(
    @PrimaryKey val trackName: String, // "Python", "Web Development", "AI", "Projects"
    val learningHours: Double = 0.0,
    val projectsCompleted: Int = 0,
    val conceptsLearned: String = "",
    val notes: String = ""
)

@Entity(tableName = "self_improvement")
data class SelfImprovementTracker(
    @PrimaryKey val topicName: String, // "Handwriting", "English Communication", "Reading", "Typing Speed", "Discipline"
    val focusText: String = "",
    val levelPercent: Int = 20, // progress %
    val notes: String = ""
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val name: String, // "Wake up on time", "Exercise", "Meditation", "Study", "Programming", "Temple/Puja", "Reading", "Sleep before 10:30 PM"
    val streak: Int = 0,
    val monthlyStreak: Int = 0
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitName: String,
    val dateStr: String, // "yyyy-MM-dd"
    val status: Boolean
)

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "STUDY", "PROGRAMMING", "PERSONAL"
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalMetric(
    @PrimaryKey val name: String, // e.g., "Score 85%+ in Class 12", "Learn Programming", etc.
    val targetValue: Double = 100.0,
    val currentValue: Double = 0.0,
    val unit: String = "%",
    val category: String = "SECONDARY" // "MAIN", "SECONDARY"
)
