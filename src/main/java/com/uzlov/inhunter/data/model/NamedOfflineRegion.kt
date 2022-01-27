package com.uzlov.inhunter.data.model

import com.mapbox.mapboxsdk.offline.OfflineRegion

data class NamedOfflineRegion(
        val nameRegion: String,
        val offlineRegion: OfflineRegion,
)