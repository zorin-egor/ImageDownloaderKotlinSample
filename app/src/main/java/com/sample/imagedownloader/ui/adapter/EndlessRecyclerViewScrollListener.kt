package com.sample.imagedownloader.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessRecyclerViewScrollListener : RecyclerView.OnScrollListener() {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private var mIsDown = false

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            (recyclerView.layoutManager as? LinearLayoutManager)?.let { manager ->
                val visibleItemCount = manager.childCount
                val totalItemCount = manager.itemCount
                val firstVisibleItemPosition = manager.findFirstVisibleItemPosition()
                if (mIsDown && visibleItemCount + firstVisibleItemPosition >= totalItemCount - VISIBLE_THRESHOLD) {
                    mIsDown = false
                    onListEnd()
                }
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        mIsDown = dy > 0
    }

    abstract fun onListEnd()

}