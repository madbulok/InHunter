package com.uzlov.inhunter.map

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.offline.*
import com.mapbox.mapboxsdk.offline.OfflineManager.ListOfflineRegionsCallback
import com.uzlov.inhunter.R
import com.uzlov.inhunter.data.model.NamedOfflineRegion
import org.json.JSONObject
import java.nio.charset.Charset
import javax.inject.Inject
import kotlin.math.roundToInt


class MapService @Inject constructor(private val mContext: Context) {

    private val JSON_FIELD_REGION_NAME: String = "JSON_FIELD_REGION_NAME"
    private val JSON_CHARSET: Charset = Charset.forName("UTF-8")
    private var offlineManager: OfflineManager


    init {
        Mapbox.getInstance(mContext, mContext.getString(R.string.mapbox_access_token))
        offlineManager = OfflineManager.getInstance(mContext)
    }

    fun saveRegion(name: String, definition: OfflineTilePyramidRegionDefinition, load: ISaveMap) {

        var metadata: ByteArray? = null
        try {
            val jsonObject = JSONObject()
            jsonObject.put(JSON_FIELD_REGION_NAME, name)
            val json = jsonObject.toString()
            metadata = json.toByteArray(charset("UTF-8"))

        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        metadata?.let {
            offlineManager.createOfflineRegion(definition, it,
                object : OfflineManager.CreateOfflineRegionCallback {
                    override fun onCreate(offlineRegion: OfflineRegion) {
                        launchDownload(load, offlineRegion)
                    }

                    override fun onError(error: String) {
                        Log.e(javaClass.simpleName, "Error: $error")
                        load.onError(error)
                    }
                })
        }
    }

    private fun launchDownload(load: ISaveMap, offlineRegion: OfflineRegion) {
        // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
            override fun onStatusChanged(status: OfflineRegionStatus) {
                // Compute a percentage
                val percentage =
                    if (status.requiredResourceCount >= 0) 100.0 * status.completedResourceCount / status.requiredResourceCount else 0.0
                Log.d(javaClass.simpleName, "onStatusChanged: $percentage")
                if (status.isComplete) {
                    // Download complete
                    load.onSuccess()
                } else if (status.isRequiredResourceCountPrecise) {
                    // Switch to determinate state
                    load.progressChanged(percentage.roundToInt())
                }
            }

            override fun onError(error: OfflineRegionError) {
                Log.e(javaClass.simpleName, "onError: ${error.message}" )
                load.onError("${error.message} ${error.reason}")
            }

            override fun mapboxTileCountLimitExceeded(limit: Long) {
                load.onError("Достигнут лимит скачивания : $limit")
            }
        })


        // Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
    }

    fun downloadedRegionsArray(loadableRegion: LoadableRegion<NamedOfflineRegion>) {
        // Query the DB asynchronously
        offlineManager.listOfflineRegions(object : ListOfflineRegionsCallback {
            override fun onList(offlineRegions: Array<OfflineRegion>) {
//                Add all of the region names to a list
                val offlineRegionsNames: MutableList<NamedOfflineRegion> = mutableListOf()
                for (offlineRegion in offlineRegions) {
                    offlineRegionsNames.add(NamedOfflineRegion(getRegionName(offlineRegion),
                        offlineRegion))
                }

                loadableRegion.loadRegion(regions = offlineRegionsNames)
            }

            override fun onError(error: String) {
                loadableRegion.onError(error)
            }
        })
    }

    fun getRegionName(offlineRegion: OfflineRegion): String {
        // Get the region name from the offline region metadata
        val regionName: String = try {
            val metadata = offlineRegion.metadata
            val json = String(metadata, JSON_CHARSET)
            val jsonObject = JSONObject(json)
            jsonObject.getString(JSON_FIELD_REGION_NAME)
        } catch (exception: Exception) {
            String.format(mContext.getString(R.string.region_name), offlineRegion.id)
        }
        return regionName
    }


    fun onPause() {

    }

    fun deleteRegion(region: OfflineRegion) {
        region.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
            override fun onDelete() {
                Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: String?) {

            }
        })
    }
}