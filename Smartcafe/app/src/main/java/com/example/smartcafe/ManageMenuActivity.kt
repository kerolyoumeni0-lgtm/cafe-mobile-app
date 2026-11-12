
package com.example.smartcafe

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ManageMenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var menuItemsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_menu)

        menuItemsContainer =
            findViewById(R.id.menuItemsContainer)

        val btnAddMenuItem =
            findViewById<Button>(R.id.btnAddMenuItem)

        btnAddMenuItem.setOnClickListener {
            val intent = Intent(
                this,
                AddEditMenuActivity::class.java
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMenuItems()
    }

    private fun loadMenuItems() {

        db.collection("MenuItems")
            .get()
            .addOnSuccessListener { result ->

                menuItemsContainer.removeAllViews()

                if (result.isEmpty) {
                    val emptyMessage = TextView(this).apply {
                        text = "No menu items yet. Add your first item!"
                        textSize = 15f
                        gravity = Gravity.CENTER
                        setPadding(20, 40, 20, 40)
                    }

                    menuItemsContainer.addView(emptyMessage)
                    return@addOnSuccessListener
                }

                for (document in result) {

                    val name = document.getString("name") ?: "Unnamed item"
                    val description =
                        document.getString("description") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val category =
                        document.getString("category") ?: ""
                    val available =
                        document.getBoolean("available") ?: false

                    val itemView = LinearLayout(this).apply {
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

                    val itemName = TextView(this).apply {
                        text = name
                        textSize = 19f
                        setTextColor(
                            android.graphics.Color.rgb(62, 39, 35)
                        )
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }

                    val itemDescription = TextView(this).apply {
                        text = description
                        textSize = 14f
                    }

                    val itemPrice = TextView(this).apply {
                        text = "£${"%.2f".format(price)}"
                        textSize = 16f
                    }

                    val itemCategory = TextView(this).apply {
                        text = "Category: $category"
                        textSize = 14f
                    }

                    val itemAvailability = TextView(this).apply {
                        text = if (available) {
                            "Available"
                        } else {
                            "Unavailable"
                        }
                        textSize = 14f
                    }

                    itemView.addView(itemName)
                    itemView.addView(itemDescription)
                    itemView.addView(itemPrice)
                    itemView.addView(itemCategory)
                    itemView.addView(itemAvailability)

                    menuItemsContainer.addView(itemView)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Could not load menu: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

