
package com.example.smartcafe

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.models.DailySales
import com.example.smartcafe.models.ProductCount
import com.example.smartcafe.models.SalesAnalytics
import com.example.smartcafe.viewmodels.SalesAnalyticsViewModel
import java.util.Locale

class SalesAnalyticsActivity : AppCompatActivity() {

    private lateinit var salesViewModel: SalesAnalyticsViewModel

    // Screen components.
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

        // Connect the Activity to the SalesAnalyticsViewModel.
        salesViewModel = ViewModelProvider(this)
            .get(SalesAnalyticsViewModel::class.java)

        // Connect the XML views to their Kotlin variables.
        txtTotalSales = findViewById(R.id.txtTotalSales)
        txtTotalOrders = findViewById(R.id.txtTotalOrders)
        txtCompletedOrders = findViewById(R.id.txtCompletedOrders)
        txtAverageOrderValue = findViewById(R.id.txtAverageOrderValue)
        topProductsContainer = findViewById(R.id.topProductsContainer)
        txtRecommendation = findViewById(R.id.txtRecommendation)
        weeklySalesChart = findViewById(R.id.weeklySalesChart)

        // Observe sales analytics returned by the ViewModel.
        salesViewModel.analytics.observe(this) { analytics ->

            if (analytics != null) {
                displayAnalytics(analytics)
            }
        }

        // Observe error messages from the ViewModel.
        salesViewModel.message.observe(this) { message ->

            if (!message.isNullOrEmpty()) {

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()

                txtRecommendation.text =
                    "Analytics are currently unavailable. Please try again."

                android.util.Log.e(
                    "SalesAnalytics",
                    message
                )
            }
        }

        // Retrieve analytics through the ViewModel.
        salesViewModel.loadAnalytics()
    }

    // Display the sales information on the dashboard.
    private fun displayAnalytics(analytics: SalesAnalytics) {

        txtTotalSales.text = formatMoney(analytics.totalSales)

        txtTotalOrders.text = analytics.totalOrders.toString()

        txtCompletedOrders.text =
            analytics.completedOrders.toString()

        txtAverageOrderValue.text =
            formatMoney(analytics.averageOrderValue)

        // Display popular products.
        displayPopularProducts(analytics.popularProducts)

        // Display the product recommendation.
        txtRecommendation.text = analytics.recommendation

        // Display the seven-day sales chart.
        generateWeeklySalesChart(analytics.weeklySales)
    }

    // Format sales amounts in UK pounds.
    private fun formatMoney(amount: Double): String {
        return String.format(Locale.UK, "£%.2f", amount)
    }

    // Display the most popular products.
    private fun displayPopularProducts(
        productCounts: List<ProductCount>
    ) {

        topProductsContainer.removeAllViews()

        // Check whether product information is available.
        if (productCounts.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No product data available yet."
                textSize = 16f
            }

            topProductsContainer.addView(emptyText)
            return
        }

        // Display products in popularity order.
        for (product in productCounts) {

            val productText = TextView(this).apply {

                text = "${product.productName}  -  " +
                        "${product.orderCount} orders"

                textSize = 17f
                setTextColor(Color.DKGRAY)
                setPadding(16, 20, 16, 20)
                gravity = Gravity.CENTER_VERTICAL
            }

            topProductsContainer.addView(productText)
        }
    }

    // Display the seven-day sales chart.
    private fun generateWeeklySalesChart(
        weeklySales: List<DailySales>
    ) {

        weeklySalesChart.removeAllViews()

        // Find the highest sales amount for chart scaling.
        val maxSales = weeklySales
            .maxOfOrNull { it.salesAmount } ?: 0.0

        // Create one column for each day.
        for (day in weeklySales) {

            val dayColumn = LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                gravity = Gravity.BOTTOM or
                        Gravity.CENTER_HORIZONTAL

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )

                setPadding(2, 0, 2, 0)
            }

            // Display the sales amount above the bar.
            val amountText = TextView(this).apply {

                text = String.format(
                    Locale.UK,
                    "£%.0f",
                    day.salesAmount
                )

                textSize = 10f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
            }

            dayColumn.addView(amountText)

            // Calculate the height of the sales bar.
            val barHeight = if (maxSales > 0) {

                ((day.salesAmount / maxSales) * 120)
                    .toInt()
                    .coerceAtLeast(4)

            } else {
                4
            }

            // Draw the sales bar.
            val bar = View(this).apply {

                background = GradientDrawable().apply {
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

            // Display the day name below the bar.
            val dayText = TextView(this).apply {

                text = day.day
                textSize = 11f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
            }

            dayColumn.addView(dayText)

            // Add the completed column to the chart.
            weeklySalesChart.addView(dayColumn)
        }
    }

    // Convert dp into pixels.
    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
