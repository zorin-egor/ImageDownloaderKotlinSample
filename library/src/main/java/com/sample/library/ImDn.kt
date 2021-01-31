package com.sample.library

import android.content.Context
import android.widget.ImageView
import java.util.*

class ImDn {

    companion object {

        private var sTasks = WeakHashMap<ImageView, ImageDownload>()
        private var sIsInit = false
        private var sPath: String? = null
        private var sHolder: Int? = null
        private var sError: Int? = null
        private var sTransform: Transformation? = null

        fun init(context: Context): Companion {
            sIsInit = true
            ImageCache.getInstance(context)
            return this
        }

        fun path(path: String): Companion {
            sPath = path
            return this
        }

        fun holder(holder: Int?): Companion {
            sHolder = holder
            return this
        }

        fun error(error: Int?): Companion {
            sError = error
            return this
        }

        fun transform(transformation: Transformation): Companion {
            sTransform = transformation
            return this
        }

        fun into(imageView: ImageView) {
            if (!sIsInit || sPath == null) {
                throw IllegalArgumentException("Set parameters: Context, ImageView, Path!")
            }
            addTask(imageView)
        }

        private fun addTask(view: ImageView) {
            sTasks[view] = ImageDownload(sPath!!, view, sHolder, sError, sTransform, null).apply {
                exec()
            }
        }
    }
}