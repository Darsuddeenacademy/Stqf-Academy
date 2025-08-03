package com.darsuddeen.academy.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File

object DownloadUtils {

    fun downloadPdf(
        context: Context,
        pdfUrl: String,
        fileName: String
    ): String {
        val downloadDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "pdf_books"
        )
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val file = File(downloadDir, fileName)

        if (file.exists()) {
            Toast.makeText(context, "File already downloaded", Toast.LENGTH_SHORT).show()
            return file.absolutePath
        }

        val request = DownloadManager.Request(Uri.parse(pdfUrl))
        request.setTitle("Downloading PDF")
        request.setDescription("Downloading $fileName")

        // ✅ Safe way to set destination (No FileUriExposed issue)
        request.setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            "pdf_books/$fileName"
        )

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
        return file.absolutePath
    }
}
