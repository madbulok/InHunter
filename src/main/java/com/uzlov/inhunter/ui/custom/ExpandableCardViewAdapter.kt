package com.uzlov.inhunter.ui.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uzlov.inhunter.databinding.SimpleTextItemBinding

class ExpandableCardViewAdapter(
    private val selectListener: OnItemSelectListener? = null
) : RecyclerView.Adapter<ExpandableCardViewAdapter.ItemHolder>() {

    // callback for user select item
    interface OnItemSelectListener {
        fun itemSelect(pos: Int, item: String)
    }

    private var _viewBinding: SimpleTextItemBinding? = null
    private val viewBinding get() = _viewBinding!!

    private val listItem: MutableList<String> = mutableListOf()

    fun setItems(items: Collection<String>) {
        listItem.clear()
        listItem.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        _viewBinding = SimpleTextItemBinding.inflate(inflater, parent, false)
        return ItemHolder(viewBinding)
    }

    override fun getItemCount(): Int = listItem.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = holder.bind(listItem[position])


    // hold view item
    inner class ItemHolder(private val binding: SimpleTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(value: String) {
            with(binding) {
                tvItemTitle.text = value
                root.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        selectListener?.itemSelect(adapterPosition, value)
                    }
                }
            }
        }
    }
}