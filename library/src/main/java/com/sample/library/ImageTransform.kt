package com.sample.library

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path

interface Transformation {
    fun transform(bitmap: Bitmap) : Bitmap
}

class CircleTransformation : Transformation {

    override fun transform(bitmap: Bitmap): Bitmap {
        return Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).also {
            val path = Path().apply {
                addCircle(
                    (bitmap.width / 2).toFloat(),
                    (bitmap.height / 2).toFloat(),
                    bitmap.width.coerceAtMost(bitmap.height / 2).toFloat() * 0.9f,
                    Path.Direction.CCW
                )
            }

            Canvas(it).apply {
                clipPath(path)
                drawBitmap(bitmap, 0.0f, 0.0f, null)
            }
        }
    }

}