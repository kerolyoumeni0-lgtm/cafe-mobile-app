
package com.example.smartcafe.models

// Represents an authenticated administrator account.
data class AdminUser(
    val uid: String = "",
    val email: String = "",
    val role: String = ""
)
