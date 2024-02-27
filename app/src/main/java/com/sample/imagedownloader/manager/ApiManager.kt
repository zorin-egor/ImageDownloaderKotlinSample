package com.sample.imagedownloader.manager

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL


class ApiManager internal constructor() {

    companion object {

        val TAG = ApiManager::class.java.simpleName

        private const val TIMEOUT_READ = 8000
        private const val TIMEOUT_CONNECT = 8000
        private const val REQUEST_METHOD = "GET"

        private var apiManager = ApiManager()

        fun getInstance(): ApiManager {
            return apiManager
        }
    }

    interface OnActionCallback {
        fun onBegin()
        fun onResult(items: List<DataObject>?)
        fun onProgress(total: Int, progress: Int)
        fun onError(error: Throwable?, message: String? = null)
    }

    private var mScope = MainScope()
    private var mJob: Job? = null
    private var mCallbackRef: WeakReference<OnActionCallback>? = null
    private val mItems: MutableList<DataObject> = mutableListOf()

    private fun getData(url: String, onProgress: (Int, Int) -> Unit, onError: (String?, Throwable?) -> Unit): String? {
        var result: String? = null
        var connect: HttpURLConnection? = null

        try {
            connect  = URL(url).openConnection() as HttpURLConnection
            connect.readTimeout = TIMEOUT_READ
            connect.connectTimeout = TIMEOUT_CONNECT
            connect.requestMethod = REQUEST_METHOD
            connect.doOutput = false
            connect.connect()

            when(connect.responseCode) {
                HTTP_OK -> {
                    result = StringBuilder().also { builder ->
                        var time = System.currentTimeMillis()

                        connect.inputStream.reader().buffered().use { input ->
                            while(input.readLine()?.also { builder.append(it) } != null) {
                                val deltaTime = System.currentTimeMillis() - time
                                if (deltaTime > 200) {
                                    time += deltaTime
                                    onProgress(connect.contentLength, builder.count() * 8)
                                }
                            }
                        }

                        onProgress(connect.contentLength, builder.count() * 8)

                    }.toString()
                }

                else -> {
                    onError(connect.responseMessage, null)
                }
            }

        } catch (e: Throwable) {
            onError(null, e)
        } finally {
            connect?.disconnect()
        }

        Log.d(TAG, result ?: "-")
        return result
    }

    fun exec(url: String) {
        mJob = mScope.launch {
            try {
                // Ui begin
                mCallbackRef?.get()?.onBegin()

                // Thread
                withContext(Dispatchers.IO) {
                    getData(url, { total, progress ->
                        this@launch.launch {
                            mCallbackRef?.get()?.onProgress(0, progress)
                        }
                    }, { message, error ->
                        this@launch.launch {
                            mCallbackRef?.get()?.onError(error, message)
                        }
                    })?.also { json ->
                        GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create()
                                .fromJson<List<DataObject>>(json, object : TypeToken<MutableList<DataObject>>(){}.type)?.let { items ->
                                    mItems.addAll(items)
                                }
                    }
                }

                // Ui end
                mCallbackRef?.get()?.let {
                    it.onResult(mItems.toList())
                    mItems.clear()
                }

            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    mCallbackRef?.get()?.onError(e)
                }
            }
        }
    }

    fun cancel() {
        mScope.coroutineContext.cancelChildren()
    }

    fun setCallback(callback: OnActionCallback): ApiManager {
        mCallbackRef = WeakReference(callback)
        if (mJob?.isActive == false && mItems.isNotEmpty()) {
            callback.onResult(mItems.toList())
            mItems.clear()
        }
        return this
    }

    fun removeCallback(): ApiManager {
        mCallbackRef = null
        return this
    }
}



