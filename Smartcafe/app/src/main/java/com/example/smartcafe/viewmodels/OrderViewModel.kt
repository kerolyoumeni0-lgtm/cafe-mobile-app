
package com.example.smartcafe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartcafe.models.Order
import com.example.smartcafe.repositories.OrderRepository

// Manages order data and communicates with OrderRepository.
class OrderViewModel : ViewModel() {

    // Repository responsible for Firebase operations.
    private val repository = OrderRepository()

    // Stores all orders retrieved from Firestore.
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    // Stores messages showing the result of an operation.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Stores whether orders are currently loading.
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Retrieve all orders through the repository.
    fun loadOrders() {

        _isLoading.value = true

        repository.getAllOrders(
            onSuccess = { orderList ->

                _isLoading.value = false
                _orders.value = orderList

            },
            onFailure = { error ->

                _isLoading.value = false
                _message.value =
                    "Could not load orders: ${error.localizedMessage}"

            }
        )
    }
    // Update an order's status through the repository.
    fun updateOrderStatus(orderId: String, newStatus: String) {

        _isLoading.value = true

        repository.updateOrderStatus(
            orderId,
            newStatus,
            onSuccess = {
                _isLoading.value = false
                _message.value = "Order status updated successfully!"
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Update failed: ${error.localizedMessage}"
            }
        )
    }
    // Start preparing an order through the repository.
    fun startPreparingOrder(orderId: String, endTime: Long) {

        _isLoading.value = true

        repository.startPreparingOrder(
            orderId,
            endTime,
            onSuccess = {
                _isLoading.value = false
                _message.value = "Order is now being prepared!"
            },
            onFailure = { error ->
                _isLoading.value = false
                _message.value =
                    "Could not start timer: ${error.localizedMessage}"
            }
        )
    }
}
