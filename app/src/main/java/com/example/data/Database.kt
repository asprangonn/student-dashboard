package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val studentId: String, // e.g. STU-10023
    val name: String,
    val email: String,
    val gradeClass: String,
    val loginKey: String, // Generated unique key: "KEY-XXXX"
    val securityQuestion: String,
    val securityAnswer: String,
    val is2faEnabled: Boolean = false
)

@Entity(tableName = "grades")
data class GradeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val courseCode: String,
    val courseName: String,
    val grade: String,
    val marks: Int,
    val marksMax: Int = 100,
    val semester: String
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val date: String, // e.g. "2026-05-15"
    val status: String, // "Present", "Absent", "Excused"
    val remark: String = ""
)

@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val dayOfWeek: String, // e.g. "Monday"
    val timeSlot: String, // e.g. "09:00 AM - 10:30 AM"
    val subject: String,
    val classroom: String,
    val teacher: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE loginKey = :key LIMIT 1")
    suspend fun getStudentByLoginKey(key: String): Student?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): Student?

    @Query("SELECT * FROM students WHERE email = :email LIMIT 1")
    suspend fun getStudentByEmail(email: String): Student?

    @Query("SELECT * FROM students WHERE studentId = :studentId AND securityAnswer = :answer LIMIT 1")
    suspend fun queryKeyRecovery(studentId: String, answer: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Query("UPDATE students SET is2faEnabled = :enabled WHERE studentId = :studentId")
    suspend fun update2fa(studentId: String, enabled: Boolean)
}

@Dao
interface AcademicDao {
    @Query("SELECT * FROM grades WHERE studentId = :studentId")
    fun getGradesForStudent(studentId: String): Flow<List<GradeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeRecord>)

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(records: List<AttendanceRecord>)

    @Query("SELECT * FROM schedules WHERE studentId = :studentId")
    fun getSchedulesForStudent(studentId: String): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(items: List<ScheduleItem>)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE studentId = :studentId ORDER BY timestamp ASC")
    fun getMessagesForStudent(studentId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE studentId = :studentId")
    suspend fun clearChat(studentId: String)
}

@Database(
    entities = [
        Student::class,
        GradeRecord::class,
        AttendanceRecord::class,
        ScheduleItem::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun academicDao(): AcademicDao
    abstract fun chatDao(): ChatDao
}

class PortalRepository(
    private val studentDao: StudentDao,
    private val academicDao: AcademicDao,
    private val chatDao: ChatDao
) {
    suspend fun getStudentByLoginKey(key: String): Student? = studentDao.getStudentByLoginKey(key)
    suspend fun getStudentById(id: String): Student? = studentDao.getStudentById(id)
    suspend fun getStudentByEmail(email: String): Student? = studentDao.getStudentByEmail(email)
    suspend fun queryKeyRecovery(studentId: String, answer: String): Student? = studentDao.queryKeyRecovery(studentId, answer)
    suspend fun insertStudent(student: Student) = studentDao.insertStudent(student)
    suspend fun update2fa(studentId: String, enabled: Boolean) = studentDao.update2fa(studentId, enabled)

    fun getGradesForStudent(studentId: String): Flow<List<GradeRecord>> = academicDao.getGradesForStudent(studentId)
    suspend fun insertGrades(grades: List<GradeRecord>) = academicDao.insertGrades(grades)

    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>> = academicDao.getAttendanceForStudent(studentId)
    suspend fun insertAttendance(records: List<AttendanceRecord>) = academicDao.insertAttendance(records)

    fun getSchedulesForStudent(studentId: String): Flow<List<ScheduleItem>> = academicDao.getSchedulesForStudent(studentId)
    suspend fun insertSchedules(items: List<ScheduleItem>) = academicDao.insertSchedules(items)

    fun getMessagesForStudent(studentId: String): Flow<List<ChatMessage>> = chatDao.getMessagesForStudent(studentId)
    suspend fun insertMessage(message: ChatMessage) = chatDao.insertMessage(message)
    suspend fun clearChat(studentId: String) = chatDao.clearChat(studentId)
}
