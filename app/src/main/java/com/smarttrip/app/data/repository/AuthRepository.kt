package com.smarttrip.app.data.repository

import com.smarttrip.app.data.local.TokenDataStore
import com.smarttrip.app.data.remote.ApiService
import com.smarttrip.app.data.remote.models.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object RequiresVerification : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(email: String, password: String): AuthResult<UserDto> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                // Backend: { token, user, message } — pas de champ "success"
                if (body.token != null && body.user != null) {
                    tokenDataStore.saveToken(body.token)
                    AuthResult.Success(body.user)
                } else {
                    AuthResult.Error(body.message ?: body.error ?: "Connexion échouée")
                }
            } else if (response.code() == 403) {
                // Backend retourne 403 quand l'email n'est pas vérifié
                // Le error body contient { requiresVerification: true, email: ... }
                val errorBodyStr = response.errorBody()?.string() ?: ""
                if (errorBodyStr.contains("requiresVerification")) {
                    AuthResult.RequiresVerification
                } else {
                    AuthResult.Error("Compte non autorisé")
                }
            } else {
                AuthResult.Error("Email ou mot de passe incorrect")
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): AuthResult<String> {
        return try {
            val response = api.register(RegisterRequest(email, password, firstName, lastName))
            if (response.isSuccessful) {
                val body = response.body()
                body?.token?.let { tokenDataStore.saveToken(it) }
                AuthResult.Success(email)
            } else {
                // Parse le corps d'erreur pour afficher le vrai message
                val errMsg = try {
                    val json = org.json.JSONObject(response.errorBody()?.string() ?: "")
                    json.optString("error", null) ?: json.optString("message", "Erreur lors de l'inscription")
                } catch (e: Exception) { "Erreur lors de l'inscription" }
                AuthResult.Error(errMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun verifyEmail(email: String, code: String): AuthResult<UserDto> {
        return try {
            val response = api.verifyEmail(VerifyEmailRequest(email, code))
            if (response.isSuccessful) {
                // Backend retourne juste { message: 'Email vérifié' } — pas de token
                // On signale la vérification réussie via RequiresVerification
                // Le ViewModel redirigera vers la page de connexion
                AuthResult.RequiresVerification
            } else {
                AuthResult.Error("Code de vérification invalide")
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun resendVerification(email: String): AuthResult<String> {
        return try {
            val response = api.resendVerification(mapOf("email" to email))
            if (response.isSuccessful) {
                AuthResult.Success("Email renvoyé")
            } else {
                AuthResult.Error("Erreur lors de l'envoi")
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun forgotPassword(email: String): AuthResult<String> {
        return try {
            val response = api.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                AuthResult.Success("Email envoyé")
            } else {
                val errMsg = try {
                    val json = org.json.JSONObject(response.errorBody()?.string() ?: "")
                    json.optString("error", null) ?: json.optString("message", "Erreur lors de l'envoi")
                } catch (e: Exception) { "Erreur lors de l'envoi" }
                AuthResult.Error(errMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun resetPassword(token: String, password: String): AuthResult<String> {
        return try {
            val response = api.resetPassword(ResetPasswordRequest(token, password)) // newPassword via @SerializedName
            if (response.isSuccessful) {
                AuthResult.Success("Mot de passe réinitialisé")
            } else {
                AuthResult.Error("Token invalide ou expiré")
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun getMe(): AuthResult<UserDto> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) {
                // ProfileResponse: { user: { id, email, firstName, ... } }
                val body = response.body()!!
                if (body.user != null) {
                    AuthResult.Success(body.user)
                } else {
                    AuthResult.Error("Non authentifié")
                }
            } else {
                tokenDataStore.clearToken()
                AuthResult.Error("Session expirée")
            }
        } catch (e: Exception) {
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun logout() {
        tokenDataStore.clearToken()
    }

    suspend fun deleteSelf(): AuthResult<String> {
        return try {
            val response = api.deleteMe()
            tokenDataStore.clearToken()
            if (response.isSuccessful) AuthResult.Success("Compte supprimé")
            else AuthResult.Error("Erreur lors de la suppression")
        } catch (e: Exception) {
            tokenDataStore.clearToken()
            AuthResult.Error("Impossible de se connecter au serveur")
        }
    }
}
