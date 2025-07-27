package com.darsuddeen.academy.Utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PDFUtils {

    fun getFirstPageThumbnail(context: Context, assetFileName: String): Bitmap? {
        try {
            // Copy the PDF file from assets to cache (PdfRenderer needs File)
            val file = File(context.cacheDir, assetFileName)
            if (!file.exists()) {
                val inputStream = context.assets.open(assetFileName)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }

            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)

            val page = renderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()

            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
        }
}