package com.nearchat.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.nearchat.connectivity.BluetoothTransportManager
import com.nearchat.connectivity.ConnectivityCoordinator
import com.nearchat.connectivity.WifiDirectTransportManager

class NearChatService : Service() {
    private val binder = LocalBinder()

    lateinit var coordinator: ConnectivityCoordinator
        private set

    override fun onCreate() {
        super.onCreate()
        coordinator = ConnectivityCoordinator(
            BluetoothTransportManager(this),
            WifiDirectTransportManager(this)
        )
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): NearChatService = this@NearChatService
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "NearChat", NotificationManager.IMPORTANCE_LOW)
            )
        }

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("NearChat running")
            .setContentText("Keeping nearby messaging active")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "nearchat_service"
        private const val NOTIFICATION_ID = 11
    }
}
