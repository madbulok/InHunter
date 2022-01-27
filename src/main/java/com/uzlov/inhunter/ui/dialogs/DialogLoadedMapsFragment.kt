package com.uzlov.inhunter.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uzlov.inhunter.R
import com.uzlov.inhunter.data.model.NamedOfflineRegion
import com.uzlov.inhunter.databinding.DialogLoadedMapsLayoutBinding
import com.uzlov.inhunter.map.LoadableRegion
import com.uzlov.inhunter.map.MapService

class DialogLoadedMapsFragment(private var listenerAction: OnActionListener? = null) : DialogFragment() {
    private var _viewBinding: DialogLoadedMapsLayoutBinding? = null
    private val viewBinding: DialogLoadedMapsLayoutBinding get() = _viewBinding!!

    private var mapService: MapService? = null
    private var adapterRegion : AdapterRegion? = null

    // взаимодействие с внешним миром
    interface OnActionListener {
        fun select(map: NamedOfflineRegion)
    }

    fun removeActionListener() {
        listenerAction = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapService = MapService(requireContext())
        adapterRegion = AdapterRegion()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBinding = DialogLoadedMapsLayoutBinding.inflate(layoutInflater)
        isCancelable = false
        loadMaps()
        with(viewBinding) {
            rvListMaps.adapter = adapterRegion
            rvListMaps.layoutManager = LinearLayoutManager(requireContext())
            tvLabelDialog.setOnClickListener {
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(viewBinding.root)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }

    //    загрузка сохраненных карт
    private fun loadMaps() {
        showLoading()
        mapService?.downloadedRegionsArray(object : LoadableRegion<NamedOfflineRegion> {
            override fun loadRegion(regions: List<NamedOfflineRegion>) {
                if (regions.isNullOrEmpty()) {
                    showEmptyResult()
                } else {
                    showResult(regions)
                }
            }

            override fun onError(message: String) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(){
        with(viewBinding){
            pgLoadSavedMaps.visibility = View.VISIBLE
            tvEmptyLoadedMap.visibility = View.GONE
            rvListMaps.visibility = View.INVISIBLE
        }
    }

    private fun showResult(regions: List<NamedOfflineRegion>) {
        with(viewBinding){
            pgLoadSavedMaps.visibility = View.GONE
            tvEmptyLoadedMap.visibility = View.GONE
            rvListMaps.visibility = View.VISIBLE
        }
        adapterRegion?.addRegion(regions)
    }

    private fun showEmptyResult() {
        with(viewBinding){
            pgLoadSavedMaps.visibility = View.GONE
            rvListMaps.visibility = View.INVISIBLE
            tvEmptyLoadedMap.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapService = null
        removeActionListener()
        _viewBinding = null
        adapterRegion = null
    }

    private inner class AdapterRegion : RecyclerView.Adapter<AdapterRegion.RegionViewHolder>() {

        private var itemsRegion : MutableList<NamedOfflineRegion> = mutableListOf()

        fun addRegion(items : List<NamedOfflineRegion>){
            itemsRegion.clear()
            itemsRegion.addAll(items)
            notifyDataSetChanged()
        }

        fun deleteRegion(pos : Int){
            itemsRegion.removeAt(pos)
            notifyItemRemoved(pos)
            if (itemsRegion.isEmpty()) showEmptyResult()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.region_item, parent, false)
            return RegionViewHolder(v)
        }

        override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
            holder.onBind(itemsRegion[position])
        }

        override fun getItemCount(): Int  = itemsRegion.size

        private inner class RegionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            private var tvNameRegion : TextView = itemView.findViewById(R.id.tvNameRegion)
            private var btnRemoveRegion : ImageView = itemView.findViewById(R.id.btnRemoveRegion)

            fun onBind(region: NamedOfflineRegion){
                tvNameRegion.text = region.nameRegion

                btnRemoveRegion.setOnClickListener {
                    mapService?.deleteRegion(region.offlineRegion)
                    deleteRegion(adapterPosition)
                }

                itemView.setOnClickListener {
                    // maybe open maps?
                    if ( adapterPosition != RecyclerView.NO_POSITION) {
                        listenerAction?.select(region)
                    }
                }
            }
        }
    }
}