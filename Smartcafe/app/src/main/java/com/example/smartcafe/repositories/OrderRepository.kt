
package com.example.smartcafe.repositories

import com.example.smartcafe.models.Order
import com.google.firebase.firestore.FirebaseFirestore

// Handles all Firebase operations related to orders.
class OrderRepository {

    // Connect to Firebase Cloud Firestore.
    private val db = FirebaseFirestore.getInstance()

    // Reference the orders collection.
    private val ordersCollection = db.collection("orders")


    // Retrieve all orders from Firestore.
    fun getAllOrders(
        onSuccess: (List<Order>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        ordersCollection.get()
            .addOnSuccessListener { result ->

                val orders = result.documents.map { document ->

                    // Convert each Firestore document into an Order object.
                    Order(
                        id = document.id,

                        customerName = document.getString("customerName")
                            ?: document.getString("customer_id")
                            ?: "Unknown customer",

                        customerEmail = document.getString("customerEmail")
                            ?: "",

                        items = document.getString("items")
                            ?: "Not provided",

                        totalPrice = (document.get("totalPrice") as? Number)
                            ?.toDouble() ?: 0.0,

                        status = document.getString("status")
                            ?: "Pending",

                        preparationEndTime = document.getLong(
                            "preparationEndTime"
                        )
                    )
                }

                onSuccess(orders)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
    // Update an order's status in Firestore.
    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        ordersCollection.document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
    // Start preparing an order and save its preparation end time.
    fun startPreparingOrder(
        orderId: String,
        endTime: Long,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        ordersCollection.document(orderId)
            .update(
                mapOf(
                    "status" to "Preparing",
                    "preparationEndTime" to endTime
                )
            )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
}
