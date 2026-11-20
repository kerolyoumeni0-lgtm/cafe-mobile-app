
package com.example.smartcafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
private var feedbackListener: ListenerRegistration? = null
private var firstFeedbackSnapshot = true

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var ordersListener: ListenerRegistration? = null
    private var firstOrdersSnapshot = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
        NotificationHelper(this).createNotificationChannel()


        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }


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
            val intent = Intent(
                this,
                UpdateOrderStatusActivity::class.java
            )
            startActivity(intent)
        }
        val btnCustomerFeedback = findViewById<Button>(R.id.btnCustomerFeedback)

        btnCustomerFeedback.setOnClickListener {
            val intent = Intent(this, CustomerFeedbackActivity::class.java)
            startActivity(intent)
        }
        val btnSalesAnalytics =
            findViewById<Button>(R.id.btnSalesAnalytics)
        btnSalesAnalytics.setOnClickListener {
            val intent = Intent(this, SalesAnalyticsActivity::class.java)
            startActivity(intent)
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
    private val requestNotificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(
                    this,
                    "Notifications enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    private fun listenForNewOrders() {

        ordersListener = db.collection("orders")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Toast.makeText(
                        this,
                        "Unable to monitor new orders",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                if (firstOrdersSnapshot) {
                    firstOrdersSnapshot = false
                    return@addSnapshotListener
                }

                for (change in snapshots.documentChanges) {

                    if (change.type ==
                        com.google.firebase.firestore.DocumentChange.Type.ADDED
                    ) {
                        val orderId = change.document.id

                        NotificationHelper(this)
                            .showNewOrderNotification(orderId)
                    }
                }
            }
    }

    private fun listenForNewFeedback() {

        feedbackListener = db.collection("feedback")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Toast.makeText(
                        this,
                        "Unable to monitor new feedback",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                // Ignore existing feedback when listening starts.
                if (firstFeedbackSnapshot) {
                    firstFeedbackSnapshot = false
                    return@addSnapshotListener
                }

                for (change in snapshots.documentChanges) {

                    if (change.type ==
                        com.google.firebase.firestore.DocumentChange.Type.ADDED
                    ) {
                        val customerName =
                            change.document.getString("customerName")
                                ?: "A customer"

                        NotificationHelper(this)
                            .showNewFeedbackNotification(customerName)
                    }
                }
            }
    }


    override fun onDestroy() {
        super.onDestroy()

        ordersListener?.remove()
    }
    override fun onStart() {
        super.onStart()

        if (::db.isInitialized) {
            firstOrdersSnapshot = true
            listenForNewOrders()
            firstFeedbackSnapshot = true
            listenForNewFeedback()
        }
    }

    override fun onStop() {
        super.onStop()

        ordersListener?.remove()
        ordersListener = null
        feedbackListener?.remove()
        feedbackListener = null
    }

}
