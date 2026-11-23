
package com.example.smartcafe

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.models.MenuItem
import com.example.smartcafe.viewmodels.MenuViewModel

class AddEditMenuActivity : AppCompatActivity() {

    // Connect the Activity to its ViewModel.
    private lateinit var menuViewModel: MenuViewModel

    // Stores the ID of the menu item being edited.
    private var itemId: String? = null

    // Declare the interface components.
    private lateinit var edtItemName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtPrice: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchAvailable: Switch
    private lateinit var btnSelectImage: Button
    private lateinit var btnSaveMenuItem: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_menu)

        // Initialise the ViewModel.
        menuViewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        // Connect Kotlin variables to XML views.
        edtItemName = findViewById(R.id.edtItemName)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        switchAvailable = findViewById(R.id.switchAvailable)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSaveMenuItem = findViewById(R.id.btnSaveMenuItem)

        // Define menu categories.
        val categories = arrayOf("Food", "Drinks", "Desserts")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerCategory.adapter = adapter

        // Observe the menu item returned by the ViewModel.
        menuViewModel.menuItem.observe(this) { menuItem ->
            if (menuItem != null) {
                displayMenuItem(menuItem)
            }
        }

        // Observe messages returned by the ViewModel.
        menuViewModel.message.observe(this) { message ->

            if (message.isNullOrEmpty()) return@observe

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            when {
                message.startsWith("Menu item added successfully") ||
                        message.startsWith("Menu item updated successfully") -> {
                    finish()
                }

                message == "Menu item not found." -> {
                    finish()
                }

                else -> {
                    btnSaveMenuItem.isEnabled = true
                    btnSaveMenuItem.text =
                        if (itemId == null) "Save Menu Item"
                        else "Update Menu Item"
                }
            }
        }

        // Observe loading status.
        menuViewModel.isLoading.observe(this) { loading ->
            if (loading) {
                btnSaveMenuItem.isEnabled = false
                btnSaveMenuItem.text = "Please wait..."
            } else {
                btnSaveMenuItem.isEnabled = true
                btnSaveMenuItem.text =
                    if (itemId == null) "Save Menu Item"
                    else "Update Menu Item"
            }
        }

        // Handle image selection.
        btnSelectImage.setOnClickListener {
            Toast.makeText(
                this,
                "Food images will be added later.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Check whether the Activity is adding or editing an item.
        itemId = intent.getStringExtra("itemId")

        if (itemId != null) {
            btnSaveMenuItem.text = "Update Menu Item"
            loadMenuItem()
        }

        // Save the menu item when the button is clicked.
        btnSaveMenuItem.setOnClickListener {
            saveMenuItem()
        }
    }

    // Ask the ViewModel to retrieve the existing menu item.
    private fun loadMenuItem() {
        val id = itemId ?: return
        menuViewModel.loadMenuItem(id)
    }

    // Display the retrieved menu item's information in the form.
    private fun displayMenuItem(menuItem: MenuItem) {

        edtItemName.setText(menuItem.name)
        edtDescription.setText(menuItem.description)
        edtPrice.setText(menuItem.price.toString())

        val categoryPosition =
            (spinnerCategory.adapter as ArrayAdapter<String>)
                .getPosition(menuItem.category)

        if (categoryPosition >= 0) {
            spinnerCategory.setSelection(categoryPosition)
        }

        switchAvailable.isChecked = menuItem.available
    }

    // Validate the form and send the data to the ViewModel.
    private fun saveMenuItem() {

        val name = edtItemName.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val priceText = edtPrice.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val available = switchAvailable.isChecked

        // Validate the item name.
        if (name.isEmpty()) {
            edtItemName.error = "Enter the item name"
            return
        }

        // Validate the description.
        if (description.isEmpty()) {
            edtDescription.error = "Enter a description"
            return
        }

        // Validate the price.
        val price = priceText.toDoubleOrNull()

        if (price == null || price <= 0) {
            edtPrice.error = "Enter a valid price"
            return
        }

        // Create a MenuItem model containing the form information.
        val menuItem = MenuItem(
            name = name,
            description = description,
            price = price,
            category = category,
            available = available,
            imageUrl = ""
        )

        // Send the request to the ViewModel.
        val id = itemId

        if (id == null) {
            menuViewModel.addMenuItem(menuItem)
        } else {
            menuViewModel.updateMenuItem(id, menuItem)
        }
    }
}
