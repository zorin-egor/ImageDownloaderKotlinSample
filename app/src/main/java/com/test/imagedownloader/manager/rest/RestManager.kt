package com.test.imagedownloader.manager.rest

import android.os.AsyncTask
import android.util.Log
import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK
import java.net.URL


class RestManager internal constructor() {

    companion object {
        val TAG = RestManager::class.java.simpleName

        private val TIMEOUT_READ = 8000
        private val TIMEOUT_CONNECT = 8000
        private val REQUEST_METHOD = "GET"
        private var sRestManger: RestManager? = null

        fun getInstance(): RestManager {
            if (sRestManger == null) {
                sRestManger = RestManager()
            }
            return sRestManger!!
        }
    }

    interface OnResult {
        fun onBegin()
        fun onFinish(dataList: List<DataObject>?)
        fun onProgress(progress: Int)
        fun onError(message: String?)
    }

    private var mUrl: String? = null
    private var mOnResult: WeakReference<OnResult>? = null
    private var mAsyncRest: AsyncRest? = null

    var mCache: MutableList<DataObject> = ArrayList()
        private set
    var mIsProcess: Boolean = false
        private set

    fun exec(url: String) {
        mUrl = url

        if (mAsyncRest != null && !mAsyncRest?.isCancelled!!) {
            mAsyncRest?.cancel(true)
        }

        mAsyncRest = AsyncRest()
        mAsyncRest?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun getData(): List<DataObject>? {
        var resultList: List<DataObject>? = null
        var connect: HttpURLConnection? = null
        try {
            val URL = URL(mUrl)
            connect  = URL.openConnection() as HttpURLConnection
            connect.readTimeout = TIMEOUT_READ
            connect.connectTimeout = TIMEOUT_CONNECT
            connect.requestMethod = REQUEST_METHOD
            connect.doOutput = false;
            connect.connect();

            val responseCode: Int = connect.responseCode
            when(responseCode) {
                HTTP_OK -> {
                    val tempStream = connect.inputStream
                    if (tempStream != null) {
                        resultList = toJson(toString(tempStream))
                    } else {
                        mOnResult?.get()?.onError("Can't open connection")
                    }
                }

                else -> mOnResult?.get()?.onError(responseCode.toString())
            }

        } catch (ex: Exception) {
            Log.d(TAG, ex.message.plus(" - ").plus(mUrl))
            mOnResult?.get()?.onError(ex.message)
        } finally {
            connect?.disconnect()
        }

        return resultList
    }

    private fun toString(inStream: InputStream): String {
        var result = StringBuilder()
        val isReader = InputStreamReader(inStream)
        var byteReader = BufferedReader(isReader)
        var tempStr: String?

        while (true) {
            tempStr = byteReader.readLine()
            if (tempStr == null) { break }
            result.append(tempStr);
        }

        return result.toString()
    }

    private fun toJson(jsonString: String): List<DataObject> {
        val stringReader = StringReader(jsonString)
        val gsonBuilder = GsonBuilder().serializeNulls()
        gsonBuilder.registerTypeAdapter(DataObject::class.java, DataDeserializer())
        val gson = gsonBuilder.create()
        val dataList = gson.fromJson(stringReader , Array<DataObject>::class.java).toList()
        return dataList
    }

    fun setCallback(onResult: OnResult) { mOnResult = WeakReference(onResult) }

    fun removeCallback() { mOnResult = null }

    /*
    * Async get request
    * */
    inner class AsyncRest : AsyncTask<Unit, Int, List<DataObject>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mIsProcess = true
            mOnResult?.get()?.onBegin()
        }

        override fun doInBackground(vararg params: Unit): List<DataObject>? {
            mIsProcess = true
            return getData()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            mOnResult?.get()?.onProgress(values.getOrElse(0) { 0 }!!)
        }

        override fun onPostExecute(result: List<DataObject>?) {
            super.onPostExecute(result)
            mIsProcess = false
            if (result != null) {
                mCache.addAll(result.toMutableList())
            }
            mOnResult?.get()?.onFinish(result)
        }

        override fun onCancelled() {
            super.onCancelled()
            mIsProcess = false
        }

        override fun onCancelled(result: List<DataObject>?) {
            super.onCancelled(result)
            mIsProcess = false
        }
    }

}



