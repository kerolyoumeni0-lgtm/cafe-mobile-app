
package com.example.smartcafe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartcafe.models.SalesAnalytics
import com.example.smartcafe.repositories.SalesAnalyticsRepository

// Manages sales analytics data and communicates with the Repository.
class SalesAnalyticsViewModel : ViewModel() {

    private val repository = SalesAnalyticsRepository()

    // Holds the sales analytics information.
    private val _analytics = MutableLiveData<SalesAnalytics>()
    val analytics: LiveData<SalesAnalytics> = _analytics

    // Indicates whether analytics are currently loading.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Holds error messages.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Retrieve sales analytics through the Repository.
    fun loadAnalytics() {

        _isLoading.value = true
        _message.value = ""

        repository.getSalesAnalytics(

            onSuccess = { result ->

                _analytics.postValue(result)
                _isLoading.postValue(false)
            },

            onFailure = { exception ->

                _message.postValue(
                    "Unable to load analytics. Check your connection."
                )

                _isLoading.postValue(false)
            }
        )
    }
}
