package com.test.library

import android.content.Context
import android.os.AsyncTask
import android.widget.ImageView
import java.util.*

class ImDn {
    companion object {
        private val QUEUE_SIZE = 10
        private var sQueue: Queue<ImageDownload> = ArrayDeque()

        private var sIsInit = false
        private var sPath: String? = null
        private var sHolder: Int? = null
        private var sError: Int? = null
        private var sTransform: Transformation? = null

        fun init(context: Context): ImDn.Companion {
            sIsInit = true
            ImageCache.getInstance(context)
            return this
        }

        fun path(path: String): ImDn.Companion {
            sPath = path
            return this
        }

        fun holder(holder: Int?): ImDn.Companion {
            sHolder = holder
            return this
        }

        fun error(error: Int?): ImDn.Companion {
            sError = error
            return this
        }

        fun transform(transformation: Transformation): ImDn.Companion {
            sTransform = transformation
            return this
        }

        fun into(imageView: ImageView) {
            if (!sIsInit || imageView == null || sPath == null) {
                throw IllegalArgumentException("Set parameters: Context, ImageView, Path!")
            }

            addTask(imageView)
        }

        private fun addTask(imageView: ImageView) {
            trimTasks()
            val imageDownload = ImageDownload(sPath!!, imageView, sHolder, sError, sTransform, null)
            imageDownload.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            sQueue.offer(imageDownload)
        }

        private fun trimTasks() {
            while (sQueue.size >= QUEUE_SIZE) {
                val imageDownload = sQueue.poll()
                if (!imageDownload.isCancelled) {
                    imageDownload.cancel(true)
                }
            }
        }
    }
}