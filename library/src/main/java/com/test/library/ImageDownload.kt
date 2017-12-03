package com.test.library

import android.graphics.Bitmap
import android.os.AsyncTask
import java.lang.ref.WeakReference
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.drawable.AnimationDrawable

internal class ImageDownload(url: String, view: ImageView, val holder: Int?, val error: Int?,
                 transform: Transformation?, callback: OnResult?) : AsyncTask<String, Int, Bitmap>() {

    companion object {
        val TAG = ImageDownload::class.java.simpleName

        private val TIMEOUT_READ = 5000
        private val TIMEOUT_CONNECT = 5000
        private val REQUEST_METHOD = "GET"
    }

    interface OnResult {
        fun onBegin()
        fun onFinish(image: Bitmap?)
        fun onProgress(progress: Int)
        fun onError(message: String?)
    }

    var mIsProcess: Boolean = false
        private set

    private val mUrl: String = url
    private val mViewReference: WeakReference<ImageView> = WeakReference(view)
    private var mCallback: WeakReference<OnResult>? = null
    private var mTransform: Transformation? = null

    init {
        mTransform = transform
        if (callback != null) {
            mCallback = WeakReference(callback)
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mIsProcess = true
        setImage(holder)
        mCallback?.get()?.onBegin()
    }

    override fun doInBackground(vararg params: String?): Bitmap? {
        mIsProcess = true

        var bitmap: Bitmap? = ImageCache.getInstance(mViewReference?.get()?.context!!).get(mUrl)
        if(bitmap != null) {
            return bitmap
        }

        var connect: HttpURLConnection? = null
        try {
            val uri = URL(mUrl)
            connect = uri.openConnection() as HttpURLConnection
            connect.readTimeout = TIMEOUT_READ
            connect.connectTimeout = TIMEOUT_CONNECT
            connect.requestMethod = REQUEST_METHOD
            connect.doOutput = false;

            val responseCode: Int = connect.responseCode
            when(responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val inputStream = connect.inputStream
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream)
                        bitmap = mTransform?.transform(bitmap)?: bitmap
                        ImageCache.getInstance(mViewReference?.get()?.context!!).add(mUrl, bitmap!!)
                    } else {
                        mCallback?.get()?.onError("Can't open connection")
                    }
                }

                else -> mCallback?.get()?.onError(responseCode.toString())
            }

        } catch (ex: Exception) {
            Log.d(TAG, ex.message.plus(" - ").plus(mUrl))
            mCallback?.get()?.onError(ex.message)
        } finally {
            connect?.disconnect()
        }

        return bitmap
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if (values?.size > 0) { mCallback?.get()?.onProgress(values.get(0)!!) }
    }

    override fun onCancelled(result: Bitmap?) {
        super.onCancelled(result)
        mIsProcess = false
        setImage(error)
    }

    override fun onCancelled() {
        super.onCancelled()
        mIsProcess = false
        setImage(error)
    }

    override fun onPostExecute(result: Bitmap?) {
        super.onPostExecute(result)
        mIsProcess = false
        when(result) {
            null -> setImage(error)
            else ->  {
                mViewReference?.get()?.setImageBitmap(result)
            }
        }

        mCallback?.get()?.onFinish(result)
    }

    private fun setImage(id: Int?) {
        if (id != null) {
            val imageView = mViewReference?.get()
            if (imageView != null) {
                imageView.setImageResource(id)
                (imageView.drawable as? AnimationDrawable)?.start()
            }
        }
    }

    fun removeCallback() {
        mCallback = null
    }

}