
package com.example.smartcafe

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ManageOrdersActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var ordersContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_orders)

        ordersContainer = findViewById(R.id.ordersContainer)

        loadOrders()
    }

    private fun loadOrders() {

        db.collection("Orders")
            .get()
            .addOnSuccessListener { result ->

                ordersContainer.removeAllViews()

                if (result.isEmpty) {
                    val emptyMessage = TextView(this).apply {
                        text = "No orders available yet."
                        textSize = 15f
                        gravity = Gravity.CENTER
                        setPadding(20, 40, 20, 40)
                    }

                    ordersContainer.addView(emptyMessage)
                    return@addOnSuccessListener
                }

                for (document in result) {

                    val orderId = document.id

                    val customerName =
                        document.getString("customerName")
                            ?: "Unknown Customer"

                    val totalPrice =
                        document.getDouble("totalPrice") ?: 0.0

                    val status =
                        document.getString("status") ?: "Pending"

                    // Create an order card
                    val orderCard = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(20, 20, 20, 20)

                        setBackgroundColor(
                            android.graphics.Color.WHITE
                        )

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        params.bottomMargin = 16
                        layoutParams = params
                    }

                    val txtOrderId = TextView(this).apply {
                        text = "Order ID: ${orderId.take(8)}"
                        textSize = 17f
                        setTypeface(
                            null,
                            android.graphics.Typeface.BOLD
                        )
                    }

                    val txtCustomer = TextView(this).apply {
                        text = "Customer: $customerName"
                        textSize = 15f
                    }

                    val txtPrice = TextView(this).apply {
                        text = "Total: £${"%.2f".format(totalPrice)}"
                        textSize = 15f
                    }

                    val txtStatus = TextView(this).apply {
                        text = "Status: $status"
                        textSize = 15f
                    }

                    orderCard.addView(txtOrderId)
                    orderCard.addView(txtCustomer)
                    orderCard.addView(txtPrice)
                    orderCard.addView(txtStatus)

                    ordersContainer.addView(orderCard)
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Could not load orders: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
