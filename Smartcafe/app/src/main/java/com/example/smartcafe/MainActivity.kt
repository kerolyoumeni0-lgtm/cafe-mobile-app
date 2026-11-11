
package com.example.smartcafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

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

        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {

            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter your email and password",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Check the registered account with Firebase
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Login successful!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Open the Admin Dashboard
                            // We'll connect this once DashboardActivity exists.

                        } else {

                            Toast.makeText(
                                this,
                                task.exception?.localizedMessage
                                    ?: "Login failed. Check your details.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }


}
