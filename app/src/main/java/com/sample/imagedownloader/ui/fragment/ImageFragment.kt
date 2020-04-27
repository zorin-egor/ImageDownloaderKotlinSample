package com.sample.imagedownloader.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sample.imagedownloader.R
import com.sample.imagedownloader.manager.ApiManager
import com.sample.imagedownloader.manager.DataObject
import com.sample.imagedownloader.manager.NetworkManager
import com.sample.imagedownloader.ui.adapter.EndlessRecyclerViewScrollListener
import com.sample.imagedownloader.ui.adapter.ImageAdapter
import kotlinx.android.synthetic.main.fragment_list.*

class ImageFragment : BaseFragment(), ApiManager.OnActionCallback, SwipeRefreshLayout.OnRefreshListener {

    companion object {

        val TAG = BaseFragment::class.java.simpleName

        private val TAG_ID = "TAG_ID"
        private val TAG_ADAPTER = "TAG_ADAPTER"

        private val URL = "https://api.github.com/users"
        private val URL_ARG = "?since="
        private val URL_DEFAULT_SINCE = "0"

        fun newInstance(): ImageFragment {
            return ImageFragment()
        }
    }

    private val mUrl: String
        get() = URL + URL_ARG + mSinceId

    private var mListAdapter = ImageAdapter()
    private var mSinceId: String = URL_DEFAULT_SINCE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putString(TAG_ID, mSinceId)
            putParcelableArrayList(TAG_ADAPTER, mListAdapter.items as ArrayList<out Parcelable>)
        }
    }

    override fun onStart() {
        super.onStart()
        ApiManager.getInstance().setCallback(this)
    }

    override fun onStop() {
        super.onStop()
        ApiManager.getInstance().removeCallback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ApiManager.getInstance().removeCallback()
    }

    override fun onRefresh() {
        if (NetworkManager.isOnline(requireContext())) {
            mListAdapter.clear()
            mSinceId = URL_DEFAULT_SINCE
            ApiManager.getInstance().exec(mUrl)
        } else {
            showToast(getString(R.string.app_error_network))
        }
    }

    override fun onBegin() {
        swipeRefreshLayout.isRefreshing = false
        progressBar.visibility = View.VISIBLE
    }

    override fun onResult(items: List<DataObject>?) {
        progressBar.visibility = View.GONE

        if (items?.isNotEmpty() == true) {
            mSinceId = items[items.size - 1].id ?: throw IllegalArgumentException("User id mustn't be null")
            mListAdapter.addItems(items)
        }
    }

    override fun onProgress(total: Int, progress: Int) {
        // Stub
    }

    override fun onError(error: Throwable?, message: String?) {
        progressBar.visibility = View.GONE
        showToast(getString(R.string.app_error_base) +
                (message ?: error?.message ?: getString(R.string.app_error_unknown)))
    }

    private fun init(savedInstanceState: Bundle?) {
        swipeRefreshLayout.setOnRefreshListener(this)
        progressBar.visibility = View.GONE
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mListAdapter
        recyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener() {
            override fun onListEnd() {
                ApiManager.getInstance().exec(mUrl)
            }
        })

        restore(savedInstanceState)
    }

    private fun restore(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            onRefresh()
        } else {
            mSinceId = savedInstanceState?.getString(TAG_ID, URL_DEFAULT_SINCE) ?: ""
            mListAdapter.items = savedInstanceState.getParcelableArrayList<DataObject>(TAG_ADAPTER) as ArrayList<DataObject>
        }
    }

}
