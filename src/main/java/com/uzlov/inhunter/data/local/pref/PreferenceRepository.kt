package com.uzlov.inhunter.data.local.pref

import android.content.SharedPreferences
import com.mapbox.mapboxsdk.maps.Style
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import javax.inject.Inject

class PreferenceRepository @Inject constructor(private var preferences: SharedPreferences) {

    fun readOwner() : Owner {
        return Owner(
            email = preferences.getString("key_type_email", "") ?: "",
            nick = preferences.getString("key_profile_name", "") ?: "",
            type = preferences.getString("key_type_player", "0") ?: ""
        )
    }

    fun updateOwner(profile: Owner) {
        preferences.edit().apply {
            putString("key_profile_name", profile.nick)
            putString("key_type_player", profile.type)
            putString("key_type_email", profile.email)
        }.apply()
    }


    fun readLocalMapSetting(): String {
        return when (preferences.getString("type_map_key", "") ?: "MAPBOX_STREETS") {
            "MAPBOX_STREETS" -> Style.MAPBOX_STREETS
            "OUTDOORS" -> Style.OUTDOORS
            "LIGHT" -> Style.LIGHT
            "DARK" -> Style.DARK
            "SATELLITE" -> Style.SATELLITE
            "SATELLITE_STREETS" -> Style.SATELLITE_STREETS
            "TRAFFIC_DAY" -> Style.TRAFFIC_DAY
            "TRAFFIC_NIGHT" -> Style.TRAFFIC_NIGHT
            else -> Style.SATELLITE
        }
    }

    fun readMapTypes() = listOf(
        "MAPBOX_STREETS",
        "OUTDOORS",
        "LIGHT",
        "DARK",
        "SATELLITE",
        "SATELLITE_STREETS",
        "TRAFFIC_DAY",
        "TRAFFIC_NIGHT"
    )

    fun getStyleMap(key: String): String {
        return when (key) {
            "MAPBOX_STREETS" -> Style.MAPBOX_STREETS
            "OUTDOORS" -> Style.OUTDOORS
            "LIGHT" -> Style.LIGHT
            "DARK" -> Style.DARK
            "SATELLITE" -> Style.SATELLITE
            "SATELLITE_STREETS" -> Style.SATELLITE_STREETS
            "TRAFFIC_DAY" -> Style.TRAFFIC_DAY
            "TRAFFIC_NIGHT" -> Style.TRAFFIC_NIGHT
            else -> Style.SATELLITE
        }
    }



    fun getAllTypes(): Map<String, Boolean> {
        val isActive = getActiveType()
        return mapOf(
            "Загонщик" to ("Загонщик" == isActive),
            "Стрелок" to ("Стрелок" == isActive),
            "Молчун" to ("Молчун" == isActive),
            "Егерь" to ("Егерь" == isActive)
        )
    }

    fun setActiveType(type: String) {
        preferences.edit().putString("key_type_player", type).apply()
    }

    fun getActiveType() = preferences.getString("key_type_player", "Загонщик")

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun getActiveMapType(): String =
        preferences.getString("type_map_key", "Not selected") ?: "Not selected"

    fun setActiveMapType(type: String) {
        preferences.edit().apply {
            putString("type_map_key", type)
        }.apply()
    }
}