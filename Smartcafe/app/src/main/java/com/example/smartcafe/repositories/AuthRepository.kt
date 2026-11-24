
package com.example.smartcafe.repositories

import com.example.smartcafe.models.AdminUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Handles Firebase Authentication and administrator verification.
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Sign in and verify whether the user is an administrator.
    fun loginAdmin(
        email: String,
        password: String,
        onSuccess: (AdminUser) -> Unit,
        onFailure: (String) -> Unit
    ) {

        // Authenticate the user with Firebase.
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val user = result.user

                // Check whether Firebase returned a valid user ID.
                if (user == null) {
                    auth.signOut()
                    onFailure("Could not verify your account.")
                    return@addOnSuccessListener
                }

                val userId = user.uid

                // Check the user's role in Firestore.
                db.collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->

                        val role = document.getString("role")

                        // Allow access only to administrators.
                        if (role == "admin") {

                            val adminUser = AdminUser(
                                uid = userId,
                                email = user.email ?: email,
                                role = role
                            )

                            onSuccess(adminUser)

                        } else {

                            // Sign out users who are not administrators.
                            auth.signOut()
                            onFailure(
                                "Access denied. Admin account required."
                            )
                        }
                    }
                    .addOnFailureListener { error ->

                        // Sign out if the administrator role cannot be verified.
                        auth.signOut()

                        onFailure(
                            "Could not verify admin role: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error ->

                onFailure("Login failed: ${error.message}")
            }
    }

    // Sign out the current user.
    fun logout() {
        auth.signOut()
    }
}
