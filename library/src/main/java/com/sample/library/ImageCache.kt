package com.sample.library

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import androidx.collection.LruCache
import com.jakewharton.disklrucache.DiskLruCache
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest


class ImageCache {

    companion object {

        private const val MD5 = "MD5"
        private const val CACHE_SUB_DIR = "image_cache"
        private const val CACHE_MEM_SIZE = 10
        private const val CACHE_STORE_COUNT = 1
        private const val CACHE_STORE_SIZE: Long = 1024 * 1024 * 50 // 50Mb
        private const val IO_BUFFER_SIZE = 8 * 1024 // 8Kb
        private const val COMPRESS_QUALITY = 100
        private val COMPRESS_FORMAT = CompressFormat.PNG

        private var sImageCache: ImageCache? = null

        fun getInstance(context: Context): ImageCache {
            return sImageCache ?: synchronized(this) {
                sImageCache ?: ImageCache(context).also { sImageCache = it }
            }
        }
    }

    private val mMaxMemory = Runtime.getRuntime().maxMemory().toInt()
    private val mCacheSize = mMaxMemory / CACHE_MEM_SIZE
    private val mStorageCache: DiskLruCache
    private val mMemCache: LruCache<String, Bitmap>

    private constructor(context: Context) {
        mStorageCache = DiskLruCache.open(getDiskCacheFile(context), BuildConfig.VERSION_CODE, CACHE_STORE_COUNT, CACHE_STORE_SIZE)
        mMemCache = object : LruCache<String, Bitmap>(mCacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount
            }
        }
    }

    fun add(key: String, bitmap: Bitmap) {
        md5(key)?.let { md5 ->
            addBitmapToMemoryCache(md5, bitmap)
            addBitmapToStorageCache(md5, bitmap)
        }
    }

    fun get(key: String): Bitmap? {
        return md5(key)?.let { md5 ->
            getBitmapFromMemCache(md5) ?: getBitmapFromStorageCache(md5)
        }
    }

    /*
    * Memory block
    * */
    private fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        mMemCache.put(key, bitmap)
    }

    private fun getBitmapFromMemCache(key: String): Bitmap? {
        return mMemCache.get(key)
    }

    /*
    * Storage block
    * */
    private fun getDiskCacheFile(context: Context): File {
        val cachePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !isExternalStorageRemovable()) {
            context.externalCacheDir?.path
        } else {
            context.cacheDir.path
        }

        return File(cachePath + File.separator + CACHE_SUB_DIR)
    }

    private fun writeBitmapToStorageCache(bitmap: Bitmap, editor: DiskLruCache.Editor): Boolean {
        return BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE).use { output ->
            bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, output)
        }
    }

    private fun addBitmapToStorageCache(key: String, data: Bitmap) {
        mStorageCache.edit(key)?.let { editor ->
            try {
                if (writeBitmapToStorageCache(data, editor)) {
                    mStorageCache.flush()
                    editor.commit()
                } else {
                    editor.abort()
                }
            } catch (e: IOException) {
                try {
                    editor.abort()
                } catch (ignored: IOException) {
                    // No need to handle
                }
            }
        }
    }

    private fun getBitmapFromStorageCache(key: String): Bitmap? {
        return try {
            mStorageCache.get(key)?.let { snapshot ->
                BufferedInputStream(snapshot.getInputStream(0), IO_BUFFER_SIZE).use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } catch (e: IOException) {
            return null
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance(MD5)
        val md5Data = BigInteger(1, md.digest(input.toByteArray()))
        return String.format("%032X", md5Data).toLowerCase()
    }

}