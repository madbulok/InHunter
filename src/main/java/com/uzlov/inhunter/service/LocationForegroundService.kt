package com.uzlov.inhunter.service

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.uzlov.inhunter.R
import com.uzlov.inhunter.activities.HostActivity
import com.uzlov.inhunter.interfaces.LocationListener


class LocationForegroundService : Service(), LocationListener {
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private val TAG = "MyForegroundService"
    private lateinit var locationService: LocationService
    private val CHANNEL_ID = "ForegroundServiceChannel"
    private val ACTION_STOP_SERVICE = "STOP"

    companion object {
        const val ACTION_RECIEVER_LOCATION = "com.uzlov.inhunter.action_reciever_location"
    }

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(applicationContext, this)
        mLocationSettingsRequest = locationService.buildLocationSettingsRequest()

    }

    private fun startUpdater() {
        locationService.startUpdatesButtonHandler()
        locationService.mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener {
                Log.i(TAG, "All location settings are satisfied.")
                locationService.requestLocationUpdates()
            }
            .addOnFailureListener { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(
                            TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings "
                        )
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)

                        locationService.setRequestingLocationUpdates(false)
                    }
                }
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP_SERVICE == intent?.action) {
            sendBroadcast(
                Intent().apply {
                    action = ACTION_RECIEVER_LOCATION
                    putExtra("isStopped", true)
                }
            )
            stopSelf()
        } else {
            startUpdater()
            if (intent != null) {

                createNotificationChannel()
                val notificationIntent = Intent(this, HostActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0, notificationIntent, 0
                )

                val stopSelf = Intent(this, LocationForegroundService::class.java).apply {
                    action = ACTION_STOP_SERVICE
                }
                val pStopSelf =
                    PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT)

                val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Охота началась!")
                    .setSmallIcon(R.drawable.mapbox_compass_icon)
                    .addAction(R.drawable.ic_baseline_stop_24, "Stop", pStopSelf)
                    .build()
                startForeground(1, notification)
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        locationService.stopLocationUpdates()
        Toast.makeText(this, getString(R.string.hunt_is_end), Toast.LENGTH_SHORT).show()
    }

    override fun onStartLocationListener() {
        Log.e(TAG, "onStartLocationListener")
    }

    override fun onUpdateLocation(location: Location) {
        val intent = Intent().apply {
            action = ACTION_RECIEVER_LOCATION
            putExtra("longitude", location.longitude)
            putExtra("latitude", location.latitude)
        }
        sendBroadcast(intent)
    }

    override fun onStopLocationListener() {
        Log.e(TAG, "onStopLocationListener")
    }
}