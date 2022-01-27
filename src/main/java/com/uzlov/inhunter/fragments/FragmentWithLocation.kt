package com.uzlov.inhunter.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.uzlov.inhunter.BuildConfig
import com.uzlov.inhunter.R
import com.uzlov.inhunter.interfaces.Constants
import com.uzlov.inhunter.utils.showShack
import android.os.Build
import androidx.core.content.ContextCompat.checkSelfPermission


open class FragmentWithLocation : Fragment() {

    private val TAG = javaClass.simpleName
    protected var requestingLocationUpdates: Boolean = false
    private val REQUEST_CODE_ASK_PERMISSIONS = 1002

    protected fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (shouldProvideRationale) {
            Log.i("TAG", "Displaying permission rationale to provide additional context.")

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val res: Int = checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)
            if (res != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), 123)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == Constants.REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i("TAG", "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    if (requestingLocationUpdates) {
                        Log.i(
                            TAG,
                            "Permission granted, updates requested, starting location updates"
                        )
                    }
                }
                else -> {
                    // Permission denied.
                    showShack(
                        R.string.permission_denied_explanation,
                        R.string.settings
                    ) { // Build intent that displays the App settings screen.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }

                        startActivity(intent)
                    }
                }
            }
        }

        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Log.e(TAG, "onRequestPermissionsResult: GRANTED")
            } else {
                Log.e(TAG, "onRequestPermissionsResult: NOT GRANTED")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.i(
                    TAG,
                    "User agreed to make required location settings changes."
                )
                Activity.RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    requestingLocationUpdates = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!checkPermissions()) {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}


