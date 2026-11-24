
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
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.viewmodels.AuthViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        showRegistrationMessage(intent)

        // Connect the Activity to the AuthViewModel.
        authViewModel = ViewModelProvider(this)
            .get(AuthViewModel::class.java)

        // Connect the login screen components.
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        // Connect the Administrator Policy components.
        val txtAdminPolicy = findViewById<TextView>(R.id.txtAdminPolicy)
        val chkAcceptPolicy = findViewById<CheckBox>(R.id.chkAcceptPolicy)

        // Observe successful administrator authentication.
        authViewModel.adminUser.observe(this) { admin ->

            if (admin != null) {

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

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
        }

        // Observe login errors and messages.
        authViewModel.message.observe(this) { message ->

            if (!message.isNullOrEmpty()) {

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Open the Administrator Policy PDF.
        txtAdminPolicy.setOnClickListener {
            openAdminPolicy()
        }

        // Navigate to the registration screen.
        txtRegister.setOnClickListener {

            val intent = Intent(
                this,
                RegisterActivity::class.java
            )

            startActivity(intent)
        }

        // Handle administrator login.
        btnLogin.setOnClickListener {

            // Check whether the administrator accepted the policy.
            if (!chkAcceptPolicy.isChecked) {

                Toast.makeText(
                    this,
                    "Please read and accept the Administrator Policy.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            // Retrieve the email and password.
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Validate the login fields.
            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter your email and password",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Authenticate through the AuthViewModel.
                authViewModel.loginAdmin(email, password)
            }
        }
    }

    // Copy the PDF from assets and open it using an available PDF viewer.
    private fun openAdminPolicy() {

        try {

            // Create a file inside the app's internal files directory.
            val policyFile = File(filesDir, "AdminPolicy.pdf")

            // Copy the PDF from assets into internal storage.
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

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
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
    // Display registration success message on the login screen.
    private fun showRegistrationMessage(intent: Intent?) {
        val message = intent?.getStringExtra("registration_success")

        if (!message.isNullOrEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            // Prevent the same message from appearing again.
            intent.removeExtra("registration_success")
        }
    }

    // Handle the message when MainActivity is already open.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showRegistrationMessage(intent)
    }
}
