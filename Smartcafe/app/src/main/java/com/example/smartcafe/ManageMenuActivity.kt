
package com.example.smartcafe

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.models.MenuItem
import com.example.smartcafe.viewmodels.MenuViewModel

class ManageMenuActivity : AppCompatActivity() {

    // Connect this Activity to the MenuViewModel.
    private lateinit var menuViewModel: MenuViewModel

    // Container used to display menu item cards.
    private lateinit var menuItemsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_menu)

        // Initialise the ViewModel.
        menuViewModel =
            ViewModelProvider(this)[MenuViewModel::class.java]

        menuItemsContainer =
            findViewById(R.id.menuItemsContainer)

        val btnAddMenuItem =
            findViewById<Button>(R.id.btnAddMenuItem)

        // Open the Add Menu Item screen.
        btnAddMenuItem.setOnClickListener {
            startActivity(
                Intent(this, AddEditMenuActivity::class.java)
            )
        }

        // Observe the menu items returned by the ViewModel.
        menuViewModel.menuItems.observe(this) { items ->
            displayMenuItems(items)
        }

        // Observe messages from the ViewModel.
        menuViewModel.message.observe(this) { message ->

            if (message.isNullOrEmpty()) return@observe

            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()

            // Reload the list after a successful deletion.
            if (message.startsWith("Menu item deleted successfully")) {
                menuViewModel.loadAllMenuItems()
            }
        }
    }

    // Refresh the menu whenever this screen becomes visible.
    override fun onResume() {
        super.onResume()
        menuViewModel.loadAllMenuItems()
    }

    // Display the menu items returned by the ViewModel.
    private fun displayMenuItems(
        items: List<Pair<String, MenuItem>>
    ) {

        menuItemsContainer.removeAllViews()

        // Show a message if there are no menu items.
        if (items.isEmpty()) {

            val emptyMessage = TextView(this).apply {
                text = "No menu items yet. Add your first item!"
                textSize = 15f
                gravity = Gravity.CENTER
                setPadding(20, 40, 20, 40)
            }

            menuItemsContainer.addView(emptyMessage)
            return
        }

        // Create a card for every menu item.
        for ((itemId, menuItem) in items) {

            val itemCard = LinearLayout(this).apply {
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

            // Display the menu item name.
            val txtName = TextView(this).apply {
                text = menuItem.name.ifEmpty { "Unnamed item" }
                textSize = 19f
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

            // Display the description.
            val txtDescription = TextView(this).apply {
                text = menuItem.description
                textSize = 14f
            }

            // Display the price.
            val txtPrice = TextView(this).apply {
                text = "£${"%.2f".format(menuItem.price)}"
                textSize = 16f
            }

            // Display availability.
            val txtAvailability = TextView(this).apply {
                text = if (menuItem.available) {
                    "Available"
                } else {
                    "Unavailable"
                }

                textSize = 14f
            }

            // Create a row for the Edit and Delete buttons.
            val buttonRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            // Edit button.
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

            // Delete button.
            val btnDelete = Button(this).apply {
                text = "Delete"

                setOnClickListener {
                    deleteMenuItem(itemId)
                }
            }

            // Add buttons to their row.
            buttonRow.addView(btnEdit)
            buttonRow.addView(btnDelete)

            // Add information to the card.
            itemCard.addView(txtName)
            itemCard.addView(txtDescription)
            itemCard.addView(txtPrice)
            itemCard.addView(txtAvailability)
            itemCard.addView(buttonRow)

            // Display the completed card.
            menuItemsContainer.addView(itemCard)
        }
    }

    // Ask the ViewModel to delete a menu item.
    private fun deleteMenuItem(itemId: String) {
        menuViewModel.deleteMenuItem(itemId)
    }
}
