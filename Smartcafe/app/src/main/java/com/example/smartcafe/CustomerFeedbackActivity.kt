package com.example.smartcafe

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CustomerFeedbackActivity : AppCompatActivity() {

    // Declare variables for Firestore and the screen components.
    private lateinit var db: FirebaseFirestore
    private lateinit var feedbackContainer: LinearLayout
    private lateinit var txtAverageRating: TextView
    private lateinit var txtEmptyFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the XML layout for the Customer Feedback screen.
        setContentView(R.layout.activity_customer_feedback)

        // Connect to the Firebase Cloud Firestore database.
        db = FirebaseFirestore.getInstance()

        // Link the Kotlin variables to their corresponding XML views.
        feedbackContainer = findViewById(R.id.feedbackContainer)
        txtAverageRating = findViewById(R.id.txtAverageRating)
        txtEmptyFeedback = findViewById(R.id.txtEmptyFeedback)

        // Retrieve and display customer feedback when the screen opens.
        loadFeedback()
    }

    // Retrieve customer feedback from the feedback collection in Firestore.
    private fun loadFeedback() {
        db.collection("feedback")
            .get()
            .addOnSuccessListener { documents ->

                // Clear any existing feedback cards before displaying new results.
                feedbackContainer.removeAllViews()

                // Check whether the feedback collection contains any documents.
                if (documents.isEmpty) {

                    // Display a message when no customer ratings are available.
                    txtAverageRating.text = "Average rating: No ratings yet"
                    txtEmptyFeedback.visibility = View.VISIBLE

                    // Stop running this function because there is no feedback to display.
                    return@addOnSuccessListener
                }

                // Hide the empty feedback message when feedback is available.
                txtEmptyFeedback.visibility = View.GONE

                // Variables used to calculate the average customer rating.
                var totalRating = 0.0
                var ratingCount = 0

                // Loop through each feedback document retrieved from Firestore.
                for (document in documents) {

                    // Retrieve the customer's name.
                    val customerName =
                        document.getString("customerName") ?: "Unknown customer"

                    // Retrieve the rating and convert it into a Double.
                    val rating =
                        (document.get("rating") as? Number)?.toDouble() ?: 0.0

                    // Retrieve the customer's comment.
                    val comment =
                        document.getString("comment") ?: "No comment provided"

                    // Retrieve the order ID associated with the feedback.
                    val orderId =
                        document.getString("orderId") ?: "Not provided"

                    // Check whether the feedback is sample or simulated data.
                    val isSimulated =
                        document.getBoolean("isSimulated") ?: false

                    // Include valid ratings between 1 and 5 in the average calculation.
                    if (rating in 1.0..5.0) {
                        totalRating += rating
                        ratingCount++
                    }

                    // Create a vertical layout to display one feedback card.
                    val feedbackCard = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL

                        // Add spacing around the feedback card content.
                        setPadding(24, 20, 24, 20)

                        // Set a light background colour for the feedback card.
                        setBackgroundColor(
                            android.graphics.Color.rgb(248, 247, 243)
                        )
                    }

                    // Create a TextView to display the customer's feedback details.
                    val feedbackText = TextView(this).apply {

                        // Display the customer name, rating, comment and order ID.
                        // Show a label if the feedback is simulated.
                        text = """
                            Customer: $customerName
                            Rating: ${"⭐".repeat(rating.toInt().coerceIn(0, 5))}
                            Comment: $comment
                            Order ID: $orderId
                            ${if (isSimulated) "Sample / simulated feedback" else ""}
                        """.trimIndent()

                        // Set the feedback text size and colour.
                        textSize = 16f
                        setTextColor(android.graphics.Color.BLACK)
                    }

                    // Add the feedback text to its card.
                    feedbackCard.addView(feedbackText)

                    // Define the width and height of each feedback card.
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    // Add spacing below each feedback card.
                    params.setMargins(0, 0, 0, 20)

                    // Add the completed feedback card to the main container.
                    feedbackContainer.addView(feedbackCard, params)
                }

                // Calculate the average rating if valid ratings are available.
                val average = if (ratingCount > 0) {
                    totalRating / ratingCount
                } else {
                    0.0
                }

                // Display the average rating to one decimal place out of five.
                txtAverageRating.text =
                    "Average rating: ${"%.1f".format(average)} / 5"
            }
            .addOnFailureListener {

                // Display an error message if Firestore cannot retrieve feedback.
                Toast.makeText(
                    this,
                    "Unable to load customer feedback",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}