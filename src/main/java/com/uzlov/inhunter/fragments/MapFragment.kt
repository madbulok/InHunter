package com.uzlov.inhunter.fragments

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.HuntingDataState
import com.uzlov.inhunter.app.MapGlobalProperty.Companion.iconHunter
import com.uzlov.inhunter.app.MapGlobalProperty.Companion.mStyleMap
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.app.exceptions.IllegalAuthStateException
import com.uzlov.inhunter.app.exceptions.PinNotFoundException
import com.uzlov.inhunter.app.exceptions.TeamNotFoundException
import com.uzlov.inhunter.data.auth.AuthService
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.data.model.NamedOfflineRegion
import com.uzlov.inhunter.data.net.entities.ResponseTeamPositionItem
import com.uzlov.inhunter.data.net.entities.bodies.Owner
import com.uzlov.inhunter.databinding.MapFragmentContentBinding
import com.uzlov.inhunter.interfaces.Constants
import com.uzlov.inhunter.map.MapService
import com.uzlov.inhunter.service.LocationForegroundService
import com.uzlov.inhunter.ui.custom.DraggableLockButton
import com.uzlov.inhunter.ui.dialogs.*
import com.uzlov.inhunter.utils.colorTrack
import com.uzlov.inhunter.utils.isNotNull
import com.uzlov.inhunter.utils.nickToMarker
import com.uzlov.inhunter.viewmodels.HuntingViewModel
import java.util.*
import javax.inject.Inject


