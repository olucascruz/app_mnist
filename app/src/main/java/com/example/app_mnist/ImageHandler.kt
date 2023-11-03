package com.example.app_mnist

import android.graphics.Bitmap
import android.util.Log


fun convertToBlackAndWhite(bitmap: Bitmap, threshold: Int): Bitmap{
    val width = bitmap.width
    val height = bitmap.height
    val blackAndWhiteBitmap = Bitmap.createBitmap(width, height, bitmap.config)

    for(x in 0 until width){
        for(y in 0 until height){
            val pixelColor = bitmap.getPixel(x, y)
            val alpha = pixelColor shr 24 and 0xFF
            val grayValue = pixelColor and 0xFF
            val newPixelColor = if (grayValue < threshold) {
                // Se o valor de intensidade for menor que o limite, defina como preto
                alpha shl 24 or 0x00
            } else {
                // Caso contrário, defina como branco
                alpha shl 24 or 0xFFFFFF
            }

            blackAndWhiteBitmap.setPixel(x, y, newPixelColor)
        }
    }
    return blackAndWhiteBitmap
}

fun invertColors(bitmap: Bitmap): Bitmap{
    val width = bitmap.width
    val height = bitmap.height
    val invertedBitmap = Bitmap.createBitmap(width, height, bitmap.config)

    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixelColor = bitmap.getPixel(x, y)
            val alpha = pixelColor shr 24 and 0xFF
            val grayValue = 255 - (pixelColor and 0xFF) // Inverte o valor de preto (0) para branco (255) e vice-versa

            val invertedPixelColor = alpha shl 24 or (grayValue and 0xFF)
            invertedBitmap.setPixel(x, y, invertedPixelColor)
        }
    }


    return invertedBitmap
}

fun seeBitmap(bitmap: Bitmap){
    val width = bitmap.width
    val height = bitmap.height
    for (x in 0 until width) {
        for (y in 0 until height) {
            val pixelColor = bitmap.getPixel(x, y)
            val alpha = pixelColor shr 24 and 0xFF
            val red = pixelColor shr 16 and 0xFF
            val green = pixelColor shr 8 and 0xFF
            val blue = pixelColor and 0xFF

            Log.d("bitmap", "$pixelColor")
            Log.d("bitmap", "$alpha")
            Log.d("bitmap", "$red")
            Log.d("bitmap", "$green")
            Log.d("bitmap", "$blue")

        }
    }
}

fun convertToARGB8888(bitmap: Bitmap): Bitmap {
    return bitmap.copy(Bitmap.Config.ARGB_8888, true)
}