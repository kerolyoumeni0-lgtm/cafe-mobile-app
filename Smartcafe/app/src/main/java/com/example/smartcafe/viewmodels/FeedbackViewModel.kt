
package com.example.smartcafe.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.smartcafe.models.Feedback
import com.example.smartcafe.repositories.FeedbackRepository

// Manages customer feedback data and communicates with the Repository.
class FeedbackViewModel : ViewModel() {

    private val repository = FeedbackRepository()

    // Holds the list of customer feedback.
    private val _feedback = MutableLiveData<List<Feedback>>()
    val feedback: LiveData<List<Feedback>> = _feedback

    // Holds the calculated average customer rating.
    private val _averageRating = MutableLiveData<String>()
    val averageRating: LiveData<String> = _averageRating

    // Holds loading and error messages.
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Retrieves feedback through the Repository.
    fun loadFeedback() {

        repository.getAllFeedback(
            onSuccess = { feedbackList ->

                _feedback.value = feedbackList

                // Calculate the average using valid ratings between 1 and 5.
                val validRatings = feedbackList.filter {
                    it.rating in 1.0..5.0
                }

                val average = if (validRatings.isNotEmpty()) {
                    validRatings.map { it.rating }.average()
                } else {
                    0.0
                }

                // Prepare the average rating display.
                _averageRating.value = if (validRatings.isEmpty()) {
                    "Average rating: No ratings yet"
                } else {
                    "Average rating: ${"%.1f".format(average)} / 5"
                }

                _message.value = if (feedbackList.isEmpty()) {
                    "No customer feedback available"
                } else {
                    ""
                }
            },

            onFailure = { exception ->
                _message.value = "Unable to load customer feedback"
            }
        )
    }
}
