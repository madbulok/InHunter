package com.uzlov.inhunter.interfaces

import android.location.Location

interface LocationListener {
    fun onStartLocationListener()
    fun onUpdateLocation(location : Location)
    fun onStopLocationListener()
}