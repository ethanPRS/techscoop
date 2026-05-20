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

// Capa de acceso a Firebase Authentication: login, registro, reset y sincronización con Room.
object AuthRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    fun currentUser(): FirebaseUser? = auth.currentUser    // Inicio de sesión con email y contraseña usando Firebase Auth (corrutina suspend).
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Usuario nulo"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

        // Registra usuario nuevo y opcionalmente guarda el nombre para mostrar en Firebase.
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

        // Envía el correo de recuperación de contraseña desde Firebase.
    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3 posibles escenarios al iniciar sesión
    enum class SyncStatus { SUCCESS, DELETED, REQUIRES_REACTIVATION }

    suspend fun syncUserToRoom(context: Context, email: String, name: String, password: String = ""): SyncStatus {
        val dao = AppDatabase.getInstance(context).userDao()
        val repo = UserRepository(dao)
        val existing = repo.getUser()

        if (existing != null && existing.email == email) {
            // Verificar si la cuenta está inactiva
            if (existing.status == "inactivo" && existing.deactivationDate != null) {
                val now = java.util.Calendar.getInstance().timeInMillis
                val daysInactive = (now - existing.deactivationDate) / (1000 * 60 * 60 * 24)

                if (daysInactive >= 30) {
                    // Pasaron 30 días: Se elimina la cuenta
                    repo.deleteUser(existing)
                    auth.currentUser?.delete()?.await()
                    return SyncStatus.DELETED
                } else {
                    // Está en periodo de gracia: Detenemos el login y pedimos confirmación
                    return SyncStatus.REQUIRES_REACTIVATION
                }
            }

            // Login normal (cuenta activa)
            val finalPassword = if (password.isNotBlank()) password else existing.password
            repo.updateUser(existing.copy(password = finalPassword))
            return SyncStatus.SUCCESS
        }

        // Usuario nuevo
        repo.clearAll()
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
        return SyncStatus.SUCCESS
    }

    // Se llama solo si el usuario presiona "Yes" en el diálogo
    suspend fun reactivateUser(context: Context, email: String, password: String = "") {
        val dao = AppDatabase.getInstance(context).userDao()
        val repo = UserRepository(dao)
        val existing = repo.getUser()

        if (existing != null && existing.email == email) {
            val finalPassword = if (password.isNotBlank()) password else existing.password
            repo.updateUser(existing.copy(status = "activo", deactivationDate = null, password = finalPassword))
        }
    }

    // Cierra sesión en Firebase y Google, borra prefs y tabla user_profile local.
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
        // Note: user profile data is intentionally kept in ROOM so it
        // persists across logout/login cycles for the same user.
        // syncUserToRoom() handles cleanup when a *different* user logs in.
    }

        // Traduce errores de Firebase a mensajes legibles para el usuario (Toast).
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



