@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

// ----------------------------------------------------
// Theme Color Definitions
// ----------------------------------------------------
val DarkBackground = Color(0xFF0F172A)      // Deep Indigo Space Dark
val DeepSlateCard = Color(0xFF1E293B)       // Glass Card base
val CyanPrimary = Color(0xFF06B6D4)         // Radiant Cyan tech highlight
val PurpleAccent = Color(0xFF8B5CF6)        // Gamification / XP color
val EmeraldPositive = Color(0xFF10B981)     // Completed / Mastered positive
val CrimsonAlert = Color(0xFFEF4444)        // Error / Alert / skipped target
val AmberWarm = Color(0xFFF59E0B)           // Streak / Coins
val TextLightGray = Color(0xFF94A3B8)       // Subtitle labels
val TextSolidWhite = Color(0xFFF8FAFC)      // High-contrast clean white

fun Modifier.glassCard(
    borderColor: Color = Color(0xFF334155),
    backgroundColor: Color = DeepSlateCard
) = this
    .clip(RoundedCornerShape(16.dp))
    .background(backgroundColor)
    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    .padding(16.dp)

// ----------------------------------------------------
// Main Entrance Entry Point layout
// ----------------------------------------------------
@Composable
fun StudentOSLayout(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val activeUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isWizardNeeded by viewModel.needsSetupWizard.collectAsStateWithLifecycle()

    var showBackupDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf(false) }
    var importRawJson by remember { mutableStateOf("") }

    if (activeUser == null) {
        // Welcome and Secured Authentication Entrance
        AuthenticationScreen(viewModel = viewModel)
    } else if (isWizardNeeded) {
        // First Signup setup wizard screen and target metrics setting
        SetupWizardScreen(viewModel = viewModel)
    } else {
        // Full Student Operating System workspace interface
        val userObj = activeUser!!
        val taskList by viewModel.tasks.collectAsStateWithLifecycle()
        val subjectList by viewModel.subjects.collectAsStateWithLifecycle()
        val weeklyPlanList by viewModel.weeklyPlans.collectAsStateWithLifecycle()
        val progTrackerList by viewModel.programmingTrackers.collectAsStateWithLifecycle()
        val habitList by viewModel.habits.collectAsStateWithLifecycle()
        val habitLogList by viewModel.habitLogs.collectAsStateWithLifecycle()
        val noteList by viewModel.notes.collectAsStateWithLifecycle()
        val goalList by viewModel.goals.collectAsStateWithLifecycle()
        val sessionList by viewModel.studySessions.collectAsStateWithLifecycle()
        val chStatusList by viewModel.chapterStatuses.collectAsStateWithLifecycle()
        val journalList by viewModel.journalEntries.collectAsStateWithLifecycle()
        val friendsList by viewModel.friends.collectAsStateWithLifecycle()
        val selfImpList by viewModel.selfImprovements.collectAsStateWithLifecycle()

        val dailyProgress by viewModel.dailyProgressPercent.collectAsStateWithLifecycle()
        val liveTime by viewModel.currentTime.collectAsStateWithLifecycle()
        val dateStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

        var selectedTab by remember { mutableStateOf("Dashboard") }
        val tabs = listOf("Dashboard", "Timetable", "Subjects", "Trackers", "Habits", "Notes", "Analytics")

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
                                "STUDENT LIFE OS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextSolidWhite,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PurpleAccent.copy(alpha = 0.2f))
                                .border(1.dp, PurpleAccent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "LVL ${userObj.level}",
                                color = TextSolidWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
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
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = "Sign Out", tint = CrimsonAlert)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkBackground,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar")
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val icon = when (tab) {
                            "Dashboard" -> Icons.Filled.Dashboard
                            "Timetable" -> Icons.Filled.CalendarMonth
                            "Subjects" -> Icons.Filled.MenuBook
                            "Trackers" -> Icons.Filled.SettingsInputHdmi
                            "Habits" -> Icons.Filled.CheckCircle
                            "Notes" -> Icons.Filled.Notes
                            else -> Icons.Filled.BarChart
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.testTag("nav_tab_$tab"),
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab,
                                    tint = if (isSelected) CyanPrimary else TextLightGray
                                )
                            },
                            label = {
                                Text(
                                    text = tab,
                                    fontSize = 11.sp,
                                    color = if (isSelected) TextSolidWhite else TextLightGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = DeepSlateCard
                            )
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    "Dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        userObj = userObj,
                        dailyProgress = dailyProgress,
                        liveTime = liveTime,
                        dateStr = dateStr,
                        sessionList = sessionList,
                        journalList = journalList,
                        friendsList = friendsList,
                        goalList = goalList
                    )
                    "Timetable" -> TimetableScreen(
                        tasks = taskList,
                        viewModel = viewModel
                    )
                    "Subjects" -> SubjectsScreen(
                        subjects = subjectList,
                        chapterStatuses = chStatusList,
                        viewModel = viewModel
                    )
                    "Trackers" -> TrackersScreen(
                        trackers = progTrackerList,
                        selfImprovements = selfImpList,
                        viewModel = viewModel
                    )
                    "Habits" -> HabitsScreen(
                        habits = habitList,
                        logs = habitLogList,
                        viewModel = viewModel
                    )
                    "Notes" -> NotesScreen(
                        notes = noteList,
                        viewModel = viewModel
                    )
                    "Analytics" -> AnalyticsScreen(
                        sessions = sessionList,
                        journal = journalList,
                        habits = habitList,
                        logs = habitLogList
                    )
                }
            }
        }

        // Space OS Backup / Restore dialogue Panel
        if (showBackupDialog) {
            Dialog(onDismissRequest = { showBackupDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = DeepSlateCard
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "DATA OPERATING SYSTEM BACKUP",
                            color = TextSolidWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            "To comply with local security protocols, all actions are persistent on SQLite Room. You can export or import your data JSON backup package as an open-source standard block.",
                            color = TextLightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val backupJson = viewModel.exportJson()
                                        clipboardManager.setText(AnnotatedString(backupJson))
                                        Toast.makeText(context, "Backup JSON copied to Clipboard!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export to Clipboard", fontSize = 11.sp, color = DarkBackground)
                            }

                            Button(
                                onClick = { importText = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                            ) {
                                Icon(Icons.Filled.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import JSON", fontSize = 11.sp, color = TextSolidWhite)
                            }
                        }

                        if (importText) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = importRawJson,
                                onValueChange = { importRawJson = it },
                                label = { Text("Paste JSON Backup", color = TextLightGray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("import_raw_json"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextSolidWhite,
                                    unfocusedTextColor = TextLightGray,
                                    focusedBorderColor = CyanPrimary,
                                    unfocusedBorderColor = TextLightGray
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val ok = viewModel.importJson(importRawJson)
                                        if (ok) {
                                            Toast.makeText(context, "Operating System Reloaded Successfully!", Toast.LENGTH_SHORT).show()
                                            showBackupDialog = false
                                            importText = false
                                        } else {
                                            Toast.makeText(context, "Invalid Backup Json Format.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive)
                            ) {
                                Text("Verify & Restore Database", color = TextSolidWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { showBackupDialog = false }) {
                            Text("Close Dashboard Menu", color = TextLightGray)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Welcome & Authenticaton Views (Local Storage Hashed Access)
// ----------------------------------------------------
@Composable
fun AuthenticationScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var isForgotMode by remember { mutableStateOf(false) }

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var challengeQuestionAnswer by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.AccountCircle,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                "STUDENT LIFE OS IDENTITY ENGINE",
                fontSize = 15.sp,
                color = TextLightGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                if (isSignUpMode) "Register Secure Private Local Space" else if (isForgotMode) "Verification Recovery Control" else "Sign In Private Workspace",
                fontSize = 18.sp,
                color = TextSolidWhite,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = { Text("Private Username Key", color = TextLightGray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_username"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextSolidWhite,
                    unfocusedTextColor = TextLightGray,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextLightGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isForgotMode) {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password", color = TextLightGray) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextSolidWhite,
                        unfocusedTextColor = TextLightGray,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextLightGray
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isSignUpMode || isForgotMode) {
                Text(
                    "Challenge Question: What's your favorite board study subject?",
                    color = TextLightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = challengeQuestionAnswer,
                    onValueChange = { challengeQuestionAnswer = it },
                    label = { Text("Verification Answer", color = TextLightGray) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_challenge"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextSolidWhite,
                        unfocusedTextColor = TextLightGray,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextLightGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isForgotMode) {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Type New Password", color = TextLightGray) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextSolidWhite,
                        unfocusedTextColor = TextLightGray,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = TextLightGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (isSignUpMode) {
                        viewModel.register(usernameInput, passwordInput, challengeQuestionAnswer) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) isSignUpMode = false
                        }
                    } else if (isForgotMode) {
                        viewModel.resetPassword(usernameInput, challengeQuestionAnswer, passwordInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) isForgotMode = false
                        }
                    } else {
                        viewModel.login(usernameInput, passwordInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(
                    if (isSignUpMode) "Generate Secure Private Account" else if (isForgotMode) "Revise Password" else "Verify & Boot Operating System",
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    isSignUpMode = !isSignUpMode
                    isForgotMode = false
                }) {
                    Text(
                        if (isSignUpMode) "Have account? Sign In" else "Create Private OS Account",
                        color = CyanPrimary,
                        fontSize = 12.sp
                    )
                }

                if (!isSignUpMode) {
                    TextButton(onClick = {
                        isForgotMode = !isForgotMode
                    }) {
                        Text(
                            if (isForgotMode) "Cancel Recovery" else "Forgot Key?",
                            color = CrimsonAlert,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = EmeraldPositive, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "All credentials are securely hashed locally. Absolute data isolation.",
                    color = EmeraldPositive,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ----------------------------------------------------
// Setup Wizard screen for profile init
// ----------------------------------------------------
@Composable
fun SetupWizardScreen(viewModel: DashboardViewModel) {
    val commerceOptions = listOf("Accounts", "Mathematics", "Economics", "OCM", "Secretarial Practice", "English", "Hindi")
    val selectedSubjects = remember { mutableStateListOf("Accounts", "Mathematics", "Economics") }
    var targetStudyHoursInput by remember { mutableStateOf("80.0") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AmberWarm, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "STUDENT OS SETUP WIZARD",
                color = TextSolidWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                "Configure your Board Courses & Target metrics. This seeds localized subject chapter status and milestone metrics. Zero fabricated stats.",
                color = TextLightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Choose Your Active Board Subjects:",
                color = TextSolidWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.height(160.dp)) {
                items(commerceOptions) { subject ->
                    val isChecked = selectedSubjects.contains(subject)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedSubjects.remove(subject)
                                else selectedSubjects.add(subject)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(subject, color = TextSolidWhite, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = targetStudyHoursInput,
                onValueChange = { targetStudyHoursInput = it },
                label = { Text("Monthly Focus Target Study (Hours)", color = TextLightGray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("wizard_hours"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextSolidWhite,
                    unfocusedTextColor = TextLightGray,
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = TextLightGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val targetDouble = targetStudyHoursInput.toDoubleOrNull() ?: 80.0
                    viewModel.completeWizard(selectedSubjects.toList(), targetDouble)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive)
            ) {
                Text("Initialize Operating System", color = TextSolidWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------------------------------------------
// 1. Interactive Dashboard View with XP levels & Logs
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userObj: UserLocal,
    dailyProgress: Int,
    liveTime: String,
    dateStr: String,
    sessionList: List<StudySession>,
    journalList: List<JournalEntry>,
    friendsList: List<Friend>,
    goalList: List<GoalMetric>
) {
    var showStudyLogDialog by remember { mutableStateOf(false) }
    var showJournalDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isGeneratingAi by viewModel.isGeneratingAiCoach.collectAsStateWithLifecycle()
    val coachFeedback by viewModel.aiCoachFeedback.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space OS Cosmic clock title Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "STUDENT DECK",
                        color = TextLightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "COMMERCE SYSTEM SECURE",
                        color = TextSolidWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        liveTime,
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        dateStr,
                        color = TextLightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // LEVEL GAMIFICATION METER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(borderColor = PurpleAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = AmberWarm, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Level ${userObj.level} Warrior",
                            color = TextSolidWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Row {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = AmberWarm, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${userObj.coins} Gold Coins", color = AmberWarm, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // XP progress bar
                val levelUpThreshold = userObj.level * 120
                val progressPercent = (userObj.xp.toFloat() / levelUpThreshold).coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = PurpleAccent,
                    trackColor = DarkBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("XP: ${userObj.xp} / $levelUpThreshold", color = TextLightGray, fontSize = 11.sp)
                    Text("Streak Counter: ${userObj.streak} Days 🔥", color = AmberWarm, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // DAILY ACTION BUTTONS ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showStudyLogDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("log_study_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Study", color = DarkBackground, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showJournalDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("log_journal_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = null, tint = TextSolidWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Journal", color = TextSolidWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ACTIVE TIMETABLE TARGET (Current vs Next task)
        item {
            val currentAndNextTaskState by viewModel.currentAndNextTask.collectAsStateWithLifecycle()
            val (current, next) = currentAndNextTaskState
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Text(
                    "CLOCK WORK SCHEDULER",
                    color = TextLightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Track:", color = TextLightGray, fontSize = 11.sp)
                        Text(
                            current?.name ?: "No scheduled items active.",
                            color = if (current != null) CyanPrimary else TextLightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(current?.timeRange ?: "Free Block", color = TextLightGray, fontSize = 11.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Coming Up:", color = TextLightGray, fontSize = 11.sp)
                        Text(
                            next?.name ?: "No upcoming tasks.",
                            color = TextSolidWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(next?.timeRange ?: "", color = TextLightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        // REAL ANALYTICS WIDGETS
        item {
            val totalHours by viewModel.totalStudyHoursSpent.collectAsStateWithLifecycle()
            val certsCount by viewModel.totalChaptersCompletedCount.collectAsStateWithLifecycle()
            val testsAverage by viewModel.recentAverageTestScores.collectAsStateWithLifecycle()
            val codingHours by viewModel.programmingHoursCompleted.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Text(
                    "GENUINE TRACKING STATISTICS (ZERO GENERATED FABRICATIONS)",
                    color = TextLightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(DarkBackground, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Focused Study", color = TextLightGray, fontSize = 11.sp)
                        Text("${String.format("%.1f", totalHours)} Hrs", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(DarkBackground, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Code Practice", color = TextLightGray, fontSize = 11.sp)
                        Text("${String.format("%.1f", codingHours)} Hrs", color = PurpleAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(DarkBackground, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Chs. Mastered", color = TextLightGray, fontSize = 11.sp)
                        Text("$certsCount Chapters", color = EmeraldPositive, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(DarkBackground, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Mock Score Avg.", color = TextLightGray, fontSize = 11.sp)
                        Text(if (testsAverage > 0.0) "${String.format("%.1f", testsAverage)}%" else "No tests log", color = AmberWarm, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        // DOCK AI STUDY COACH ADVISORY
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(borderColor = PurpleAccent.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "AI STUDY ADVOCATE COACH",
                            color = TextSolidWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.generateAiCoachFeedback() },
                        enabled = !isGeneratingAi,
                        modifier = Modifier.testTag("trigger_ai_coach"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isGeneratingAi) "Analyzing..." else "Ask Coach", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = coachFeedback,
                    color = TextSolidWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 18.sp
                )
            }
        }

        // SOCIAL ACCOUNTABILITY SCOREBOARD
        item {
            var showAddFriendDialog by remember { mutableStateOf(false) }
            var friendInput by remember { mutableStateOf("") }
            var privatePrivacySetting by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "STUDY MATES ACCOUNTABILITY SCOREBOARD",
                        color = TextLightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Add mate", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // simulated self user stats item on scoreboard
                val totalStudyHours by viewModel.totalStudyHoursSpent.collectAsStateWithLifecycle()
                val masterChCount by viewModel.totalChaptersCompletedCount.collectAsStateWithLifecycle()

                val sortedMates = (friendsList + Friend(
                    friendUsername = "${userObj.username} (You)",
                    xp = userObj.xp + (userObj.level * 120),
                    streak = userObj.streak,
                    chaptersCompleted = masterChCount,
                    weeklyHours = totalStudyHours,
                    isPending = false
                )).sortedByDescending { it.xp }

                sortedMates.forEachIndexed { idx, friend ->
                    val isSelf = friend.friendUsername.contains("(You)")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(if (isSelf) PurpleAccent.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "#${idx + 1} ",
                                color = if (idx == 0) AmberWarm else TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Column {
                                Text(
                                    friend.friendUsername,
                                    color = if (isSelf) CyanPrimary else TextSolidWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    if (friend.privacySetting == "PRIVATE" && !isSelf) "Privacy: Scores Only" else "🔥 ${friend.streak} Day | 📚 ${friend.chaptersCompleted} Mastered",
                                    color = TextLightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            if (friend.privacySetting == "PRIVATE" && !isSelf) "${friend.xp} XP" else "${String.format("%.1f", friend.weeklyHours)} Hrs | ${friend.xp} XP",
                            color = if (idx == 0) AmberWarm else TextLightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Privacy Switch toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scoreboard Mode Option:", color = TextLightGray, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (privatePrivacySetting) "Show Scores Only" else "Full Dynamic progress", color = CyanPrimary, fontSize = 11.sp)
                        Switch(
                            checked = privatePrivacySetting,
                            onCheckedChange = { privatePrivacySetting = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }
                }

                if (showAddFriendDialog) {
                    Dialog(onDismissRequest = { showAddFriendDialog = false }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DeepSlateCard,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Add Accountability Mate", color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = friendInput,
                                    onValueChange = { friendInput = it },
                                    label = { Text("Friend profile ID or username", color = TextLightGray) },
                                    modifier = Modifier.fillMaxWidth().testTag("add_friend_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextSolidWhite,
                                        unfocusedTextColor = TextLightGray,
                                        focusedBorderColor = CyanPrimary,
                                        unfocusedBorderColor = TextLightGray
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (friendInput.isNotEmpty()) {
                                            viewModel.sendFriendRequestSimulated(friendInput)
                                            friendInput = ""
                                            showAddFriendDialog = false
                                            Toast.makeText(context, "Partner connection invite generated!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                                ) {
                                    Text("Send Invite Code")
                                }
                            }
                        }
                    }
                }
            }
        }

        // PENDING COMPANIONS REJECT / INCOMING REQUEST PANEL
        val pendings = friendsList.filter { it.isPending }
        if (pendings.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().glassCard(borderColor = CrimsonAlert)) {
                    Text("Pending accountability request connection code:", color = CrimsonAlert, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    pendings.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.friendUsername, color = TextSolidWhite, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.acceptFriendRequest(p.friendUsername) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Accept", fontSize = 10.sp, color = TextSolidWhite)
                                }
                                Button(
                                    onClick = { viewModel.declineFriendRequest(p.friendUsername) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Deny", fontSize = 10.sp, color = TextSolidWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1.1 Study Logging dialogue block
    if (showStudyLogDialog) {
        var logSubject by remember { mutableStateOf("Accounts") }
        var logChapter by remember { mutableStateOf("") }
        var logDurationInput by remember { mutableStateOf("1.5") }
        var logConfidence by remember { mutableStateOf(3) }
        var logIsRevision by remember { mutableStateOf(false) }
        var testScoreInput by remember { mutableStateOf("") }
        var logNotesInput by remember { mutableStateOf("") }

        val subjectsSelection = listOf("Accounts", "Mathematics", "Economics", "OCM", "Secretarial Practice", "English", "Hindi")

        Dialog(onDismissRequest = { showStudyLogDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            "LOG REAL STUDY SESSION",
                            color = TextSolidWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Text("Subject Course Selection:", color = TextLightGray, fontSize = 11.sp)
                        LazyRow(modifier = Modifier.padding(vertical = 6.dp)) {
                            items(subjectsSelection) { sub ->
                                val isSelected = logSubject == sub
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyanPrimary else DarkBackground)
                                        .clickable { logSubject = sub }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(sub, color = if (isSelected) DarkBackground else TextSolidWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = logChapter,
                            onValueChange = { logChapter = it },
                            label = { Text("Chapter Topic Name", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().testTag("log_chapter_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSolidWhite,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TextLightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = logDurationInput,
                            onValueChange = { logDurationInput = it },
                            label = { Text("Duration spent (Hours)", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().testTag("log_duration_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSolidWhite,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TextLightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Performance Confidence Rating (1 to 5):", color = TextLightGray, fontSize = 11.sp)
                        Row(modifier = Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { rate ->
                                val isSelected = logConfidence == rate
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AmberWarm else DarkBackground)
                                        .clickable { logConfidence = rate }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(rate.toString(), color = TextSolidWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = logIsRevision, onCheckedChange = { logIsRevision = it }, colors = CheckboxDefaults.colors(checkedColor = CyanPrimary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("This is a strategic review/revision session", color = TextSolidWhite, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = testScoreInput,
                            onValueChange = { testScoreInput = it },
                            label = { Text("Associated Test Score % (E.g. 85, Optional)", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSolidWhite,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TextLightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = logNotesInput,
                            onValueChange = { logNotesInput = it },
                            label = { Text("Study review notes...", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextSolidWhite,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = TextLightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (logChapter.isEmpty()) {
                                    Toast.makeText(context, "Please enter chapter name topic.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val duration = logDurationInput.toDoubleOrNull() ?: 1.0
                                val scoreDouble = testScoreInput.toDoubleOrNull()
                                viewModel.logStudySession(
                                    subject = logSubject,
                                    chapter = logChapter,
                                    durationHrs = duration,
                                    confidence = logConfidence,
                                    notes = logNotesInput,
                                    isRevision = logIsRevision,
                                    examScore = scoreDouble
                                )
                                showStudyLogDialog = false
                                Toast.makeText(context, "Study Session securely logged! XP awarded.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive)
                        ) {
                            Text("Secure Session Log", color = TextSolidWhite, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showStudyLogDialog = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("Discard", color = CrimsonAlert)
                        }
                    }
                }
            }
        }
    }

    // 1.2 Learning Journal dialogue block
    if (showJournalDialog) {
        var inputLearned by remember { mutableStateOf("") }
        var inputMistakes by remember { mutableStateOf("") }
        var inputConfused by remember { mutableStateOf("") }
        var inputRevise by remember { mutableStateOf("") }
        var inputWin by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showJournalDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            "DAILY LEARNING REFLECTION",
                            color = TextSolidWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Secure daily evaluation journal. High-impact reflection questions stored locally.",
                            color = TextLightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = inputLearned,
                            onValueChange = { inputLearned = it },
                            label = { Text("What did I learn today?", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().height(70.dp).testTag("journal_q1"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputMistakes,
                            onValueChange = { inputMistakes = it },
                            label = { Text("What mistakes did I make?", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().height(70.dp).testTag("journal_q2"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputConfused,
                            onValueChange = { inputConfused = it },
                            label = { Text("What confused me the most?", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().height(70.dp).testTag("journal_q3"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputRevise,
                            onValueChange = { inputRevise = it },
                            label = { Text("What topics should I revise?", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().height(70.dp).testTag("journal_q4"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputWin,
                            onValueChange = { inputWin = it },
                            label = { Text("What was my major win today?", color = TextLightGray) },
                            modifier = Modifier.fillMaxWidth().height(70.dp).testTag("journal_q5"),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (inputLearned.isEmpty() || inputWin.isEmpty()) {
                                    Toast.makeText(context, "Please answer at least lessons learned and win.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.addJournalEntry(
                                    learned = inputLearned,
                                    mistakes = inputMistakes,
                                    confused = inputConfused,
                                    revise = inputRevise,
                                    win = inputWin
                                )
                                showJournalDialog = false
                                Toast.makeText(context, "Reflection filed permanently! streak and XP logged.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive)
                        ) {
                            Text("File Daily Journal", color = TextSolidWhite, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showJournalDialog = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("Discard", color = CrimsonAlert)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Timetable Management (Edit, Skip, Reprioritize)
// ----------------------------------------------------
@Composable
fun TimetableScreen(tasks: List<TimetableTask>, viewModel: DashboardViewModel) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskNameInput by remember { mutableStateOf("") }
    var taskTimeInput by remember { mutableStateOf("4:00 PM - 5:30 PM") }
    var taskCategory by remember { mutableStateOf("STUDY") }
    var taskNotesInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DYNAMIC SCHEDULER", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("ACTIVE ROTATIONS", color = TextSolidWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }

                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = DarkBackground)
                    Text("Add Row", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Missed tasks recovery prompt section
        val missedTasks = tasks.filter { it.status == "SKIPPED" || it.status == "MOVED_TOMORROW" }
        if (missedTasks.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(borderColor = CrimsonAlert)
                ) {
                    Text("MISSED WORK RECOVERY QUEUE", color = CrimsonAlert, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Overdue or skipped slots are parked here. Tackle them to earn double XP!", color = TextLightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    missedTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(task.name, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(task.timeRange, color = TextLightGray, fontSize = 11.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.updateTask(task.copy(status = "COMPLETED")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPositive),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Re-Do", fontSize = 10.sp, color = TextSolidWhite)
                                }
                                Button(
                                    onClick = { viewModel.deleteTask(task) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Discard", fontSize = 10.sp, color = TextSolidWhite)
                                }
                            }
                        }
                    }
                }
            }
        }

        items(tasks.filter { it.status != "SKIPPED" && it.status != "MOVED_TOMORROW" }) { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (task.status) {
                                        "COMPLETED" -> EmeraldPositive
                                        else -> AmberWarm
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            task.name,
                            color = TextSolidWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = if (task.status == "COMPLETED") androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    }
                    Text("${task.timeRange} | ${task.type}", color = TextLightGray, fontSize = 11.sp)
                    if (task.notes.isNotEmpty()) {
                        Text(task.notes, color = TextLightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (task.status != "COMPLETED") {
                        IconButton(onClick = { viewModel.updateTask(task.copy(status = "COMPLETED")) }) {
                            Icon(Icons.Filled.Check, contentDescription = "Complete", tint = EmeraldPositive)
                        }

                        IconButton(onClick = { viewModel.updateTask(task.copy(status = "SKIPPED")) }) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Skip to Recovery", tint = CrimsonAlert)
                        }
                    }

                    IconButton(onClick = { viewModel.deleteTask(task) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete row", tint = TextLightGray)
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ADD SCHEDULE SLOT", color = TextSolidWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = taskNameInput,
                        onValueChange = { taskNameInput = it },
                        label = { Text("Task Target Board Activity (e.g., Accounts Study block)", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth().testTag("add_task_name_field"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskTimeInput,
                        onValueChange = { taskTimeInput = it },
                        label = { Text("Time range (e.g., 5:00 PM - 6:30 PM)", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth().testTag("add_task_time_field"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Slot Category:", color = TextLightGray, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("STUDY", "PROGRAMMING", "HABIT", "OTHER").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (taskCategory == type) CyanPrimary else DarkBackground)
                                    .clickable { taskCategory = type }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(type, color = if (taskCategory == type) DarkBackground else TextSolidWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskNotesInput,
                        onValueChange = { taskNotesInput = it },
                        label = { Text("Additional directives", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (taskNameInput.isEmpty()) return@Button
                            viewModel.addTask(taskNameInput, taskTimeInput, taskCategory, taskNotesInput)
                            showAddTaskDialog = false
                            taskNameInput = ""
                            Toast.makeText(context, "Activity Seeded!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Add to Timetable Grid")
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. Subjects Curriculum Chapter status Screen
// ----------------------------------------------------
@Composable
fun SubjectsScreen(
    subjects: List<SubjectProgress>,
    chapterStatuses: List<ChapterStatus>,
    viewModel: DashboardViewModel
) {
    var selectedSubject by remember { mutableStateOf<SubjectProgress?>(null) }
    var showChapterEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("COURSE MATRICES", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("COMMERCE SYLLABUS DIRECTORY", color = TextSolidWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        }

        items(subjects) { subject ->
            val chaptersOfSubject = chapterStatuses.filter { it.subject.equals(subject.subjectName, ignoreCase = true) }
            val completedChapters = chaptersOfSubject.count { it.status == "MASTERED" }
            val progressFactor = if (chaptersOfSubject.isNotEmpty()) completedChapters.toFloat() / chaptersOfSubject.size else 0.1f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .clickable { selectedSubject = subject; showChapterEditDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(subject.subjectName, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$completedChapters / ${chaptersOfSubject.size} Mastered", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressFactor },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanPrimary,
                    trackColor = DarkBackground
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subject.notes.ifEmpty { "Target focused review. Log study sessions to automatically update." },
                    color = TextLightGray,
                    fontSize = 11.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }

    if (showChapterEditDialog && selectedSubject != null) {
        val activeS = selectedSubject!!
        val chapterList = chapterStatuses.filter { it.subject.equals(activeS.subjectName, ignoreCase = true) }
        var tempNotesInput by remember { mutableStateOf(activeS.notes) }

        Dialog(onDismissRequest = { showChapterEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${activeS.subjectName} CHAPTER AUDITOR", color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Subject Prep Directives:", color = TextLightGray, fontSize = 11.sp)
                    OutlinedTextField(
                        value = tempNotesInput,
                        onValueChange = { tempNotesInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Chapter Mastery Roadmap:", color = TextLightGray, fontSize = 12.sp)

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(chapterList) { cs ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    cs.chapterName,
                                    color = TextSolidWhite,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                var isMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    Button(
                                        onClick = { isMenuExpanded = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when (cs.status) {
                                                "MASTERED" -> EmeraldPositive
                                                "REVISION" -> AmberWarm
                                                "NOT_STARTED" -> Color(0xFF64748B)
                                                else -> CyanPrimary
                                            }
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(cs.status, fontSize = 9.sp, color = TextSolidWhite)
                                    }

                                    DropdownMenu(
                                        expanded = isMenuExpanded,
                                        onDismissRequest = { isMenuExpanded = false }
                                    ) {
                                        listOf("NOT_STARTED", "LEARNING", "PRACTICING", "REVISION", "MASTERED").forEach { status ->
                                            DropdownMenuItem(
                                                text = { Text(status) },
                                                onClick = {
                                                    viewModel.updateChapterStatusLocal(cs.subject, cs.chapterName, status)
                                                    isMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.updateSubjectMetadata(activeS.subjectName, tempNotesInput, "UPDATED")
                                showChapterEditDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Text("Save Metadata Changes", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. Skills & Trackers Boards
// ----------------------------------------------------
@Composable
fun TrackersScreen(
    trackers: List<ProgrammingTracker>,
    selfImprovements: List<SelfImprovementTracker>,
    viewModel: DashboardViewModel
) {
    var selectedTracker by remember { mutableStateOf<ProgrammingTracker?>(null) }
    var trackerHours by remember { mutableStateOf("") }
    var trackerConcepts by remember { mutableStateOf("") }
    var trackerNotes by remember { mutableStateOf("") }
    var trackerProjectsStr by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("TECHNICAL & PERSONAL LOGS", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("PROGRAMMING SKILLS DECK", color = TextSolidWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        }

        // Programming skills checklist
        items(trackers) { track ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .clickable {
                        selectedTracker = track
                        trackerHours = track.learningHours.toString()
                        trackerConcepts = track.conceptsLearned
                        trackerNotes = track.notes
                        trackerProjectsStr = track.projectsCompleted.toString()
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(track.trackName, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${track.learningHours} Hours Practiced", color = PurpleAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Latest Notes: ${track.notes}", color = TextLightGray, fontSize = 11.sp)
                Text("Certs & Projects completed: ${track.projectsCompleted}", color = EmeraldPositive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (track.conceptsLearned.isNotEmpty()) {
                    Text("Skills target mapped: ${track.conceptsLearned}", color = CyanPrimary, fontSize = 11.sp)
                }
            }
        }
    }

    if (selectedTracker != null) {
        val t = selectedTracker!!
        Dialog(onDismissRequest = { selectedTracker = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SECURE LEVEL UPDATER: ${t.trackName.uppercase()}", color = TextSolidWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trackerHours,
                        onValueChange = { trackerHours = it },
                        label = { Text("Cumulative hours logged", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trackerProjectsStr,
                        onValueChange = { trackerProjectsStr = it },
                        label = { Text("Total Projects built", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trackerConcepts,
                        onValueChange = { trackerConcepts = it },
                        label = { Text("Core concepts learned (Comma separated)", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trackerNotes,
                        onValueChange = { trackerNotes = it },
                        label = { Text("Development logs...", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val hrsDouble = trackerHours.toDoubleOrNull() ?: 0.0
                            val projInt = trackerProjectsStr.toIntOrNull() ?: 0
                            viewModel.updateProgrammingTrack(t.trackName, hrsDouble, projInt, trackerConcepts, trackerNotes)
                            selectedTracker = null
                            Toast.makeText(context, "Tech metrics compiled!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Verify & Save Update", color = DarkBackground)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. Habits Screening Panel
// ----------------------------------------------------
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    logs: List<HabitLog>,
    viewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val liveDateStr by viewModel.currentDateStr.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("DISCIPLINE COUNTERS", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("DAILY HABIT CYCLES", color = TextSolidWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        }

        items(habits) { habit ->
            val logsForHabit = logs.filter { it.habitName == habit.name }
            val completedToday = logsForHabit.any { it.dateStr == liveDateStr && it.status }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(habit.name, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Streak Tracker: ${habit.streak} Days 🔥", color = AmberWarm, fontSize = 12.sp)
                }

                Checkbox(
                    checked = completedToday,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleHabitToday(habit.name, isChecked)
                        Toast.makeText(
                            context,
                            if (isChecked) "Habit locked! XP granted." else "Habit unchecked.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("habit_checkbox_${habit.name.replace(" ", "_")}"),
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldPositive)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 6. RICH NOTES LIBRARY WITH FOLDERS & SEARCH
// ----------------------------------------------------
@Composable
fun NotesScreen(notes: List<NoteItem>, viewModel: DashboardViewModel) {
    var searchToken by remember { mutableStateOf("") }
    var selectedNoteForEdit by remember { mutableStateOf<NoteItem?>(null) }
    var activeCategoryFilter by remember { mutableStateOf("ALL") }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("STUDY") }
    var inputTags by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }

    val context = LocalContext.current

    val filteredNotes = notes.filter { note ->
        val matchesCategory = activeCategoryFilter == "ALL" || note.category.equals(activeCategoryFilter, ignoreCase = true)
        val matchesSearch = note.title.contains(searchToken, ignoreCase = true) ||
                note.content.contains(searchToken, ignoreCase = true) ||
                note.tags.contains(searchToken, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("DOCUMENTATION ENGINE", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("RICH STUDY REPOSITORY", color = TextSolidWhite, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }

            Button(
                onClick = { showAddNoteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Icon(Icons.Filled.NoteAdd, contentDescription = null, tint = DarkBackground)
                Text("Add Note", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar Outlined
        OutlinedTextField(
            value = searchToken,
            onValueChange = { searchToken = it },
            label = { Text("Search title, tags, content...", color = TextLightGray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("notes_search_input"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Folders tabs row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "STUDY", "PROGRAMMING", "PERSONAL").forEach { cat ->
                val isSelected = activeCategoryFilter == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) PurpleAccent else Color.Transparent)
                        .clickable { activeCategoryFilter = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(cat, color = if (isSelected) TextSolidWhite else TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredNotes) { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard()
                        .clickable { selectedNoteForEdit = note }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(note.title, color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(note.category, color = CyanPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        note.content,
                        color = TextLightGray,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (note.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            note.tags.split(",").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkBackground)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("#$tag", color = TextLightGray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        Dialog(onDismissRequest = { showAddNoteDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ADD SECURED NOTE DOCUMENT", color = TextSolidWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Note Title", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth().testTag("add_note_title"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputTags,
                        onValueChange = { inputTags = it },
                        label = { Text("Tags (Comma separated: math, review, revision)", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth().testTag("add_note_tags"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("File Categories Folders:", color = TextLightGray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("STUDY", "PROGRAMMING", "PERSONAL").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (inputCategory == type) CyanPrimary else DarkBackground)
                                    .clickable { inputCategory = type }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(type, color = if (inputCategory == type) DarkBackground else TextSolidWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputContent,
                        onValueChange = { inputContent = it },
                        label = { Text("Workspace markdown formatting content...", color = TextLightGray) },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_note_content"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextSolidWhite, unfocusedTextColor = TextLightGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (inputTitle.isEmpty() || inputContent.isEmpty()) return@Button
                            viewModel.addNote(inputTitle, inputContent, inputCategory, inputTags)
                            showAddNoteDialog = false
                            inputTitle = ""
                            inputContent = ""
                            Toast.makeText(context, "Note Document Filed!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text("Commit Document", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (selectedNoteForEdit != null) {
        val note = selectedNoteForEdit!!
        Dialog(onDismissRequest = { selectedNoteForEdit = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSlateCard,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(note.title, color = TextSolidWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        note.content,
                        color = TextLightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.deleteNote(note)
                            selectedNoteForEdit = null
                            Toast.makeText(context, "Note document discarded.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Text("Delete Document", color = TextSolidWhite)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. CUSTOM HIGH-PERFORMANCE INTERACTIVE ANALYTICS CANVAS CHARTS
// ----------------------------------------------------
@Composable
fun AnalyticsScreen(
    sessions: List<StudySession>,
    journal: List<JournalEntry>,
    habits: List<Habit>,
    logs: List<HabitLog>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("METRICS DASHBOARD", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("REAL ANALYTICS REPORTS (ZERO MOCKED STATES)", color = TextSolidWhite, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("These charts compile and render from actual study logs stored on device. If empty, start recording session blocks.", color = TextLightGray, fontSize = 11.sp)
            }
        }

        if (sessions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Timeline, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No local study records logged.", color = TextSolidWhite, fontWeight = FontWeight.SemiBold)
                    Text("Filing your first mock study logs creates gorgeous, dynamic, interactive canvas graphs here instantly.", color = TextLightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            // Study Hours allocation line chart drawn over Canvas!
            item {
                Column(modifier = Modifier.fillMaxWidth().glassCard()) {
                    Text("WEEKLY STUDY DAILY TIMEPATH (HOURS)", color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val groupScores = sessions.take(7).map { it.durationHours.toFloat() }.ifEmpty { listOf(0f) }
                    val maxVal = groupScores.maxOrNull()?.coerceAtLeast(1f) ?: 1f

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val strokeWidth = 5f
                        val widthBetweenPoints = size.width / (groupScores.size.coerceAtLeast(2) - 1).toFloat()
                        val points = groupScores.mapIndexed { idx, hrs ->
                            Offset(
                                x = idx * widthBetweenPoints,
                                y = size.height - (hrs / maxVal) * size.height
                            )
                        }

                        // Drawing grid lines helper
                        for (i in 0..3) {
                            val yLine = (size.height / 3f) * i
                            drawLine(
                                color = Color(0xFF334155),
                                start = Offset(0f, yLine),
                                end = Offset(size.width, yLine),
                                strokeWidth = 1f
                            )
                        }

                        // Drawing Line
                        val path = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                        }

                        drawPath(
                            path = path,
                            color = CyanPrimary,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Draw Point circular node highlights
                        points.forEach { pt ->
                            drawCircle(
                                color = PurpleAccent,
                                radius = 8f,
                                center = pt
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Earliest Log", color = TextLightGray, fontSize = 10.sp)
                        Text("Active Track Summary", color = CyanPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Most Recent Log", color = TextLightGray, fontSize = 10.sp)
                    }
                }
            }

            // Subject completions Bar charts canvas
            item {
                Column(modifier = Modifier.fillMaxWidth().glassCard()) {
                    Text("MOCK COMPILER: ALL STUDY LOGS RECOGNIZED BY FIELD", color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val subSums = sessions.groupBy { it.subject }.mapValues { entry -> entry.value.sumOf { it.durationHours }.toFloat() }
                    val subjectsList = subSums.keys.toList()
                    val hourlyHeights = subSums.values.toList()
                    val maxHrVal = hourlyHeights.maxOrNull()?.coerceAtLeast(1f) ?: 1f

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val barWidth = size.width / (subjectsList.size * 2 + 1).toFloat()
                        subjectsList.forEachIndexed { idx, sub ->
                            val hrsTotal = subSums[sub] ?: 0f
                            val barHeightActual = (hrsTotal / maxHrVal) * size.height
                            val startX = (idx * 2 + 1) * barWidth
                            val startY = size.height - barHeightActual

                            drawRoundRect(
                                color = PurpleAccent,
                                topLeft = Offset(startX, startY),
                                size = Size(barWidth, barHeightActual),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        subjectsList.forEach { sub ->
                            Text(sub.take(4), color = TextLightGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Habit completions reporting totals
        item {
            Column(modifier = Modifier.fillMaxWidth().glassCard()) {
                Text("HABIT AUDIT CYCLES IN PRIVATE WORKSPACE", color = TextSolidWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val totalChecked = logs.count { it.status }
                val overallGoal = habits.size * 10
                val progressRatio = if (overallGoal > 0) totalChecked.toFloat() / overallGoal else 0.5f

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Checked Tasks:", color = TextLightGray, fontSize = 12.sp)
                    Text("$totalChecked Times locked completely", color = EmeraldPositive, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressRatio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldPositive,
                    trackColor = DarkBackground
                )
            }
        }
    }
}
