
package com.example.smartcafe

// Imports required for Android notifications and permissions.
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.media.AudioAttributes
import android.provider.Settings

// Imports for creating and displaying notifications.
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {

    // Defines a unique ID for the application's notification channel.
    companion object {
        const val CHANNEL_ID = "smartcafe_notifications_sound_v2"
    }

    // Creates the notification channel used by the Smart Café application.
    fun createNotificationChannel() {

        // Notification channels are required on Android 8.0 and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Gets the default notification sound from the device.
            val soundUri = Settings.System.DEFAULT_NOTIFICATION_URI

            // Defines how the notification sound should be played.
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // Creates a notification channel with high importance.
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smart Café Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                // Explains the purpose of the notification channel.
                description = "Alerts about orders and customer feedback"

                // Sets the default notification sound.
                setSound(soundUri, audioAttributes)

                // Enables vibration when notifications arrive.
                enableVibration(true)
            }

            // Gets the Android notification manager service.
            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            // Registers the notification channel with Android.
            manager.createNotificationChannel(channel)
        }
    }

    // Displays a notification when a new customer order is received.
    fun showNewOrderNotification(orderId: String) {

        // Checks whether notification permission is required and granted.
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Stops the notification if permission has not been granted.
            return
        }

        // Builds the notification message for a new customer order.
        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Customer Order")
            .setContentText("A new order has been received: $orderId")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Displays the notification using the order ID as its notification ID.
        NotificationManagerCompat.from(context)
            .notify(orderId.hashCode(), notification)
    }

    // Displays a notification when an order status is changed.
    fun showOrderStatusNotification(orderId: String, newStatus: String) {

        // Checks whether notification permission has been granted.
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Builds a notification showing the updated order status.
        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Order Status Updated")
            .setContentText("Order $orderId has been marked as $newStatus.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Displays the order status notification.
        NotificationManagerCompat.from(context)
            .notify(orderId.hashCode() + 1, notification)
    }

    // Displays a notification when a customer submits new feedback.
    fun showNewFeedbackNotification(customerName: String) {

        // Checks whether the application has notification permission.
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Builds a notification containing the customer's name.
        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Customer Feedback")
            .setContentText("$customerName has submitted a review.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Displays the feedback notification using a unique notification ID.
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
