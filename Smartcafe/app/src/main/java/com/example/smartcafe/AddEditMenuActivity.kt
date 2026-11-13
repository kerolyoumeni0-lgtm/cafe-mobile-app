
package com.example.smartcafe

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


class AddEditMenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()


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

        // Set up the category dropdown
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

        // Open the phone's image picker
        btnSelectImage.setOnClickListener {
            Toast.makeText(
                this,
                "Food images will be added later.",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Save the menu item
        btnSaveMenuItem.setOnClickListener {
            saveMenuItem()
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

        // Save directly to Firestore without an image
        saveToFirestore(
            name,
            description,
            price,
            category,
            available,
            ""
        )
    }

    private fun saveToFirestore(
        name: String,
        description: String,
        price: Double,
        category: String,
        available: Boolean,
        imageUrl: String
    ) {

        val menuItem = hashMapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "category" to category,
            "available" to available,
            "imageUrl" to imageUrl
        )

        db.collection("MenuItems")
            .add(menuItem)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Menu item saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { error ->
                btnSaveMenuItem.isEnabled = true
                btnSaveMenuItem.text = "Save Menu Item"

                Toast.makeText(
                    this,
                    "Could not save item: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
