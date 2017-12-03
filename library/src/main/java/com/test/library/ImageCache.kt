package com.test.library

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import android.support.v4.util.LruCache
import android.util.Log
import java.lang.ref.WeakReference

import com.jakewharton.disklrucache.DiskLruCache
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


internal class ImageCache private constructor(context: Context) {

    companion object {
        val TAG = ImageCache::class.java.simpleName

        private val MD5 = "MD5"
        private val CACHE_SUB_DIR = "image_cache"
        private val CACHE_MEM_SIZE = 10
        private val CACHE_STORE_COUNT = 1
        private val CACHE_STORE_SIZE: Long = 1024 * 1024 * 50 // 50Mb
        private val IO_BUFFER_SIZE = 8 * 1024 // 8Kb
        private val COMPRESS_FORMAT = CompressFormat.PNG
        private val COMPRESS_QUALITY = 100

        private var sImageCache: ImageCache? = null

        fun getInstance(context: Context): ImageCache {
            if (sImageCache == null) {
                sImageCache = ImageCache(context)
            }

            return sImageCache!!
        }
    }

    private val mMaxMemory = Runtime.getRuntime().maxMemory().toInt()
    private val mCacheSize = mMaxMemory / CACHE_MEM_SIZE
    private val mContextReference: WeakReference<Context> = WeakReference(context)
    private var mStorageCache: DiskLruCache? = null
    private val mMemCache: LruCache<String, Bitmap>

    init {
        mMemCache = object : LruCache<String, Bitmap>(mCacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return bitmap!!.byteCount
            }
        }

        mStorageCache = DiskLruCache.open(getDiskCacheDir(), BuildConfig.VERSION_CODE, CACHE_STORE_COUNT, CACHE_STORE_SIZE)
    }

    fun add(key: String, bitmap: Bitmap) {
        val md5Key = md5(key)
        addBitmapToMemoryCache(md5Key, bitmap)
        addBitmapToStorageCache(md5Key, bitmap)
    }

    fun get(key: String): Bitmap? {
        val md5Key = md5(key)
        var bitmap = getBitmapFromMemCache(md5Key)
        if (bitmap == null) {
            bitmap = getBitmapFromStorageCache(md5Key)
        }

        return bitmap
    }

    /*
    * Memory block
    * */
    private fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemCache.put(key, bitmap)
        }
    }

    private fun getBitmapFromMemCache(key: String): Bitmap? {
        return mMemCache.get(key)
    }

    /*
    * Storage block
    * */
    private fun getDiskCacheDir(): File {
        val cachePath = if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable())
            mContextReference?.get()?.getExternalCacheDir()!!.getPath()
        else
            mContextReference?.get()?.cacheDir!!.path

        return File(cachePath + File.separator + CACHE_SUB_DIR)
    }

    private fun writeBitmapToStorageCache(bitmap: Bitmap, editor: DiskLruCache.Editor): Boolean {
        var out: OutputStream? = null
        try {
            out = BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE)
            return bitmap.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, out)
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        } finally {
            out?.close()
        }

        return false
    }

    private fun addBitmapToStorageCache(key: String, data: Bitmap) {
        var editor: DiskLruCache.Editor? = null
        try {
            editor = mStorageCache!!.edit(key)

            // Check if another edit is in progress
            if (editor == null) {
                return
            }

            // Cache bitmap
            if (writeBitmapToStorageCache(data, editor)) {
                mStorageCache!!.flush()
                editor!!.commit()
            } else {
                editor!!.abort()
            }

        } catch (ex: IOException) {
            Log.d(TAG, ex.message + " - " + key)
            try {
                editor?.abort()
            } catch (ignored: IOException) {
                // No need to handle
            }
        }
    }

    private fun getBitmapFromStorageCache(key: String): Bitmap? {
        var bitmap: Bitmap? = null
        var snapshot: DiskLruCache.Snapshot? = null
        try {
            snapshot = mStorageCache!!.get(key)

            // Snapshot doesn't exist is not currently readable
            if (snapshot == null) {
                return null
            }

            // Get cached bitmap
            val inputStream = snapshot!!.getInputStream(0)
            if (inputStream != null) {
                val bufferedInputStream = BufferedInputStream(inputStream, IO_BUFFER_SIZE)
                bitmap = BitmapFactory.decodeStream(bufferedInputStream)
            }
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        } finally {
            snapshot?.close()
        }

        return bitmap
    }

    private fun md5(input: String): String {
        try {
            val md = MessageDigest.getInstance(MD5)
            val md5Data = BigInteger(1, md.digest(input.toByteArray()))
            return String.format("%032X", md5Data).toLowerCase()
        } catch (ex: NoSuchAlgorithmException) {
            throw RuntimeException(ex)
        }
    }

    private fun containsInStorageCache(key: String): Boolean {
        var contained = false
        var snapshot: DiskLruCache.Snapshot? = null
        try {
            snapshot = mStorageCache?.get(key)
            contained = snapshot != null
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        } finally {
            snapshot?.close()
        }

        return contained

    }

    private fun clearStorageCache() {
        try {
            mStorageCache?.delete()
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        }
    }

}