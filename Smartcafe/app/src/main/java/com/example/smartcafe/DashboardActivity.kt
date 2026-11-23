
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
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dashboardViewModel: DashboardViewModel

    // Called when the dashboard screen is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Only signed-in users can access this screen.
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        // Connect the Activity to the DashboardViewModel.
        dashboardViewModel = ViewModelProvider(this)
            .get(DashboardViewModel::class.java)

        // Connect the XML views to their Kotlin variables.
        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        val btnManageMenu = findViewById<Button>(R.id.btnManageMenu)
        val btnManageOrders = findViewById<Button>(R.id.btnManageOrders)
        val btnUpdateStatus = findViewById<Button>(R.id.btnUpdateStatus)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnCustomerFeedback =
            findViewById<Button>(R.id.btnCustomerFeedback)
        val btnSalesAnalytics =
            findViewById<Button>(R.id.btnSalesAnalytics)

        // Create the notification channel.
        NotificationHelper(this).createNotificationChannel()

        // Request notification permission on Android 13 and above.
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

        // Display the administrator's name.
        val user = auth.currentUser

        val name = user?.displayName
            ?.takeIf { it.isNotBlank() }
            ?: user?.email
            ?: "Administrator"

        txtWelcome.text = "Welcome, $name!"

        // Observe newly created orders.
        dashboardViewModel.newOrder.observe(this) { orderId ->

            if (orderId != null) {

                NotificationHelper(this)
                    .showNewOrderNotification(orderId)

                // Clear the event after displaying the notification.
                dashboardViewModel.clearNewOrderEvent()
            }
        }

        // Observe newly submitted customer feedback.
        dashboardViewModel.newFeedback.observe(this) { customerName ->

            if (customerName != null) {

                NotificationHelper(this)
                    .showNewFeedbackNotification(customerName)

                // Clear the event after displaying the notification.
                dashboardViewModel.clearNewFeedbackEvent()
            }
        }

        // Observe Firebase monitoring errors.
        dashboardViewModel.message.observe(this) { message ->

            if (!message.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Open the menu management screen.
        btnManageMenu.setOnClickListener {
            startActivity(
                Intent(this, ManageMenuActivity::class.java)
            )
        }

        // Open the order management screen.
        btnManageOrders.setOnClickListener {
            startActivity(
                Intent(this, ManageOrdersActivity::class.java)
            )
        }

        // Open the order status screen.
        btnUpdateStatus.setOnClickListener {
            startActivity(
                Intent(this, UpdateOrderStatusActivity::class.java)
            )
        }

        // Open the customer feedback screen.
        btnCustomerFeedback.setOnClickListener {
            startActivity(
                Intent(this, CustomerFeedbackActivity::class.java)
            )
        }

        // Open the sales analytics screen.
        btnSalesAnalytics.setOnClickListener {
            startActivity(
                Intent(this, SalesAnalyticsActivity::class.java)
            )
        }

        // Log out the administrator.
        btnLogout.setOnClickListener {

            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    // Request permission to display Android notifications.
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

    // Start Firebase monitoring when the dashboard becomes active.
    override fun onStart() {
        super.onStart()

        if (::dashboardViewModel.isInitialized) {
            dashboardViewModel.startOrderMonitoring()
            dashboardViewModel.startFeedbackMonitoring()
        }
    }

    // Stop Firebase monitoring when the dashboard is no longer active.
    override fun onStop() {
        super.onStop()

        if (::dashboardViewModel.isInitialized) {
            dashboardViewModel.stopMonitoring()
        }
    }
}
