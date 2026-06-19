package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.DashboardViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Glassmorphism Card Style Modifier
@Composable
fun Modifier.glassCard(
    borderColor: Color = Color(0x33E5E9F0),
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = CardGlassSurface
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentOSLayout(viewModel: DashboardViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe StateFlows from ViewModel
    val currentAndNext by viewModel.currentAndNextTask.collectAsStateWithLifecycle()
    val (currentTask, nextTask) = currentAndNext

    val dailyProgress by viewModel.dailyProgressPercent.collectAsStateWithLifecycle()
    val studyHrsCompleted by viewModel.studyHoursCompleted.collectAsStateWithLifecycle()
    val progHrsCompleted by viewModel.programmingHoursCompleted.collectAsStateWithLifecycle()

    val taskList by viewModel.tasks.collectAsStateWithLifecycle()
    val subjectList by viewModel.subjects.collectAsStateWithLifecycle()
    val weeklyPlanList by viewModel.weeklyPlans.collectAsStateWithLifecycle()
    val progTrackerList by viewModel.programmingTrackers.collectAsStateWithLifecycle()
    val selfImpList by viewModel.selfImprovements.collectAsStateWithLifecycle()
    val habitList by viewModel.habits.collectAsStateWithLifecycle()
    val habitLogList by viewModel.habitLogs.collectAsStateWithLifecycle()
    val noteList by viewModel.notes.collectAsStateWithLifecycle()
    val goalList by viewModel.goals.collectAsStateWithLifecycle()

    val liveTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val dateStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

    // Tab Selection state
    var selectedTab by remember { mutableStateOf("Dashboard") }
    val tabs = listOf("Dashboard", "Timetable", "Subjects", "Trackers", "Habits", "Notes")

    // Overlay dialog controls
    var showBackupDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "STUDENT LIFE OPERATING SYSTEM",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSolidWhite,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showBackupDialog = true },
                        modifier = Modifier.testTag("backup_button")
                    ) {
                        Icon(Icons.Filled.Backup, contentDescription = "Backup Menu", tint = CyanPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            // Adaptive horizontal menu
            NavigationBar(
                containerColor = DarkBackground,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val (icon, label) = when (tab) {
                        "Dashboard" -> Pair(Icons.Filled.Dashboard, "Home")
                        "Timetable" -> Pair(Icons.Filled.Schedule, "Schedule")
                        "Subjects" -> Pair(Icons.Filled.Book, "Subjects")
                        "Trackers" -> Pair(Icons.Filled.TrendingUp, "Trackers")
                        "Habits" -> Pair(Icons.Filled.Beenhere, "Habits")
                        "Notes" -> Pair(Icons.Filled.NoteAlt, "Notes")
                        else -> Pair(Icons.Filled.Help, "")
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            unselectedIconColor = TextMutedGray,
                            selectedTextColor = CyanPrimary,
                            unselectedTextColor = TextMutedGray,
                            indicatorColor = CyanPrimary
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBackground, Color(0xFF0F172A))
                    )
                )
        ) {
            // Top Live Banner (Always visible in Header)
            LiveHeaderRow(liveTime, dateStr, dailyProgress, currentTask, nextTask)

            // Dynamic Content Pane based on current tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { width -> width / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        "Dashboard" -> DashboardScreen(
                            dailyProgress,
                            studyHrsCompleted,
                            progHrsCompleted,
                            taskList,
                            goalList,
                            viewModel
                        )
                        "Timetable" -> TimetableScreen(taskList, viewModel)
                        "Subjects" -> SubjectsScreen(subjectList, weeklyPlanList, viewModel)
                        "Trackers" -> TrackersScreen(progTrackerList, selfImpList, viewModel)
                        "Habits" -> HabitsScreen(habitList, habitLogList, viewModel)
                        "Notes" -> NotesScreen(noteList, viewModel)
                    }
                }
            }
        }
    }

    // Backup & Restore Dialog
    if (showBackupDialog) {
        Dialog(onDismissRequest = { showBackupDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .glassCard()
                    .padding(20.dp),
                color = CardGlassSurface
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "DATA OS CONTROL PANEL",
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.exportJson()
                                clipboardManager.setText(AnnotatedString(json))
                                Toast.makeText(context, "Backup JSON copied to Clipboard!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = DarkBackground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("export_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Export Data OS (Copy JSON)")
                    }

                    HorizontalDivider(color = Color(0x33FFFFFF), modifier = Modifier.padding(vertical = 12.dp))

                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text("Paste Import JSON here", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSolidWhite,
                            unfocusedTextColor = TextSolidWhite,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("import_text_field")
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (importText.trim().isNotEmpty()) {
                                    val success = viewModel.importJson(importText)
                                    if (success) {
                                        Toast.makeText(context, "OS Loaded Successfully!", Toast.LENGTH_SHORT).show()
                                        showBackupDialog = false
                                        importText = ""
                                    } else {
                                        Toast.makeText(context, "Invalid Backup Structure", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleSecondary, contentColor = TextSolidWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("import_button")
                    ) {
                        Text("Import & Restore")
                    }

                    TextButton(
                        onClick = {
                            viewModel.resetData()
                            Toast.makeText(context, "All data reset to Class 12 Defaults!", Toast.LENGTH_SHORT).show()
                            showBackupDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorNeon),
                        modifier = Modifier.testTag("reset_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Reset strictly to initial layout")
                    }

                    TextButton(
                        onClick = { showBackupDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSolidWhite)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// live banner showing clock and instant updates
// ----------------------------------------------------
@Composable
fun LiveHeaderRow(
    liveTime: String,
    dateStr: String,
    dailyProgress: Int,
    currentTask: TimetableTask?,
    nextTask: TimetableTask?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .glassCard(borderColor = Color(0x1A00E5FF), backgroundColor = Color(0x900E1726))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "L I V E   O S   C L O C K",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = liveTime,
                    color = TextSolidWhite,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = dateStr,
                    color = TextMutedGray,
                    fontSize = 12.sp,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "D A I L Y   L O O P",
                    color = PurpleSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                        CircularProgressIndicator(
                            progress = { dailyProgress.toFloat() / 100f },
                            color = CyanPrimary,
                            strokeWidth = 5.dp,
                            trackColor = Color(0x1EFFFFFF),
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            "$dailyProgress%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSolidWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0x1F909CB4))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CURRENT PROCESS", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val indicatorColor = when (currentTask?.type) {
                        "STUDY" -> TealAccent
                        "PROGRAMMING" -> CyanPrimary
                        else -> PurpleSecondary
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (currentTask != null) indicatorColor else Color.Gray)
                    )
                    Text(
                        text = currentTask?.name ?: "Free or Buffer Time",
                        color = TextSolidWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("NEXT UP", color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = nextTask?.name ?: "No tasks left",
                    color = TextSolidWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (nextTask != null) {
                    Text(nextTask.timeRange, color = CyanPrimary, fontSize = 11.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 1: DASHBOARD SCREEN
// ----------------------------------------------------
@Composable
fun DashboardScreen(
    dailyProgress: Int,
    studyHrs: Double,
    progHrs: Double,
    tasks: List<TimetableTask>,
    goals: List<GoalMetric>,
    viewModel: DashboardViewModel
) {
    val scrollState = rememberScrollState()

    val boardGoal = goals.find { it.category == "MAIN" } ?: GoalMetric("Score 85%+ in Class 12 Boards", 100.0, 0.0, "%", "MAIN")
    val actualBoardProgress = boardGoal.currentValue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {
        // Goal Banner (Score 85% Board Exam Goal)
        MainGoalBanner(boardGoal, actualBoardProgress)

        Spacer(modifier = Modifier.height(16.dp))

        // Core stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Study Hours",
                value = String.format(Locale.getDefault(), "%.1fh", studyHrs),
                goal = "6.0h",
                icon = Icons.Filled.MenuBook,
                color = TealAccent,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Coding hours",
                value = String.format(Locale.getDefault(), "%.1fh", progHrs),
                goal = "3.0h",
                icon = Icons.Filled.Terminal,
                color = CyanPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Analytics Row with Graph (Study and habit trends)
        Text(
            "PROGRESS METRICS & OUTLOOK",
            color = CyanPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .glassCard()
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daily Study Hours Projection", color = TextSolidWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .glassCard(borderColor = Color(0x40FFFFFF), backgroundColor = Color(0x33000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Accounts • Maths • Python", color = CyanPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Draw Analytics Graph
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)) {
                    val width = size.width
                    val height = size.height

                    // Draw grid axes
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = 2f
                    )

                    // Dummy points representing study hours of the past 7 days (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
                    val dataPoints = listOf(4.5f, 5.2f, 6.0f, 3.8f, 7.1f, 8.0f, studyHrs.toFloat())
                    val maxVal = 10f

                    val stepX = width / (dataPoints.size - 1)
                    val points = dataPoints.mapIndexed { idx, valHours ->
                        val x = idx * stepX
                        val y = height - (valHours / maxVal) * height
                        Offset(x, y)
                    }

                    // Draw Area Gradient
                    val path = Path().apply {
                        moveTo(0f, height)
                        points.forEachIndexed { index, offset ->
                            if (index == 0) lineTo(offset.x, offset.y)
                            else lineTo(offset.x, offset.y)
                        }
                        lineTo(width, height)
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x5500E5FF), Color.Transparent)
                        )
                    )

                    // Draw connecting line
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = CyanPrimary,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw values & dots
                    points.forEachIndexed { i, offset ->
                        drawCircle(
                            color = if (i == points.size - 1) PurpleSecondary else CyanPrimary,
                            radius = 6f,
                            center = offset
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today")
                    labels.forEach {
                        Text(it, color = TextMutedGray, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recovery Queue block (Skipped tasks and dynamic actions)
        val skippedTasks = tasks.filter { it.status == "SKIPPED" }
        RecoveryQueueSection(skippedTasks, viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary goals visual tracker
        SecondaryGoalsSection(goals, viewModel)

        // Bottom space
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MainGoalBanner(goal: GoalMetric, actualBoardProgress: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(borderColor = TealAccent, backgroundColor = Color(0xFF07241F))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "MAIN MISSION TARGET",
                        color = TealAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        goal.name,
                        color = TextSolidWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealAccent)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "TARGET 85%+",
                        color = DarkBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Showing visual progression bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { actualBoardProgress.toFloat() / 100f },
                    color = TealAccent,
                    trackColor = Color(0x33FFFFFF),
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", actualBoardProgress),
                    color = TextSolidWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Calculated based on Chapter progress across Board Subjects. Complete chapters to raise percentage!",
                color = TextMutedGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    goal: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassCard(borderColor = color.copy(alpha = 0.3f), backgroundColor = CardGlassSurface)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title.uppercase(), color = TextMutedGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, color = TextSolidWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Target: $goal", color = color, fontSize = 11.sp)
            }

            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun RecoveryQueueSection(skippedTasks: List<TimetableTask>, viewModel: DashboardViewModel) {
    var selectedTaskForReschedule by remember { mutableStateOf<TimetableTask?>(null) }
    var newTimeInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(borderColor = ErrorNeon.copy(alpha = 0.4f), backgroundColor = Color(0xFF1F1116))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = ErrorNeon, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        "RECOVERY QUEUE (${skippedTasks.size} Skipped)",
                        color = ErrorNeon,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (skippedTasks.isNotEmpty()) {
                    Text(
                        "Needs Action",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSolidWhite,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ErrorNeon)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (skippedTasks.isEmpty()) {
                Text(
                    "Pristine state! No skipped or missed sessions in queue. Keep up the high discipline!",
                    color = TextMutedGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                skippedTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .glassCard(borderColor = Color(0x33FFFFFF), backgroundColor = Color(0x22000000))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.name, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Missed ${task.timeRange} session", color = TextMutedGray, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Recover Button
                            IconButton(
                                onClick = {
                                    viewModel.updateTask(task.copy(status = "COMPLETED"))
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TealAccent)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Recover Complete", tint = DarkBackground, modifier = Modifier.size(18.dp))
                            }

                            // Reschedule button
                            IconButton(
                                onClick = {
                                    selectedTaskForReschedule = task
                                    newTimeInput = task.timeRange
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanPrimary)
                            ) {
                                Icon(Icons.Filled.Schedule, contentDescription = "Reschedule", tint = DarkBackground, modifier = Modifier.size(18.dp))
                            }

                            // Delete button
                            IconButton(
                                onClick = {
                                    viewModel.deleteTask(task)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ErrorNeon)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextSolidWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Reschedule dialog
    if (selectedTaskForReschedule != null) {
        Dialog(onDismissRequest = { selectedTaskForReschedule = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .padding(16.dp),
                color = CardGlassSurface
            ) {
                Column {
                    Text(
                        "Reschedule Skipped Task",
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newTimeInput,
                        onValueChange = { newTimeInput = it },
                        label = { Text("New Time Range", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSolidWhite,
                            unfocusedTextColor = TextSolidWhite,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedTaskForReschedule = null }) {
                            Text("Cancel", color = TextSolidWhite)
                        }
                        Button(
                            onClick = {
                                val t = selectedTaskForReschedule
                                if (t != null) {
                                    viewModel.updateTask(t.copy(timeRange = newTimeInput, status = "PENDING"))
                                }
                                selectedTaskForReschedule = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Save", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecondaryGoalsSection(goals: List<GoalMetric>, viewModel: DashboardViewModel) {
    val secondaryGoals = goals.filter { it.category == "SECONDARY" }
    var goalToEdit by remember { mutableStateOf<GoalMetric?>(null) }
    var newValueInput by remember { mutableStateOf("") }

    Column {
        Text(
            "SECONDARY TARGETS & DISCIPLINE",
            color = CyanPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        secondaryGoals.forEach { goal ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .glassCard()
                    .clickable {
                        goalToEdit = goal
                        newValueInput = goal.currentValue.toString()
                    }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            goal.name,
                            color = TextSolidWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                            color = CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = {
                            val ratio = goal.currentValue.toFloat() / goal.targetValue.toFloat()
                            ratio.coerceIn(0f, 1f)
                        },
                        color = PurpleSecondary,
                        trackColor = Color(0x22FFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }

    if (goalToEdit != null) {
        Dialog(onDismissRequest = { goalToEdit = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .padding(16.dp),
                color = CardGlassSurface
            ) {
                Column {
                    Text(
                        "Update Progress: ${goalToEdit?.name}",
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newValueInput,
                        onValueChange = { newValueInput = it },
                        label = { Text("Current Score / Value (${goalToEdit?.unit})", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSolidWhite,
                            unfocusedTextColor = TextSolidWhite,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { goalToEdit = null }) {
                            Text("Cancel", color = TextSolidWhite)
                        }
                        Button(
                            onClick = {
                                val g = goalToEdit
                                val doubleVal = newValueInput.toDoubleOrNull()
                                if (g != null && doubleVal != null) {
                                    viewModel.updateGoal(g.name, doubleVal, g.targetValue, g.unit)
                                }
                                goalToEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Update", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 2: TIMETABLE SCREEN
// ----------------------------------------------------
@Composable
fun TimetableScreen(tasks: List<TimetableTask>, viewModel: DashboardViewModel) {
    val context = LocalContext.current
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<TimetableTask?>(null) }

    // Dialogue input states
    var taskNameInput by remember { mutableStateOf("") }
    var taskTimeInput by remember { mutableStateOf("") }
    var taskTypeInput by remember { mutableStateOf("STUDY") }
    var taskNotesInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "SMART TIMETABLE & DAILY LOOP",
                color = CyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    taskNameInput = ""
                    taskTimeInput = ""
                    taskTypeInput = "STUDY"
                    taskNotesInput = ""
                    showAddTaskDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("add_task_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks populated. Insert default elements!", color = TextMutedGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("timetable_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TimetableRowItem(
                        task = task,
                        onAction = { action ->
                            when (action) {
                                "COMPLETE" -> viewModel.updateTask(task.copy(status = "COMPLETED"))
                                "SKIP" -> viewModel.updateTask(task.copy(status = "SKIPPED"))
                                "TOMORROW" -> {
                                    viewModel.updateTask(task.copy(status = "MOVED_TOMORROW"))
                                    Toast.makeText(context, "Task shifted to tomorrow", Toast.LENGTH_SHORT).show()
                                }
                                "EDIT" -> {
                                    taskNameInput = task.name
                                    taskTimeInput = task.timeRange
                                    taskTypeInput = task.type
                                    taskNotesInput = task.notes
                                    selectedTaskForEdit = task
                                }
                                "DELETE" -> viewModel.deleteTask(task)
                                "PENDING" -> viewModel.updateTask(task.copy(status = "PENDING"))
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Surface(modifier = Modifier.glassCard().padding(16.dp), color = CardGlassSurface) {
                Column {
                    Text("Add Timetable Task", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskNameInput,
                        onValueChange = { taskNameInput = it },
                        label = { Text("Task Name", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("add_name_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskTimeInput,
                        onValueChange = { taskTimeInput = it },
                        label = { Text("Time Range (e.g. 12:00 PM - 2:00 PM)", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("add_time_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Task Core Domain:", color = TextMutedGray, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("STUDY", "PROGRAMMING", "OTHER").forEach { type ->
                            val isSelected = taskTypeInput == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { taskTypeInput = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = DarkBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskNotesInput,
                        onValueChange = { taskNotesInput = it },
                        label = { Text("Optional Notes", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel", color = TextSolidWhite) }
                        Button(
                            onClick = {
                                if (taskNameInput.isNotBlank() && taskTimeInput.isNotBlank()) {
                                    viewModel.addTask(taskNameInput, taskTimeInput, taskTypeInput, taskNotesInput)
                                    showAddTaskDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Create", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }

    // Edit Task Dialog
    if (selectedTaskForEdit != null) {
        Dialog(onDismissRequest = { selectedTaskForEdit = null }) {
            Surface(modifier = Modifier.glassCard().padding(16.dp), color = CardGlassSurface) {
                Column {
                    Text("Modify Timetable Task", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskNameInput,
                        onValueChange = { taskNameInput = it },
                        label = { Text("Task Name", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("edit_name_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskTimeInput,
                        onValueChange = { taskTimeInput = it },
                        label = { Text("Time Range (e.g. 10:30 PM)", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Task Core Domain:", color = TextMutedGray, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("STUDY", "PROGRAMMING", "OTHER").forEach { type ->
                            val isSelected = taskTypeInput == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { taskTypeInput = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary,
                                    selectedLabelColor = DarkBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskNotesInput,
                        onValueChange = { taskNotesInput = it },
                        label = { Text("Task Notes", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { selectedTaskForEdit = null }) { Text("Cancel", color = TextSolidWhite) }
                        Button(
                            onClick = {
                                val t = selectedTaskForEdit
                                if (t != null && taskNameInput.isNotBlank() && taskTimeInput.isNotBlank()) {
                                    viewModel.updateTask(
                                        t.copy(
                                            name = taskNameInput,
                                            timeRange = taskTimeInput,
                                            type = taskTypeInput,
                                            notes = taskNotesInput
                                        )
                                    )
                                    selectedTaskForEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Save", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableRowItem(task: TimetableTask, onAction: (String) -> Unit) {
    val borderColor = when (task.status) {
        "COMPLETED" -> TealAccent.copy(alpha = 0.5f)
        "SKIPPED" -> ErrorNeon.copy(alpha = 0.5f)
        "MOVED_TOMORROW" -> PurpleSecondary.copy(alpha = 0.5f)
        else -> Color(0x33FFFFFF)
    }

    val statusIcon = when (task.status) {
        "COMPLETED" -> Icons.Default.CheckCircle
        "SKIPPED" -> Icons.Default.RemoveCircle
        "MOVED_TOMORROW" -> Icons.Default.ArrowCircleRight
        else -> Icons.Default.RadioButtonUnchecked
    }

    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(borderColor = borderColor)
            .clickable { isExpanded = !isExpanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (task.status == "COMPLETED") {
                        onAction("PENDING")
                    } else {
                        onAction("COMPLETE")
                    }
                }
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status Toggle",
                    tint = if (task.status == "COMPLETED") TealAccent else TextMutedGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.name,
                        color = if (task.status == "COMPLETED") TealAccent else TextSolidWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (task.type) {
                                    "STUDY" -> TealAccent.copy(alpha = 0.15f)
                                    "PROGRAMMING" -> CyanPrimary.copy(alpha = 0.15f)
                                    else -> PurpleSecondary.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            task.type,
                            color = when (task.type) {
                                "STUDY" -> TealAccent
                                "PROGRAMMING" -> CyanPrimary
                                else -> PurpleSecondary
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(task.timeRange, color = CyanPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Actions",
                    tint = TextSolidWhite
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(10.dp))
            if (task.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${task.notes}",
                    color = TextMutedGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Skip Button
                Button(
                    onClick = { onAction("SKIP") },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorNeon),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Skip", fontSize = 11.sp, color = TextSolidWhite)
                }

                // Defer to Tomorrow Button
                Button(
                    onClick = { onAction("TOMORROW") },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleSecondary),
                    modifier = Modifier.weight(1.3f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Tomorrow", fontSize = 11.sp, color = TextSolidWhite)
                }

                // Edit Button
                IconButton(
                    onClick = { onAction("EDIT") },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF))
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = TextSolidWhite, modifier = Modifier.size(16.dp))
                }

                // Delete Button
                IconButton(
                    onClick = { onAction("DELETE") },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorNeon.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorNeon, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 3: SUBJECT MANAGEMENT & WEEKLY PLAN
// ----------------------------------------------------
@Composable
fun SubjectsScreen(
    subjects: List<SubjectProgress>,
    weeklyPlans: List<WeeklyPlan>,
    viewModel: DashboardViewModel
) {
    var selectedTabSub by remember { mutableStateOf("Subjects") }
    var subjectToEdit by remember { mutableStateOf<SubjectProgress?>(null) }

    var chapterCountInput by remember { mutableStateOf("0") }
    var totalChaptersInput by remember { mutableStateOf("10") }
    var revisionInput by remember { mutableStateOf("NOT_REVISED") }
    var notesInput by remember { mutableStateOf("") }

    var weekDayToEdit by remember { mutableStateOf<WeeklyPlan?>(null) }
    var topicsIn by remember { mutableStateOf("") }
    var chCompletedIn by remember { mutableStateOf("0") }
    var testsGivenIn by remember { mutableStateOf("0") }
    var weakAreasIn by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        // Inner Sub Tabs
        TabRow(
            selectedTabIndex = if (selectedTabSub == "Subjects") 0 else 1,
            containerColor = DarkBackground,
            contentColor = CyanPrimary
        ) {
            Tab(
                selected = selectedTabSub == "Subjects",
                onClick = { selectedTabSub = "Subjects" },
                text = { Text("CLASS 12 Commerce", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabSub == "Weekly",
                onClick = { selectedTabSub = "Weekly" },
                text = { Text("WEEKLY GOAL PLANNER", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTabSub == "Subjects") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("subjects_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(subjects) { sub ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard()
                            .clickable {
                                subjectToEdit = sub
                                chapterCountInput = sub.currentChapter.toString()
                                totalChaptersInput = sub.totalChapters.toString()
                                revisionInput = sub.revisionStatus
                                notesInput = sub.notes
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sub.subjectName, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        "Revision: ${sub.revisionStatus.replace("_", " ")}",
                                        color = CyanPrimary,
                                        fontSize = 11.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (sub.completionStatus) {
                                                "COMPLETED" -> TealAccent.copy(alpha = 0.2f)
                                                else -> PurpleSecondary.copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        sub.completionStatus,
                                        color = if (sub.completionStatus == "COMPLETED") TealAccent else PurpleSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom progress
                            val percent = if (sub.totalChapters > 0) {
                                ((sub.currentChapter.toFloat() / sub.totalChapters) * 100).toInt()
                            } else 0

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { percent.toFloat() / 100f },
                                    color = if (percent >= 100) TealAccent else CyanPrimary,
                                    trackColor = Color(0x33FFFFFF),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    "$percent%",
                                    color = TextSolidWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Chapters completed: ${sub.currentChapter} / ${sub.totalChapters}",
                                    color = TextMutedGray,
                                    fontSize = 11.sp
                                )
                                if (sub.notes.isNotBlank()) {
                                    Text(
                                        sub.notes,
                                        color = TextMutedGray,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(150.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Weekly Planner Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(weeklyPlans) { plan ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard()
                            .clickable {
                                weekDayToEdit = plan
                                topicsIn = plan.topicsCompleted
                                chCompletedIn = plan.chaptersCompleted.toString()
                                testsGivenIn = plan.testsGiven.toString()
                                weakAreasIn = plan.weakAreas
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(plan.dayOfWeek, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Badge(containerColor = PurpleSecondary) { Text("Ch: ${plan.chaptersCompleted}", color = TextSolidWhite) }
                                    Badge(containerColor = TealAccent) { Text("Tests: ${plan.testsGiven}", color = DarkBackground) }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Topics Completed: ${plan.topicsCompleted.ifBlank { "None documented" }}", color = TextSolidWhite, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Weak Areas: ${plan.weakAreas.ifBlank { "None detected" }}", color = ErrorNeon, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal to Edit Subject Details
    if (subjectToEdit != null) {
        Dialog(onDismissRequest = { subjectToEdit = null }) {
            Surface(modifier = Modifier.glassCard().padding(16.dp), color = CardGlassSurface) {
                Column {
                    Text("Configure Subject: ${subjectToEdit?.subjectName}", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = chapterCountInput,
                        onValueChange = { chapterCountInput = it },
                        label = { Text("Chapters Completed", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("chapters_completed_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = totalChaptersInput,
                        onValueChange = { totalChaptersInput = it },
                        label = { Text("Total Chapters in Syllabus", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Revision Status", color = TextMutedGray, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("NOT_REVISED", "REVISED_ONCE", "REVISED_MULTIPLE").forEach { status ->
                            val isSel = revisionInput == status
                            FilterChip(
                                selected = isSel,
                                onClick = { revisionInput = status },
                                label = { Text(status.substringAfter("_"), fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanPrimary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Syllabus & Target Notes", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { subjectToEdit = null }) { Text("Cancel", color = TextSolidWhite) }
                        Button(
                            onClick = {
                                val s = subjectToEdit
                                val mins = chapterCountInput.toIntOrNull() ?: 0
                                val maxs = totalChaptersInput.toIntOrNull() ?: 10
                                if (s != null) {
                                    viewModel.updateSubjectChapters(s.subjectName, mins, maxs)
                                    viewModel.updateSubjectMetadata(s.subjectName, notesInput, revisionInput)
                                    subjectToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Apply", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }

    // Modal to Edit Weekly Plan
    if (weekDayToEdit != null) {
        Dialog(onDismissRequest = { weekDayToEdit = null }) {
            Surface(modifier = Modifier.glassCard().padding(16.dp), color = CardGlassSurface) {
                Column {
                    Text("Plan: ${weekDayToEdit?.dayOfWeek}", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = topicsIn,
                        onValueChange = { topicsIn = it },
                        label = { Text("Topics Covered Today", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = chCompletedIn,
                            onValueChange = { chCompletedIn = it },
                            label = { Text("Chapters Finish", color = TextMutedGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = testsGivenIn,
                            onValueChange = { testsGivenIn = it },
                            label = { Text("Tests Attempt", color = TextMutedGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = weakAreasIn,
                        onValueChange = { weakAreasIn = it },
                        label = { Text("Weak areas requiring Revision", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { weekDayToEdit = null }) { Text("Cancel", color = TextSolidWhite) }
                        Button(
                            onClick = {
                                val currentDay = weekDayToEdit
                                if (currentDay != null) {
                                    val chapters = chCompletedIn.toIntOrNull() ?: 0
                                    val tests = testsGivenIn.toIntOrNull() ?: 0
                                    viewModel.updateWeeklyPlan(currentDay.dayOfWeek, topicsIn, chapters, tests, weakAreasIn)
                                    weekDayToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Save", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 4: PROGRAMMING & SELF IMPROVEMENT TRACKERS
// ----------------------------------------------------
@Composable
fun TrackersScreen(
    programmingTracks: List<ProgrammingTracker>,
    selfImprovements: List<SelfImprovementTracker>,
    viewModel: DashboardViewModel
) {
    var trackerToEdit by remember { mutableStateOf<ProgrammingTracker?>(null) }
    var devHoursInput by remember { mutableStateOf("0.0") }
    var devProjectsInput by remember { mutableStateOf("0") }
    var devConceptsInput by remember { mutableStateOf("") }
    var devNotesInput by remember { mutableStateOf("") }

    var selectedSection by remember { mutableStateOf("Coding") }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = if (selectedSection == "Coding") 0 else 1,
            containerColor = DarkBackground,
            contentColor = CyanPrimary
        ) {
            Tab(selected = selectedSection == "Coding", onClick = { selectedSection = "Coding" }, text = { Text("CODING & LANGUAGES", fontWeight = FontWeight.Bold) })
            Tab(selected = selectedSection == "Growth", onClick = { selectedSection = "Growth" }, text = { Text("GROWTH & DELIBERATE PROGRESS", fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSection == "Coding") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("programming_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(programmingTracks) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard()
                            .clickable {
                                trackerToEdit = item
                                devHoursInput = item.learningHours.toString()
                                devProjectsInput = item.projectsCompleted.toString()
                                devConceptsInput = item.conceptsLearned
                                devNotesInput = item.notes
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Code, contentDescription = null, tint = CyanPrimary, modifier = Modifier.padding(end = 8.dp))
                                    Text(item.trackName, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Badge(containerColor = CyanPrimary) {
                                    Text("${item.projectsCompleted} Projects", color = DarkBackground, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Learning Hours Invested: ${item.learningHours} hrs", color = TextSolidWhite, fontSize = 13.sp)
                            Text("Key Concepts: ${item.conceptsLearned.ifBlank { "None specified" }}", color = TextMutedGray, fontSize = 12.sp)

                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tracker Notes: ${item.notes}", color = TealAccent, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Self Improvement Sliders and ratings
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                selfImprovements.forEach { item ->
                    var sliderVal by remember(item.levelPercent) { mutableFloatStateOf(item.levelPercent.toFloat()) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard()
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.topicName, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${sliderVal.toInt()}% Progress", color = PurpleSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.focusText.ifBlank { "Daily intentional habits and assessments" }, color = TextMutedGray, fontSize = 11.sp)

                            Slider(
                                value = sliderVal,
                                onValueChange = {
                                    sliderVal = it
                                },
                                onValueChangeFinished = {
                                    viewModel.updateSelfImprovement(item.topicName, item.focusText, sliderVal.toInt(), item.notes)
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = PurpleSecondary,
                                    activeTrackColor = PurpleSecondary,
                                    inactiveTrackColor = Color(0x33FFFFFF)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal to Edit Coding Trackers
    if (trackerToEdit != null) {
        Dialog(onDismissRequest = { trackerToEdit = null }) {
            Surface(modifier = Modifier.glassCard().padding(16.dp), color = CardGlassSurface) {
                Column {
                    Text("Modify: ${trackerToEdit?.trackName}", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = devHoursInput,
                        onValueChange = { devHoursInput = it },
                        label = { Text("Learning Hours", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("track_hours_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = devProjectsInput,
                        onValueChange = { devProjectsInput = it },
                        label = { Text("Completed Projects Count", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("track_projects_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = devConceptsInput,
                        onValueChange = { devConceptsInput = it },
                        label = { Text("Key Concepts Mastered", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = devNotesInput,
                        onValueChange = { devNotesInput = it },
                        label = { Text("Growth Goal Notes", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { trackerToEdit = null }) { Text("Cancel", color = TextSolidWhite) }
                        Button(
                            onClick = {
                                val t = trackerToEdit
                                val hrs = devHoursInput.toDoubleOrNull() ?: 0.0
                                val projs = devProjectsInput.toIntOrNull() ?: 0
                                if (t != null) {
                                    viewModel.updateProgrammingTrack(t.trackName, hrs, projs, devConceptsInput, devNotesInput)
                                    trackerToEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Save", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 5: HABIT TRACKER SCREEN
// ----------------------------------------------------
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    habitLogs: List<HabitLog>,
    viewModel: DashboardViewModel
) {
    var showHabitStats by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "DAILY HABIT STACKS & PERSISTENCE",
                color = CyanPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { showHabitStats = !showHabitStats },
                modifier = Modifier.testTag("habit_stats_toggle")
            ) {
                Icon(
                    imageVector = if (showHabitStats) Icons.Filled.ViewAgenda else Icons.Filled.BarChart,
                    contentDescription = "Toggle completion grid model view",
                    tint = CyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (showHabitStats) {
            // Visual habit compliance stats card
            HabitComplianceStats(habits, habitLogs)
        } else {
            // Standard lists of Habits
            val currentDateStr by viewModel.currentDateStr.collectAsStateWithLifecycle()
            val completedToday = habitLogs.filter { it.dateStr == currentDateStr }.map { it.habitName }.toSet()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("habits_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(habits) { habit ->
                    val checked = completedToday.contains(habit.name)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(borderColor = if (checked) TealAccent.copy(alpha = 0.5f) else Color(0x33FFFFFF))
                            .clickable {
                                viewModel.toggleHabitToday(habit.name, !checked)
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { viewModel.toggleHabitToday(habit.name, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = TealAccent, uncheckedColor = TextMutedGray),
                                    modifier = Modifier.testTag("habit_cb_${habit.name.replace(" ", "_")}")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    habit.name,
                                    color = if (checked) TealAccent else TextSolidWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = ErrorNeon, modifier = Modifier.size(16.dp))
                                    Text("${habit.streak}d", color = TextSolidWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                    Text("${habit.monthlyStreak}m", color = TextSolidWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitComplianceStats(habits: List<Habit>, logs: List<HabitLog>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall completions indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Compliance Outlook Grid", color = CyanPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                // Custom completion circles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    habits.take(4).forEach { habit ->
                        val habitLogsCount = logs.count { it.habitName == habit.name && it.status }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(46.dp)) {
                                CircularProgressIndicator(
                                    progress = { 1.0f },
                                    color = Color(0x22FFFFFF),
                                    strokeWidth = 3.dp
                                )
                                CircularProgressIndicator(
                                    progress = { (habitLogsCount.toFloat() / 30f).coerceIn(0f, 1f) },
                                    color = PurpleSecondary,
                                    strokeWidth = 3.dp
                                )
                                Text("$habitLogsCount", color = TextSolidWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(habit.name.take(8) + "..", color = TextMutedGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Habit grid rendering past 5 days matrix
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .padding(16.dp)
        ) {
            Column {
                Text("HISTORIC HABIT MATRIX (Past 5 Days)", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dates = (0..4).map { offset ->
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DATE, -offset)
                    sdf.format(cal.time)
                }.reversed()

                dates.forEach { dt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dt, color = TextSolidWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            habits.take(5).forEach { h ->
                                val completedOnDate = logs.any { it.habitName == h.name && it.dateStr == dt && it.status }
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (completedOnDate) TealAccent else Color(0x33FFFFFF))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 6: NOTES MANAGEMENT SYSTEM
// ----------------------------------------------------
@Composable
fun NotesScreen(notes: List<NoteItem>, viewModel: DashboardViewModel) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("STUDY") }

    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category selectors
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("STUDY", "PROGRAMMING", "PERSONAL").forEach { cat ->
                    val isSel = selectedCategory == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = DarkBackground
                        ),
                        modifier = Modifier.testTag("notes_chip_$cat")
                    )
                }
            }

            Button(
                onClick = {
                    titleInput = ""
                    contentInput = ""
                    showAddNoteDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("add_note_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Note", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredNotes = notes.filter { it.category == selectedCategory }

        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No notes inside categorizations. Create one!", color = TextMutedGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("notes_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredNotes) { note ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard()
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    note.title,
                                    color = TextSolidWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { viewModel.deleteNote(note) },
                                    modifier = Modifier.size(24.dp).testTag("delete_note_${note.id}")
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete note", tint = ErrorNeon, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(note.content, color = TextMutedGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        Dialog(onDismissRequest = { showAddNoteDialog = false }) {
            Surface(modifier = Modifier.glassCard().padding(16.dp), color = CardGlassSurface) {
                Column {
                    Text("Create New OS Note", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Note Title", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier.fillMaxWidth().testTag("note_title_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Write content body ...", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextSolidWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("note_content_field")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel", color = TextSolidWhite) }
                        Button(
                            onClick = {
                                if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                                    viewModel.addNote(titleInput, contentInput, selectedCategory)
                                    showAddNoteDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Save", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}
