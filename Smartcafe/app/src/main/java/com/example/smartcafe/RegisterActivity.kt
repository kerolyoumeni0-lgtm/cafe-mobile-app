
package com.example.smartcafe

// Imports required for screen navigation and displaying messages.
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Firebase Authentication manages administrator accounts.
import com.google.firebase.auth.FirebaseAuth


class RegisterActivity : AppCompatActivity() {

    // Firebase Authentication instance.
    private lateinit var auth: FirebaseAuth

    // Called when the registration screen is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connects this Activity to the registration XML layout.
        setContentView(R.layout.own)

        // Initialises Firebase Authentication.
        auth = FirebaseAuth.getInstance()

        // Connects the XML input fields to Kotlin variables.
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirmPassword =
            findViewById<EditText>(R.id.edtConfirmPassword)

        // Connects the registration button and login link.
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        // Runs when the administrator clicks the register button.
        btnRegister.setOnClickListener {

            // Retrieves the values entered into the registration fields.
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmPassword = edtConfirmPassword.text.toString()

            // Validates the registration information before creating an account.
            when {

                // Checks whether any required field is empty.
                name.isEmpty() || email.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please complete all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Checks whether the email address has a valid format.
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(
                        this,
                        "Please enter a valid email address",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Checks that the password contains at least six characters.
                password.length < 6 -> {
                    Toast.makeText(
                        this,
                        "Password must contain at least 6 characters",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Checks that both password fields contain the same password.
                password != confirmPassword -> {
                    Toast.makeText(
                        this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // If all validation checks pass, create the account.
                else -> {
                    createAccount(name, email, password)
                }
            }
        }

        // Returns the user to the login screen.
        txtLogin.setOnClickListener {
            finish()
        }
    }

    // Creates a new administrator account using Firebase Authentication.
    private fun createAccount(
        name: String,
        email: String,
        password: String
    ) {

        // Sends the email and password to Firebase to create the account.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                // Checks whether Firebase successfully created the account.
                if (task.isSuccessful) {

                    // Updates the Firebase user's profile with the administrator's name.
                    auth.currentUser?.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                    )

                } else {

                    // Displays the error message if registration fails.
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage
                            ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Continues only if account creation was successful.
                if (task.isSuccessful) {

                    // Signs out the newly created account so the administrator
                    // can log in manually using the login screen.
                    auth.signOut()

                    // Confirms successful registration to the user.
                    Toast.makeText(
                        this,
                        "Registration successful! Please log in.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigates back to the login screen.
                    val intent = Intent(this, MainActivity::class.java)

                    // Clears the previous registration screen from the navigation stack.
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                    startActivity(intent)
                    finish()
                }
            }
    }
}
