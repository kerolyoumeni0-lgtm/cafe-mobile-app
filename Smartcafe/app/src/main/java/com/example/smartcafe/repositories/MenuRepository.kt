
package com.example.smartcafe.repositories

import com.example.smartcafe.models.MenuItem
import com.google.firebase.firestore.FirebaseFirestore

// Handles communication between the application and Firebase Firestore.
class MenuRepository {

    // Connect to the Firestore database.
    private val db = FirebaseFirestore.getInstance()

    // Reference to the MenuItems collection.
    private val menuCollection = db.collection("MenuItems")

    // Add a new menu item to Firestore.
    fun addMenuItem(
        menuItem: MenuItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        menuCollection
            .add(menuItem)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    // Update an existing menu item using its document ID.
    fun updateMenuItem(
        itemId: String,
        menuItem: MenuItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        menuCollection
            .document(itemId)
            .set(menuItem)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    // Retrieve a menu item from Firestore.
    fun getMenuItem(
        itemId: String,
        onSuccess: (MenuItem?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        menuCollection
            .document(itemId)
            .get()
            .addOnSuccessListener { document ->

                // Convert the Firestore document into a MenuItem object.
                val menuItem = document.toObject(MenuItem::class.java)

                onSuccess(menuItem)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }

    // Delete a menu item from Firestore.
    fun deleteMenuItem(
        itemId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        menuCollection
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
    // Retrieve all menu items from Firestore, including their document IDs.
    fun getAllMenuItems(
        onSuccess: (List<Pair<String, MenuItem>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        menuCollection.get()
            .addOnSuccessListener { result ->

                val menuItems = result.documents.mapNotNull { document ->

                    val menuItem = document.toObject(MenuItem::class.java)

                    // Keep the Firestore document ID for editing and deleting.
                    menuItem?.let {
                        document.id to it
                    }
                }

                onSuccess(menuItems)
            }
            .addOnFailureListener { error ->
                onFailure(error)
            }
    }
}
