package com.example.findme.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.findme.R
import com.example.findme.model.ItemType
import com.example.findme.ui.detail.ItemDetailActivity

object NotificationHelper {

    private const val CHANNEL_ID = "findme_reports_channel"
    private const val CHANNEL_NAME = "Report Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for submitted lost and found reports"

    /**
     * Creates the notification channel. Safe to call multiple times;
     * the system ignores duplicate channel creation.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Returns true if the app has notification permission.
     * On Android 12 and below, this always returns true.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Shows a local notification after a report is successfully submitted.
     * Silently skips if notification permission is not granted.
     */
    fun showReportSubmittedNotification(
        context: Context,
        itemId: String,
        itemTitle: String,
        itemType: ItemType
    ) {
        // Ensure channel exists
        createNotificationChannel(context)

        // Check permission — silently skip if denied (report is still saved)
        if (!hasNotificationPermission(context)) return

        val typeLabel = if (itemType == ItemType.LOST) "Lost" else "Found"

        // Intent to open the item detail when notification is tapped
        val detailIntent = Intent(context, ItemDetailActivity::class.java).apply {
            putExtra(ItemDetailActivity.EXTRA_ITEM_ID, itemId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId.hashCode(),
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_add_report)
            .setContentTitle("$typeLabel Item Report Submitted")
            .setContentText(itemTitle)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your $typeLabel item report \"$itemTitle\" has been saved successfully.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(itemId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the notify call — ignore
        }
    }
}

