package com.test.library

import android.graphics.*

interface Transformation {
    fun transform(bitmap: Bitmap) : Bitmap
}

class CircleTransformation : Transformation {

    override fun transform(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val path = Path()
        path.addCircle((width / 2).toFloat(), (height / 2).toFloat(), Math.min(width, height / 2).toFloat() * 0.9f, Path.Direction.CCW)
        val canvas = Canvas(outputBitmap)
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, null)
        return outputBitmap
    }

}

/*
* TODO : corners for images with different sizes
* */
class RoundTransformation(private val radius: Int = 20) : Transformation {

    override fun transform(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = 0xff424242
        val paint = Paint()
        val rectF = RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color.toInt()
        canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint)
        return output
    }

}