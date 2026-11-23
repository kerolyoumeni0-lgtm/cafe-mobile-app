package com.example.smartcafe

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddEditMenuActivity : AppCompatActivity() {

    // Connect to the Firebase Cloud Firestore database.
    private val db = FirebaseFirestore.getInstance()

    // Stores the ID of the menu item being edited.
    // It remains null when adding a new menu item.
    private var itemId: String? = null

    // Declare the interface components used in the activity.
    private lateinit var edtItemName: EditText
    private lateinit var edtDescription: EditText
    private lateinit var edtPrice: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var switchAvailable: Switch
    private lateinit var btnSelectImage: Button
    private lateinit var btnSaveMenuItem: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the XML layout for the Add/Edit Menu screen.
        setContentView(R.layout.activity_add_edit_menu)

        // Connect the Kotlin variables to their XML views using their IDs.
        edtItemName = findViewById(R.id.edtItemName)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        switchAvailable = findViewById(R.id.switchAvailable)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSaveMenuItem = findViewById(R.id.btnSaveMenuItem)

        // Define the available menu item categories.
        val categories = arrayOf("Food", "Drinks", "Desserts")

        // Create an adapter to display the categories inside the Spinner.
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        // Set the layout used when the category dropdown is opened.
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // Connect the adapter to the category Spinner.
        spinnerCategory.adapter = adapter

        // Handle clicks on the image selection button.
        btnSelectImage.setOnClickListener {
            // Display a message because image selection is not implemented yet.
            Toast.makeText(
                this,
                "Food images will be added later.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Check whether this screen was opened to edit an existing item.
        // If no ID is received, the activity will be used to add a new item.
        itemId = intent.getStringExtra("itemId")

        if (itemId != null) {
            // Change the button text and load the existing item's information.
            btnSaveMenuItem.text = "Update Menu Item"
            loadMenuItem()
        }

        // Save the menu item when the button is clicked.
        btnSaveMenuItem.setOnClickListener {
            saveMenuItem()
        }
    }

    // Retrieve an existing menu item from Firestore for editing.
    private fun loadMenuItem() {
        // Stop if there is no menu item ID.
        val id = itemId ?: return

        // Find the menu item document in the MenuItems collection.
        db.collection("MenuItems")
            .document(id)
            .get()
            .addOnSuccessListener { document ->

                // Check whether the requested document exists.
                if (document.exists()) {

                    // Display the existing item name and description.
                    edtItemName.setText(document.getString("name") ?: "")
                    edtDescription.setText(
                        document.getString("description") ?: ""
                    )

                    // Retrieve the item's price and display it.
                    val price = document.getDouble("price")
                    if (price != null) {
                        edtPrice.setText(price.toString())
                    }

                    // Retrieve the item's category from Firestore.
                    val category =
                        document.getString("category") ?: "Food"

                    // Find the position of the saved category in the Spinner.
                    val categoryPosition =
                        (spinnerCategory.adapter as ArrayAdapter<String>)
                            .getPosition(category)

                    // Select the saved category in the Spinner.
                    if (categoryPosition >= 0) {
                        spinnerCategory.setSelection(categoryPosition)
                    }

                    // Set the availability switch using the saved value.
                    switchAvailable.isChecked =
                        document.getBoolean("available") ?: true

                } else {
                    // Display an error if the menu item cannot be found.
                    Toast.makeText(
                        this,
                        "Menu item not found.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Close the activity.
                    finish()
                }
            }
            .addOnFailureListener { error ->
                // Display an error if Firestore fails to load the item.
                Toast.makeText(
                    this,
                    "Could not load item: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // Validate the form and save the menu item to Firestore.
    private fun saveMenuItem() {

        // Get the values entered by the administrator.
        val name = edtItemName.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val priceText = edtPrice.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val available = switchAvailable.isChecked

        // Check that the menu item name has been entered.
        if (name.isEmpty()) {
            edtItemName.error = "Enter the item name"
            return
        }

        // Check that the description has been entered.
        if (description.isEmpty()) {
            edtDescription.error = "Enter a description"
            return
        }

        // Convert the price from text into a number.
        val price = priceText.toDoubleOrNull()

        // Make sure the price is a valid number greater than zero.
        if (price == null || price <= 0) {
            edtPrice.error = "Enter a valid price"
            return
        }

        // Disable the button to prevent repeated submissions.
        btnSaveMenuItem.isEnabled = false
        btnSaveMenuItem.text = "Saving..."

        // Store all menu item details in a HashMap.
        // These values will be saved as fields in the Firestore document.
        val menuItem = hashMapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "category" to category,
            "available" to available,
            "imageUrl" to ""
        )

        // Retrieve the existing menu item ID, if one is available.
        val id = itemId

        if (id == null) {

            // ADD a new menu item to the MenuItems collection.
            db.collection("MenuItems")
                .add(menuItem)
                .addOnSuccessListener {
                    // Confirm that the new menu item was added successfully.
                    Toast.makeText(
                        this,
                        "Menu item added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Close the activity after saving.
                    finish()
                }
                .addOnFailureListener { error ->
                    // Display an error if the new item cannot be saved.
                    showSaveError(error)
                }

        } else {

            // UPDATE an existing menu item using its document ID.
            db.collection("MenuItems")
                .document(id)
                .update(menuItem as Map<String, Any>)
                .addOnSuccessListener {
                    // Confirm that the menu item was updated successfully.
                    Toast.makeText(
                        this,
                        "Menu item updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Close the activity after updating.
                    finish()
                }
                .addOnFailureListener { error ->
                    // Display an error if the update fails.
                    showSaveError(error)
                }
        }
    }

    // Handle errors that occur when adding or updating a menu item.
    private fun showSaveError(error: Exception) {

        // Re-enable the button so the administrator can try again.
        btnSaveMenuItem.isEnabled = true

        // Restore the correct button text depending on the operation.
        btnSaveMenuItem.text =
            if (itemId == null) "Save Menu Item" else "Update Menu Item"

        // Display the reason why the item could not be saved.
        Toast.makeText(
            this,
            "Could not save item: ${error.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}