
package com.example.smartcafe.repositories

import com.example.smartcafe.models.Feedback
import com.google.firebase.firestore.FirebaseFirestore

// Handles all Firebase Firestore operations related to customer feedback.
class FeedbackRepository {

    private val db = FirebaseFirestore.getInstance()

    // Retrieve all customer feedback from Firestore.
    fun getAllFeedback(
        onSuccess: (List<Feedback>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("feedback")
            .get()
            .addOnSuccessListener { documents ->

                // Convert each Firestore document into a Feedback object.
                val feedbackList = documents.map { document ->

                    Feedback(
                        id = document.id,
                        customerName = document.getString("customerName")
                            ?: "Unknown customer",
                        rating = (document.get("rating") as? Number)
                            ?.toDouble() ?: 0.0,
                        comment = document.getString("comment")
                            ?: "No comment provided",
                        orderId = document.getString("orderId")
                            ?: "Not provided",
                        isSimulated = document.getBoolean("isSimulated")
                            ?: false
                    )
                }

                onSuccess(feedbackList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
