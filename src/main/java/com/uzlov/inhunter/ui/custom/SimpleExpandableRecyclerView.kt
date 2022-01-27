package com.uzlov.inhunter.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uzlov.inhunter.R

class SimpleExpandableRecyclerView : FrameLayout {

    private val TAG: String = javaClass.simpleName
    private var isExpand = true

    private var recyclerViewItems: RecyclerView? = null
    private var tvCurrentItem: TextView? = null
    private var iconDropDown: ImageView? = null

    private val titleCard: String

    private var selectListener: ExpandableCardViewAdapter.OnItemSelectListener? = object : ExpandableCardViewAdapter.OnItemSelectListener {
        override fun itemSelect(pos: Int, item: String) {
            stateListener?.itemSelect(item)
        }
    }
    private val adapter: ExpandableCardViewAdapter by lazy {
        ExpandableCardViewAdapter(selectListener)
    }

    interface StateListener{
        fun expand()
        fun squeeze()
        fun itemSelect(item: String)
    }

    private var stateListener: StateListener? = null

    fun setStateListener(l: StateListener){
        stateListener  = l
    }

    private var view: View? = inflate(context, R.layout.ex_view_card, this)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context,
        attrs,
        defStyle) {

        recyclerViewItems = findViewById(R.id.rvItems)
        recyclerViewItems?.layoutManager = LinearLayoutManager(context)
        recyclerViewItems?.adapter = adapter

        iconDropDown = findViewById(R.id.iconDropdownAction)
        tvCurrentItem = findViewById(R.id.currentItem)

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SimpleExpandableView
        )

        titleCard = typedArray.getString(R.styleable.SimpleExpandableView_simpleExTitle) ?: "Title"
        tvCurrentItem?.text = titleCard

        iconDropDown?.setOnClickListener {
            changeState(true)
        }


        typedArray.recycle()
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {}

    fun setTitleItem(item: String) {
        tvCurrentItem?.text = item
        tvCurrentItem?.invalidate()
    }

    fun setItems(items: Collection<String>) {
        adapter.setItems(items)
        recyclerViewItems?.visibility = View.VISIBLE
    }

    // сворачивает карточку в определенный угол
    fun changeState(animate: Boolean = false) {

        if (isExpand) {
            squeeze()
        } else {
            expand()
        }
        invalidate()
        //change state view
        isExpand = !isExpand
    }

    private fun squeeze() {
        recyclerViewItems?.visibility = GONE
        stateListener?.squeeze()
    }

    private fun expand() {
        recyclerViewItems?.visibility = VISIBLE
        stateListener?.expand()
    }

    // destroy this view
    fun onDestroy() {
        recyclerViewItems = null
        tvCurrentItem = null
        iconDropDown = null
        view = null
        stateListener = null
        selectListener = null
    }
}