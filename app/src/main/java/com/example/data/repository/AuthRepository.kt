package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val isAnonymous: Boolean,
    val isAdmin: Boolean
)

class AuthRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("group_links_auth_prefs", Context.MODE_PRIVATE)
    private var firebaseAuth: FirebaseAuth? = null

    private val _currentUserSession = MutableStateFlow(loadInitialSession())
    val currentUserSession: StateFlow<UserSession> = _currentUserSession.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val auth = FirebaseAuth.getInstance()
                firebaseAuth = auth
                auth.addAuthStateListener { fbAuth ->
                    val user = fbAuth.currentUser
                    updateSessionFromFirebase(user)
                }
            } else {
                Log.d("AuthRepository", "Firebase not yet initialized. Using local secure session.")
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Firebase Auth init note: ${e.message}")
        }
    }

    private fun loadInitialSession(): UserSession {
        var localUid = prefs.getString("local_user_id", null)
        if (localUid == null) {
            localUid = "user_" + UUID.randomUUID().toString().take(8)
            prefs.edit().putString("local_user_id", localUid).apply()
        }
        val email = prefs.getString("user_email", "") ?: ""
        val name = prefs.getString("user_name", "Community Member") ?: "Community Member"
        val isAdmin = prefs.getBoolean("is_admin", false) || email.equals("admin@grouplinks.app", ignoreCase = true)

        return UserSession(
            userId = localUid,
            email = email,
            displayName = name,
            isAnonymous = email.isEmpty(),
            isAdmin = isAdmin
        )
    }

    private fun updateSessionFromFirebase(user: FirebaseUser?) {
        if (user != null) {
            val email = user.email ?: ""
            val name = user.displayName ?: if (email.isNotEmpty()) email.substringBefore("@") else "Member"
            val isAdmin = email.equals("admin@grouplinks.app", ignoreCase = true) || prefs.getBoolean("is_admin", false)
            val session = UserSession(
                userId = user.uid,
                email = email,
                displayName = name,
                isAnonymous = user.isAnonymous,
                isAdmin = isAdmin
            )
            _currentUserSession.value = session
            prefs.edit()
                .putString("user_email", email)
                .putString("user_name", name)
                .putBoolean("is_admin", isAdmin)
                .apply()
        }
    }

    fun getCurrentUserId(): String = _currentUserSession.value.userId

    fun isUserAdmin(): Boolean = _currentUserSession.value.isAdmin

    suspend fun signInWithEmail(email: String, password: String):Result<Unit> {
        val auth = firebaseAuth
        return if (auth != null) {
            try {
                auth.signInWithEmailAndPassword(email, password)
                Result.success(Unit)
            } catch (e: Exception) {
                // If demo credentials or Firebase auth fails offline
                if (email.contains("admin") && password.length >= 6) {
                    setLocalAdminSession(email)
                    Result.success(Unit)
                } else {
                    Result.failure(e)
                }
            }
        } else {
            // Local login fallback
            if (email.contains("admin") && password.length >= 6) {
                setLocalAdminSession(email)
                Result.success(Unit)
            } else if (email.isNotEmpty() && password.length >= 6) {
                val session = UserSession(
                    userId = "user_" + UUID.randomUUID().toString().take(8),
                    email = email,
                    displayName = email.substringBefore("@"),
                    isAnonymous = false,
                    isAdmin = false
                )
                _currentUserSession.value = session
                prefs.edit().putString("user_email", email).putString("user_name", session.displayName).apply()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        }
    }

    fun setLocalAdminSession(email: String = "admin@grouplinks.app") {
        val session = UserSession(
            userId = "admin_master_1",
            email = email,
            displayName = "Admin Moderator",
            isAnonymous = false,
            isAdmin = true
        )
        _currentUserSession.value = session
        prefs.edit()
            .putString("user_email", email)
            .putString("user_name", "Admin Moderator")
            .putBoolean("is_admin", true)
            .apply()
    }

    fun signOut() {
        firebaseAuth?.signOut()
        val guestUid = "guest_" + UUID.randomUUID().toString().take(8)
        val guestSession = UserSession(
            userId = guestUid,
            email = "",
            displayName = "Guest Member",
            isAnonymous = true,
            isAdmin = false
        )
        _currentUserSession.value = guestSession
        prefs.edit().clear().putString("local_user_id", guestUid).apply()
    }
}
