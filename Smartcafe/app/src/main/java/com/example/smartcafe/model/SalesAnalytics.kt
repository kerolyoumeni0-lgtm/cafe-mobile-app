
package com.example.smartcafe.models

// Represents the sales analytics information.
data class SalesAnalytics(

    // Total revenue from completed orders.
    val totalSales: Double = 0.0,

    // Total number of orders.
    val totalOrders: Int = 0,

    // Number of completed orders.
    val completedOrders: Int = 0,

    // Average value of completed orders.
    val averageOrderValue: Double = 0.0,

    // List of popular products and their order counts.
    val popularProducts: List<ProductCount> = emptyList(),

    // Product recommendation generated from order data.
    val recommendation: String = "",

    // Sales information for the last seven days.
    val weeklySales: List<DailySales> = emptyList()
)

// Represents a product and how many times it was ordered.
data class ProductCount(
    val productName: String = "",
    val orderCount: Int = 0
)

// Represents sales for one particular day.
data class DailySales(
    val day: String = "",
    val salesAmount: Double = 0.0
)
