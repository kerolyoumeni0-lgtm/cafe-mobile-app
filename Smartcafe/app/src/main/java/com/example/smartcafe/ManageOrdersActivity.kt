
package com.example.smartcafe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ManageOrdersActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var ordersContainer: LinearLayout
    private lateinit var txtEmptyOrders: TextView
    private lateinit var edtSearchOrders: EditText
    private lateinit var spinnerOrderFilter: Spinner

    private val orderList = mutableListOf<Order>()

    private val filterOptions = arrayOf(
        "All Statuses",
        "Pending",
        "Preparing",
        "Ready",
        "Completed"
    )

    data class Order(
        val id: String,
        val customerName: String,
        val items: String,
        val totalPrice: Double,
        val status: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_orders)

        ordersContainer = findViewById(R.id.ordersContainer)
        txtEmptyOrders = findViewById(R.id.txtEmptyOrders)
        edtSearchOrders = findViewById(R.id.edtSearchOrders)
        spinnerOrderFilter = findViewById(R.id.spinnerOrderFilter)

        setupFilter()
        setupSearch()
        loadOrders()
    }

    private fun setupFilter() {

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filterOptions
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerOrderFilter.adapter = adapter

        spinnerOrderFilter.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    displayFilteredOrders()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }
    }

    private fun setupSearch() {

        edtSearchOrders.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                displayFilteredOrders()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadOrders() {

        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->

                orderList.clear()

                for (document in result) {

                    val customerName =
                        document.getString("customerName")
                            ?: document.getString("customer_id")
                            ?: "Unknown customer"

                    val items =
                        document.getString("items")
                            ?: "Not provided"

                    val totalPrice =
                        (document.get("totalPrice") as? Number)
                            ?.toDouble() ?: 0.0

                    val status =
                        document.getString("status") ?: "Pending"

                    orderList.add(
                        Order(
                            document.id,
                            customerName,
                            items,
                            totalPrice,
                            status
                        )
                    )
                }

                displayFilteredOrders()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Could not load orders: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun displayFilteredOrders() {

        ordersContainer.removeAllViews()

        val searchText =
            edtSearchOrders.text.toString().trim()

        val selectedStatus =
            spinnerOrderFilter.selectedItem?.toString()
                ?: "All Statuses"

        val filteredOrders = orderList.filter { order ->

            val matchesSearch =
                order.customerName.contains(
                    searchText,
                    ignoreCase = true
                ) ||
                        order.id.contains(
                            searchText,
                            ignoreCase = true
                        )

            val matchesStatus =
                selectedStatus == "All Statuses" ||
                        order.status.equals(
                            selectedStatus,
                            ignoreCase = true
                        )

            matchesSearch && matchesStatus
        }

        if (filteredOrders.isEmpty()) {

            txtEmptyOrders.visibility = View.VISIBLE

            txtEmptyOrders.text =
                if (orderList.isEmpty()) {
                    "No orders available yet."
                } else {
                    "No orders match your search or filter."
                }

            return
        }

        txtEmptyOrders.visibility = View.GONE

        for (order in filteredOrders) {
            displayOrder(order)
        }
    }

    private fun displayOrder(order: Order) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(0xFFF0EAE5.toInt())
        }

        val title = TextView(this).apply {
            text = "Order #${order.id.take(8)}"
            textSize = 19f
            setTextColor(0xFF4E342E.toInt())
        }

        val customer = TextView(this).apply {
            text = "Customer: ${order.customerName}"
            textSize = 16f
        }

        val items = TextView(this).apply {
            text = "Items: ${order.items}"
            textSize = 15f
        }

        val price = TextView(this).apply {
            text = "Total: £${String.format("%.2f", order.totalPrice)}"
            textSize = 16f
        }

        val status = TextView(this).apply {
            text = "Status: ${order.status}"
            textSize = 16f
            setTextColor(0xFF795548.toInt())
        }

        card.addView(title)
        card.addView(customer)
        card.addView(items)
        card.addView(price)
        card.addView(status)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 24)

        ordersContainer.addView(card, params)
    }
}
