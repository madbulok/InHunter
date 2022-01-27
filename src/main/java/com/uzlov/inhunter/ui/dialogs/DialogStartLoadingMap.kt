package com.uzlov.inhunter.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.databinding.DialogInputMapLayoutBinding
import com.uzlov.inhunter.map.ISaveMap
import com.uzlov.inhunter.map.MapService
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class DialogStartLoadingMap(private val mapDefinition: OfflineTilePyramidRegionDefinition) :
    DialogFragment() {

    private var jobLoadMap: Job? = null
    private var viewBinding: DialogInputMapLayoutBinding? = null

    @Inject
    lateinit var mapService: MapService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().appComponent.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogInputMapLayoutBinding.inflate(layoutInflater)
        isCancelable = false
        val alert = AlertDialog.Builder(requireContext())
            .setView(viewBinding?.root)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

        initListeners()
        return alert
    }


    private fun initListeners() {
        viewBinding?.btnCancel?.setOnClickListener {
            dismiss()
        }

        viewBinding?.btnAccept?.setOnClickListener {
            viewBinding?.etNameRegionMap?.text?.let {
                if (it.trim().length < 4) {
                    viewBinding?.etNameRegionMap?.error = "Минимум 4 символа!"
                    return@setOnClickListener
                }
                downloadRegion(it.trim().toString())
            }
        }

        viewBinding?.tvLabelDialog?.setOnClickListener {
            dismiss()
        }
    }

    private fun downloadRegion(regionName: String) {
        startProgress()

        runBlocking {
            jobLoadMap = launch(Dispatchers.IO) {
                mapService.saveRegion(regionName, mapDefinition, object : ISaveMap {
                    override fun onSuccess() {
                        launch(Dispatchers.Main){
                            endProgress()
                        }
                    }

                    override fun progressChanged(percent: Int) {
                        launch(Dispatchers.Main){
                            setPercentage(percent)
                        }
                    }

                    override fun onError(message: String) {
                        launch(Dispatchers.Main){
                            showError(message)
                        }
                    }
                })
            }
        }

    }

    // Progress bar methods
    private fun startProgress() {
        // Start and show the progress bar
        viewBinding?.progressBarLoadingMap?.visibility = View.VISIBLE
        viewBinding?.layoutInputText?.visibility = View.GONE
        viewBinding?.btnAccept?.isEnabled = false
        viewBinding?.btnCancel?.isEnabled = false

    }

    private fun setPercentage(percentage: Int) {
        viewBinding?.progressBarLoadingMap?.progress = percentage
    }

    private fun endProgress() {
        viewBinding?.layoutInputText?.visibility = View.VISIBLE
        viewBinding?.btnAccept?.isEnabled = true
        viewBinding?.btnCancel?.isEnabled = true
        viewBinding?.progressBarLoadingMap?.visibility = View.GONE
    }

    private fun showError(message: String){
        endProgress()
        viewBinding?.etNameRegionMap?.error = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
        jobLoadMap?.cancel("Скачивание отменено пользователем!")
    }
}