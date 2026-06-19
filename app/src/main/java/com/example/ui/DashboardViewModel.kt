package com.example.ui

import com.example.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(private val repository: Repository) : ViewModel() {

    // ----------------------------------------------------
    // User Authentication & Profile Session States
    // ----------------------------------------------------
    val currentUser = MutableStateFlow<UserLocal?>(null)
    
    // Setup wizards trigger flags
    val needsSetupWizard = MutableStateFlow(false)

    // Reactive streams mapped and isolated by CURRENT USERNAME
    val tasks = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.allTasks(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val subjects = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.allSubjects(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val weeklyPlans = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.weeklyPlans(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val programmingTrackers = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.programmingTrackers(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selfImprovements = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.selfImprovements(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val habits = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.allHabits(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val habitLogs = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.habitLogs(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notes = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.allNotes(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val goals = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.allGoals(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val studySessions = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.studySessions(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val chapterStatuses = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.chapterStatuses(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val journalEntries = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.journalEntries(user.username)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Simulated Social Friends: Reactive stream from database
    val friends = repository.allFriends.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dynamic Current and Next Task hour resolver
    val currentAndNextTask: StateFlow<Pair<TimetableTask?, TimetableTask?>> = tasks.map { list ->
        if (list.isEmpty()) return@map Pair(null, null)
        
        val calendar = Calendar.getInstance()
        val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val totalMinutesNow = hour24 * 60 + minutes

        fun parseTimeRangeToMinutes(timeRange: String): Pair<Int, Int>? {
            return try {
                val parts = timeRange.split("-")
                if (parts.size < 2) {
                    // Try parsing single time slot like "10:30 PM"
                    val singleStr = timeRange.trim()
                    val partsDigits = singleStr.replace("PM", "").replace("AM", "").trim().split(":")
                    var hr = partsDigits[0].toInt()
                    val min = if (partsDigits.size > 1) partsDigits[1].toInt() else 0
                    if (singleStr.uppercase().contains("PM") && hr < 12) hr += 12
                    if (singleStr.uppercase().contains("AM") && hr == 12) hr = 0
                    val minutesVal = hr * 60 + min
                    return Pair(minutesVal, minutesVal + 45) // Default 45 mins block for single targets
                }
                
                fun parseTimeStr(timeStr: String): Int {
                    val clean = timeStr.trim().uppercase()
                    val isPM = clean.contains("PM")
                    val isAM = clean.contains("AM")
                    val timeDigits = clean.replace("PM", "").replace("AM", "").trim()
                    val splitDigits = timeDigits.split(":")
                    var hr = splitDigits[0].toInt()
                    val min = if (splitDigits.size > 1) splitDigits[1].toInt() else 0
                    if (isPM && hr < 12) hr += 12
                    if (isAM && hr == 12) hr = 0
                    return hr * 60 + min
                }

                Pair(parseTimeStr(parts[0]), parseTimeStr(parts[1]))
            } catch (e: Exception) {
                null
            }
        }

        var activeTask: TimetableTask? = null
        var upcomingTask: TimetableTask? = null

        val withTimes = list.mapNotNull { task ->
            val range = parseTimeRangeToMinutes(task.timeRange)
            if (range != null) Triple(task, range.first, range.second) else null
        }.sortedBy { it.second }

        for (i in withTimes.indices) {
            val (task, start, end) = withTimes[i]
            if (totalMinutesNow in start..end) {
                activeTask = task
                upcomingTask = if (i + 1 < withTimes.size) withTimes[i + 1].first else null
                break
            }
        }

        if (activeTask == null && withTimes.isNotEmpty()) {
            upcomingTask = withTimes.firstOrNull { it.second > totalMinutesNow }?.first
        }

        Pair(activeTask, upcomingTask)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(null, null))

    // UI Clock
    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDateStr = MutableStateFlow("")
    val currentDateStr: StateFlow<String> = _currentDateStr.asStateFlow()

    // AI Coach State Management
    private val _aiCoachFeedback = MutableStateFlow("Start tracking your study habits and daily learning journal! Click 'Generate AI Deep Feedback' once you have logged some data.")
    val aiCoachFeedback: StateFlow<String> = _aiCoachFeedback.asStateFlow()

    private val _isGeneratingAiCoach = MutableStateFlow(false)
    val isGeneratingAiCoach: StateFlow<Boolean> = _isGeneratingAiCoach.asStateFlow()

    init {
        updateTimeAndDate()
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

    // ----------------------------------------------------
    // Authentication Operations
    // ----------------------------------------------------
    fun register(username: String, passwordRaw: String, questionAnswer: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (username.trim().isEmpty() || passwordRaw.trim().isEmpty()) {
                onResult(false, "Username and Password cannot be empty.")
                return@launch
            }
            val hashed = passwordRaw.trim().hashCode().toString() // Basic safe integer hash string representation
            val ok = repository.registerUser(username.trim(), hashed, questionAnswer.trim())
            if (ok) {
                // Auto-login newly registered user
                val user = repository.loginUser(username.trim(), hashed)
                currentUser.value = user
                needsSetupWizard.value = true
                onResult(true, "Registration Successful! Complete setup wizard.")
            } else {
                onResult(false, "Username already exists.")
            }
        }
    }

    fun login(username: String, passwordRaw: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val hashed = passwordRaw.trim().hashCode().toString()
            val user = repository.loginUser(username.trim(), hashed)
            if (user != null) {
                currentUser.value = user
                needsSetupWizard.value = false
                onResult(true, "Welcome back, ${user.username}!")
            } else {
                onResult(false, "Invalid Username or Password.")
            }
        }
    }

    fun resetPassword(username: String, securityAnswer: String, newPasswordRaw: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val hashed = newPasswordRaw.trim().hashCode().toString()
            val success = repository.verifyReset(username.trim(), securityAnswer.trim(), hashed)
            if (success) {
                onResult(true, "Password Reset Successful! You can login now.")
            } else {
                onResult(false, "Incorrect verification challenge. Please try again.")
            }
        }
    }

    fun logout() {
        currentUser.value = null
        needsSetupWizard.value = false
    }

    // Wizard Completion
    fun completeWizard(wizardSubjects: List<String>, monthlyStudyGoal: Double) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            // Clear default structures to seed exactly what they chose
            repository.clearTimetable(uName)
            
            // Seed base timetable tasks for their custom study slots
            val wizardTasks = listOf(
                TimetableTask(name = "Morning Mind Prep", timeRange = "6:00 AM - 6:30 AM", type = "OTHER", status = "PENDING", username = uName),
                TimetableTask(name = "Core Study Session", timeRange = "7:00 AM - 9:00 AM", type = "STUDY", status = "PENDING", username = uName),
                TimetableTask(name = "Technical Skill Practice", timeRange = "12:00 PM - 2:00 PM", type = "PROGRAMMING", status = "PENDING", username = uName),
                TimetableTask(name = "Revision & Test Practice", timeRange = "4:00 PM - 6:30 PM", type = "STUDY", status = "PENDING", username = uName)
            )
            repository.insertTasks(wizardTasks)

            // Dynamic User Preference subject list insertion
            val finalSubjects = if (wizardSubjects.isNotEmpty()) wizardSubjects else listOf("Accounts", "Mathematics", "Economics")
            val defaultSubjects = finalSubjects.map {
                SubjectProgress(subjectName = it, currentChapter = 0, totalChapters = 10, notes = "Custom user-selected course content.", completionStatus = "NOT_STARTED", revisionStatus = "NOT_REVISED", username = uName)
            }
            repository.insertSubjects(defaultSubjects)

            // Setup appropriate target goals
            val finalStudyHoursGoal = if (monthlyStudyGoal > 0.0) monthlyStudyGoal else 100.0
            val defaultGoals = listOf(
                GoalMetric(name = "Monthly Focused Study Hours", targetValue = finalStudyHoursGoal, currentValue = 0.0, unit = "hrs", category = "MAIN", username = uName),
                GoalMetric(name = "Exams Master (Track Chapters Completed)", targetValue = (finalSubjects.size * 10).toDouble(), currentValue = 0.0, unit = "chapters", category = "SECONDARY", username = uName),
                GoalMetric(name = "Programming/Builder Projects Goal", targetValue = 5.0, currentValue = 0.0, unit = "Projects", category = "SECONDARY", username = uName)
            )
            repository.goalsDao.clearAll(uName)
            repository.goalsDao.insertGoals(defaultGoals)

            // Reward initial creation coins/experience!
            awardGamificationPoints(150, 15) // +150 XP, +15 Coins
            needsSetupWizard.value = false
        }
    }

    // ----------------------------------------------------
    // Gamification System & Profile Stats updates
    // ----------------------------------------------------
    private fun awardGamificationPoints(xpToAward: Int, coinsToAward: Int) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            var tempXp = user.xp + xpToAward
            var currentLvl = user.level
            var tempCoins = user.coins + coinsToAward

            // Experience levelling logic (E.g. each level threshold = level * 120 XP)
            var threshold = currentLvl * 120
            while (tempXp >= threshold) {
                tempXp -= threshold
                currentLvl += 1
                tempCoins += 10 // Levelling cash bonus!
                threshold = currentLvl * 120
            }

            val updatedUser = user.copy(
                xp = tempXp,
                level = currentLvl,
                coins = tempCoins,
                streak = user.streak
            )
            repository.updateUserProfile(updatedUser)
            currentUser.value = updatedUser
        }
    }

    // ----------------------------------------------------
    // Dynamic Stats Calculations Based purely on actual user data
    // ----------------------------------------------------
    val dailyProgressPercent: StateFlow<Int> = tasks.map { list ->
        if (list.isEmpty()) return@map 0
        val completed = list.count { it.status == "COMPLETED" }
        val total = list.size
        ((completed.toFloat() / total) * 100).toInt()
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalStudyHoursSpent: StateFlow<Double> = studySessions.map { list ->
        list.sumOf { it.durationHours }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val studyHoursCompleted: StateFlow<Double> = totalStudyHoursSpent

    val programmingHoursCompleted: StateFlow<Double> = programmingTrackers.map { list ->
        list.sumOf { it.learningHours }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalChaptersCompletedCount: StateFlow<Int> = chapterStatuses.map { list ->
        list.count { it.status == "MASTERED" }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalRevisionSessionsCompleted: StateFlow<Int> = studySessions.map { list ->
        list.count { it.isRevision }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val recentAverageTestScores: StateFlow<Double> = studySessions.map { list ->
        val graded = list.filter { it.testScore != null }
        if (graded.isEmpty()) 0.0 else graded.sumOf { it.testScore!! } / graded.size
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val currentLevelXpProgress: StateFlow<Float> = currentUser.map { user ->
        if (user == null) 0f
        else {
            val threshold = user.level * 120f
            user.xp.toFloat() / threshold
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    // Missed and Skipped tasks automatic recovery flow
    val recoveryQueue: StateFlow<List<TimetableTask>> = tasks.map { list ->
        list.filter { it.status == "SKIPPED" }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ----------------------------------------------------
    // Write Track Action Modifiers
    // ----------------------------------------------------

    // Study Sessions logger
    fun logStudySession(subject: String, chapter: String, durationHrs: Double, confidence: Int, notes: String, isRevision: Boolean = false, examScore: Double? = null) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            val session = StudySession(
                username = uName,
                subject = subject,
                chapter = chapter,
                durationHours = durationHrs,
                confidence = confidence,
                testScore = examScore,
                isRevision = isRevision,
                notes = notes
            )
            repository.insertStudySession(session)

            // Adjust overall goal current value
            val primaryGoal = goals.value.find { it.category == "MAIN" }
            if (primaryGoal != null) {
                val totalHours = totalStudyHoursSpent.value + durationHrs
                repository.insertGoal(primaryGoal.copy(currentValue = totalHours))
            }

            // Award Gamified accomplishments XP
            val xpGain = 25 + (confidence * 5) + (if (examScore != null) 30 else 0)
            val coinsGain = 3 + (if (examScore != null) 5 else 0)
            awardGamificationPoints(xpGain, coinsGain)

            // If a score is entered or subject is studied, record user status for chapter
            val chStatusStr = when {
                confidence >= 5 -> "MASTERED"
                confidence >= 3 -> "REVISION"
                else -> "LEARNING"
            }
            // Insert status or update
            val match = chapterStatuses.value.find { it.subject.equals(subject, ignoreCase = true) && it.chapterName.equals(chapter, ignoreCase = true) }
            if (match != null) {
                repository.insertChapterStatus(match.copy(status = chStatusStr))
            } else {
                repository.insertChapterStatus(ChapterStatus(username = uName, subject = subject, chapterName = chapter, status = chStatusStr))
            }
        }
    }

    // Timetable Tasks Actions
    fun addTask(name: String, timeRange: String, type: String, notes: String = "") {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            repository.insertTask(TimetableTask(username = uName, name = name, timeRange = timeRange, type = type, status = "PENDING", notes = notes))
        }
    }

    fun updateTask(task: TimetableTask) {
        viewModelScope.launch {
            repository.insertTask(task)
            if (task.status == "COMPLETED") {
                awardGamificationPoints(15, 2) // +15 XP for scheduling hygiene completion!
            }
        }
    }

    fun deleteTask(task: TimetableTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Chapters Master Tracker
    fun updateChapterStatusLocal(subject: String, chapter: String, newStatus: String) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            val match = chapterStatuses.value.find { it.subject.equals(subject, ignoreCase = true) && it.chapterName.equals(chapter, ignoreCase = true) }
            if (match != null) {
                repository.insertChapterStatus(match.copy(status = newStatus))
            } else {
                repository.insertChapterStatus(ChapterStatus(username = uName, subject = subject, chapterName = chapter, status = newStatus))
            }

            // If mastered, award big achievement
            if (newStatus == "MASTERED") {
                awardGamificationPoints(50, 5)
                // update secondary goals count
                val examsGoal = goals.value.find { it.name.contains("Exams") || it.unit.contains("chapters") }
                if (examsGoal != null) {
                    val completedTotal = totalChaptersCompletedCount.value + 1
                    repository.insertGoal(examsGoal.copy(currentValue = completedTotal.toDouble()))
                }
            }
        }
    }

    // Learning Journal
    fun addJournalEntry(learned: String, mistakes: String, confused: String, revise: String, win: String) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            val date = currentDateStr.value
            val entry = JournalEntry(
                username = uName,
                dateStr = date,
                learned = learned,
                mistakes = mistakes,
                confused = confused,
                revise = revise,
                win = win
            )
            repository.insertJournal(entry)
            awardGamificationPoints(30, 3) // Daily journal reflection bonus
        }
    }

    // Programming tracker updater
    fun updateProgrammingTrack(name: String, hours: Double, projects: Int, concepts: String, notes: String) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            repository.insertProgrammingTrack(
                ProgrammingTracker(
                    trackName = name,
                    learningHours = hours,
                    projectsCompleted = projects,
                    conceptsLearned = concepts,
                    notes = notes,
                    username = uName
                )
            )
            awardGamificationPoints(10, 1)
        }
    }

    // Notes adding
    fun addNote(title: String, content: String, category: String, csvTags: String = "") {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            repository.insertNote(NoteItem(title = title, content = content, category = category, tags = csvTags, username = uName))
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Friends adding request
    fun sendFriendRequestSimulated(friendName: String) {
        viewModelScope.launch {
            // Generate simulated friend record
            val f = Friend(
                friendUsername = friendName,
                xp = (200..900).random(),
                streak = (1..20).random(),
                chaptersCompleted = (1..10).random(),
                weeklyHours = (5..30).random().toDouble(),
                isPending = true
            )
            repository.insertFriend(f)
        }
    }

    fun acceptFriendRequest(friendName: String) {
        viewModelScope.launch {
            val matched = friends.value.find { it.friendUsername == friendName }
            if (matched != null) {
                repository.insertFriend(matched.copy(isPending = false))
            }
        }
    }

    fun declineFriendRequest(friendName: String) {
        viewModelScope.launch {
            repository.deleteFriend(friendName)
        }
    }

    // ----------------------------------------------------
    // AI Study Coach Generation Flow
    // ----------------------------------------------------
    fun generateAiCoachFeedback() {
        val user = currentUser.value ?: return
        val key = BuildConfig.GEMINI_API_KEY
        
        viewModelScope.launch {
            _isGeneratingAiCoach.value = true
            _aiCoachFeedback.value = "AI Coach is assembling tracked study data and querying the Gemini Model..."

            // Compile structured facts to prevent hallucinating
            val hours = totalStudyHoursSpent.value
            val activeStreak = user.streak
            val weakSubjectSummary = weeklyPlans.value.filter { it.weakAreas.isNotEmpty() && it.weakAreas != "None" }
                .joinToString { "${it.dayOfWeek}: ${it.weakAreas}" }
                .ifEmpty { "None entered yet" }
                
            val sessionList = studySessions.value
            val sessionsSummary = sessionList.take(5).joinToString("; ") {
                "${it.subject} Ch.${it.chapter} (Confidence: ${it.confidence}/5, Revise: ${it.isRevision})"
            }.ifEmpty { "No study logs recorded yet" }

            val journals = journalEntries.value
            val recentWin = journals.firstOrNull()?.win ?: "None logged yet"
            val recentConfusion = journals.firstOrNull()?.confused ?: "None logged yet"

            val feedback = com.example.ui.AiCoachService.getStudyCoachFeeback(
                apiKey = key,
                username = user.username,
                studyHours = hours,
                streak = activeStreak,
                weakAreas = weakSubjectSummary,
                sessionsCount = sessionList.size,
                loggedSessionsSummary = sessionsSummary,
                recentJournalWin = recentWin,
                recentJournalConfusion = recentConfusion
            )

            _aiCoachFeedback.value = feedback
            _isGeneratingAiCoach.value = false
        }
    }

    fun updateGoal(name: String, currentValue: Double, targetValue: Double, unit: String) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            val existing = goals.value.find { it.name == name }
            val cat = existing?.category ?: "SECONDARY"
            repository.insertGoal(
                GoalMetric(
                    name = name,
                    currentValue = currentValue,
                    targetValue = targetValue,
                    unit = unit,
                    category = cat,
                    username = uName
                )
            )
        }
    }

    fun updateWeeklyPlan(day: String, topics: String, chapters: Int, tests: Int, weakAreas: String) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            repository.insertPlan(
                WeeklyPlan(
                    dayOfWeek = day,
                    topicsCompleted = topics,
                    chaptersCompleted = chapters,
                    testsGiven = tests,
                    weakAreas = weakAreas,
                    username = uName
                )
            )
        }
    }

    fun toggleHabitToday(habitName: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            val date = currentDateStr.value
            if (isCompleted) {
                val log = HabitLog(username = uName, habitName = habitName, dateStr = date, status = true)
                repository.insertHabitLog(log)
                
                // Adjust Habit Streak count
                val habit = habits.value.find { it.name == habitName }
                if (habit != null) {
                    repository.insertHabit(habit.copy(streak = habit.streak + 1))
                }
                // Award Gamification Points for self-improvement completeness!
                awardGamificationPoints(10, 1)
            } else {
                repository.deleteHabitLog(uName, habitName, date)
                val habit = habits.value.find { it.name == habitName }
                if (habit != null) {
                    repository.insertHabit(habit.copy(streak = (habit.streak - 1).coerceAtLeast(0)))
                }
            }
        }
    }

    fun updateSubjectMetadata(subjectName: String, notes: String, completionStatus: String) {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            val existing = subjects.value.find { it.subjectName == subjectName }
            if (existing != null) {
                repository.insertSubject(existing.copy(notes = notes, completionStatus = completionStatus))
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            val uName = currentUser.value?.username ?: return@launch
            repository.resetDatabase(uName)
        }
    }

    suspend fun exportJson(): String {
        val uName = currentUser.value?.username ?: return "{}"
        return repository.exportAsJson(uName)
    }

    suspend fun importJson(json: String): Boolean {
        val uName = currentUser.value?.username ?: return false
        return repository.importFromJson(uName, json)
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
