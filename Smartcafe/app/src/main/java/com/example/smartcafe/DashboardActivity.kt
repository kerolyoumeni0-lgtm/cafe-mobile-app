
package com.example.smartcafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Only signed-in users can access this screen
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        val btnManageMenu = findViewById<Button>(R.id.btnManageMenu)
        val btnManageOrders = findViewById<Button>(R.id.btnManageOrders)
        val btnUpdateStatus = findViewById<Button>(R.id.btnUpdateStatus)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        val user = auth.currentUser
        val name = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email
            ?: "Administrator"

        txtWelcome.text = "Welcome, $name!"

        btnManageMenu.setOnClickListener {
            val intent = Intent(this, ManageMenuActivity::class.java)
            startActivity(intent)
        }
        btnManageOrders.setOnClickListener {
            val intent = Intent(this, ManageOrdersActivity::class.java)
            startActivity(intent)
        }
        btnUpdateStatus.setOnClickListener {
            Toast.makeText(
                this,
                "Update Order Status - Coming Next",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnLogout.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}
