
package com.example.smartcafe.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartcafe.repositories.DashboardRepository
import com.google.firebase.firestore.ListenerRegistration

// Manages real-time dashboard events using the Repository.
class DashboardViewModel : ViewModel() {

    private val repository = DashboardRepository()

    // Holds the ID of a newly created order.
    private val _newOrder = MutableLiveData<String>()
    val newOrder: LiveData<String> = _newOrder

    // Holds the name of a customer who submitted new feedback.
    private val _newFeedback = MutableLiveData<String>()
    val newFeedback: LiveData<String> = _newFeedback

    // Holds error messages from Firebase monitoring.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private var ordersListener: ListenerRegistration? = null
    private var feedbackListener: ListenerRegistration? = null

    // Start monitoring newly created orders.
    fun startOrderMonitoring() {

        if (ordersListener != null) return

        ordersListener = repository.listenForNewOrders(
            onNewOrder = { orderId ->
                _newOrder.postValue(orderId)
            },
            onFailure = {
                _message.postValue("Unable to monitor new orders")
            }
        )
    }

    // Start monitoring newly submitted customer feedback.
    fun startFeedbackMonitoring() {

        if (feedbackListener != null) return

        feedbackListener = repository.listenForNewFeedback(
            onNewFeedback = { customerName ->
                _newFeedback.postValue(customerName)
            },
            onFailure = {
                _message.postValue("Unable to monitor new feedback")
            }
        )
    }
    // Clear the order event after the Activity handles it.
    fun clearNewOrderEvent() {
        _newOrder.value = null
    }

    // Clear the feedback event after the Activity handles it.
    fun clearNewFeedbackEvent() {
        _newFeedback.value = null
    }

    // Stop monitoring when the dashboard is no longer active.
    fun stopMonitoring() {

        ordersListener?.remove()
        ordersListener = null

        feedbackListener?.remove()
        feedbackListener = null
    }

    // Remove Firebase listeners when the ViewModel is destroyed.
    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}
