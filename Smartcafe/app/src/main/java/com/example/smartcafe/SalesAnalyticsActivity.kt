
package com.example.smartcafe

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout.LayoutParams
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class SalesAnalyticsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var txtTotalSales: TextView
    private lateinit var txtTotalOrders: TextView
    private lateinit var txtCompletedOrders: TextView
    private lateinit var txtAverageOrderValue: TextView
    private lateinit var weeklySalesChart: LinearLayout
    private lateinit var topProductsContainer: LinearLayout
    private lateinit var txtRecommendation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_analytics)

        db = FirebaseFirestore.getInstance()

        txtTotalSales = findViewById(R.id.txtTotalSales)
        txtTotalOrders = findViewById(R.id.txtTotalOrders)
        txtCompletedOrders = findViewById(R.id.txtCompletedOrders)
        txtAverageOrderValue = findViewById(R.id.txtAverageOrderValue)
        topProductsContainer = findViewById(R.id.topProductsContainer)
        txtRecommendation = findViewById(R.id.txtRecommendation)
        weeklySalesChart = findViewById(R.id.weeklySalesChart)

        loadAnalytics()
    }

    private fun loadAnalytics() {

        db.collection("orders")
            .get()
            .addOnSuccessListener { documents ->

                var totalSales = 0.0
                var completedOrders = 0

                val productCounts = mutableMapOf<String, Int>()

                for (document in documents) {

                    val status =
                        document.getString("status") ?: ""

                    val price =
                        (document.get("totalPrice") as? Number)?.toDouble()

                    if (price != null && price.isFinite() && price >= 0) {
                        if (status.equals("Completed", ignoreCase = true)) {
                            totalSales += price
                            completedOrders++
                        }
                    }

                    val items = document.getString("items") ?: ""

                    items.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .forEach { item ->
                            productCounts[item] =
                                (productCounts[item] ?: 0) + 1
                        }
                }

                val totalOrders = documents.size()

                val averageOrderValue =
                    if (completedOrders > 0) {
                        totalSales / completedOrders
                    } else {
                        0.0
                    }

                txtTotalSales.text = formatMoney(totalSales)
                txtTotalOrders.text = totalOrders.toString()
                txtCompletedOrders.text = completedOrders.toString()
                txtAverageOrderValue.text =
                    formatMoney(averageOrderValue)

                displayPopularProducts(productCounts)
                generateRecommendation(productCounts)
                generateWeeklySalesChart(documents.documents)

            }
            .addOnFailureListener { exception ->

                Toast.makeText(
                    this,
                    "Unable to load analytics. Check your connection.",
                    Toast.LENGTH_LONG
                ).show()

                txtRecommendation.text =
                    "Analytics are currently unavailable. Please try again."

                android.util.Log.e(
                    "SalesAnalytics",
                    "Firestore error",
                    exception
                )
            }
    }

    private fun formatMoney(amount: Double): String {
        return String.format(Locale.UK, "£%.2f", amount)
    }

    private fun displayPopularProducts(
        productCounts: Map<String, Int>
    ) {

        topProductsContainer.removeAllViews()

        if (productCounts.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No product data available yet."
                textSize = 16f
            }

            topProductsContainer.addView(emptyText)
            return
        }

        val sortedProducts = productCounts.entries
            .sortedByDescending { it.value }

        for ((product, count) in sortedProducts) {

            val productText = TextView(this).apply {
                text = "$product  -  $count orders"
                textSize = 17f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(16, 20, 16, 20)
                gravity = Gravity.CENTER_VERTICAL
            }

            topProductsContainer.addView(productText)
        }
    }

    private fun generateRecommendation(productCounts: Map<String, Int>) {

        if (productCounts.isEmpty()) {
            txtRecommendation.text =
                "Not enough customer order data to generate a recommendation."
            return
        }

        val recommendedProduct = productCounts.maxByOrNull {
            it.value
        }

        if (recommendedProduct != null) {

            txtRecommendation.text =
                "Recommended Product: ${recommendedProduct.key}\n\n" +
                        "This product has been ordered " +
                        "${recommendedProduct.value} times.\n\n" +
                        "Recommendation reason: It is currently the most popular " +
                        "product based on recorded customer orders."
        }
    }

    private fun generateWeeklySalesChart(
        orders: List<com.google.firebase.firestore.DocumentSnapshot>
    ) {
        weeklySalesChart.removeAllViews()

        // Prepare the last seven days, including today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dailySales = mutableMapOf<Int, Double>()
        val dayCalendars = mutableListOf<Calendar>()

        for (i in 6 downTo 0) {
            val day = calendar.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, -i)
            dayCalendars.add(day)

            val key = day.get(Calendar.YEAR) * 1000 +
                    day.get(Calendar.DAY_OF_YEAR)

            dailySales[key] = 0.0
        }

        // Calculate completed sales for each day
        for (order in orders) {

            val status = order.getString("status") ?: ""
            if (!status.equals("Completed", ignoreCase = true)) continue

            val timestamp = order.getTimestamp("createdAt") ?: continue
            val price = (order.get("totalPrice") as? Number)?.toDouble()
                ?: continue

            if (!price.isFinite() || price < 0) continue

            val orderDate = Calendar.getInstance()
            orderDate.time = timestamp.toDate()

            val key = orderDate.get(Calendar.YEAR) * 1000 +
                    orderDate.get(Calendar.DAY_OF_YEAR)

            if (dailySales.containsKey(key)) {
                dailySales[key] = (dailySales[key] ?: 0.0) + price
            }
        }

        val maxSales = dailySales.values.maxOrNull() ?: 0.0

        // Create one column for each day
        for (day in dayCalendars) {

            val key = day.get(Calendar.YEAR) * 1000 +
                    day.get(Calendar.DAY_OF_YEAR)

            val sales = dailySales[key] ?: 0.0

            val dayColumn = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                setPadding(2, 0, 2, 0)
            }

            // Sales amount above the bar
            val amountText = TextView(this).apply {
                text = String.format(Locale.UK, "£%.0f", sales)
                textSize = 10f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
            }

            dayColumn.addView(amountText)

            // Calculate bar height
            val barHeight = if (maxSales > 0) {
                ((sales / maxSales) * 120).toInt().coerceAtLeast(4)
            } else {
                4
            }

            // Draw the sales bar
            val bar = View(this).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.parseColor("#795548"))
                    cornerRadius = 8f
                }
            }

            val barParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                barHeight.dp()
            )

            barParams.setMargins(3, 6, 3, 6)
            dayColumn.addView(bar, barParams)

            // Day name below the bar
            val dayText = TextView(this).apply {
                text = SimpleDateFormat("EEE", Locale.UK)
                    .format(day.time)
                textSize = 11f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
            }

            dayColumn.addView(dayText)

            weeklySalesChart.addView(dayColumn)
        }
    }

    // Convert dp into pixels
    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }


}
