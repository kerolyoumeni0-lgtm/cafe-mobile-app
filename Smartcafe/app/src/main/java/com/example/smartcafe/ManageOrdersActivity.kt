
package com.example.smartcafe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.models.Order
import com.example.smartcafe.viewmodels.OrderViewModel

class ManageOrdersActivity : AppCompatActivity() {

    // Connect the Activity to the OrderViewModel.
    private lateinit var orderViewModel: OrderViewModel

    // Declare the interface components.
    private lateinit var ordersContainer: LinearLayout
    private lateinit var txtEmptyOrders: TextView
    private lateinit var edtSearchOrders: EditText
    private lateinit var spinnerOrderFilter: Spinner

    // Stores the orders displayed by the Activity.
    private var orderList = mutableListOf<Order>()

    // Available order status filters.
    private val filterOptions = arrayOf(
        "All Statuses",
        "Pending",
        "Preparing",
        "Ready",
        "Completed"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_orders)

        // Initialise the ViewModel.
        orderViewModel =
            ViewModelProvider(this)[OrderViewModel::class.java]

        // Connect Kotlin variables to XML views.
        ordersContainer = findViewById(R.id.ordersContainer)
        txtEmptyOrders = findViewById(R.id.txtEmptyOrders)
        edtSearchOrders = findViewById(R.id.edtSearchOrders)
        spinnerOrderFilter = findViewById(R.id.spinnerOrderFilter)

        setupFilter()
        setupSearch()

        // Observe orders returned by the ViewModel.
        orderViewModel.orders.observe(this) { orders ->

            orderList.clear()
            orderList.addAll(orders)

            displayFilteredOrders()
        }

        // Observe error messages from the ViewModel.
        orderViewModel.message.observe(this) { message ->

            if (!message.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Load orders through the ViewModel.
        loadOrders()
    }

    // Set up the order status dropdown.
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
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    displayFilteredOrders()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}
            }
    }

    // Set up the order search field.
    private fun setupSearch() {

        edtSearchOrders.addTextChangedListener(
            object : TextWatcher {

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
            }
        )
    }

    // Ask the ViewModel to retrieve orders.
    private fun loadOrders() {
        orderViewModel.loadOrders()
    }

    // Search and filter the retrieved orders.
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

        // Show an appropriate message if no orders match.
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

        // Display each filtered order.
        for (order in filteredOrders) {
            displayOrder(order)
        }
    }

    // Create and display an individual order card.
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

        // Add order information to the card.
        card.addView(title)
        card.addView(customer)
        card.addView(items)
        card.addView(price)
        card.addView(status)

        // Set card margins.
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 24)

        ordersContainer.addView(card, params)
    }
}
