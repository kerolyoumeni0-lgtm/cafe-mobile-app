
package com.example.smartcafe.models

// Represents an order stored in Firebase Firestore.
data class Order(

    // Unique Firestore document ID.
    val id: String = "",

    // Customer information.
    val customerName: String = "",
    val customerEmail: String = "",

    // Order information.
    val items: String = "",
    val totalPrice: Double = 0.0,
    val status: String = "Pending",

    // Preparation timer end time.
    val preparationEndTime: Long? = null
)
