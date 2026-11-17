
package com.example.smartcafe

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CustomerFeedbackActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var feedbackContainer: LinearLayout
    private lateinit var txtAverageRating: TextView
    private lateinit var txtEmptyFeedback: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_feedback)

        db = FirebaseFirestore.getInstance()

        feedbackContainer = findViewById(R.id.feedbackContainer)
        txtAverageRating = findViewById(R.id.txtAverageRating)
        txtEmptyFeedback = findViewById(R.id.txtEmptyFeedback)

        loadFeedback()
    }

    private fun loadFeedback() {
        db.collection("feedback")
            .get()
            .addOnSuccessListener { documents ->

                feedbackContainer.removeAllViews()

                if (documents.isEmpty) {
                    txtAverageRating.text = "Average rating: No ratings yet"
                    txtEmptyFeedback.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                txtEmptyFeedback.visibility = View.GONE

                var totalRating = 0.0
                var ratingCount = 0

                for (document in documents) {
                    val customerName =
                        document.getString("customerName") ?: "Unknown customer"

                    val rating =
                        (document.get("rating") as? Number)?.toDouble() ?: 0.0

                    val comment =
                        document.getString("comment") ?: "No comment provided"

                    val orderId =
                        document.getString("orderId") ?: "Not provided"

                    val isSimulated =
                        document.getBoolean("isSimulated") ?: false

                    if (rating in 1.0..5.0) {
                        totalRating += rating
                        ratingCount++
                    }

                    val feedbackCard = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(24, 20, 24, 20)
                        setBackgroundColor(
                            android.graphics.Color.rgb(248, 247, 243)
                        )
                    }

                    val feedbackText = TextView(this).apply {
                        text = """
                            Customer: $customerName
                            Rating: ${"⭐".repeat(rating.toInt().coerceIn(0, 5))}
                            Comment: $comment
                            Order ID: $orderId
                            ${if (isSimulated) "Sample / simulated feedback" else ""}
                        """.trimIndent()
                        textSize = 16f
                        setTextColor(android.graphics.Color.BLACK)
                    }

                    feedbackCard.addView(feedbackText)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 0, 0, 20)

                    feedbackContainer.addView(feedbackCard, params)
                }

                val average = if (ratingCount > 0) {
                    totalRating / ratingCount
                } else {
                    0.0
                }

                txtAverageRating.text =
                    "Average rating: ${"%.1f".format(average)} / 5"
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Unable to load customer feedback",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
