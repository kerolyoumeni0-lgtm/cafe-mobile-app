
package com.example.smartcafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.viewmodels.RegistrationViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var registrationViewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the registration XML layout.
        setContentView(R.layout.own)

        // Connect the Activity to the RegistrationViewModel.
        registrationViewModel = ViewModelProvider(this)
            .get(RegistrationViewModel::class.java)

        // Connect the registration fields.
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirmPassword =
            findViewById<EditText>(R.id.edtConfirmPassword)

        // Connect the registration button and login link.
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        // Observe registration success.
        registrationViewModel.registrationSuccessful.observe(this) { successful ->

            if (successful == true) {

                // Sign out the newly registered account.
                registrationViewModel.logout()

                // Return to the login screen with a success message.
                val intent = Intent(this, MainActivity::class.java)

                intent.putExtra(
                    "registration_success",
                    "Registration successful! Your account is awaiting administrator approval."
                )

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP

                startActivity(intent)
                finish()
            }
        }

        // Observe registration errors.
        registrationViewModel.message.observe(this) { message ->

            if (!message.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Handle registration button clicks.
        btnRegister.setOnClickListener {

            // Retrieve the entered registration information.
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            // Validate the registration information.
            when {

                name.isEmpty() || email.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty() -> {

                    Toast.makeText(
                        this,
                        "Please complete all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches() -> {

                    Toast.makeText(
                        this,
                        "Please enter a valid email address",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                password.length < 6 -> {

                    Toast.makeText(
                        this,
                        "Password must contain at least 6 characters",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                password != confirmPassword -> {

                    Toast.makeText(
                        this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                    // Register through the ViewModel.
                    registrationViewModel.registerAdmin(
                        name,
                        email,
                        password
                    )
                }
            }
        }

        // Return to the login screen.
        txtLogin.setOnClickListener {
            finish()
        }
    }
}
