package com.example.baseconverter

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.security.MessageDigest

open class DatabaseManager(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BaseConverterDB"
        private const val DATABASE_VERSION = 5 // Incremented for first_name column
        private const val TABLE_USERS = "users"
        private const val TABLE_REQUESTS = "requests"
        private const val TABLE_CONVERSIONS = "conversions"
        private const val TABLE_SESSIONS = "sessions"
        // Users table columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_FIRST_NAME = "first_name"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
        private const val COLUMN_ROLE = "role"
        // Requests table columns
        private const val COLUMN_REQUEST_ID = "id"
        private const val COLUMN_STUDENT_USERNAME = "student_username"
        private const val COLUMN_TUTOR_USERNAME = "tutor_username"
        private const val COLUMN_STATUS = "status"
        // Conversions table columns
        private const val COLUMN_CONVERSION_ID = "id"
        private const val COLUMN_USER_USERNAME = "user_username"
        private const val COLUMN_INPUT_VALUE = "input_value"
        private const val COLUMN_INPUT_BASE = "input_base"
        private const val COLUMN_OUTPUT_VALUE = "output_value"
        private const val COLUMN_OUTPUT_BASE = "output_base"
        private const val COLUMN_TIMESTAMP = "timestamp"
        // Sessions table columns
        private const val COLUMN_SESSION_ID = "id"
        private const val COLUMN_SESSION_STUDENT_USERNAME = "student_username"
        private const val COLUMN_SESSION_TUTOR_USERNAME = "tutor_username"
        private const val COLUMN_TOPIC = "topic"
        private const val COLUMN_SESSION_DATE = "session_date"
        private const val COLUMN_SESSION_STATUS = "session_status"
        private const val TAG = "DatabaseManager"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating database schema")
        // Users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_FIRST_NAME TEXT NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL,
                $COLUMN_ROLE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        // Requests table
        val createRequestsTable = """
            CREATE TABLE $TABLE_REQUESTS (
                $COLUMN_REQUEST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_USERNAME TEXT NOT NULL,
                $COLUMN_TUTOR_USERNAME TEXT NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL DEFAULT 'Pending',
                FOREIGN KEY ($COLUMN_STUDENT_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME),
                FOREIGN KEY ($COLUMN_TUTOR_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
            )
        """.trimIndent()
        db.execSQL(createRequestsTable)

        // Conversions table
        val createConversionsTable = """
            CREATE TABLE $TABLE_CONVERSIONS (
                $COLUMN_CONVERSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_USERNAME TEXT NOT NULL,
                $COLUMN_INPUT_VALUE TEXT NOT NULL,
                $COLUMN_INPUT_BASE INTEGER NOT NULL,
                $COLUMN_OUTPUT_VALUE TEXT NOT NULL,
                $COLUMN_OUTPUT_BASE INTEGER NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_USER_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
            )
        """.trimIndent()
        db.execSQL(createConversionsTable)

        // Sessions table
        val createSessionsTable = """
            CREATE TABLE $TABLE_SESSIONS (
                $COLUMN_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SESSION_STUDENT_USERNAME TEXT NOT NULL,
                $COLUMN_SESSION_TUTOR_USERNAME TEXT NOT NULL,
                $COLUMN_TOPIC TEXT NOT NULL,
                $COLUMN_SESSION_DATE INTEGER NOT NULL,
                $COLUMN_SESSION_STATUS TEXT NOT NULL DEFAULT 'Scheduled',
                FOREIGN KEY ($COLUMN_SESSION_STUDENT_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME),
                FOREIGN KEY ($COLUMN_SESSION_TUTOR_USERNAME) REFERENCES $TABLE_USERS($COLUMN_USERNAME)
            )
        """.trimIndent()
        db.execSQL(createSessionsTable)

        // Indexes
        db.execSQL("CREATE INDEX idx_username ON $TABLE_USERS($COLUMN_USERNAME)")
        db.execSQL("CREATE INDEX idx_email ON $TABLE_USERS($COLUMN_EMAIL)")
        db.execSQL("CREATE INDEX idx_tutor_username ON $TABLE_REQUESTS($COLUMN_TUTOR_USERNAME)")
        db.execSQL("CREATE INDEX idx_user_username ON $TABLE_CONVERSIONS($COLUMN_USER_USERNAME)")
        db.execSQL("CREATE INDEX idx_session_tutor ON $TABLE_SESSIONS($COLUMN_SESSION_TUTOR_USERNAME)")
        Log.d(TAG, "Database schema created successfully")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrading database from version $oldVersion to $newVersion")
        if (oldVersion < 5) {
            // Add first_name column to users table
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_FIRST_NAME TEXT DEFAULT ''")
        }
        Log.d(TAG, "Database schema upgraded successfully")
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun registerUserWithRole(username: String, email: String, firstName: String, password: String, role: String): Pair<Boolean, String> {
        val db = writableDatabase
        return try {
            val hashedPassword = hashPassword(password)
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, username)
                put(COLUMN_EMAIL, email)
                put(COLUMN_FIRST_NAME, firstName)
                put(COLUMN_PASSWORD_HASH, hashedPassword)
                put(COLUMN_ROLE, role)
            }
            Log.d(TAG, "Attempting to register user: $username, Email: $email, Role: $role")
            val result = db.insertOrThrow(TABLE_USERS, null, values)
            val success = result != -1L
            Log.d(TAG, "Registration result for $username: $success (ID: $result)")
            Pair(success, if (success) "Success" else "Unknown error")
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            Log.e(TAG, "Constraint violation for $username: ${e.message}", e)
            Pair(false, "Username or email already exists")
        } catch (e: android.database.sqlite.SQLiteException) {
            Log.e(TAG, "Database error for $username: ${e.message}", e)
            Pair(false, "Database error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error for $username: ${e.message}", e)
            Pair(false, "Unexpected error: ${e.message}")
        } finally {
            db.close()
        }
    }

    open fun registerUser(username: String, email: String, firstName: String, password: String): Pair<Boolean, String> {
        return registerUserWithRole(username, email, firstName, password, "Student")
    }

    fun loginUserWithRole(username: String, password: String, role: String): Pair<Boolean, String> {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_PASSWORD_HASH, $COLUMN_ROLE FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?",
                arrayOf(username)
            )
            if (cursor.moveToFirst()) {
                val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
                val storedRole = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
                val inputHash = hashPassword(password)
                val isPasswordCorrect = storedHash == inputHash
                val isRoleCorrect = role.isEmpty() || storedRole == role
                Log.d(TAG, "Login check for $username: Password match: $isPasswordCorrect, Role match: $isRoleCorrect")
                return Pair(isPasswordCorrect && isRoleCorrect, storedRole)
            }
            Log.d(TAG, "No user found with username: $username")
            return Pair(false, "")
        } catch (e: Exception) {
            Log.e(TAG, "Login failed for $username: ${e.message}", e)
            return Pair(false, "")
        } finally {
            cursor?.close()
            db.close()
        }
    }

    open fun loginUser(username: String, password: String): Boolean {
        val (success, _) = loginUserWithRole(username, password, "")
        return success
    }

    open fun getUserDetails(username: String): Triple<String, String, String>? {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_USERNAME, $COLUMN_EMAIL, $COLUMN_FIRST_NAME FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?",
                arrayOf(username)
            )
            if (cursor.moveToFirst()) {
                val userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
                return Triple(userName, email, firstName)
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get details for $username: ${e.message}", e)
            return null
        } finally {
            cursor?.close()
            db.close()
        }
    }

    open fun updateUserDetails(username: String, newUsername: String, email: String, firstName: String): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, newUsername)
                put(COLUMN_EMAIL, email)
                put(COLUMN_FIRST_NAME, firstName)
            }
            val rowsAffected = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_USERNAME = ?",
                arrayOf(username)
            )
            Log.d(TAG, "Updated $rowsAffected user records for $username")
            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user details for $username: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    open fun userExists(username: String, email: String): Pair<Boolean, Boolean> {
        val db = readableDatabase
        var usernameCursor: android.database.Cursor? = null
        var emailCursor: android.database.Cursor? = null
        try {
            var usernameExists = false
            var emailExists = false
            usernameCursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?",
                arrayOf(username)
            )
            if (usernameCursor.moveToFirst()) {
                usernameExists = usernameCursor.getInt(0) > 0
            }
            emailCursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?",
                arrayOf(email)
            )
            if (emailCursor.moveToFirst()) {
                emailExists = emailCursor.getInt(0) > 0
            }
            Log.d(TAG, "User exists check: Username $username: $usernameExists, Email $email: $emailExists")
            return Pair(usernameExists, emailExists)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user existence: ${e.message}", e)
            return Pair(false, false)
        } finally {
            usernameCursor?.close()
            emailCursor?.close()
            db.close()
        }
    }

    fun requestTutor(studentUsername: String, tutorUsername: String): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_STUDENT_USERNAME, studentUsername)
                put(COLUMN_TUTOR_USERNAME, tutorUsername)
                put(COLUMN_STATUS, "Pending")
            }
            val result = db.insertOrThrow(TABLE_REQUESTS, null, values)
            return result != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request tutor: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    fun getStudentRequests(tutorUsername: String): List<Pair<String, String>> {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        val requests = mutableListOf<Pair<String, String>>()
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_STUDENT_USERNAME, $COLUMN_STATUS FROM $TABLE_REQUESTS WHERE $COLUMN_TUTOR_USERNAME = ?",
                arrayOf(tutorUsername)
            )
            while (cursor.moveToNext()) {
                val studentUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_USERNAME))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                requests.add(Pair(studentUsername, status))
            }
            return requests
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get student requests: ${e.message}", e)
            return emptyList()
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun getTutorRequestsForStudent(studentUsername: String): List<Pair<String, String>> {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        val requests = mutableListOf<Pair<String, String>>()
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_TUTOR_USERNAME, $COLUMN_STATUS FROM $TABLE_REQUESTS WHERE $COLUMN_STUDENT_USERNAME = ?",
                arrayOf(studentUsername)
            )
            while (cursor.moveToNext()) {
                val tutorUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_USERNAME))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS))
                requests.add(Pair(tutorUsername, status))
            }
            return requests
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get tutor requests for $studentUsername: ${e.message}", e)
            return emptyList()
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun updateRequestStatus(studentUsername: String, tutorUsername: String, newStatus: String): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_STATUS, newStatus)
            }
            val rowsAffected = db.update(
                TABLE_REQUESTS,
                values,
                "$COLUMN_STUDENT_USERNAME = ? AND $COLUMN_TUTOR_USERNAME = ?",
                arrayOf(studentUsername, tutorUsername)
            )
            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update request status: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    fun getAvailableTutors(): List<String> {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        val tutors = mutableListOf<String>()
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_USERNAME FROM $TABLE_USERS WHERE $COLUMN_ROLE = ?",
                arrayOf("Tutor")
            )
            while (cursor.moveToNext()) {
                val tutorUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                tutors.add(tutorUsername)
            }
            return tutors
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get available tutors: ${e.message}", e)
            return emptyList()
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun saveConversion(
        username: String,
        inputValue: String,
        inputBase: Int,
        outputValue: String,
        outputBase: Int
    ): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_USER_USERNAME, username)
                put(COLUMN_INPUT_VALUE, inputValue)
                put(COLUMN_INPUT_BASE, inputBase)
                put(COLUMN_OUTPUT_VALUE, outputValue)
                put(COLUMN_OUTPUT_BASE, outputBase)
                put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            }
            val result = db.insertOrThrow(TABLE_CONVERSIONS, null, values)
            return result != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save conversion for $username: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    data class ConversionEntry(
        val inputValue: String,
        val inputBase: Int,
        val outputValue: String,
        val outputBase: Int,
        val timestamp: Long
    )

    open fun getConversionHistory(username: String): List<ConversionEntry> {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        val conversions = mutableListOf<ConversionEntry>()
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_INPUT_VALUE, $COLUMN_INPUT_BASE, $COLUMN_OUTPUT_VALUE, $COLUMN_OUTPUT_BASE, $COLUMN_TIMESTAMP " +
                        "FROM $TABLE_CONVERSIONS WHERE $COLUMN_USER_USERNAME = ? ORDER BY $COLUMN_TIMESTAMP DESC",
                arrayOf(username)
            )
            while (cursor.moveToNext()) {
                val inputValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INPUT_VALUE))
                val inputBase = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INPUT_BASE))
                val outputValue = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTPUT_VALUE))
                val outputBase = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OUTPUT_BASE))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                conversions.add(ConversionEntry(inputValue, inputBase, outputValue, outputBase, timestamp))
            }
            return conversions
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get conversion history for $username: ${e.message}", e)
            return emptyList()
        } finally {
            cursor?.close()
            db.close()
        }
    }

    open fun clearConversionHistory(username: String): Boolean {
        val db = writableDatabase
        try {
            val rowsAffected = db.delete(
                TABLE_CONVERSIONS,
                "$COLUMN_USER_USERNAME = ?",
                arrayOf(username)
            )
            Log.d(TAG, "Cleared $rowsAffected conversion records for $username")
            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear conversion history for $username: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    fun getAnyUser(): Triple<String, String, String>? {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        try {
            cursor = db.rawQuery(
                "SELECT $COLUMN_USERNAME, $COLUMN_EMAIL, $COLUMN_FIRST_NAME FROM $TABLE_USERS LIMIT 1",
                null
            )
            if (cursor.moveToFirst()) {
                val userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
                return Triple(userName, email, firstName)
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get any user: ${e.message}", e)
            return null
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun createSession(
        studentUsername: String,
        tutorUsername: String,
        topic: String,
        sessionDate: Long
    ): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_SESSION_STUDENT_USERNAME, studentUsername)
                put(COLUMN_SESSION_TUTOR_USERNAME, tutorUsername)
                put(COLUMN_TOPIC, topic)
                put(COLUMN_SESSION_DATE, sessionDate)
                put(COLUMN_SESSION_STATUS, "Scheduled")
            }
            val result = db.insertOrThrow(TABLE_SESSIONS, null, values)
            return result != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create session for $studentUsername with $tutorUsername: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }

    data class SessionEntry(
        val studentUsername: String,
        val tutorUsername: String,
        val topic: String,
        val sessionDate: Long,
        val status: String
    )

    fun getSessionsForUser(username: String, isTutor: Boolean): List<SessionEntry> {
        val db = readableDatabase
        var cursor: android.database.Cursor? = null
        val sessions = mutableListOf<SessionEntry>()
        try {
            val column = if (isTutor) COLUMN_SESSION_TUTOR_USERNAME else COLUMN_SESSION_STUDENT_USERNAME
            cursor = db.rawQuery(
                "SELECT $COLUMN_SESSION_STUDENT_USERNAME, $COLUMN_SESSION_TUTOR_USERNAME, $COLUMN_TOPIC, $COLUMN_SESSION_DATE, $COLUMN_SESSION_STATUS " +
                        "FROM $TABLE_SESSIONS WHERE $column = ? ORDER BY $COLUMN_SESSION_DATE DESC",
                arrayOf(username)
            )
            while (cursor.moveToNext()) {
                val studentUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_STUDENT_USERNAME))
                val tutorUsername = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_TUTOR_USERNAME))
                val topic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOPIC))
                val sessionDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SESSION_DATE))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_STATUS))
                sessions.add(SessionEntry(studentUsername, tutorUsername, topic, sessionDate, status))
            }
            return sessions
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get sessions for $username: ${e.message}", e)
            return emptyList()
        } finally {
            cursor?.close()
            db.close()
        }
    }

    fun updateSessionStatus(
        studentUsername: String,
        tutorUsername: String,
        sessionDate: Long,
        newStatus: String
    ): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put(COLUMN_SESSION_STATUS, newStatus)
            }
            val rowsAffected = db.update(
                TABLE_SESSIONS,
                values,
                "$COLUMN_SESSION_STUDENT_USERNAME = ? AND $COLUMN_SESSION_TUTOR_USERNAME = ? AND $COLUMN_SESSION_DATE = ?",
                arrayOf(studentUsername, tutorUsername, sessionDate.toString())
            )
            return rowsAffected > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update session status: ${e.message}", e)
            return false
        } finally {
            db.close()
        }
    }
}
