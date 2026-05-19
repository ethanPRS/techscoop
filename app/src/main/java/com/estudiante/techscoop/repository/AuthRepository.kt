package com.estudiante.techscoop.repository

import android.content.Context
import com.estudiante.techscoop.R
import com.estudiante.techscoop.SessionManager
import com.estudiante.techscoop.data.local.AppDatabase
import com.estudiante.techscoop.data.local.UserEntity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun currentUser(): FirebaseUser? = auth.currentUser
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Usuario nulo"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Usuario nulo"))
            if (displayName.isNotBlank()) {
                user.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                ).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUserToRoom(context: Context, email: String, name: String, password: String = "") {
        val dao = AppDatabase.getInstance(context).userDao()
        val repo = UserRepository(dao)
        repo.insertUser(
            UserEntity(
                name = name.ifBlank { email.substringBefore("@") },
                email = email,
                password = password,
                bio = null,
                status = "activo",
                profileImageUri = null,
                deactivationDate = null
            )
        )
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, gso).signOut().await()
        } catch (_: Exception) {
        }
        SessionManager.clear(context)
        val dao = AppDatabase.getInstance(context).userDao()
        UserRepository(dao).clearAll()
    }

    fun userMessage(context: Context, error: Throwable): String {
        if (error is FirebaseNetworkException ||
            error.message?.contains("network error", ignoreCase = true) == true
        ) {
            return context.getString(R.string.auth_error_network)
        }

        return when (error) {
            is FirebaseAuthUserCollisionException -> context.getString(R.string.auth_error_email_in_use)
            is FirebaseAuthWeakPasswordException -> context.getString(R.string.auth_error_weak_password)
            is FirebaseAuthInvalidCredentialsException -> context.getString(R.string.auth_error_wrong_credentials)
            else -> error.localizedMessage ?: context.getString(R.string.auth_error_generic)
        }
    }
}


