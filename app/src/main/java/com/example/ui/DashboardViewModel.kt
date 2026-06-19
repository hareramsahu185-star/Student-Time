package com.example.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(private val repository: Repository) : ViewModel() {

    // Central Data Flows from Repository
    val tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val subjects = repository.allSubjects.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val weeklyPlans = repository.weeklyPlans.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val programmingTrackers = repository.programmingTrackers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val selfImprovements = repository.selfImprovements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val habits = repository.allHabits.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val habitLogs = repository.habitLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val notes = repository.allNotes.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // UI Helpers
    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDateStr = MutableStateFlow("")
    val currentDateStr: StateFlow<String> = _currentDateStr.asStateFlow()

    init {
        updateTimeAndDate()
        // Start a tick system to update live clock every 30 seconds
        viewModelScope.launch {
            while (true) {
                updateTimeAndDate()
                kotlinx.coroutines.delay(30000)
            }
        }
    }

    private fun updateTimeAndDate() {
        val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Date()
        _currentTime.value = sdfTime.format(now)
        _currentDateStr.value = sdfDate.format(now)
    }

    // Dynamic Derived Values for Current & Next Task
    val currentAndNextTask: StateFlow<Pair<TimetableTask?, TimetableTask?>> = combine(tasks, currentTime) { taskList, time ->
        if (taskList.isEmpty()) return@combine Pair(null, null)

        val calendar = Calendar.getInstance()
        val currentMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        var active: TimetableTask? = null
        var next: TimetableTask? = null
        var minTimeDiff = Int.MAX_VALUE

        for (task in taskList) {
            val (startMin, endMin) = parseTimeRange(task.timeRange)
            if (currentMin in startMin until endMin) {
                active = task
            } else if (startMin >= currentMin) {
                val diff = startMin - currentMin
                if (diff < minTimeDiff) {
                    minTimeDiff = diff
                    next = task
                }
            }
        }

        // If no upcoming task found in the remainder of the day, pick the first task of the day
        if (next == null && taskList.isNotEmpty()) {
            next = taskList.first()
        }

        Pair(active, next)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    // Helper to parse time-ranges such as "6:30 AM - 8:00 AM" or single times "5:00 AM"
    private fun parseTimeRange(timeRange: String): Pair<Int, Int> {
        val parts = timeRange.split("-", "to")
        if (parts.isEmpty()) return Pair(0, 0)

        val startTimeStr = parts[0].trim()
        val startMins = parseTimeStr(startTimeStr) ?: 0

        val endMins = if (parts.size > 1) {
            parseTimeStr(parts[1].trim()) ?: (startMins + 60)
        } else {
            startMins + 40 // Default duration of 40 mins
        }
        return Pair(startMins, endMins)
    }

    private fun parseTimeStr(timeStr: String): Int? {
        val clean = timeStr.uppercase(Locale.getDefault()).trim()
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return try {
            val date = sdf.parse(clean) ?: return null
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) {
            // Try fallback like "5:00 AM"
            try {
                val sdfFallback = SimpleDateFormat("h:mm a", Locale.getDefault())
                val date = sdfFallback.parse(clean) ?: return null
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            } catch (ex2: Exception) {
                null
            }
        }
    }

    // Dynamic stats calculations
    val dailyProgressPercent: StateFlow<Int> = tasks.map { list ->
        if (list.isEmpty()) return@map 100
        val completed = list.count { it.status == "COMPLETED" }
        val skippedOrMoved = list.count { it.status == "SKIPPED" || it.status == "MOVED_TOMORROW" }
        val totalActioned = completed + skippedOrMoved
        val total = list.size
        if (total == 0) 0 else ((completed.toFloat() / total) * 100).toInt()
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val studyHoursCompleted: StateFlow<Double> = tasks.map { list ->
        list.filter { it.status == "COMPLETED" && it.type == "STUDY" }.sumOf {
            val (start, end) = parseTimeRange(it.timeRange)
            (end - start).toDouble() / 60.0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val programmingHoursCompleted: StateFlow<Double> = tasks.map { list ->
        val completeInTasks = list.filter { it.status == "COMPLETED" && it.type == "PROGRAMMING" }.sumOf {
            val (start, end) = parseTimeRange(it.timeRange)
            (end - start).toDouble() / 60.0
        }
        completeInTasks
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Timetable Functions
    fun addTask(name: String, timeRange: String, type: String, notes: String = "") {
        viewModelScope.launch {
            repository.insertTask(TimetableTask(name = name, timeRange = timeRange, type = type, status = "PENDING", notes = notes))
        }
    }

    fun updateTask(task: TimetableTask) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TimetableTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Recover / Skipped Queues
    val recoveryQueue: StateFlow<List<TimetableTask>> = tasks.map { list ->
        list.filter { it.status == "SKIPPED" }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Subject operations
    fun updateSubjectChapters(subjectName: String, currentChapter: Int, totalChapters: Int) {
        viewModelScope.launch {
            val newProgress = if (totalChapters > 0) ((currentChapter.toFloat() / totalChapters) * 100).toInt() else 0
            val status = when {
                newProgress >= 100 -> "COMPLETED"
                newProgress > 0 -> "IN_PROGRESS"
                else -> "NOT_STARTED"
            }
            val existing = subjects.value.find { it.subjectName == subjectName }
            if (existing != null) {
                repository.insertSubject(
                    existing.copy(
                        currentChapter = currentChapter,
                        totalChapters = totalChapters,
                        completionStatus = status
                    )
                )
            } else {
                repository.insertSubject(
                    SubjectProgress(subjectName, currentChapter, totalChapters, "", status, "NOT_REVISED")
                )
            }
            updateBoardExamProgress()
        }
    }

    fun updateSubjectMetadata(subjectName: String, notes: String, revisionStatus: String) {
        viewModelScope.launch {
            val existing = subjects.value.find { it.subjectName == subjectName }
            if (existing != null) {
                repository.insertSubject(existing.copy(notes = notes, revisionStatus = revisionStatus))
            }
        }
    }

    private suspend fun updateBoardExamProgress() {
        val subjectList = subjects.value
        if (subjectList.isEmpty()) return
        val totalChapters = subjectList.sumOf { it.totalChapters }
        val completedChapters = subjectList.sumOf { it.currentChapter }
        val overallProgress = if (totalChapters > 0) {
            (completedChapters.toDouble() / totalChapters) * 100
        } else {
            0.0
        }

        // Clip board completion up to 100% and update main goal
        val goal = goals.value.find { it.category == "MAIN" }
        if (goal != null) {
            repository.insertGoal(goal.copy(currentValue = overallProgress))
        }
    }

    // Weekly Planner
    fun updateWeeklyPlan(day: String, topics: String, chapters: Int, tests: Int, weakAreas: String) {
        viewModelScope.launch {
            repository.insertPlan(
                WeeklyPlan(
                    dayOfWeek = day,
                    topicsCompleted = topics,
                    chaptersCompleted = chapters,
                    testsGiven = tests,
                    weakAreas = weakAreas
                )
            )
        }
    }

    // Programming Tracker
    fun updateProgrammingTrack(name: String, hours: Double, projects: Int, concepts: String, notes: String) {
        viewModelScope.launch {
            repository.insertProgrammingTrack(
                ProgrammingTracker(
                    trackName = name,
                    learningHours = hours,
                    projectsCompleted = projects,
                    conceptsLearned = concepts,
                    notes = notes
                )
            )
        }
    }

    // Self Improvement Tracker
    fun updateSelfImprovement(name: String, focusText: String, levelPercent: Int, notes: String) {
        viewModelScope.launch {
            repository.insertSelfImprovement(
                SelfImprovementTracker(
                    topicName = name,
                    focusText = focusText,
                    levelPercent = levelPercent,
                    notes = notes
                )
            )
        }
    }

    // Habit operations
    fun toggleHabitToday(habitName: String, completed: Boolean) {
        viewModelScope.launch {
            val todayStr = currentDateStr.value
            if (completed) {
                repository.insertHabitLog(HabitLog(habitName = habitName, dateStr = todayStr, status = true))
                // Increment streak
                val habitObj = habits.value.find { it.name == habitName }
                if (habitObj != null) {
                    val currStreak = habitObj.streak + 1
                    val maxMonth = maxOf(habitObj.monthlyStreak, currStreak)
                    repository.insertHabit(habitObj.copy(streak = currStreak, monthlyStreak = maxMonth))
                }
            } else {
                repository.deleteHabitLog(habitName, todayStr)
                val habitObj = habits.value.find { it.name == habitName }
                if (habitObj != null) {
                    val currStreak = maxOf(0, habitObj.streak - 1)
                    repository.insertHabit(habitObj.copy(streak = currStreak))
                }
            }
        }
    }

    // Notes
    fun addNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertNote(NoteItem(title = title, content = content, category = category))
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Goals
    fun updateGoal(name: String, currentValue: Double, targetValue: Double, unit: String) {
        viewModelScope.launch {
            val existing = goals.value.find { it.name == name }
            val cat = existing?.category ?: "SECONDARY"
            repository.insertGoal(
                GoalMetric(
                    name = name,
                    currentValue = currentValue,
                    targetValue = targetValue,
                    unit = unit,
                    category = cat
                )
            )
        }
    }

    // Database Actions: Reset/Backup
    suspend fun exportJson(): String {
        return repository.exportAsJson()
    }

    suspend fun importJson(json: String): Boolean {
        val success = repository.importFromJson(json)
        if (success) {
            updateBoardExamProgress()
        }
        return success
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }
}

class DashboardViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
