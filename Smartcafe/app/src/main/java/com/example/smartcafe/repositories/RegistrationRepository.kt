
package com.example.smartcafe.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

// Handles administrator registration using Firebase.
class RegistrationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Create a new account and save it as pending approval.
    fun registerAdmin(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        // Create the account using Firebase Authentication.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val user = result.user

                if (user == null) {
                    onFailure("Could not create your account.")
                    return@addOnSuccessListener
                }

                val userId = user.uid

                // Save the user details in Firestore.
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "pending"
                )

                db.collection("Users")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener {

                        // Update the Firebase user's display name.
                        val profileUpdate =
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()

                        user.updateProfile(profileUpdate)
                            .addOnCompleteListener {

                                // Registration is complete.
                                onSuccess()
                            }
                    }
                    .addOnFailureListener { error ->

                        onFailure(
                            "Could not save registration details: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error ->

                onFailure(
                    "Registration failed: ${error.message}"
                )
            }
    }

    // Sign out after registration.
    fun logout() {
        auth.signOut()
    }
}
