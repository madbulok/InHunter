package com.uzlov.inhunter.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.uzlov.inhunter.R
import com.uzlov.inhunter.app.appComponent
import com.uzlov.inhunter.data.local.pref.PreferenceRepository
import com.uzlov.inhunter.databinding.DialogSelectMapTypeLayoutBinding
import com.uzlov.inhunter.databinding.DialogSelectTypeLayoutBinding
import javax.inject.Inject

class DialogAcceptTypeMap : DialogFragment() {

    private var _viewBinding: DialogSelectMapTypeLayoutBinding? = null
    private val viewBinding: DialogSelectMapTypeLayoutBinding get() = _viewBinding!!
    private var typesAdapter: AdapterTypes? = AdapterTypes()
    private val mapTypes: MutableMap<String, Boolean> = mutableMapOf()

    @Inject
    lateinit var prefRepository: PreferenceRepository

    interface OnActionListener {
        fun select(type: String)
    }

    private var listenerAction: OnActionListener? = object : OnActionListener {
        override fun select(type: String) {
            prefRepository.setActiveMapType(type)
            loadData()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBinding = DialogSelectMapTypeLayoutBinding.inflate(layoutInflater)
        requireContext().appComponent.inject(this)
        with(viewBinding) {
            typesAdapter?.setTypes(mapTypes.keys)
            rvListMaps.adapter = typesAdapter
            tvLabelDialog.setOnClickListener {
                listenerAction = null
                dismiss()
            }
        }

        loadData()

        return AlertDialog.Builder(requireContext())
            .setView(viewBinding.root)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }

    private fun loadData() {
        val listMap = prefRepository.readMapTypes()
        listMap.forEach {
            mapTypes[it] = it == prefRepository.getActiveMapType()
        }
        typesAdapter?.setTypes(listMap)
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listenerAction = null
        typesAdapter = null
        _viewBinding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerAction = null
        typesAdapter = null
        _viewBinding = null
    }

    inner class AdapterTypes : RecyclerView.Adapter<TypeViewHolder>() {

        private var typesPlayer: MutableList<String> = mutableListOf()

        fun setTypes(items: Collection<String>) {
            typesPlayer.clear()
            typesPlayer.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.team_item_layout, parent, false)
            return TypeViewHolder(v)
        }

        override fun onBindViewHolder(holder: TypeViewHolder, position: Int) =
            holder.onBind(typesPlayer[position], mapTypes[typesPlayer[position]] ?: false)

        override fun getItemCount() = typesPlayer.size
    }


    inner class TypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var tvNameType: MaterialTextView = itemView.findViewById(R.id.tvNameTeam)

        fun onBind(type: String, activated: Boolean) {
            tvNameType.text = type
            if (activated) {
                tvNameType.setTextColor(resources.getColor(R.color.selected_item))
                tvNameType.setCompoundDrawablesWithIntrinsicBounds(viewBinding.root.context.getDrawable(
                    R.drawable.ic_check_selected), null, null, null)
            } else {
                tvNameType.setTextColor(resources.getColor(R.color.black))
                tvNameType.setCompoundDrawablesWithIntrinsicBounds(viewBinding.root.context.getDrawable(
                    R.drawable.ic_check_not_selected), null, null, null)
            }

            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    tvNameType.setTextColor(resources.getColor(R.color.selected_item))
                    listenerAction?.select(type)
                }
            }
        }
    }
}
