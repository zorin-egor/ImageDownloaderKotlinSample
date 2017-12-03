package com.test.imagedownloader.ui.fragment

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.test.imagedownloader.R
import com.test.imagedownloader.manager.rest.DataObject
import com.test.imagedownloader.manager.rest.RestManager
import com.test.imagedownloader.ui.adapter.EndlessRecyclerViewScrollListener
import com.test.imagedownloader.ui.adapter.ImageAdapter
import kotlinx.android.synthetic.main.fragment_list.view.*

class ImageFragment : BaseFragment(), RestManager.OnResult, SwipeRefreshLayout.OnRefreshListener {

    companion object {

        val TAG = BaseFragment::class.java.simpleName
        private val TAG_ID = "TAG_ID"
        private val TAG_LOAD = "TAG_LOAD"
        private val URL = "https://api.github.com/users"
        private val URL_ARGS = "?since="
        private val URL_DEFAULT_SINCE = "1"

        fun newInstance(): ImageFragment {
            return ImageFragment()
        }
    }

    private var mIsFirstLoad = false
    private lateinit var mView: View
    private var mListAdapter = ImageAdapter()
    private var mLastId: String = URL_DEFAULT_SINCE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_list, container, false)
        init(savedInstanceState)
        return mView
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(TAG_ID, mLastId)
        outState?.putBoolean(TAG_LOAD, mIsFirstLoad)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RestManager.getInstance().removeCallback()
    }

    override fun onRefresh() {
        if (!mIsFirstLoad) {
            RestManager.getInstance().exec(URL)
        }
    }

    override fun onBegin() {
        mView.mSwipeListView.isRefreshing = false
        mView.mLoadingProgress.visibility = View.VISIBLE
    }

    override fun onFinish(dataList: List<DataObject>?) {
        mView.mLoadingProgress.visibility = View.GONE
        if (dataList != null && dataList.size > 0) {
            mIsFirstLoad = true
            mView.mSwipeListView.isEnabled = false
            mLastId = dataList.get(dataList.size - 1).id!!
            mListAdapter.addItems(dataList)
        }
    }

    override fun onProgress(progress: Int) {

    }

    override fun onError(message: String?) {
        activity.runOnUiThread({
            mView.mLoadingProgress.visibility = View.GONE
            showToast(getString(R.string.app_error).plus(message))
        })
    }

    private fun init(savedInstanceState: Bundle?) {
        mView.mSwipeListView.setOnRefreshListener(this)
        mView.mListView.layoutManager = LinearLayoutManager(context)
        mView.mListView.adapter = mListAdapter
        mView.mLoadingProgress.visibility = View.GONE
        mView.mListView.addOnScrollListener(object : EndlessRecyclerViewScrollListener() {
            override fun onListEnd() {
                RestManager.getInstance().exec(URL.plus(URL_ARGS).plus(mLastId))
            }
        })

        restore(savedInstanceState)
        exec(savedInstanceState)
    }

    private fun restore(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mLastId = savedInstanceState.getString(TAG_ID, URL_DEFAULT_SINCE)
            mIsFirstLoad = savedInstanceState.getBoolean(TAG_LOAD)
            mView.mSwipeListView.isEnabled = mIsFirstLoad
        }
    }

    private fun exec(savedInstanceState: Bundle?) {
        RestManager.getInstance().setCallback(this)
        if (savedInstanceState == null) {
            RestManager.getInstance().exec(URL)
        } else {
            mListAdapter.setItems(RestManager.getInstance().mCache)
        }
    }

}
