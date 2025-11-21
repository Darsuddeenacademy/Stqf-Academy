package com.stqf.academy.adapter

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
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

class ColorQuranAdapter(
    private val context: Context,
    private val quranList: List<BookApiModel>
) : RecyclerView.Adapter<ColorQuranAdapter.QuranViewHolder>() {

    inner class QuranViewHolder(val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        val binding =
            ItemBookBinding.inflate(LayoutInflater.from(context), parent, false)
        return QuranViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuranViewHolder, position: Int) {
        val item = quranList[position]

        holder.binding.bookTitle.text = item.title
        holder.binding.bookDescription.text = item.description

        // 🔹 থাম্বনেইল (আগে ডাউনলোড করা থাকলে লোকাল, নইলে URL)
        val thumbFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "quran_thumbnails/${item.title.replace(" ", "_")}.jpg"
        )

        if (thumbFile.exists()) {
            Glide.with(context)
                .load(thumbFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.binding.bookThumbnail)
        } else if (item.thumbnail_url.startsWith("http")) {
            Glide.with(context)
                .load(item.thumbnail_url)
                .error(R.drawable.default_thumb)
                .into(holder.binding.bookThumbnail)
        } else {
            Glide.with(context)
                .load(R.drawable.default_thumb)
                .into(holder.binding.bookThumbnail)
        }

        holder.itemView.setOnClickListener {
            val activity = context as? Activity ?: return@setOnClickListener

            val fileName = item.title.replace(" ", "_") + ".pdf"
            val downloadDir = File(
                activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "color_quran"
            )
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val localFile = File(downloadDir, fileName)

            if (localFile.exists()) {
                // 🔹 লোকাল PDF ওপেন
                val intent = Intent(activity, PdfViewerActivity::class.java)
                intent.putExtra("pdf_file", localFile.absolutePath)
                activity.startActivity(intent)
            } else {
                // 🔹 আগে ডাউনলোড হয়নি → ডাউনলোড ডায়ালগ
                AlertDialog.Builder(activity)
                    .setTitle("Download Quran")
                    .setMessage("Do you want to download this Quran for offline reading?")
                    .setPositiveButton("Yes") { _, _ ->
                        startDownloadWithProgress(activity, item, fileName)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = quranList.size

    private fun startDownloadWithProgress(
        activity: Activity,
        item: BookApiModel,
        fileName: String
    ) {
        val progressText = TextView(activity).apply {
            text = "Downloading..."
            setPadding(20, 20, 20, 20)
        }

        val progressBar = ProgressBar(
            activity,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            isIndeterminate = false
            max = 100
        }

        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
            addView(progressText)
            addView(progressBar)
        }

        val progressDialog = AlertDialog.Builder(activity)
            .setView(layout)
            .setCancelable(false)
            .create()

        progressDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        progressDialog.show()

        val request = DownloadManager.Request(Uri.parse(item.pdf_url))
        request.setTitle("Downloading Quran PDF")
        request.setDescription("Downloading $fileName")
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE
        )
        request.setDestinationInExternalFilesDir(
            activity,
            Environment.DIRECTORY_DOWNLOADS,
            "color_quran/$fileName"
        )

        val dm = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        val handler = Handler(activity.mainLooper)
        handler.post(object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_STATUS
                        )
                    )
                    val total = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                        )
                    )
                    val downloaded = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                        )
                    )

                    if (total > 0) {
                        val percent =
                            (downloaded * 100 / total.toFloat()).toInt()
                        progressBar.progress = percent
                        progressText.text = "Downloading: $percent%"
                    }

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        cursor.close()
                        progressDialog.dismiss()

                        // 🔹 থাম্বনেইল আলাদা ডাউনলোড
                        downloadThumbnail(activity, item)

                        val downloadedFile = File(
                            activity.getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS
                            ),
                            "color_quran/$fileName"
                        )

                        if (downloadedFile.exists()) {
                            val intent =
                                Intent(activity, PdfViewerActivity::class.java)
                            intent.putExtra(
                                "pdf_file",
                                downloadedFile.absolutePath
                            )
                            activity.startActivity(intent)
                        } else {
                            Toast.makeText(
                                activity,
                                "Download failed or file missing",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return
                    }
                }
                cursor?.close()
                handler.postDelayed(this, 1000)
            }
        })

        Toast.makeText(activity, "Download started", Toast.LENGTH_SHORT).show()
    }

    private fun downloadThumbnail(context: Context, item: BookApiModel) {
        if (!item.thumbnail_url.startsWith("http")) return

        val fileName = item.title.replace(" ", "_") + ".jpg"
        val request = DownloadManager.Request(Uri.parse(item.thumbnail_url))
        request.setTitle("Downloading Quran Thumbnail")
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE
        )
        request.setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_PICTURES,
            "quran_thumbnails/$fileName"
        )

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }
}
