
package com.example.smartcafe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Connect to Firebase Authentication.
        auth = FirebaseAuth.getInstance()

        // Connect the login screen components.
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        // Connect the Administrator Policy components.
        val txtAdminPolicy = findViewById<TextView>(R.id.txtAdminPolicy)
        val chkAcceptPolicy = findViewById<CheckBox>(R.id.chkAcceptPolicy)

        // Open the Administrator Policy PDF when clicked.
        txtAdminPolicy.setOnClickListener {
            openAdminPolicy()
        }

        // Navigate to the registration screen.
        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Handle administrator login.
        btnLogin.setOnClickListener {

            // Check that the administrator has accepted the policy.
            if (!chkAcceptPolicy.isChecked) {
                Toast.makeText(
                    this,
                    "Please read and accept the Administrator Policy.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Retrieve the email and password entered by the user.
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Check that both login fields have been completed.
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your email and password",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Sign in using Firebase Authentication.
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->

                        // Retrieve the unique Firebase user ID.
                        val userId = result.user?.uid

                        if (userId == null) {
                            Toast.makeText(
                                this,
                                "Could not verify your account.",
                                Toast.LENGTH_LONG
                            ).show()
                            auth.signOut()
                            return@addOnSuccessListener
                        }

                        // Check the user's administrator role in Firestore.
                        FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { document ->

                                val role = document.getString("role")

                                // Allow access only to approved administrators.
                                if (role == "admin") {

                                    Toast.makeText(
                                        this,
                                        "Admin login successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Open the Admin Dashboard.
                                    val intent = Intent(
                                        this,
                                        DashboardActivity::class.java
                                    )

                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                                    startActivity(intent)

                                } else {

                                    // Sign out users without administrator approval.
                                    auth.signOut()

                                    Toast.makeText(
                                        this,
                                        "Access denied. Admin account required.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            .addOnFailureListener { error ->

                                // Sign out if the administrator role cannot be checked.
                                auth.signOut()

                                Toast.makeText(
                                    this,
                                    "Could not verify admin role: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { error ->

                        // Display an error if Firebase login fails.
                        Toast.makeText(
                            this,
                            "Login failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }

    // Copy the PDF from assets and open it using an available PDF viewer.
    private fun openAdminPolicy() {

        try {
            // Create a file inside the app's internal files directory.
            val policyFile = File(filesDir, "AdminPolicy.pdf")

            // Copy the PDF from the assets folder into internal storage.
            assets.open("AdminPolicy.pdf").use { input ->
                FileOutputStream(policyFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Generate a secure content URI using FileProvider.
            val policyUri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                policyFile
            )

            // Open the PDF using an installed PDF viewer.
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(policyUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)

        } catch (error: Exception) {

            // Display an error if the PDF cannot be opened.
            Toast.makeText(
                this,
                "Unable to open Administrator Policy: ${error.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
