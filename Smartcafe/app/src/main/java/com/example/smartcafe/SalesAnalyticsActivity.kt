
package com.example.smartcafe

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class SalesAnalyticsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var txtTotalSales: TextView
    private lateinit var txtTotalOrders: TextView
    private lateinit var txtCompletedOrders: TextView
    private lateinit var txtAverageOrderValue: TextView
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

}
