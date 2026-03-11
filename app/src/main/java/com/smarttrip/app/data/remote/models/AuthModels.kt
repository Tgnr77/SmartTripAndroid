package com.smarttrip.app.data.remote.models

import com.google.gson.annotations.SerializedName

// ───── AUTH ─────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String
)

data class AuthResponse(
    val success: Boolean? = null,    // absent de certaines réponses backend
    val token: String? = null,
    val user: UserDto? = null,
    val message: String? = null,
    val error: String? = null,       // backend retourne parfois "error" au lieu de "message"
    val requiresVerification: Boolean? = null,
    val email: String? = null
)

data class UserDto(
    val id: String,
    val email: String,
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("email_verified") val isVerified: Boolean = false
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    @SerializedName("newPassword") val newPassword: String   // backend attend "newPassword"
)

data class GenericResponse(
    val success: Boolean? = null,
    val message: String? = null
)

// Réponse du GET /api/auth/profile
data class ProfileResponse(
    val user: UserDto? = null,
    val message: String? = null
)
