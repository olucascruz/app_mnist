package com.example.app_mnist

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
fun prepareBitmap(bitmap: Bitmap, threshold: Int, invert:Boolean = false): Bitmap{
    val startTime = Instant.now()
    val width = bitmap.width
    val height = bitmap.height

    val preparedBitmap = Bitmap.createBitmap(width, height, bitmap.config)

    for(x in 0 until width){
        for(y in 0 until height){
            val pixelColor = bitmap.getPixel(x, y)
            val alpha = pixelColor shr 24 and 0xFF
            val grayValue = pixelColor and 0xFF
            var newPixelColor = if (grayValue < threshold) {
                // Se o valor de intensidade for menor que o limite, defina como preto
                alpha shl 24 or 0x00
            } else {
                // Caso contrário, defina como branco
                alpha shl 24 or 0xFFFFFF
            }
            if(invert) {
                val invertedPixelColor = invertPixelColor(newPixelColor)
                newPixelColor = invertedPixelColor
            }
            preparedBitmap.setPixel(x, y, newPixelColor)
        }
    }

    // Registre a hora após a seção de código que você deseja medir
    val endTime = Instant.now()

    // Calcule a diferença de tempo em milissegundos
    val executionTime = endTime.toEpochMilli() - startTime.toEpochMilli()

    Log.d("TempoPrepared:", "$executionTime ms, ${executionTime/1000} s")
    return preparedBitmap
}

fun invertPixelColor(pixelColor: Int): Int {
    val newAlpha = pixelColor shr 24 and 0xFF
    val newGrayValue =
        255 - (pixelColor and 0xFF) // Inverte o valor de preto (0) para branco (255) e vice-versa
    return newAlpha shl 24 or (newGrayValue and 0xFF)
}

fun convertToARGB8888(bitmap: Bitmap): Bitmap {
    return bitmap.copy(Bitmap.Config.ARGB_8888, true)
}


