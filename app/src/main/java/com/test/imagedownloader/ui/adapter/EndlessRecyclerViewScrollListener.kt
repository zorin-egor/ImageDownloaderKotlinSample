package com.test.imagedownloader.ui.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class EndlessRecyclerViewScrollListener : RecyclerView.OnScrollListener() {

    companion object {
        private val VISIBLE_THRESHOLD = 5
    }

    private var mPreviousTotalItemCount = 0
    private var mLoadingMode = true

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            if (mLoadingMode) {
                if (totalItemCount >= mPreviousTotalItemCount) {
                    mLoadingMode = false
                    mPreviousTotalItemCount = totalItemCount
                }
            }

            if (!mLoadingMode && visibleItemCount + firstVisibleItemPosition >= totalItemCount - VISIBLE_THRESHOLD) {
                mLoadingMode = true
                onListEnd()
            }
        }
    }

    abstract fun onListEnd()

}