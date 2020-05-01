package com.sample.library

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class ImageDownload(url: String,
                    view: ImageView,
                    @DrawableRes private val holder: Int? = null,
                    @DrawableRes private val error: Int? = null,
                    private val transform: Transformation? = null,
                    callback: OnResult? = null) {

    companion object {
        private val TIMEOUT_READ = 5000
        private val TIMEOUT_CONNECT = 5000
        private val REQUEST_METHOD = "GET"
    }

    interface OnResult {
        fun onBegin()
        fun onResult(bitmap: Bitmap?)
        fun onProgress(total: Int, progress: Int)
        fun onError(error: Throwable?, message: String? = null)
    }

    private var mScope = MainScope()
    private var mJob: Job? = null
    private val mUrl: String = url
    private val mViewRef: WeakReference<ImageView> = WeakReference(view)
    private var mCallbackRef: WeakReference<OnResult>? = null

    init {
        mCallbackRef = callback?.let { WeakReference(callback) }
    }

    private fun setImage() {
        holder?.let { holder ->
            mViewRef?.get()?.let { image ->
                image.setImageResource(holder)
                (image.drawable as? AnimationDrawable)?.start()
            }
        }
    }

    private fun getBitmap(onProgress: (Int, Int) -> Unit, onError: (String?, Throwable?) -> Unit): Bitmap? {
        mViewRef?.get()?.let { view ->
            ImageCache.getInstance(view.context).get(mUrl)?.let { bitmap ->
                return bitmap
            }
        }

        var bitmap: Bitmap? = null
        var connect: HttpURLConnection? = null

        try {
            connect = URL(mUrl).openConnection() as HttpURLConnection
            connect.readTimeout = TIMEOUT_READ
            connect.connectTimeout = TIMEOUT_CONNECT
            connect.requestMethod = REQUEST_METHOD
            connect.doOutput = false;

            when(connect.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    var time = System.currentTimeMillis()

                    ByteArrayOutputStream().use { output ->
                        connect.inputStream.buffered().use { input ->
                            while(input.readBytes().also { output.write(it) }.isNotEmpty()) {
                                val deltaTime = System.currentTimeMillis() - time
                                if (deltaTime > 200) {
                                    time += deltaTime
                                    onProgress(connect.contentLength, output.size())
                                }
                            }
                        }

                        onProgress(connect.contentLength, output.size())

                        bitmap = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.size())?.let {
                            transform?.transform(it)?: bitmap
                        }

                        mViewRef?.get()?.let {
                            ImageCache.getInstance(it.context).add(mUrl, bitmap!!)
                        }
                    }
                }

                else -> {
                    onError(connect.responseMessage, null)
                }
            }
        } catch (e: Exception) {
            onError(null, e)
        } finally {
            connect?.disconnect()
        }

        return bitmap
    }

    fun exec() {
        mJob = mScope.launch(Dispatchers.Main) {
            try {
                // Ui begin
                setImage()
                mCallbackRef?.get()?.onBegin()

                // Thread
                val bitmap = withContext(Dispatchers.Default) {
                    getBitmap({ total, progress ->
                        this@launch.launch {
                            mCallbackRef?.get()?.onProgress(total, progress)
                        }
                    }, { message, error ->
                        this@launch.launch {
                            mCallbackRef?.get()?.onError(error, message)
                        }
                    })
                }

                // Ui end
                bitmap?.let {
                    mViewRef?.get()?.apply {
                        setImageBitmap(it)
                    }
                }

            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    mCallbackRef?.get()?.onError(e, null)
                }
            }
        }
    }

    fun cancel(): Boolean {
        return try {
            mScope.cancel()
            true
        } catch(e: Exception) {
            false
        } finally {
            mScope = MainScope()
        }
    }

}