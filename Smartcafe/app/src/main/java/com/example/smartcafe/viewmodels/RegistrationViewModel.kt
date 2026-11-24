
package com.example.smartcafe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartcafe.repositories.RegistrationRepository

// Manages administrator registration.
class RegistrationViewModel : ViewModel() {

    private val repository = RegistrationRepository()

    // Indicates whether registration is in progress.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Holds registration success or error messages.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Indicates whether registration was successful.
    private val _registrationSuccessful = MutableLiveData<Boolean>()
    val registrationSuccessful: LiveData<Boolean> =
        _registrationSuccessful

    // Register a new administrator account.
    fun registerAdmin(
        name: String,
        email: String,
        password: String
    ) {

        _isLoading.value = true
        _message.value = ""
        _registrationSuccessful.value = false

        repository.registerAdmin(
            name = name,
            email = email,
            password = password,

            onSuccess = {
                _isLoading.postValue(false)
                _registrationSuccessful.postValue(true)
            },

            onFailure = { error ->
                _isLoading.postValue(false)
                _message.postValue(error)
            }
        )
    }
    // Sign out the newly registered account.
    fun logout() {
        repository.logout()
    }
}
