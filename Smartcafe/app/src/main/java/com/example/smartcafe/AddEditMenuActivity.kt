
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

    private val db = FirebaseFirestore.getInstance()

    private var itemId: String? = null

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

        edtItemName = findViewById(R.id.edtItemName)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        switchAvailable = findViewById(R.id.switchAvailable)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSaveMenuItem = findViewById(R.id.btnSaveMenuItem)

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

        btnSelectImage.setOnClickListener {
            Toast.makeText(
                this,
                "Food images will be added later.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Check whether this screen was opened to edit an existing item.
        itemId = intent.getStringExtra("itemId")

        if (itemId != null) {
            btnSaveMenuItem.text = "Update Menu Item"
            loadMenuItem()
        }

        btnSaveMenuItem.setOnClickListener {
            saveMenuItem()
        }
    }

    private fun loadMenuItem() {
        val id = itemId ?: return

        db.collection("MenuItems")
            .document(id)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {
                    edtItemName.setText(document.getString("name") ?: "")
                    edtDescription.setText(
                        document.getString("description") ?: ""
                    )

                    val price = document.getDouble("price")
                    if (price != null) {
                        edtPrice.setText(price.toString())
                    }

                    val category =
                        document.getString("category") ?: "Food"

                    val categoryPosition =
                        (spinnerCategory.adapter as ArrayAdapter<String>)
                            .getPosition(category)

                    if (categoryPosition >= 0) {
                        spinnerCategory.setSelection(categoryPosition)
                    }

                    switchAvailable.isChecked =
                        document.getBoolean("available") ?: true

                } else {
                    Toast.makeText(
                        this,
                        "Menu item not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Could not load item: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveMenuItem() {

        val name = edtItemName.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val priceText = edtPrice.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val available = switchAvailable.isChecked

        if (name.isEmpty()) {
            edtItemName.error = "Enter the item name"
            return
        }

        if (description.isEmpty()) {
            edtDescription.error = "Enter a description"
            return
        }

        val price = priceText.toDoubleOrNull()

        if (price == null || price <= 0) {
            edtPrice.error = "Enter a valid price"
            return
        }

        btnSaveMenuItem.isEnabled = false
        btnSaveMenuItem.text = "Saving..."

        val menuItem = hashMapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "category" to category,
            "available" to available,
            "imageUrl" to ""
        )

        val id = itemId

        if (id == null) {
            // ADD a new menu item
            db.collection("MenuItems")
                .add(menuItem)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Menu item added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    showSaveError(error)
                }

        } else {
            // UPDATE the existing menu item
            db.collection("MenuItems")
                .document(id)
                .update(menuItem as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Menu item updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    showSaveError(error)
                }
        }
    }

    private fun showSaveError(error: Exception) {
        btnSaveMenuItem.isEnabled = true
        btnSaveMenuItem.text =
            if (itemId == null) "Save Menu Item" else "Update Menu Item"

        Toast.makeText(
            this,
            "Could not save item: ${error.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}
