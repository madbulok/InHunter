package com.uzlov.inhunter.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.uzlov.inhunter.R
import com.uzlov.inhunter.databinding.DialogSelectTypeLayoutBinding

class DialogAcceptTypePlayer constructor(
    private val mapTypes: MutableMap<String, Boolean> = mutableMapOf(),
    private var listenerAction: OnActionListener? = null,
) : DialogFragment() {

    private var _viewBinding: DialogSelectTypeLayoutBinding? = null
    private val viewBinding: DialogSelectTypeLayoutBinding get() = _viewBinding!!
    private var typesAdapter: AdapterTypes? = AdapterTypes()


    interface OnActionListener {
        fun select(type: String)
        fun start()
        fun cancel()
    }

    fun setActionListener(_listenerAction: OnActionListener) {
        listenerAction = _listenerAction
    }

    fun removeActionListener() {
        listenerAction = null
    }

    fun setTypes(_mapTypes: MutableMap<String, Boolean>){
        mapTypes.clear()
        mapTypes.putAll(_mapTypes)
        if (typesAdapter == null) typesAdapter = AdapterTypes()
        typesAdapter?.setTypes(mapTypes.keys)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _viewBinding = DialogSelectTypeLayoutBinding.inflate(layoutInflater)
        isCancelable = false
        with(viewBinding) {
            typesAdapter?.setTypes(mapTypes.keys)
            rvListMaps.adapter = typesAdapter
            tvLabelDialog.setOnClickListener {
                listenerAction?.cancel()
                removeActionListener()
                dismiss()
            }

            startBtn.setOnClickListener {
                if (mapTypes.isNullOrEmpty() || !mapTypes.values.contains(true)) {
                    Toast.makeText(requireContext(), "Не выбран тип участника", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                listenerAction?.start()
                removeActionListener()
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(viewBinding.root)
            .create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        removeActionListener()
        typesAdapter = null
        _viewBinding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeActionListener()
        typesAdapter = null
        _viewBinding = null
    }

    fun reselect(allTypes: Map<String, Boolean>) {
        mapTypes.clear()
        mapTypes.putAll(allTypes)
        typesAdapter?.notifyDataSetChanged()
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
                .inflate(R.layout.type_item_layout, parent, false)
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
                tvNameType.setCompoundDrawablesWithIntrinsicBounds(viewBinding.root.context.getDrawable(R.drawable.ic_check_selected), null, null, null)
            } else {
                tvNameType.setTextColor(resources.getColor(R.color.black))
                tvNameType.setCompoundDrawablesWithIntrinsicBounds(viewBinding.root.context.getDrawable(R.drawable.ic_check_not_selected), null, null, null)
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
