
package com.example.smartcafe.repositories

import com.example.smartcafe.models.DailySales
import com.example.smartcafe.models.ProductCount
import com.example.smartcafe.models.SalesAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Handles Firebase operations and sales calculations.
class SalesAnalyticsRepository {

    private val db = FirebaseFirestore.getInstance()

    // Retrieve orders and calculate the sales analytics.
    fun getSalesAnalytics(
        onSuccess: (SalesAnalytics) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        db.collection("orders")
            .get()
            .addOnSuccessListener { documents ->

                var totalSales = 0.0
                var completedOrders = 0

                // Count how many times each product was ordered.
                val productCounts = mutableMapOf<String, Int>()

                for (document in documents) {

                    val status = document.getString("status") ?: ""

                    val price =
                        (document.get("totalPrice") as? Number)?.toDouble()

                    // Include valid completed orders in total sales.
                    if (price != null && price.isFinite() && price >= 0) {

                        if (status.equals("Completed", ignoreCase = true)) {
                            totalSales += price
                            completedOrders++
                        }
                    }

                    // Count products from all recorded orders.
                    val items = document.getString("items") ?: ""

                    items.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .forEach { item ->
                            productCounts[item] =
                                (productCounts[item] ?: 0) + 1
                        }
                }

                // Calculate the total number of orders.
                val totalOrders = documents.size()

                // Calculate the average completed order value.
                val averageOrderValue =
                    if (completedOrders > 0) {
                        totalSales / completedOrders
                    } else {
                        0.0
                    }

                // Sort products from most popular to least popular.
                val popularProducts = productCounts.entries
                    .sortedByDescending { it.value }
                    .map { entry ->
                        ProductCount(
                            productName = entry.key,
                            orderCount = entry.value
                        )
                    }

                // Generate a recommendation based on the most popular product.
                val recommendation =
                    if (popularProducts.isEmpty()) {
                        "Not enough customer order data to generate a recommendation."
                    } else {
                        val product = popularProducts.first()

                        "Recommended Product: ${product.productName}\n\n" +
                                "This product has been ordered " +
                                "${product.orderCount} times.\n\n" +
                                "Recommendation reason: It is currently the most popular " +
                                "product based on recorded customer orders."
                    }

                // Calculate sales for the last seven days.
                val weeklySales = calculateWeeklySales(
                    documents.documents
                )

                // Create the final analytics result.
                val analytics = SalesAnalytics(
                    totalSales = totalSales,
                    totalOrders = totalOrders,
                    completedOrders = completedOrders,
                    averageOrderValue = averageOrderValue,
                    popularProducts = popularProducts,
                    recommendation = recommendation,
                    weeklySales = weeklySales
                )

                onSuccess(analytics)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Calculate completed sales for the last seven days.
    private fun calculateWeeklySales(
        orders: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<DailySales> {

        val calendar = Calendar.getInstance()

        // Set the calendar to the beginning of today.
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dailySales = mutableMapOf<Int, Double>()
        val dayCalendars = mutableListOf<Calendar>()

        // Prepare the last seven days, including today.
        for (i in 6 downTo 0) {

            val day = calendar.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, -i)

            dayCalendars.add(day)

            val key = day.get(Calendar.YEAR) * 1000 +
                    day.get(Calendar.DAY_OF_YEAR)

            dailySales[key] = 0.0
        }

        // Calculate completed sales for each day.
        for (order in orders) {

            val status = order.getString("status") ?: ""

            if (!status.equals("Completed", ignoreCase = true)) {
                continue
            }

            val timestamp = order.getTimestamp("createdAt") ?: continue

            val price =
                (order.get("totalPrice") as? Number)?.toDouble()
                    ?: continue

            if (!price.isFinite() || price < 0) continue

            val orderDate = Calendar.getInstance()
            orderDate.time = timestamp.toDate()

            val key = orderDate.get(Calendar.YEAR) * 1000 +
                    orderDate.get(Calendar.DAY_OF_YEAR)

            if (dailySales.containsKey(key)) {
                dailySales[key] =
                    (dailySales[key] ?: 0.0) + price
            }
        }

        // Convert the daily totals into DailySales model objects.
        return dayCalendars.map { day ->

            val key = day.get(Calendar.YEAR) * 1000 +
                    day.get(Calendar.DAY_OF_YEAR)

            DailySales(
                day = SimpleDateFormat("EEE", Locale.UK)
                    .format(day.time),
                salesAmount = dailySales[key] ?: 0.0
            )
        }
    }
}
