
package com.example.smartcafe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartcafe.models.MenuItem
import com.example.smartcafe.repositories.MenuRepository

// Manages menu data and communicates with MenuRepository.
class MenuViewModel : ViewModel() {

    // Repository responsible for Firebase operations.
    private val repository = MenuRepository()

    // Stores messages showing the result of an operation.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Stores whether a menu operation is currently running.
    // Stores the menu item retrieved from Firestore.
    private val _menuItem = MutableLiveData<MenuItem?>()
    val menuItem: LiveData<MenuItem?> = _menuItem
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    // Stores all menu items retrieved from Firestore.
    private val _menuItems =
        MutableLiveData<List<Pair<String, MenuItem>>>()

    val menuItems: LiveData<List<Pair<String, MenuItem>>> = _menuItems

    // Add a new menu item.
    fun addMenuItem(menuItem: MenuItem) {
        _isLoading.value = true

        repository.addMenuItem(
            menuItem,
            onSuccess = {
                _isLoading.value = false
                _message.value = "Menu item added successfully!"
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Could not add menu item: ${error.localizedMessage}"
            }
        )
    }

    // Update an existing menu item.
    fun updateMenuItem(itemId: String, menuItem: MenuItem) {
        _isLoading.value = true

        repository.updateMenuItem(
            itemId,
            menuItem,
            onSuccess = {
                _isLoading.value = false
                _message.value = "Menu item updated successfully!"
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Could not update menu item: ${error.localizedMessage}"
            }
        )
    }
    // Retrieve an existing menu item for editing.
    fun loadMenuItem(itemId: String) {
        _isLoading.value = true

        repository.getMenuItem(
            itemId,
            onSuccess = { item ->
                _isLoading.value = false

                // Pass the retrieved menu item to the Activity.
                _menuItem.value = item

                if (item == null) {
                    _message.value = "Menu item not found."
                }
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Could not load menu item: ${error.localizedMessage}"
            }
        )
    }
    fun deleteMenuItem(itemId: String) {
        _isLoading.value = true

        repository.deleteMenuItem(
            itemId,
            onSuccess = {
                _isLoading.value = false
                _message.value = "Menu item deleted successfully!"
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Could not delete menu item: ${error.localizedMessage}"
            }
        )
    }
    // Retrieve all menu items through the repository.
    fun loadAllMenuItems() {
        _isLoading.value = true

        repository.getAllMenuItems(
            onSuccess = { items ->
                _isLoading.value = false
                _menuItems.value = items
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Could not load menu: ${error.localizedMessage}"
            }
        )
    }
}
