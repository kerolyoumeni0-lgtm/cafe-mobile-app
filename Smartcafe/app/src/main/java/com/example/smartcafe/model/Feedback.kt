
package com.example.smartcafe.models

// Represents one customer feedback record stored in Firebase.
data class Feedback(
    val id: String = "",
    val customerName: String = "Unknown customer",
    val rating: Double = 0.0,
    val comment: String = "No comment provided",
    val orderId: String = "Not provided",
    val isSimulated: Boolean = false
)
