
package com.example.smartcafe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

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

                Toast.makeText(
                    this,
                    "Login button is working!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
