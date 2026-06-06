package com.example.katzen.Helper

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object ListUiHelper {

    fun setupVerticalList(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        }
        configureRecyclerView(recyclerView)
    }

    fun setupGridList(recyclerView: RecyclerView, spanCount: Int = 2) {
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = GridLayoutManager(recyclerView.context, spanCount)
        }
        recyclerView.setHasFixedSize(false)
        recyclerView.itemAnimator = null
        recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    private fun configureRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null
        recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        val padding = recyclerView.resources.getDimensionPixelSize(
            com.ninodev.katzen.R.dimen.list_item_spacing_half
        )
        if (recyclerView.paddingLeft == 0 && recyclerView.paddingTop == 0) {
            recyclerView.setPadding(0, padding, 0, padding)
        }
    }

    fun restoreScrollIfPending(
        listKey: String,
        recyclerView: RecyclerView,
        itemIds: List<String>
    ) {
        ListScrollStateHelper.restoreIfPending(listKey, recyclerView, itemIds)
    }
}
