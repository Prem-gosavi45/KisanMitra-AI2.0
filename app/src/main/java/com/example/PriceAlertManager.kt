package com.example

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class PriceAlertManager(private val context: Context) {
    private val channelId = "price_alerts"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Price Alerts"
            val descriptionText = "Notifications for crop price thresholds"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun simulatePriceHit(cropName: String, targetPrice: Double) {
        // Simulate waiting for market fluctuation
        delay(4000)
        showNotification(cropName, targetPrice)
    }

    fun showNotification(cropName: String, targetPrice: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("PriceAlertManager", "POST_NOTIFICATIONS permission not granted.")
                return
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Market Alert Hit! 🚀")
            .setContentText("The price for $cropName has reached your target of ₹${targetPrice.toInt()}/kg.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(cropName.hashCode(), builder.build())
            }
        } catch (e: Exception) {
            Log.e("PriceAlertManager", "Failed to send notification: ${e.message}")
        }
    }
}
