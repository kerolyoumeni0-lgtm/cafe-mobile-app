
package com.example.smartcafe

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

class UpdateOrderStatusActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

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

                    // Support both field names while old data is being corrected.
                    val customerName =
                        document.getString("customerName")
                            ?: document.getString("customer_id")
                            ?: "Unknown customer"

                    val totalPrice =
                        (document.get("totalPrice") as? Number)
                            ?.toDouble() ?: 0.0

                    val currentStatus =
                        document.getString("status") ?: "Pending"

                    displayOrder(
                        orderId,
                        customerName,
                        totalPrice,
                        currentStatus
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
        totalPrice: Double,
        currentStatus: String
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

        val price = TextView(this).apply {
            text = "Total: £${String.format("%.2f", totalPrice)}"
            textSize = 16f
        }

        val current = TextView(this).apply {
            text = "Current status: $currentStatus"
            textSize = 16f
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

            val newStatus = statusSpinner.selectedItem.toString()

            if (newStatus == currentStatus) {
                Toast.makeText(
                    this@UpdateOrderStatusActivity,
                    "Please select a different status.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            updateStatus(orderId, newStatus, updateButton)
        }

        card.addView(title)
        card.addView(customer)
        card.addView(price)
        card.addView(current)
        card.addView(statusSpinner)
        card.addView(updateButton)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 24)

        ordersStatusContainer.addView(card, params)
    }

    private fun updateStatus(
        orderId: String,
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
}
