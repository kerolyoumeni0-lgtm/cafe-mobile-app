
package com.example.smartcafe

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.smartcafe.viewmodels.FeedbackViewModel
import com.example.smartcafe.models.Feedback

class CustomerFeedbackActivity : AppCompatActivity() {

    // Declare the ViewModel and screen components.
    private lateinit var feedbackViewModel: FeedbackViewModel
    private lateinit var feedbackContainer: LinearLayout
    private lateinit var txtAverageRating: TextView
    private lateinit var txtEmptyFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the XML layout for the Customer Feedback screen.
        setContentView(R.layout.activity_customer_feedback)

        // Connect the Activity to the FeedbackViewModel.
        feedbackViewModel = ViewModelProvider(this)
            .get(FeedbackViewModel::class.java)

        // Link the Kotlin variables to their XML views.
        feedbackContainer = findViewById(R.id.feedbackContainer)
        txtAverageRating = findViewById(R.id.txtAverageRating)
        txtEmptyFeedback = findViewById(R.id.txtEmptyFeedback)

        // Observe the feedback list from the ViewModel.
        feedbackViewModel.feedback.observe(this) { feedbackList ->
            displayFeedback(feedbackList)
        }

        // Observe the calculated average rating.
        feedbackViewModel.averageRating.observe(this) { average ->
            txtAverageRating.text = average
        }

        // Observe messages and errors from the ViewModel.
        feedbackViewModel.message.observe(this) { message ->

            if (message.isNotEmpty()) {
                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Retrieve customer feedback through the ViewModel.
        feedbackViewModel.loadFeedback()
    }

    // Display customer feedback cards on the screen.
    private fun displayFeedback(feedbackList: List<Feedback>) {

        // Clear existing cards before displaying new results.
        feedbackContainer.removeAllViews()

        // Check whether feedback is available.
        if (feedbackList.isEmpty()) {
            txtEmptyFeedback.visibility = View.VISIBLE
            return
        }

        // Hide the empty feedback message.
        txtEmptyFeedback.visibility = View.GONE

        // Loop through each feedback record.
        for (feedback in feedbackList) {

            // Create a vertical layout for each feedback card.
            val feedbackCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL

                // Add spacing around the feedback content.
                setPadding(24, 20, 24, 20)

                // Set a light background colour.
                setBackgroundColor(
                    Color.rgb(248, 247, 243)
                )
            }

            // Create a TextView for the feedback details.
            val feedbackText = TextView(this).apply {

                // Display customer feedback information.
                text = """
                    Customer: ${feedback.customerName}
                    Rating: ${"⭐".repeat(feedback.rating.toInt().coerceIn(0, 5))}
                    Comment: ${feedback.comment}
                    Order ID: ${feedback.orderId}
                    ${if (feedback.isSimulated) "Sample / simulated feedback" else ""}
                """.trimIndent()

                // Set the feedback text appearance.
                textSize = 16f
                setTextColor(Color.BLACK)
            }

            // Add the feedback text to its card.
            feedbackCard.addView(feedbackText)

            // Define the card width and height.
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Add spacing below each feedback card.
            params.setMargins(0, 0, 0, 20)

            // Add the completed card to the main container.
            feedbackContainer.addView(feedbackCard, params)
        }
    }
}
