
package com.example.smartcafe.models

// Represents a menu item stored in Firebase Firestore.
data class MenuItem(

    // Name of the food or drink.
    val name: String = "",

    // Description of the menu item.
    val description: String = "",

    // Price of the menu item.
    val price: Double = 0.0,

    // Category: Food, Drinks or Desserts.
    val category: String = "",

    // Indicates whether the item is available.
    val available: Boolean = true,

    // URL of the menu item's image.
    val imageUrl: String = ""
)
