
package com.example.smartcafe.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentChange

// Handles real-time Firebase monitoring for dashboard events.
class DashboardRepository {

    private val db = FirebaseFirestore.getInstance()

    // Listen for newly created orders.
    fun listenForNewOrders(
        onNewOrder: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {

        // Ignore existing orders when monitoring first starts.
        var firstSnapshot = true

        return db.collection("orders")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                if (firstSnapshot) {
                    firstSnapshot = false
                    return@addSnapshotListener
                }

                // Check for newly added order documents.
                for (change in snapshots.documentChanges) {

                    if (change.type == DocumentChange.Type.ADDED) {
                        onNewOrder(change.document.id)
                    }
                }
            }
    }

    // Listen for newly submitted customer feedback.
    fun listenForNewFeedback(
        onNewFeedback: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration {

        // Ignore existing feedback when monitoring first starts.
        var firstSnapshot = true

        return db.collection("feedback")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                if (firstSnapshot) {
                    firstSnapshot = false
                    return@addSnapshotListener
                }

                // Check for newly added feedback documents.
                for (change in snapshots.documentChanges) {

                    if (change.type == DocumentChange.Type.ADDED) {

                        val customerName =
                            change.document.getString("customerName")
                                ?: "A customer"

                        onNewFeedback(customerName)
                    }
                }
            }
    }
}
