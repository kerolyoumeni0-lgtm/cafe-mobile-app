
package com.example.smartcafe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartcafe.models.AdminUser
import com.example.smartcafe.repositories.AuthRepository

// Manages administrator login and logout operations.
class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Stores the authenticated administrator.
    private val _adminUser = MutableLiveData<AdminUser?>()
    val adminUser: LiveData<AdminUser?> = _adminUser

    // Stores login errors and messages.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Tracks whether login is in progress.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Authenticate and verify the administrator.
    fun loginAdmin(email: String, password: String) {

        _isLoading.value = true
        _message.value = ""

        repository.loginAdmin(
            email = email,
            password = password,

            onSuccess = { admin ->
                _adminUser.postValue(admin)
                _isLoading.postValue(false)
            },

            onFailure = { error ->
                _message.postValue(error)
                _isLoading.postValue(false)
            }
        )
    }

    // Log out the current administrator.
    fun logout() {
        repository.logout()
        _adminUser.value = null
    }
}
