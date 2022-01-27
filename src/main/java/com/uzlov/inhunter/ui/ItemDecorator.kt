package com.uzlov.inhunter.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorator(@RecyclerView.Orientation var orientation: Int = RecyclerView.VERTICAL) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        when(orientation) {
            RecyclerView.HORIZONTAL -> outRect.set(Rect(16, 0, 16, 0))
            RecyclerView.VERTICAL -> outRect.set(Rect(0, 16, 0, 16))
            else -> outRect.set(Rect(16, 16, 16, 16))
        }
    }
}