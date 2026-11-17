
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
            startActivity(
                Intent(this, AddEditMenuActivity::class.java)
            )
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

                    val itemId = document.id
                    val name = document.getString("name") ?: "Unnamed item"
                    val description =
                        document.getString("description") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val category =
                        document.getString("category") ?: ""
                    val available =
                        document.getBoolean("available") ?: false

                    // Menu item card
                    val itemCard = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(20, 20, 20, 20)
                        setBackgroundColor(android.graphics.Color.WHITE)

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.bottomMargin = 16
                        layoutParams = params
                    }

                    val txtName = TextView(this).apply {
                        text = name
                        textSize = 19f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }

                    val txtDescription = TextView(this).apply {
                        text = description
                        textSize = 14f
                    }

                    val txtPrice = TextView(this).apply {
                        text = "£${"%.2f".format(price)}"
                        textSize = 16f
                    }

                    val txtAvailability = TextView(this).apply {
                        text = if (available) "Available" else "Unavailable"
                        textSize = 14f
                    }

                    val buttonRow = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }

                    val btnEdit = Button(this).apply {
                        text = "Edit"
                        setOnClickListener {
                            val intent = Intent(
                                this@ManageMenuActivity,
                                AddEditMenuActivity::class.java
                            )
                            intent.putExtra("itemId", itemId)
                            startActivity(intent)
                        }
                    }

                    val btnDelete = Button(this).apply {
                        text = "Delete"
                        setOnClickListener {
                            deleteMenuItem(itemId)
                        }
                    }

                    buttonRow.addView(btnEdit)
                    buttonRow.addView(btnDelete)

                    itemCard.addView(txtName)
                    itemCard.addView(txtDescription)
                    itemCard.addView(txtPrice)
                    itemCard.addView(txtAvailability)
                    itemCard.addView(buttonRow)

                    menuItemsContainer.addView(itemCard)
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

    private fun deleteMenuItem(itemId: String) {

        db.collection("MenuItems")
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Menu item deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

                loadMenuItems()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Delete failed: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
