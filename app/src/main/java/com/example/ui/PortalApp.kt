package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.data.GradeRecord
import com.example.data.AttendanceRecord
import com.example.data.ScheduleItem
import com.example.data.ChatMessage
import com.example.viewmodel.DashboardTab
import com.example.viewmodel.PortalViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalApp(viewModel: PortalViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = {
            if (errorMessage != null || successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (errorMessage != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (errorMessage != null) Icons.Default.Warning else Icons.Default.Check,
                                contentDescription = "Alert logo",
                                tint = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage ?: successMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (errorMessage != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearMessages() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close alert"
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.LOGIN -> LoginScreen(viewModel)
                    Screen.REGISTER -> RegisterScreen(viewModel)
                    Screen.FORGET_HELP -> ForgetHelpScreen(viewModel)
                    Screen.TWO_FACTOR -> TwoFactorScreen(viewModel)
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: PortalViewModel) {
    var keyInput by remember { mutableStateOf("") }
    var showKeyMask by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative School logo
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "School Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Text(
            text = "CampusPortal",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Secure Student Academic Dashboard",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Access Login Key",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Sign in directly with the unique key generated when registering.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Student Login Key") },
                    placeholder = { Text("CAMPUS-xxxx-xxxx") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Key Icon")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showKeyMask = !showKeyMask }) {
                            Icon(
                                imageVector = if (showKeyMask) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle access key visibility"
                            )
                        }
                    },
                    visualTransformation = if (showKeyMask) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_key_input")
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { viewModel.loginWithKey(keyInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = "Sign in")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Access Dashboard",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { viewModel.setScreen(Screen.REGISTER) },
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Sign up")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Register Student", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = { viewModel.setScreen(Screen.FORGET_HELP) },
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Help, contentDescription = "Forget Key")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Forgot Key?", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: PortalViewModel) {
    var studentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var enable2fa by remember { mutableStateOf(false) }
    
    val gradeClasses = listOf("Grade 10-A", "Grade 10-B", "Grade 11-A", "Grade 11-B", "Grade 12-A", "Grade 12-B")
    var selectedGrade by remember { mutableStateOf(gradeClasses[0]) }
    var classExpanded by remember { mutableStateOf(false) }

    val securityQuestions = listOf(
        "What was the name of your first mascot?",
        "In what city did your parents meet?",
        "What was the subject of your favorite high school course?",
        "What was your first elementary school teacher's name?",
        "What is the atomic number of Helium?"
    )
    var selectedQuestion by remember { mutableStateOf(securityQuestions[0]) }
    var questionExpanded by remember { mutableStateOf(false) }
    var securityAnswer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { viewModel.setScreen(Screen.LOGIN) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back to Login")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Student Registration Form",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sign up to auto-generate your direct secure access key",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                // Input fields inside card
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Academic & Personal Details",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("Student Roll ID number") },
                            placeholder = { Text("e.g. STU-9923") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = "Roll Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_id_input")
                                .padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Student Full Name") },
                            placeholder = { Text("e.g. Eleanor Vance") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_name_input")
                                .padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Student Email Contact") },
                            placeholder = { Text("e.g. eleanor@school.edu") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(imageVector = Icons.Default.Mail, contentDescription = "Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_email_input")
                                .padding(bottom = 16.dp)
                        )

                        // Dropdown for class
                        ExposedDropdownMenuBox(
                            expanded = classExpanded,
                            onExpandedChange = { classExpanded = !classExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedGrade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Grade / Year Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Class, contentDescription = "Class Logo") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .padding(bottom = 16.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = classExpanded,
                                onDismissRequest = { classExpanded = false }
                            ) {
                                gradeClasses.forEach { grade ->
                                    DropdownMenuItem(
                                        text = { Text(grade) },
                                        onClick = {
                                            selectedGrade = grade
                                            classExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Security & 2FA Setup Card
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Security Key Recovery Question",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Answer this verification question to instantly recover your Dashboard login key if you ever forget it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Dropped question picker
                        ExposedDropdownMenuBox(
                            expanded = questionExpanded,
                            onExpandedChange = { questionExpanded = !questionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedQuestion,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Choose Secret Recovery Question") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionExpanded) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Recovery Question") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .padding(bottom = 12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = questionExpanded,
                                onDismissRequest = { questionExpanded = false }
                            ) {
                                securityQuestions.forEach { question ->
                                    DropdownMenuItem(
                                        text = { Text(question) },
                                        onClick = {
                                            selectedQuestion = question
                                            questionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = securityAnswer,
                            onValueChange = { securityAnswer = it },
                            label = { Text("Your Confidential Security Answer") },
                            placeholder = { Text("Type answer clearly") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = "Security Answer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        // 2FA Security Switch
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Two-Factor Authentication (2FA)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Require simulated authenticator code verification at logins for extra system defense.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = enable2fa,
                                onCheckedChange = { enable2fa = it },
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.registerStudent(
                            studentId = studentId,
                            name = name,
                            email = email,
                            gradeClass = selectedGrade,
                            securityQuestion = selectedQuestion,
                            securityAnswer = securityAnswer,
                            enable2fa = enable2fa
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .height(52.dp)
                        .testTag("register_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Register now")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Student", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun ForgetHelpScreen(viewModel: PortalViewModel) {
    var studentIdInput by remember { mutableStateOf("") }
    val recoveryId by viewModel.recoveryStudentId.collectAsState()
    val securityQuestion by viewModel.recoveryQuestionState.collectAsState()
    val recoveredKey by viewModel.recoveredKey.collectAsState()
    var securityAnswerInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { viewModel.setScreen(Screen.LOGIN) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back to Login")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Key Icon",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Key Recovery System",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Recover your Login Key by answering your secret question",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    if (securityQuestion == null) {
                        // Step 1: Input student Roll ID
                        Text(
                            text = "Step 1: Verify Student ID",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = studentIdInput,
                            onValueChange = { studentIdInput = it },
                            label = { Text("Your Registered Roll ID Code") },
                            placeholder = { Text("e.g. STU-10023") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = "Student Roll identifier") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = { viewModel.requestRecoveryQuestion(studentIdInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Fetch Security Question", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Step 2: Answer question
                        Text(
                            text = "Step 2: Answer Selected Question",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "ID: $recoveryId",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = "Student Identified") },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = securityQuestion ?: "",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        OutlinedTextField(
                            value = securityAnswerInput,
                            onValueChange = { securityAnswerInput = it },
                            label = { Text("Your Security Answer") },
                            placeholder = { Text("Enter answer exactly") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Answer Icon") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        if (recoveredKey == null) {
                            Button(
                                onClick = { viewModel.verifyRecoveryAnswer(securityAnswerInput) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Decrypt Login Key", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Success! Reveal login key
                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "Decrypted Access Key Revealed:",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = recoveredKey ?: "",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("CampusPortal Access Key", recoveredKey)
                                            clipboard.setPrimaryClip(clip)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy decrypted digital key"
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.setScreen(Screen.LOGIN)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back to Sign In", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TwoFactorScreen(viewModel: PortalViewModel) {
    var otpInput by remember { mutableStateOf("") }
    val simulated2faCode by viewModel.simulated2faCode.collectAsState()
    val pendingStudent by viewModel.pendingStudent.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { viewModel.setScreen(Screen.LOGIN) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back to Login")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.PhonelinkSetup,
                        contentDescription = "2FA validation Icon",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Two-Factor Verification",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "This Student account requires a 2FA code to access campus files.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            // Dynamic Simulator Device Notification Badge for extremely simple testing
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Mock Authenticator Device",
                        tint = Color(0xFF856404)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Authenticator Notification (Simulated)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF856404)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Your code is: $simulated2faCode",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color(0xFF664D03)
                            )
                            Text(
                                text = "Copy Code",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Simulated OTP", simulated2faCode)
                                        clipboard.setPrimaryClip(clip)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFECB3))
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Verify Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Enter the 6-digit code visually appearing above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it },
                        label = { Text("6-Digit PIN Code") },
                        placeholder = { Text("******") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(imageVector = Icons.Default.Dialpad, contentDescription = "Keypad") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input")
                            .padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { viewModel.verify2faAndLogin(otpInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Authenticate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: PortalViewModel) {
    val student by viewModel.currentStudent.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // App Header Bar
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Academic Portal Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CampusPortal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out student profile")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Student badge details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = student?.name?.firstOrNull()?.uppercase()?.toString() ?: "S",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student?.name ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ID: ${student?.studentId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = student?.gradeClass ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Tab Screen Content
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (currentTab) {
                DashboardTab.GRADES -> GradesTab(viewModel)
                DashboardTab.ATTENDANCE -> AttendanceTab(viewModel)
                DashboardTab.TIMETABLE -> TimetableTab(viewModel)
                DashboardTab.CHATBOT -> ChatbotTab(viewModel)
                DashboardTab.PROFILE -> ProfileTab(viewModel)
            }
        }

        // Bottom Navigation Bar
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = currentTab == DashboardTab.GRADES,
                onClick = { viewModel.setTab(DashboardTab.GRADES) },
                icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Grades") },
                label = { Text("Grades") }
            )
            NavigationBarItem(
                selected = currentTab == DashboardTab.ATTENDANCE,
                onClick = { viewModel.setTab(DashboardTab.ATTENDANCE) },
                icon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Attendance") },
                label = { Text("Attendance") }
            )
            NavigationBarItem(
                selected = currentTab == DashboardTab.TIMETABLE,
                onClick = { viewModel.setTab(DashboardTab.TIMETABLE) },
                icon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "Timetable") },
                label = { Text("Schedule") }
            )
            NavigationBarItem(
                selected = currentTab == DashboardTab.CHATBOT,
                onClick = { viewModel.setTab(DashboardTab.CHATBOT) },
                icon = { Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = "AI Assistant") },
                label = { Text("CampusBot") }
            )
            NavigationBarItem(
                selected = currentTab == DashboardTab.PROFILE,
                onClick = { viewModel.setTab(DashboardTab.PROFILE) },
                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Profile Security") },
                label = { Text("Security") }
            )
        }
    }
}

// Sub-Tab 1: Grades & Academic GPA
@Composable
fun GradesTab(viewModel: PortalViewModel) {
    val grades by viewModel.studentGrades.collectAsState()

    // Calculate GPA based on grade strings
    val gpaVal = remember(grades) {
        if (grades.isEmpty()) 0.0 else {
            val totalPoints = grades.sumOf { record ->
                when (record.grade.uppercase()) {
                    "A" -> 4.0
                    "A-" -> 3.7
                    "B+" -> 3.3
                    "B" -> 3.0
                    "B-" -> 2.7
                    "C+" -> 2.3
                    "C" -> 2.0
                    else -> 1.0
                }
            }
            totalPoints / grades.size
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Grades Summary",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Track your current subjects performance and equivalent GPA index.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Cumulative GPA Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cumulative GPA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f", gpaVal),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Academic Scale: 4.0 Maximum",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = if (gpaVal >= 3.5) "A" else if (gpaVal >= 3.0) "B" else "C",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        if (grades.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No academic grade grades recorded currently for your class.")
                }
            }
        } else {
            items(grades) { record ->
                GradeRecordCard(record)
            }
        }
    }
}

@Composable
fun GradeRecordCard(grade: GradeRecord) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grade.courseCode,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = grade.courseName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = grade.semester,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (grade.grade.firstOrNull()) {
                            'A' -> Color(0xFFE8F5E9)
                            'B' -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFF3E0)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = grade.grade,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = when (grade.grade.firstOrNull()) {
                            'A' -> Color(0xFF2E7D32)
                            'B' -> Color(0xFF1565C0)
                            else -> Color(0xFFEF6C00)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Marks: ${grade.marks}/${grade.marksMax}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Sub-Tab 2: Attendance Tracking & custom drawing dials
@Composable
fun AttendanceTab(viewModel: PortalViewModel) {
    val attendanceList by viewModel.studentAttendance.collectAsState()

    val counts = remember(attendanceList) {
        val presentCount = attendanceList.count { it.status.equals("Present", ignoreCase = true) }
        val absentCount = attendanceList.count { it.status.equals("Absent", ignoreCase = true) }
        val excusedCount = attendanceList.count { it.status.equals("Excused", ignoreCase = true) }
        Triple(presentCount, absentCount, excusedCount)
    }

    val attendanceRate = remember(attendanceList, counts) {
        if (attendanceList.isEmpty()) 1.0f else {
            // Count Present and Excused as attendance active
            (counts.first + counts.third).toFloat() / attendanceList.size
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Attendance Tracking",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "View cumulative attendance records drawn live on custom canvas graphs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Visual Dial canvas card
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Custom drawn attendance gauge dial
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val accentColor = MaterialTheme.colorScheme.primary
                        val inactiveColor = MaterialTheme.colorScheme.surface
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // Draw tracking circle
                            drawArc(
                                color = inactiveColor,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Draw active coverage
                            drawArc(
                                color = accentColor,
                                startAngle = 135f,
                                sweepAngle = 270f * attendanceRate,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.0f%%", attendanceRate * 100),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Stat Breakdown Lists
                    Column(verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Present: ${counts.first} Days", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFEF6C00)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Excused: ${counts.third} Days", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFC62828)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Absent: ${counts.second} Days", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (attendanceList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No daily attendance records reported in the database.")
                }
            }
        } else {
            items(attendanceList) { record ->
                AttendanceCard(record)
            }
        }
    }
}

@Composable
fun AttendanceCard(record: AttendanceRecord) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (record.status.lowercase()) {
                        "present" -> Icons.Default.CheckCircle
                        "absent" -> Icons.Default.Cancel
                        else -> Icons.Default.OfflinePin
                    },
                    contentDescription = record.status,
                    tint = when (record.status.lowercase()) {
                        "present" -> Color(0xFF2E7D32)
                        "absent" -> Color(0xFFC62828)
                        else -> Color(0xFFEF6C00)
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (record.remark.isNotEmpty()) {
                        Text(
                            text = record.remark,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = record.status,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = when (record.status.lowercase()) {
                    "present" -> Color(0xFF2E7D32)
                    "absent" -> Color(0xFFC62828)
                    else -> Color(0xFFEF6C00)
                }
            )
        }
    }
}

// Sub-Tab 3: Academic schedules
@Composable
fun TimetableTab(viewModel: PortalViewModel) {
    val schedules by viewModel.studentSchedules.collectAsState()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var selectedDay by remember { mutableStateOf(days[0]) }

    val filteredList = remember(schedules, selectedDay) {
        schedules.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Academic Timetable",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Keep track of daily classrooms, subjects, scheduled lectures and times.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Day selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { day ->
                val isSelected = day == selectedDay
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedDay = day },
                    label = {
                        Text(
                            text = day.take(3),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = "No Class",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No lectures scheduled for $selectedDay.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { item ->
                    ScheduleItemCard(item)
                }
            }
        }
    }
}

@Composable
fun ScheduleItemCard(item: ScheduleItem) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(110.dp)
                    .height(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = item.timeSlot,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.classroom,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = "Instructor",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Instructor: ${item.teacher}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Sub-Tab 4: AI Study Companion Chatbot using real Gemini Rest flow
@Composable
fun ChatbotTab(viewModel: PortalViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll when messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Sparkle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CampusBot Help Desk",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Gemini AI Study Companion active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.clearChatHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(messages) { message ->
                BubbleCard(message)
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "CampusBot is thinking...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom input
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask CampusBot for study support...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    if (inputQuery.isNotEmpty()) {
                        IconButton(onClick = { inputQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear field")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (inputQuery.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(inputQuery)
                        inputQuery = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(50.dp)
                    .testTag("chat_send_button"),
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message to AI Bot")
            }
        }
    }
}

@Composable
fun BubbleCard(message: ChatMessage) {
    val isUser = message.sender == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp
            ),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
        }
    }
}

// Sub-Tab 5: Profile dashboard settings, 2FA toggle, digital Access Key updates
@Composable
fun ProfileTab(viewModel: PortalViewModel) {
    val student by viewModel.currentStudent.collectAsState()
    val context = LocalContext.current
    var isKeyHidden by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Account Security & Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Manage your access key credentials and verify double-factor security toggles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic User Profile Summary Card
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verified Student Account",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.Person, "Name", student?.name ?: "")
                    DetailRow(Icons.Default.Badge, "Student Roll Key", student?.studentId ?: "")
                    DetailRow(Icons.Default.Mail, "Email Office", student?.email ?: "")
                    DetailRow(Icons.Default.Class, "Grade Class", student?.gradeClass ?: "")
                }
            }
        }

        item {
            // Key Reveal & Copy Credentials
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Access Login Key",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Keep this digital token safe for logins.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { isKeyHidden = !isKeyHidden }) {
                            Icon(
                                imageVector = if (isKeyHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle hidden access key"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isKeyHidden) "• • • • • • • • • • • • • • •" else (student?.loginKey ?: ""),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("CampusPortal-Key", student?.loginKey ?: "")
                                    clipboard.setPrimaryClip(clip)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy access key to clipboard"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.regenerateLoginKey() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerate Digital Access Key", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            // Two factor authorization direct toggle
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Two-Factor Verification",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Toggle 2FA layers on/off for direct credential checks at login.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = student?.is2faEnabled == true,
                            onCheckedChange = { viewModel.toggle2fa(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (student?.is2faEnabled == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (student?.is2faEnabled == true) Icons.Default.Security else Icons.Default.Shield,
                                contentDescription = "Status lock",
                                tint = if (student?.is2faEnabled == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (student?.is2faEnabled == true) "2FA Security Lock is Active" else "2FA Security is Inactive",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (student?.is2faEnabled == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
