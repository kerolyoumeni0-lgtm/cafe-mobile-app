
package com.example.smartcafe

import android.content.ActivityNotFoundException
import android.content.Intent
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

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Connect to Firebase Authentication
        auth = FirebaseAuth.getInstance()

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        // Administrator policy controls
        val checkAdminPolicy =
            findViewById<CheckBox>(R.id.checkAdminPolicy)

        val txtReadPolicy =
            findViewById<TextView>(R.id.txtReadPolicy)

        // Open the Administrator Policy PDF
        txtReadPolicy.setOnClickListener {
            try {
                val policyFile = File(cacheDir, "administrator_policy.pdf")

                assets.open("administrator_policy.pdf").use { input ->
                    policyFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val pdfUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    policyFile
                )

                val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(pdfUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(
                    Intent.createChooser(pdfIntent, "Open Administrator Policy")
                )

            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "No PDF viewer is installed on this device.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Could not open the policy: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Registration navigation
        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Login
        btnLogin.setOnClickListener {

            // Check that the administrator has accepted the policy
            if (!checkAdminPolicy.isChecked) {
                Toast.makeText(
                    this,
                    "Please accept the Administrator Policy before logging in.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter your email and password",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->

                        val userId = result.user?.uid

                        if (userId == null) {
                            Toast.makeText(
                                this,
                                "Could not verify your account.",
                                Toast.LENGTH_LONG
                            ).show()

                            return@addOnSuccessListener
                        }

                        FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { document ->

                                val role = document.getString("role")

                                if (role == "admin") {

                                    Toast.makeText(
                                        this,
                                        "Admin login successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this,
                                        DashboardActivity::class.java
                                    )

                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                                    startActivity(intent)

                                } else {

                                    FirebaseAuth.getInstance().signOut()

                                    Toast.makeText(
                                        this,
                                        "Access denied. Admin account required.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            .addOnFailureListener { error ->

                                FirebaseAuth.getInstance().signOut()

                                Toast.makeText(
                                    this,
                                    "Could not verify admin role: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { error ->

                        Toast.makeText(
                            this,
                            "Login failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }
}
