package com.stqf.academy.adapter

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.stqf.academy.PdfViewerActivity
import com.stqf.academy.R
import com.stqf.academy.databinding.ItemBookBinding
import com.stqf.academy.model.BookApiModel
import java.io.File

class BookApiAdapter(
    private val context: Context,
    private val bookList: List<BookApiModel>
) : RecyclerView.Adapter<BookApiAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        holder.binding.bookTitle.text = book.title
        holder.binding.bookDescription.text = book.description

        val thumbFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "book_thumbnails/${book.title.replace(" ", "_")}.jpg"
        )

        Log.d("THUMB_PATH", "Trying: ${thumbFile.absolutePath} | Exists: ${thumbFile.exists()}")

        if (thumbFile.exists()) {
            Glide.with(context)
                .load(thumbFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.binding.bookThumbnail)
        } else if (book.thumbnail_url.startsWith("http")) {
            Glide.with(context)
                .load(book.thumbnail_url)
                .error(R.drawable.default_thumb)
                .into(holder.binding.bookThumbnail)
        } else {
            Glide.with(context)
                .load(R.drawable.default_thumb)
                .into(holder.binding.bookThumbnail)
        }

        holder.itemView.setOnClickListener {
            val activityContext = context as? Activity ?: return@setOnClickListener

            val fileName = book.title.replace(" ", "_") + ".pdf"
            val downloadDir = File(activityContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pdf_books")
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val localFile = File(downloadDir, fileName)

            if (localFile.exists()) {
                val intent = Intent(activityContext, PdfViewerActivity::class.java)
                intent.putExtra("pdf_file", localFile.absolutePath)
                activityContext.startActivity(intent)
            } else {
                AlertDialog.Builder(activityContext)
                    .setTitle("Download Book")
                    .setMessage("Do you want to download this book for offline reading?")
                    .setPositiveButton("Yes") { _, _ ->

                        val progressText = TextView(activityContext).apply {
                            text = "Downloading..."
                            setPadding(20, 20, 20, 20)
                        }

                        val progressBar = ProgressBar(
                            activityContext,
                            null,
                            android.R.attr.progressBarStyleHorizontal
                        ).apply {
                            isIndeterminate = false
                            max = 100
                        }

                        val layout = LinearLayout(activityContext).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(40, 30, 40, 30)
                            addView(progressText)
                            addView(progressBar)
                        }

                        val progressDialog = AlertDialog.Builder(activityContext)
                            .setView(layout)
                            .setCancelable(false)
                            .create()

                        progressDialog.window?.setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )

                        progressDialog.show()

                        val request = DownloadManager.Request(Uri.parse(book.pdf_url))
                        request.setTitle("Downloading PDF")
                        request.setDescription("Downloading $fileName")
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // ✅ FIXED
                        request.setDestinationInExternalFilesDir(
                            activityContext,
                            Environment.DIRECTORY_DOWNLOADS,
                            "pdf_books/$fileName"
                        )

                        val downloadManager = activityContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val downloadId = downloadManager.enqueue(request)

                        val checkDownloadHandler = Handler(activityContext.mainLooper)
                        checkDownloadHandler.post(object : Runnable {
                            override fun run() {
                                val query = DownloadManager.Query().setFilterById(downloadId)
                                val cursor = downloadManager.query(query)
                                if (cursor != null && cursor.moveToFirst()) {
                                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                                    val total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                    val downloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                                    if (total > 0) {
                                        val percent = (downloaded * 100 / total.toFloat()).toInt()
                                        progressBar.progress = percent
                                        progressText.text = "Downloading: $percent%"
                                    }

                                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                        cursor.close()
                                        progressDialog.dismiss()

                                        // ✅ Download thumbnail now
                                        downloadThumbnailImage(activityContext, book.thumbnail_url, book.title)

                                        val downloadedFile = File(
                                            activityContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                            "pdf_books/$fileName"
                                        )

                                        if (downloadedFile.exists()) {
                                            val viewIntent = Intent(activityContext, PdfViewerActivity::class.java)
                                            viewIntent.putExtra("pdf_file", downloadedFile.absolutePath)
                                            activityContext.startActivity(viewIntent)
                                        } else {
                                            Toast.makeText(context, "Download failed or file missing", Toast.LENGTH_SHORT).show()
                                        }

                                        return
                                    }
                                }
                                cursor?.close()
                                checkDownloadHandler.postDelayed(this, 1000)
                            }
                        })

                        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = bookList.size

    private fun downloadThumbnailImage(context: Context, imageUrl: String, title: String) {
        try {
            val fileName = title.replace(" ", "_") + ".jpg"
            val request = DownloadManager.Request(Uri.parse(imageUrl))
            request.setTitle("Downloading Thumbnail")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // ✅ FIXED
            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_PICTURES,
                "book_thumbnails/$fileName"
            )

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Log.d("THUMB_DOWNLOAD", "Started thumbnail download: $fileName")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("THUMB_DOWNLOAD", "Thumbnail download failed: ${e.localizedMessage}")
        }
    }
}
