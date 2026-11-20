
package com.example.smartcafe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.CountDownTimer
import android.widget.EditText
import java.util.Date

class UpdateOrderStatusActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val activeTimers = mutableListOf<CountDownTimer>()

    private lateinit var ordersStatusContainer: LinearLayout
    private lateinit var txtEmptyStatus: TextView


    private val statuses = arrayOf(
        "Pending",
        "Preparing",
        "Ready",
        "Completed"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_order_status)

        ordersStatusContainer =
            findViewById(R.id.ordersStatusContainer)

        txtEmptyStatus =
            findViewById(R.id.txtEmptyStatus)

        loadOrders()
    }

    private fun loadOrders() {

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(
                this,
                "Please log in first.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->

                ordersStatusContainer.removeAllViews()

                if (result.isEmpty) {
                    txtEmptyStatus.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                txtEmptyStatus.visibility = View.GONE

                for (document in result) {

                    val orderId = document.id

                    val customerName =
                        document.getString("customerName")
                            ?: document.getString("customer_id")
                            ?: "Unknown customer"

                    val customerEmail =
                        document.getString("customerEmail") ?: ""

                    val totalPrice =
                        (document.get("totalPrice") as? Number)
                            ?.toDouble() ?: 0.0

                    val currentStatus =
                        document.getString("status") ?: "Pending"
                    val preparationEndTime =
                        document.getLong("preparationEndTime")

                    displayOrder(
                        orderId,
                        customerName,
                        customerEmail,
                        totalPrice,
                        currentStatus,
                        preparationEndTime
                    )
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Could not load orders: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun displayOrder(
        orderId: String,
        customerName: String,
        customerEmail: String,
        totalPrice: Double,
        currentStatus: String,
        preparationEndTime: Long?
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFFF0EAE5.toInt())
        }

        val title = TextView(this).apply {
            text = "Order #${orderId.take(8)}"
            textSize = 19f
            setTextColor(0xFF4E342E.toInt())
        }

        val customer = TextView(this).apply {
            text = "Customer: $customerName"
            textSize = 16f
        }

        val email = TextView(this).apply {
            text = "Email: ${
                customerEmail.ifBlank { "Not provided" }
            }"
            textSize = 14f
        }

        val price = TextView(this).apply {
            text = "Total: £${String.format("%.2f", totalPrice)}"
            textSize = 16f
        }

        val current = TextView(this).apply {
            text = "Current status: $currentStatus"
            textSize = 16f
        }
        val preparationTimeInput = EditText(this).apply {
            hint = "Enter preparation time (minutes)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
        val startPreparingButton = Button(this).apply {
            text = "Start Preparing"
        }
        val countdownText = TextView(this).apply {
            text = "Preparation timer not started"
            textSize = 18f
            setTextColor(0xFF4E342E.toInt())
        }

        val readyButton = Button(this).apply {
            text = "Mark as Ready"
            visibility = View.GONE
        }

        val statusSpinner = Spinner(this)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statuses
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        statusSpinner.adapter = adapter

        val position = statuses.indexOf(currentStatus)

        if (position >= 0) {
            statusSpinner.setSelection(position)
        }

        val updateButton = Button(this).apply {
            text = "Save New Status"
        }

        updateButton.setOnClickListener {

            val newStatus =
                statusSpinner.selectedItem.toString()

            if (newStatus == currentStatus) {
                Toast.makeText(
                    this@UpdateOrderStatusActivity,
                    "Please select a different status.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            updateStatus(
                orderId,
                customerName,
                customerEmail,
                newStatus,
                updateButton
            )
        }

        card.addView(title)
        card.addView(customer)
        card.addView(email)
        card.addView(price)
        card.addView(current)
        card.addView(statusSpinner)
        card.addView(updateButton)
        card.addView(preparationTimeInput)
        card.addView(startPreparingButton)
        card.addView(countdownText)
        card.addView(readyButton)
        readyButton.setOnClickListener {
            updateStatus(
                orderId,
                customerName,
                customerEmail,
                "Ready",
                readyButton
            )
            startPreparingButton.setOnClickListener {

                val minutes = preparationTimeInput.text.toString().toLongOrNull()

                if (minutes == null || minutes <= 0) {
                    Toast.makeText(
                        this,
                        "Please enter a valid preparation time.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val endTime = System.currentTimeMillis() + (minutes * 60 * 1000)

                startPreparingButton.isEnabled = false
                startPreparingButton.text = "Starting..."

                db.collection("orders")
                    .document(orderId)
                    .update(
                        mapOf(
                            "status" to "Preparing",
                            "preparationEndTime" to endTime
                        )
                    )
                    .addOnSuccessListener {

                        NotificationHelper(this)
                            .showOrderStatusNotification(orderId, "Preparing")

                        if (customerEmail.isNotBlank()) {
                            sendCustomerEmail(
                                customerEmail,
                                customerName,
                                orderId,
                                "Preparing"
                            )
                        }

                        Toast.makeText(
                            this,
                            "Order is now being prepared!",
                            Toast.LENGTH_SHORT
                        ).show()

                        loadOrders()
                    }
                    .addOnFailureListener { error ->
                        startPreparingButton.isEnabled = true
                        startPreparingButton.text = "Start Preparing"

                        Toast.makeText(
                            this,
                            "Could not start timer: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }

            displayOrder(
                orderId,
                customerName,
                customerEmail,
                totalPrice,
                currentStatus,
                preparationEndTime
            )
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 24)

        ordersStatusContainer.addView(card, params)
    }

    private fun updateStatus(
        orderId: String,
        customerName: String,
        customerEmail: String,
        newStatus: String,
        button: Button
    ) {

        button.isEnabled = false
        button.text = "Updating..."

        db.collection("orders")
            .document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Order status updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Existing Android order notification
                NotificationHelper(this)
                    .showOrderStatusNotification(orderId, newStatus)

                // Prepare customer email
                if (customerEmail.isNotBlank()) {
                    sendCustomerEmail(
                        customerEmail,
                        customerName,
                        orderId,
                        newStatus
                    )
                } else {
                    Toast.makeText(
                        this,
                        "Order updated, but customer email is missing.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                loadOrders()
            }
            .addOnFailureListener { error ->

                button.isEnabled = true
                button.text = "Save New Status"

                Toast.makeText(
                    this,
                    "Update failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun sendCustomerEmail(
        customerEmail: String,
        customerName: String,
        orderId: String,
        newStatus: String
    ) {

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(customerEmail).matches()
        ) {
            Toast.makeText(
                this,
                "Customer email address is invalid.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val subject = "Smart Café - Order Status Update"

        val message = """
            Hello $customerName,

            Your Smart Café order has been updated.

            Order ID: $orderId
            New status: $newStatus

            Thank you for choosing Smart Café!

            Smart Café Team
        """.trimIndent()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(
                "mailto:${Uri.encode(customerEmail)}"
            )

            putExtra(Intent.EXTRA_EMAIL, arrayOf(customerEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    "Send customer order update"
                )
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No email application is available.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
