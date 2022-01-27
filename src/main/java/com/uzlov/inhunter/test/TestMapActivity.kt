package com.uzlov.inhunter.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.uzlov.inhunter.R


class TestMapActivity : AppCompatActivity() , OnMapReadyCallback {
    private lateinit var symbolManager: SymbolManager
    private lateinit var mapView: MapView

    private val MAKI_ICON_CAFE = "cafe-15"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.map_fragment_content)
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            symbolManager = SymbolManager(mapView, mapboxMap, it)
            symbolManager.iconAllowOverlap = true
            symbolManager.iconIgnorePlacement = true



            val symbol: Symbol = symbolManager.create(
                    SymbolOptions()
                            .withLatLng(LatLng(37.519404,55.80483))
                            .withIconImage("cafe-15")
                            .withIconSize(2.0f)
            )
        }
    }
}