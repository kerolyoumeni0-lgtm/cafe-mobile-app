
package com.example.smartcafe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.viewmodels.OrderViewModel
import com.google.firebase.auth.FirebaseAuth

class UpdateOrderStatusActivity : AppCompatActivity() {

    // Connect the Activity to the ViewModel.
    private lateinit var orderViewModel: OrderViewModel

    // Store active preparation countdowns.
    private val activeTimers = mutableListOf<CountDownTimer>()

    // Temporary callbacks for successful and failed operations.
    private var pendingSuccessAction: (() -> Unit)? = null
    private var pendingFailureAction: (() -> Unit)? = null

    private lateinit var ordersStatusContainer: LinearLayout
    private lateinit var txtEmptyStatus: TextView

    // Available order statuses.
    private val statuses = arrayOf(
        "Pending",
        "Preparing",
        "Ready",
        "Completed"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_order_status)

        // Initialise the ViewModel.
        orderViewModel =
            ViewModelProvider(this)[OrderViewModel::class.java]

        ordersStatusContainer =
            findViewById(R.id.ordersStatusContainer)

        txtEmptyStatus =
            findViewById(R.id.txtEmptyStatus)

        // Observe orders retrieved from Firestore.
        orderViewModel.orders.observe(this) { orders ->

            ordersStatusContainer.removeAllViews()

            if (orders.isEmpty()) {
                txtEmptyStatus.visibility = View.VISIBLE
                return@observe
            }

            txtEmptyStatus.visibility = View.GONE

            // Display each order.
            for (order in orders) {
                displayOrder(
                    order.id,
                    order.customerName,
                    order.customerEmail,
                    order.totalPrice,
                    order.status,
                    order.preparationEndTime
                )
            }
        }

        // Observe operation messages.
        orderViewModel.message.observe(this) { message ->

            if (message.isNullOrEmpty()) return@observe

            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()

            when {
                message.startsWith("Order status updated successfully") ||
                        message.startsWith("Order is now being prepared") -> {

                    pendingSuccessAction?.invoke()
                    pendingSuccessAction = null
                    pendingFailureAction = null
                }

                message.startsWith("Update failed") ||
                        message.startsWith("Could not start timer") -> {

                    pendingFailureAction?.invoke()
                    pendingFailureAction = null
                    pendingSuccessAction = null
                }
            }
        }

        // Check that an administrator is logged in.
        if (FirebaseAuth.getInstance().currentUser == null) {

            Toast.makeText(
                this,
                "Please log in first.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        // Load orders through the ViewModel.
        loadOrders()
    }

    // Retrieve orders through MVVM.
    private fun loadOrders() {
        orderViewModel.loadOrders()
    }

    // Display an individual order.
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
            text = "Email: ${customerEmail.ifBlank { "Not provided" }}"
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

        // Preparation time input.
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

        // Order status dropdown.
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

        // Update order status.
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
                updateButton,
                "Save New Status"
            )
        }

        // Add information to the card.
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

        // Mark the order as Ready.
        readyButton.setOnClickListener {

            updateStatus(
                orderId,
                customerName,
                customerEmail,
                "Ready",
                readyButton,
                "Mark as Ready"
            )
        }

        // Start the preparation timer.
        startPreparingButton.setOnClickListener {

            val minutes =
                preparationTimeInput.text.toString().toLongOrNull()

            if (minutes == null || minutes <= 0) {
                Toast.makeText(
                    this,
                    "Please enter a valid preparation time.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val endTime =
                System.currentTimeMillis() + (minutes * 60 * 1000)

            startPreparingButton.isEnabled = false
            startPreparingButton.text = "Starting..."

            // Ask the ViewModel to update Firestore.
            pendingSuccessAction = {

                NotificationHelper(this)
                    .showOrderStatusNotification(
                        orderId,
                        "Preparing"
                    )

                if (customerEmail.isNotBlank()) {
                    sendCustomerEmail(
                        customerEmail,
                        customerName,
                        orderId,
                        "Preparing"
                    )
                }

                loadOrders()
            }

            pendingFailureAction = {
                startPreparingButton.isEnabled = true
                startPreparingButton.text = "Start Preparing"
            }

            orderViewModel.startPreparingOrder(
                orderId,
                endTime
            )
        }

        // Restore the countdown for an order already being prepared.
        if (currentStatus == "Preparing" &&
            preparationEndTime != null
        ) {

            preparationTimeInput.visibility = View.GONE
            startPreparingButton.visibility = View.GONE

            startCountdown(
                orderId,
                preparationEndTime,
                countdownText,
                readyButton,
                startPreparingButton
            )
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 24)

        ordersStatusContainer.addView(card, params)
    }

    // Start and manage the preparation countdown.
    private fun startCountdown(
        orderId: String,
        endTime: Long,
        countdownText: TextView,
        readyButton: Button,
        startPreparingButton: Button
    ) {

        val remainingTime =
            endTime - System.currentTimeMillis()

        if (remainingTime <= 0) {

            countdownText.text = "Preparation time is finished!"
            readyButton.visibility = View.VISIBLE
            startPreparingButton.visibility = View.GONE

            return
        }

        val timer = object : CountDownTimer(remainingTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                countdownText.text = String.format(
                    "Time remaining: %02d:%02d",
                    minutes,
                    seconds
                )
            }

            override fun onFinish() {

                countdownText.text =
                    "Time is up! Your order is ready to check."

                readyButton.visibility = View.VISIBLE
                startPreparingButton.visibility = View.GONE

                NotificationHelper(this@UpdateOrderStatusActivity)
                    .showOrderStatusNotification(
                        orderId,
                        "Preparation time finished"
                    )
            }
        }

        activeTimers.add(timer)
        timer.start()
    }

    // Update an order's status through the ViewModel.
    private fun updateStatus(
        orderId: String,
        customerName: String,
        customerEmail: String,
        newStatus: String,
        button: Button,
        originalButtonText: String
    ) {

        button.isEnabled = false
        button.text = "Updating..."

        // Actions to perform after a successful update.
        pendingSuccessAction = {

            NotificationHelper(this)
                .showOrderStatusNotification(
                    orderId,
                    newStatus
                )

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

        // Restore the button if the update fails.
        pendingFailureAction = {
            button.isEnabled = true
            button.text = originalButtonText
        }

        // Send the update request to the ViewModel.
        orderViewModel.updateOrderStatus(
            orderId,
            newStatus
        )
    }

    // Prepare an email for the customer.
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

            putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(customerEmail)
            )

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

    // Cancel active timers when the Activity is destroyed.
    override fun onDestroy() {
        super.onDestroy()

        activeTimers.forEach { timer ->
            timer.cancel()
        }

        activeTimers.clear()
    }
}
