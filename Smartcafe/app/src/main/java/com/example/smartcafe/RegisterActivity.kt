
package com.example.smartcafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirmPassword =
            findViewById<EditText>(R.id.edtConfirmPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        btnRegister.setOnClickListener {

            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirmPassword =
                edtConfirmPassword.text.toString()

            when {
                name.isEmpty() || email.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Please complete all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
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
                    createAccount(name, email, password)
                }
            }
        }

        txtLogin.setOnClickListener {
            finish()
        }
    }

    private fun createAccount(
        name: String,
        email: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }

                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {

                            Toast.makeText(
                                this,
                                "Account created successfully!",
                                Toast.LENGTH_LONG
                            ).show()

                            startActivity(
                                Intent(this, MainActivity::class.java)
                            )
                            finish()
                        }

                } else {

                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage
                            ?: "Registration failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
