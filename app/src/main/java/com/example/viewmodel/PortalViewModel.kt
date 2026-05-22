package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

enum class Screen {
    LOGIN,
    REGISTER,
    FORGET_HELP,
    TWO_FACTOR,
    DASHBOARD
}

enum class DashboardTab {
    GRADES,
    ATTENDANCE,
    TIMETABLE,
    CHATBOT,
    PROFILE
}

class PortalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "campus_portal_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = PortalRepository(db.studentDao(), db.academicDao(), db.chatDao())

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(Screen.LOGIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(DashboardTab.GRADES)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    // Logged in Student Info
    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()

    // 2FA pending validation state
    private val _pendingStudent = MutableStateFlow<Student?>(null)
    val pendingStudent: StateFlow<Student?> = _pendingStudent.asStateFlow()

    private val _simulated2faCode = MutableStateFlow("")
    val simulated2faCode: StateFlow<String> = _simulated2faCode.asStateFlow()

    // Error / Custom notifications
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Dashboard Academic Flow Data
    val studentGrades = _currentStudent.flatMapLatest { student ->
        student?.let { repository.getGradesForStudent(it.studentId) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentAttendance = _currentStudent.flatMapLatest { student ->
        student?.let { repository.getAttendanceForStudent(it.studentId) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentSchedules = _currentStudent.flatMapLatest { student ->
        student?.let { repository.getSchedulesForStudent(it.studentId) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat Messages State
    val chatMessages = _currentStudent.flatMapLatest { student ->
        student?.let { repository.getMessagesForStudent(it.studentId) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Recovery State
    private val _recoveryStudentId = MutableStateFlow("")
    val recoveryStudentId: StateFlow<String> = _recoveryStudentId.asStateFlow()

    private val _recoveryQuestionState = MutableStateFlow<String?>(null)
    val recoveryQuestionState: StateFlow<String?> = _recoveryQuestionState.asStateFlow()

    private val _recoveredKey = MutableStateFlow<String?>(null)
    val recoveredKey: StateFlow<String?> = _recoveredKey.asStateFlow()

    fun setScreen(screen: Screen) {
        _errorMessage.value = null
        _successMessage.value = null
        _currentScreen.value = screen
    }

    fun setTab(tab: DashboardTab) {
        _currentTab.value = tab
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // Direct Login using Key
    fun loginWithKey(key: String) {
        if (key.trim().isEmpty()) {
            _errorMessage.value = "Please enter your school Login Key."
            return
        }

        viewModelScope.launch {
            val student = repository.getStudentByLoginKey(key.trim().uppercase())
            if (student != null) {
                if (student.is2faEnabled) {
                    // Trigger Simulated 2FA Code
                    _pendingStudent.value = student
                    val code = String.format("%06d", Random.nextInt(100000, 999999))
                    _simulated2faCode.value = code
                    _currentScreen.value = Screen.TWO_FACTOR
                } else {
                    _currentStudent.value = student
                    _currentScreen.value = Screen.DASHBOARD
                    _successMessage.value = "Logged in successfully as ${student.name}!"
                }
            } else {
                _errorMessage.value = "Student Login Key not found. Please verify your key or register."
            }
        }
    }

    // Verify 2FA
    fun verify2faAndLogin(inputCode: String) {
        if (inputCode == _simulated2faCode.value) {
            _currentStudent.value = _pendingStudent.value
            _pendingStudent.value = null
            _simulated2faCode.value = ""
            _currentScreen.value = Screen.DASHBOARD
            _successMessage.value = "Login verification successful!"
        } else {
            _errorMessage.value = "Invalid verification code! Please try again."
        }
    }

    // Key Recovery Workflow Step 1: Find student question
    fun requestRecoveryQuestion(studentId: String) {
        if (studentId.trim().isEmpty()) {
            _errorMessage.value = "Please enter your Student ID or Roll number."
            return
        }

        viewModelScope.launch {
            val student = repository.getStudentById(studentId.trim().uppercase())
            if (student != null) {
                _recoveryStudentId.value = student.studentId
                _recoveryQuestionState.value = student.securityQuestion
                _recoveredKey.value = null
            } else {
                _errorMessage.value = "Student ID / Roll number not found."
            }
        }
    }

    // Key Recovery Workflow Step 2: Form check security answer
    fun verifyRecoveryAnswer(answer: String) {
        val studentId = _recoveryStudentId.value
        if (studentId.isEmpty()) return

        if (answer.trim().isEmpty()) {
            _errorMessage.value = "Please enter your security answer."
            return
        }

        viewModelScope.launch {
            val student = repository.getStudentById(studentId)
            if (student != null) {
                if (student.securityAnswer.trim().equals(answer.trim(), ignoreCase = true)) {
                    _recoveredKey.value = student.loginKey
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Incorrect security answer! Verification failed."
                }
            }
        }
    }

    // Sign Up Student Registration Flow
    fun registerStudent(
        studentId: String,
        name: String,
        email: String,
        gradeClass: String,
        securityQuestion: String,
        securityAnswer: String,
        enable2fa: Boolean
    ) {
        if (studentId.trim().isEmpty() || name.trim().isEmpty() || email.trim().isEmpty() ||
            securityQuestion.trim().isEmpty() || securityAnswer.trim().isEmpty()
        ) {
            _errorMessage.value = "Please fill in all the required form fields."
            return
        }

        val checkedId = studentId.trim().uppercase()

        viewModelScope.launch {
            // Check if student with Roll ID already exists
            val existingStudent = repository.getStudentById(checkedId)
            if (existingStudent != null) {
                _errorMessage.value = "Student with Roll Number $checkedId already exists."
                return@launch
            }

            // Generate unique secure Login Key
            val randomDigits = String.format("%04d", Random.nextInt(1000, 9999))
            val generatedKey = "CAMPUS-${checkedId}-${randomDigits}"

            val newStudent = Student(
                studentId = checkedId,
                name = name.trim(),
                email = email.trim(),
                gradeClass = gradeClass,
                loginKey = generatedKey,
                securityQuestion = securityQuestion,
                securityAnswer = securityAnswer.trim(),
                is2faEnabled = enable2fa
            )

            // Save user profile
            repository.insertStudent(newStudent)

            // Populate rich custom academic mock data so they can instantly view beautiful schedules, attendance and grades
            populateMockAcademicData(checkedId, gradeClass)

            _successMessage.value = "Registration Successful! Here is your school Login Key: $generatedKey"
            _currentStudent.value = newStudent
            _currentScreen.value = Screen.DASHBOARD
        }
    }

    // Toggle 2FA in profile dashboard
    fun toggle2fa(enabled: Boolean) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            repository.update2fa(student.studentId, enabled)
            _currentStudent.value = student.copy(is2faEnabled = enabled)
            _successMessage.value = if (enabled) "Two-Factor Authentication is now ENABLED!" else "Two-Factor Authentication is now DISABLED."
        }
    }

    // Regenerate direct digital key from Profile
    fun regenerateLoginKey() {
        val student = _currentStudent.value ?: return
        val randomDigits = String.format("%04d", Random.nextInt(1000, 9999))
        val regeneratedKey = "CAMPUS-${student.studentId}-${randomDigits}"

        viewModelScope.launch {
            val updatedStudent = student.copy(loginKey = regeneratedKey)
            repository.insertStudent(updatedStudent)
            _currentStudent.value = updatedStudent
            _successMessage.value = "Your Portal Access Key has been successfully updated!"
        }
    }

    // Log out of student profile
    fun logout() {
        _currentStudent.value = null
        _pendingStudent.value = null
        _simulated2faCode.value = ""
        _currentScreen.value = Screen.LOGIN
    }

    // Study help chat session using actual Gemini client Beta Retrofit Client
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        val student = _currentStudent.value ?: return

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessage(studentId = student.studentId, sender = "user", message = text)
            repository.insertMessage(userMsg)

            _isChatLoading.value = true

            try {
                // Read from BuildConfig safely as mandated
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                
                // Keep context short (last 6 messages) to prevent API timeouts
                val history = chatMessages.value.takeLast(6)
                val partsContents = mutableListOf<Content>()
                
                history.forEach {
                    partsContents.add(Content(parts = listOf(Part(text = it.message))))
                }
                
                // Add current message if not yet present in list
                partsContents.add(Content(parts = listOf(Part(text = text))))

                val sysInstruction = Content(
                    parts = listOf(
                        Part(
                            text = "You are a friendly, compassionate, and highly intelligent school study assistant chatbot " +
                                    "named CampusBot inside the CampusPortal app. Assisting student ${student.name} who is in ${student.gradeClass}. " +
                                    "Provide clear explanations, breakdown formulas, study guides, or scheduling tips to help their studies. " +
                                    "Give helpful, step-by-step academic answers of appropriate length, avoiding unnecessary filler."
                        )
                    )
                )

                val request = GenerateContentRequest(
                    contents = partsContents,
                    systemInstruction = sysInstruction
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I'm sorry, I couldn't formulate an answer at this moment. Could you rephrase your academic query?"

                val aiMsg = ChatMessage(studentId = student.studentId, sender = "ai", message = responseText)
                repository.insertMessage(aiMsg)

            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    studentId = student.studentId,
                    sender = "ai",
                    message = "CampusBot: I was unable to connect with the study brain engine. Please check your internet or configuration. (Error: ${e.localizedMessage ?: "Network Timeout"})"
                )
                repository.insertMessage(errorMsg)
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            repository.clearChat(student.studentId)
            // Insert helpful greeting message
            repository.insertMessage(
                ChatMessage(
                    studentId = student.studentId,
                    sender = "ai",
                    message = "Hello ${student.name}! I am CampusBot, your personal Study AI assistant. Ask me anything about notes, tests, or schedules!"
                )
            )
        }
    }

    private suspend fun populateMockAcademicData(studentId: String, gradeClass: String) {
        // Grades Setup
        val mockGrades = listOf(
            GradeRecord(studentId = studentId, courseCode = "MATH-301", courseName = "Advanced Mathematics", grade = "A", marks = 93, semester = "Spring semester 2026"),
            GradeRecord(studentId = studentId, courseCode = "PHYS-250", courseName = "Theoretical Physics", grade = "B+", marks = 88, semester = "Spring semester 2026"),
            GradeRecord(studentId = studentId, courseCode = "CHEM-202", courseName = "Organic Chemistry Lab", grade = "A-", marks = 90, semester = "Spring semester 2026"),
            GradeRecord(studentId = studentId, courseCode = "COMP-101", courseName = "Computer Programming 1", grade = "A", marks = 97, semester = "Spring semester 2026"),
            GradeRecord(studentId = studentId, courseCode = "LIT-112", courseName = "English World Literature", grade = "B-", marks = 81, semester = "Spring semester 2026"),
            GradeRecord(studentId = studentId, courseCode = "HIST-180", courseName = "Modern Civilizations", grade = "A", marks = 92, semester = "Spring semester 2026")
        )
        repository.insertGrades(mockGrades)

        // Attendance Setup (Generates 12 realistic past logs)
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val mockAttendance = mutableListOf<AttendanceRecord>()

        val statuses = listOf("Present", "Present", "Present", "Present", "Absent", "Present", "Present", "Excused", "Present", "Present")
        val remarks = mapOf(
            "Absent" to "Excused Sick Leave (Fever)",
            "Excused" to "Represented School in Athletics Meet",
            "Present" to "Attended regular sessions"
        )

        for (i in 0 until 14) {
            val dateStr = sdf.format(cal.time)
            // Skip Sundays/Saturdays
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
                val status = statuses[Random.nextInt(statuses.size)]
                mockAttendance.add(
                    AttendanceRecord(
                        studentId = studentId,
                        date = dateStr,
                        status = status,
                        remark = remarks[status] ?: ""
                    )
                )
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        repository.insertAttendance(mockAttendance)

        // Schedule Items Setup
        val mockSchedules = listOf(
            // Monday
            ScheduleItem(studentId = studentId, dayOfWeek = "Monday", timeSlot = "09:00 AM - 10:30 AM", subject = "Advanced Mathematics", classroom = "Room Quad-A", teacher = "Dr. Carter"),
            ScheduleItem(studentId = studentId, dayOfWeek = "Monday", timeSlot = "11:00 AM - 12:30 PM", subject = "Organic Chemistry Lab", classroom = "Main Chem Lab", teacher = "Dr. Vance"),
            // Tuesday
            ScheduleItem(studentId = studentId, dayOfWeek = "Tuesday", timeSlot = "09:00 AM - 10:30 AM", subject = "Theoretical Physics", classroom = "Hall C-2", teacher = "Prof. Feynman"),
            ScheduleItem(studentId = studentId, dayOfWeek = "Tuesday", timeSlot = "01:00 PM - 02:30 PM", subject = "English World Literature", classroom = "Block Delta-5", teacher = "Mrs. Munro"),
            // Wednesday
            ScheduleItem(studentId = studentId, dayOfWeek = "Wednesday", timeSlot = "09:00 AM - 10:30 AM", subject = "Advanced Mathematics", classroom = "Room Quad-A", teacher = "Dr. Carter"),
            ScheduleItem(studentId = studentId, dayOfWeek = "Wednesday", timeSlot = "11:00 AM - 12:30 PM", subject = "Computer Programming 1", classroom = "Comp Lab 3", teacher = "Dr. Turing"),
            // Thursday
            ScheduleItem(studentId = studentId, dayOfWeek = "Thursday", timeSlot = "09:00 AM - 10:30 AM", subject = "Theoretical Physics", classroom = "Hall C-2", teacher = "Prof. Feynman"),
            ScheduleItem(studentId = studentId, dayOfWeek = "Thursday", timeSlot = "01:00 PM - 02:30 PM", subject = "Modern Civilizations", classroom = "Room Quad-D", teacher = "Mr. Winston"),
            // Friday
            ScheduleItem(studentId = studentId, dayOfWeek = "Friday", timeSlot = "11:00 AM - 12:30 PM", subject = "Computer Programming 1", classroom = "Comp Lab 3", teacher = "Dr. Turing"),
            ScheduleItem(studentId = studentId, dayOfWeek = "Friday", timeSlot = "02:00 PM - 03:30 PM", subject = "Weekly Seminar", classroom = "Seminar Hall", teacher = "Principal Albus")
        )
        repository.insertSchedules(mockSchedules)

        // chatbot introductory greeting response
        repository.insertMessage(
            ChatMessage(
                studentId = studentId,
                sender = "ai",
                message = "Hello! I am CampusBot, your personal study companion powered by Google Gemini. Need help summarizing formulas, explaining atomic numbers, or writing clean loops? Just drop a question!"
            )
        )
    }
}