class MapFragment : FragmentWithLocation(), OnMapReadyCallback,
    SharedPreferences.OnSharedPreferenceChangeListener,
    FragmentResultListener {


    private var mMapZoomCurrent: Double = 14.0
    private var symbolManager: SymbolManager? = null
    private var lineManager: LineManager? = null

    private lateinit var drawableIcon: Drawable

    private var _viewBinding: MapFragmentContentBinding? = null
    private val viewBindingRoot get() = _viewBinding!!

    private var huntingViewModel: HuntingViewModel? = null

    // di
    @Inject
    lateinit var factoryViewModel: ViewModelProvider.Factory

    @Inject
    lateinit var prefRepository: PreferenceRepository

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var mapService: MapService

    private val linePositions = mutableMapOf<String, MutableList<LatLng>>()
    private val lineMap = mutableMapOf<String, LineOptions>()

    private var selfProfile = Owner()

    private val locationReceiver: BroadcastReceiver by lazy { LocationReceiver() }
    private lateinit var mMap: MapboxMap

    private var mapIsReadyToUse = false
    private var style: Style? = null

    private val mMarkers = mutableMapOf<String, Symbol>()

    interface IHuntStateListener {
        fun onStartHunt()
        fun onStopHunt()
    }

    private var huntListener: IHuntStateListener? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        huntListener = context as IHuntStateListener
    }

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)

        huntingViewModel = factoryViewModel.create(HuntingViewModel::class.java)

        drawableIcon =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_circle_point_player, null)
                ?: ColorDrawable(Color.RED)

        parentFragmentManager.setFragmentResultListener(
            getString(R.string.key_navigate_camera_to_position),
            this,
            this
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = MapFragmentContentBinding.inflate(layoutInflater, container, false).also {
        _viewBinding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI(savedInstanceState)
        initSettings()
        readProfileFromSettings()
    }

    private val dialog by lazy {
        DialogAcceptTypePlayer(prefRepository.getAllTypes().toMutableMap())
    }

    private var mDialogTypeCallback = object : DialogAcceptTypePlayer.OnActionListener {
        // выбор новго типа учатсника
        override fun select(type: String) {
            prefRepository.setActiveType(type)
            dialog.reselect(prefRepository.getAllTypes())
        }

        // клик по кнопке "В охоту!"
        override fun start() {
            startHunt()
        }

        // при клике "Закрыть"
        override fun cancel() {
            viewBindingRoot.btnStop.setStateButton(DraggableLockButton.StateButton.BUTTON_READY)
        }
    }

    private var mLockButtonStateHuntCallback = object : DraggableLockButton.OnActionListener {
        override fun ready() {
            dialog.setTypes(prefRepository.getAllTypes().toMutableMap())
            dialog.setActionListener(mDialogTypeCallback)
            dialog.show(childFragmentManager, "typeDialog")
        }

        override fun reject() {

        }

        override fun accept() {
            stopHunt()
        }
    }

    private fun initUI(savedInstanceState: Bundle?) {
        with(viewBindingRoot) {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@MapFragment)

            btnStop.setActionListener(mLockButtonStateHuntCallback)

            fabShowTypesMaps.setOnClickListener {
                showDialogTypeMap()
            }

            fabShowOfflineMaps.setOnClickListener {
                showDownloadRegionDialog()
            }

            fabFindLocation.setOnClickListener {
                mMap.locationComponent.locationEngine?.let {
                    mMapZoomCurrent = 14.0
                    enableSelfLocation()
                }
            }

            fabZoomInCameraMap.setOnClickListener {
                inZoomCamera()
            }

            fabZoomOutCameraMap.setOnClickListener {
                outZoomCamera()
            }

            fabSaveRegion.setOnClickListener {
                saveBoundedMap()
            }
        }
    }

    private fun showDialogTypeMap() {
        DialogAcceptTypeMap().show(childFragmentManager, "tag")
    }

    private fun outZoomCamera() {
        mMapZoomCurrent--

        val position = CameraPosition.Builder()
            .target(mMap.cameraPosition.target) // Sets the new camera position
            .bearing(mMap.cameraPosition.bearing)
            .tilt(mMap.cameraPosition.tilt)
            .zoom(mMapZoomCurrent) // Sets the zoom
            .build() // Creates a CameraPosition from the builder

        mMap.animateCamera(
            CameraUpdateFactory
                .newCameraPosition(position), 1000
        )
    }

    private fun inZoomCamera() {
        mMapZoomCurrent++

        val position = CameraPosition.Builder()
            .target(mMap.cameraPosition.target) // Sets the new camera position
            .bearing(mMap.cameraPosition.bearing)
            .tilt(mMap.cameraPosition.tilt)
            .zoom(mMapZoomCurrent) // Sets the zoom
            .build() // Creates a CameraPosition from the builder

        mMap.animateCamera(
            CameraUpdateFactory
                .newCameraPosition(position), 900
        )
    }


    // передвижение камеры за маркером
    private fun moveCameraTo(lat: Double, lng: Double, zoom: Double) {
        val position = CameraPosition.Builder()
            .target(LatLng(lat, lng)) // Sets the new camera position
            .bearing(mMap.cameraPosition.bearing)
            .tilt(mMap.cameraPosition.tilt)
            .zoom(zoom) // Sets the zoom
            .build() // Creates a CameraPosition from the builder

        mMap.animateCamera(
            CameraUpdateFactory
                .newCameraPosition(position), 2000
        )
    }

    private fun startService() {

        clearMapBox()

        val intentFilter = IntentFilter().apply {
            addAction(LocationForegroundService.ACTION_RECIEVER_LOCATION)
        }

        requireContext().registerReceiver(locationReceiver, intentFilter)

        val serviceIntent = Intent(requireContext(), LocationForegroundService::class.java).apply {
            putExtra("inputExtra", "Охота началась!")
        }

        ContextCompat.startForegroundService(requireContext(), serviceIntent)
    }

    private fun stopService() {

        clearMapBox()

        try {
            val serviceIntent = Intent(requireContext(), LocationForegroundService::class.java)
            requireContext().stopService(serviceIntent)
            requireContext().unregisterReceiver(locationReceiver)
        } catch (e: IllegalArgumentException) {
            // ignore =(
        }
    }

    private fun initSettings() {
        prefRepository.registerOnSharedPreferenceChangeListener(this)
    }

    private fun readProfileFromSettings() {
        selfProfile = prefRepository.readOwner()
    }


    private fun saveBoundedMap() {
        showDialogForDownloadMap()
    }

    // перемещение камеры на очку скачанной карты
    private val clickElementCallback = object : DialogLoadedMapsFragment.OnActionListener {
        override fun select(map: NamedOfflineRegion) {
            with(map.offlineRegion.definition.bounds) {
                mMapZoomCurrent = 12.0
                moveCameraTo(
                    (latNorth + latSouth) / 2,
                    (lonEast + lonWest) / 2,
                    mMapZoomCurrent
                )
            }

        }
    }

    private fun showDownloadRegionDialog() {
        DialogLoadedMapsFragment(clickElementCallback).show(childFragmentManager, "tag")
    }

    // хранит текущее отображение карты
    private val getDefinitionVisibleMap
        get() : OfflineTilePyramidRegionDefinition? {
            mMap.style?.let {
                return OfflineTilePyramidRegionDefinition(
                    it.uri,
                    mMap.projection.visibleRegion.latLngBounds,
                    mMap.cameraPosition.zoom,
                    mMap.maxZoomLevel,
                    resources.displayMetrics.density
                )
            }
            return null
        }

    private fun showDialogForDownloadMap() {
        // получаем папраметры карты которая сейчас отображена на экране
        getDefinitionVisibleMap?.let { definition ->
            DialogStartLoadingMap(definition).show(childFragmentManager, "dialogSaveMap")
        }
    }

    override fun onResume() {
        super.onResume()
        viewBindingRoot.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()

        viewBindingRoot.mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        viewBindingRoot.mapView.onPause()
        mapService.onPause()

    }

    override fun onStop() {
        super.onStop()
        viewBindingRoot.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBindingRoot.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding?.mapView?.onDestroy()
        _viewBinding?.btnStop?.destroy()
        prefRepository.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
        huntListener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewBindingRoot.mapView.onSaveInstanceState(outState)
        outState.putBoolean(
            Constants.KEY_REQUESTING_LOCATION_UPDATES,
            requestingLocationUpdates
        )
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mStyleMap = prefRepository.readLocalMapSetting()
        loadMap(mapboxMap, mStyleMap)
    }

    private fun loadMap(mapboxMap: MapboxMap, styles: String) {
        mapboxMap.setStyle(styles) {
            mMap = mapboxMap
            mapIsReadyToUse = true
            it.addImage(iconHunter, drawableIcon)
            style = it
            symbolManager = SymbolManager(viewBindingRoot.mapView, mMap, it).apply {
                iconAllowOverlap = true
                iconIgnorePlacement = true
            }
            lineManager = LineManager(viewBindingRoot.mapView, mMap, it)


            mapboxMap.locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.Builder(
                    requireContext(),
                    it
                ).build()
            )

            enableSelfLocation()
        }
    }


    private val callbackCamera = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            result?.let { location ->
                location.lastLocation?.let {
                    moveCameraTo(
                        it.latitude,
                        it.longitude,
                        mMapZoomCurrent
                    )
                }


                // убираем автонаведение за позицией юзера
                removeCallback()
            }
        }

        override fun onFailure(exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun removeCallback() {
        mMap.locationComponent.locationEngine?.removeLocationUpdates(callbackCamera)
    }

    private fun enableSelfLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        val locationRequest: LocationEngineRequest =
            LocationEngineRequest.Builder(2000)
                .build()
//
//        mMap.locationComponent.locationEngine?.requestLocationUpdates(
//            locationRequest,
//            callbackCamera,
//            Looper.getMainLooper()
//        )

        mMap.locationComponent.locationEngine?.getLastLocation(callbackCamera)


        val locationComponentOptions: LocationComponentOptions =
            LocationComponentOptions.builder(requireContext())
                .elevation(5F)
                .trackingGesturesManagement(false)
                .accuracyAlpha(.3f)
                .accuracyColor(R.color.main_background)
                .backgroundTintColor(R.color.selected_item)
                .build()

        mMap.getStyle {
            val locationComponentActivationOptions: LocationComponentActivationOptions =
                LocationComponentActivationOptions
                    .builder(requireContext(), it)
                    .locationComponentOptions(locationComponentOptions)
                    .build()

            mMap.locationComponent.activateLocationComponent(
                locationComponentActivationOptions)
            mMap.locationComponent.isLocationComponentEnabled = true
            mMap.locationComponent.renderMode = RenderMode.COMPASS
            mMap.locationComponent.cameraMode = CameraMode.TRACKING_COMPASS
        }

        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.logoGravity = Gravity.CENTER_HORIZONTAL


    }

    // очищает карту от маркеров и полигонов
    private fun clearMapBox() {
        symbolManager?.deleteAll()
        lineManager?.deleteAll()
        lineMap.clear()
        linePositions.clear()
        mMarkers.clear()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        when (key) {
            "type_map_key" -> {
                val mapStyle =
                    prefRepository.getStyleMap(sharedPreferences?.getString(key, mStyleMap) ?: "")
                mMap.setStyle(mapStyle)
            }
            "key_profile_name" -> {
                selfProfile = prefRepository.readOwner()
            }
            "key_type_player" -> {
                selfProfile = prefRepository.readOwner()
            }
        }
    }


    // получили СВОИ координаты от GPS модуля и делаем запрос
    fun startRequestTeamLocation(latitude: Double, longitude: Double) {
        // обновить карту или передать свои координаты на сервер
        //drawSelfPositions(latitude, longitude)

        huntingViewModel?.updatePositions(
            email = authService.appAccount?.email.toString(),
            lat = latitude,
            lng = longitude
        )?.observe(viewLifecycleOwner, { state ->
            renderUiState(state)
        })
    }

    // TODO(после смены команды сразу не переключается и сожет отправляться старая команда wtf!)
    private fun renderUiState(state: HuntingDataState) {
        Log.e(javaClass.simpleName, "renderUiState: ")
        when (state) {
            is HuntingDataState.Result -> {
                startDrawPlayersOnTheMap(state.result)
            }
            is HuntingDataState.Error -> {
                stopHunt()
                when (state.error) {
                    is PinNotFoundException -> {
                        showMessage("Pin код отсутствует!")
                        showDialogEnterPinCodeTeam()
                    }
                    is IllegalAuthStateException -> {
                        showMessage("Неверный pin код!")
                        showDialogEnterPinCodeTeam()
                    }
                    is TeamNotFoundException -> {
                        showMessage("Команды с таким номером не существует!")
                    }
                }
            }
        }
    }

    fun startHunt() {
        startService()
        viewBindingRoot.btnStop.setStateButton(DraggableLockButton.StateButton.BUTTON_LOCKED)
        huntListener?.onStartHunt()

        // hide ui
        with(viewBindingRoot) {
            fabSaveRegion.hide()
            fabShowOfflineMaps.hide()
            fabShowTypesMaps.hide()
            mMap.uiSettings.isCompassEnabled = true
        }
    }

    private fun stopHunt() {
        stopService()
        viewBindingRoot.btnStop.setStateButton(DraggableLockButton.StateButton.BUTTON_READY)
        huntListener?.onStopHunt()
        huntingViewModel?.observePosition?.removeObservers(viewLifecycleOwner)

        // show ui
        with(viewBindingRoot) {
            fabSaveRegion.show()
            fabShowOfflineMaps.show()
            fabShowTypesMaps.show()
            mMap.uiSettings.isCompassEnabled = false
        }
    }

    private val callbackEnterPinTeam = object : DialogEnterPinTeam.OnActionListener {
        override fun cancel() {
            viewBindingRoot.btnStop.setStateButton(DraggableLockButton.StateButton.BUTTON_READY)
        }

        override fun accept() {
            viewBindingRoot.btnStop.setStateButton(DraggableLockButton.StateButton.BUTTON_READY)
        }
    }

    private fun showDialogEnterPinCodeTeam() {
        DialogEnterPinTeam(callbackEnterPinTeam).show(childFragmentManager, "dialogEnterPin")
    }

    // отрисовка обстановки
    private fun startDrawPlayersOnTheMap(responsePlayers: List<ResponseTeamPositionItem>) {
        responsePlayers.forEach { player ->

            if (!player.isNotNull()) return@forEach

            // сохраняем траектории для всех участников
            try {
                if (!lineMap.containsKey(player.email)) {
                    // объекта линии нет, создаем новую
                    linePositions[player.email] = mutableListOf()
                    lineMap[player.email] = LineOptions()
                        .withLatLngs(linePositions[player.email])
                        .withLineColor(player.colorTrack())
                        .withLineWidth(4f)
                }

                // линия уже есть
                // добавляем в конец новые координаты
                linePositions[player.email]?.add(
                    LatLng(player.lat!!, player.lng!!)
                )
                lineMap[player.email]?.withLatLngs(linePositions[player.email])

                // перерисовываем
                lineManager?.create(lineMap[player.email])
                lineManager?.updateSource()

            } catch (e: NumberFormatException) {
                showMessage("Некорректные широта / долгота с сервера!")
            }

            updateMarkerPosition(player)
        }
    }


    // обновляем маркеры других (upd теперь и своих)
    private fun updateMarkerPosition(player: ResponseTeamPositionItem) {

        if (!player.isNotNull()) return

        if (!mMarkers.containsKey(player.email)) {
            try {
                mMarkers[player.email] = symbolManager?.create(
                    SymbolOptions()
                        .withLatLng(LatLng(player.lat!!.toDouble(), player.lng!!.toDouble()))
                        .withTextField(player.nickToMarker())
                        .withTextOffset(arrayOf(0.0F, 1.3F))
                        .withTextSize(14F)
                        .withIconImage(iconHunter)
                ) ?: SymbolManager(viewBindingRoot.mapView, mMap, style!!).create(
                    SymbolOptions()
                        .withLatLng(LatLng(player.lat!!.toDouble(), player.lng!!.toDouble()))
                        .withTextField(player.nickToMarker())
                        .withTextOffset(arrayOf(0.0F, 1.3F))
                        .withTextSize(14F)
                        .withIconImage(iconHunter)
                )
            } catch (e: NumberFormatException) {
                showMessage("Некорректные широта / долгота с сервера!")
            }
        } else {
            try {
                mMarkers[player.email]?.latLng =
                    LatLng(player.lat!!.toDouble(), player.lng!!.toDouble())
                symbolManager?.update(mMarkers[player.email])
            } catch (e: NumberFormatException) {
                showMessage("Некорректные широта / долгота с сервера!")
            }
        }
    }


    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            Constants.ACTION_NAVIGATE_MAP_CAMERA -> {
//                val latitude = result.getDouble("latitude")
//                val longitude = result.getDouble("longitude")
                //moveCameraTo(latitude, longitude)
            }
        }
    }


    // RECEIVER LOCATION FROM SERVICE
    inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.hasExtra("isStopped") && it.getBooleanExtra("isStopped", true)
                ) {
                    stopHunt()
                    viewBindingRoot.btnStop.setStateButton(DraggableLockButton.StateButton.BUTTON_ACCEPTED_AND_READY)
                } else {
                    val lng: Double = it.getDoubleExtra("longitude", 0.0)
                    val lat: Double = it.getDoubleExtra("latitude", 0.0)
                    startRequestTeamLocation(lat, lng)
                }
            }
        }

        override fun peekService(myContext: Context?, service: Intent?): IBinder {
            return super.peekService(myContext, service)
        }
    }
}
