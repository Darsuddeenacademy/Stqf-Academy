package com.stqf.academy.adapter


import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    inner class BookViewHolder(val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root)

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
            val downloadDir = File(
                activityContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "pdf_books"
            )
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val localFile = File(downloadDir, fileName)

            if (localFile.exists()) {
                // ফাইল আগে থেকেই থাকলে সরাসরি ওপেন
                val intent = Intent(activityContext, PdfViewerActivity::class.java)
                intent.putExtra("pdf_file", localFile.absolutePath)
                activityContext.startActivity(intent)
            } else {

                // 🔹 Dynamic title + message (ইংরেজি + বাংলা)
                val dialogTitle = "Download: ${book.title}\nবইটি ডাউনলোড করবেন?"
                val dialogMessage =
                    "To read this book online or offline, you need to download it once.\n\n" +
                            "একবার ডাউনলোড করলেই অনলাইন ও অফলাইনে পড়তে পারবেন।\n\n" +
                            "Do you want to download now?\n" +
                            "আপনি কি এখন ডাউনলোড করতে চান?"

                // 🔹 কাস্টম লেআউট সহ কনফার্ম ডায়ালগ (টাইটেল + মেসেজ + YES/NO বক্স বাটন)
                val builder = AlertDialog.Builder(activityContext)
                val dialogView = LayoutInflater.from(activityContext)
                    .inflate(R.layout.dialog_download_confirm, null)

                val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
                val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
                val btnYes = dialogView.findViewById<TextView>(R.id.btnYes)
                val btnNo = dialogView.findViewById<TextView>(R.id.btnNo)

                tvTitle.text = dialogTitle
                tvMessage.text = dialogMessage

                val dialog = builder.setView(dialogView).create()
                dialog.show()

                dialog.window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activityContext,
                        R.drawable.dialog_rounded_bg
                    )
                )
                dialog.window?.setLayout(
                    (activityContext.resources.displayMetrics.widthPixels * 0.90).toInt(),
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                // 🔹 NO / না → শুধু ডায়ালগ বন্ধ
                btnNo.setOnClickListener {
                    dialog.dismiss()
                }

                // 🔹 YES / হ্যাঁ → ডাউনলোড শুরু + Progress ডায়ালগ
                btnYes.setOnClickListener {
                    dialog.dismiss()

                    // ───────── Progress Dialog ─────────
                    val bookNameText = TextView(activityContext).apply {
                        text = "Downloading: ${book.title}"
                        textSize = 18f
                        setPadding(20, 10, 20, 10)
                        gravity = Gravity.CENTER
                        setTextColor(Color.WHITE)
                    }

                    val percentText = TextView(activityContext).apply {
                        text = "Downloading: 0%\nডাউনলোড হচ্ছে: 0%"
                        textSize = 16f
                        setPadding(20, 10, 20, 10)
                        gravity = Gravity.CENTER_HORIZONTAL
                        setTextColor(Color.WHITE)
                    }

                    val progressBar = ProgressBar(
                        activityContext,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                    ).apply {
                        isIndeterminate = false
                        max = 100
                        progressDrawable = ContextCompat.getDrawable(
                            activityContext,
                            R.drawable.custom_progress_horizontal
                        )
                    }

                    val progressParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(activityContext, 16) // height = 16dp
                    ).apply {
                        topMargin = dpToPx(activityContext, 10)
                    }
                    progressBar.layoutParams = progressParams

                    val infoText = TextView(activityContext).apply {
                        text =
                            "Your internet speed may affect the download time.\n" +
                                    "আপনার ইন্টারনেট স্পিড অনুযায়ী সময় বেশি লাগতে পারে।\n\n" +
                                    "ডাউনলোড হতে হতে আসুন আল্লাহর জিকির করি:\n" +
                                    "সুবহানাল্লাহ, আলহামদুলিল্লাহ, আল্লাহু আকবার"
                        textSize = 14f
                        setPadding(20, 20, 20, 20)
                        setTextColor(Color.WHITE)
                    }

                    val layout = LinearLayout(activityContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(40, 30, 40, 30)
                        addView(bookNameText)
                        addView(percentText)
                        addView(progressBar)
                        addView(infoText)
                    }

                    val progressDialog = AlertDialog.Builder(activityContext)
                        .setView(layout)
                        .setCancelable(false)
                        .create()

                    progressDialog.show()

                    progressDialog.window?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            activityContext,
                            R.drawable.dialog_rounded_bg
                        )
                    )
                    progressDialog.window?.setLayout(
                        (activityContext.resources.displayMetrics.widthPixels * 0.90).toInt(),
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )

                    // ───────── DownloadManager লজিক ─────────
                    val request = DownloadManager.Request(Uri.parse(book.pdf_url))
                    request.setTitle("Downloading PDF")
                    request.setDescription("Downloading $fileName")
                    request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE
                    )
                    request.setDestinationInExternalFilesDir(
                        activityContext,
                        Environment.DIRECTORY_DOWNLOADS,
                        "pdf_books/$fileName"
                    )

                    val downloadManager =
                        activityContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val downloadId = downloadManager.enqueue(request)

                    val checkDownloadHandler = Handler(activityContext.mainLooper)
                    checkDownloadHandler.post(object : Runnable {
                        override fun run() {
                            val query = DownloadManager.Query().setFilterById(downloadId)
                            val cursor = downloadManager.query(query)
                            if (cursor != null && cursor.moveToFirst()) {
                                val status =
                                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                                val total =
                                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                val downloaded =
                                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                                if (total > 0) {
                                    val percent =
                                        (downloaded * 100 / total.toFloat()).toInt()
                                    progressBar.progress = percent

                                    percentText.text =
                                        "Downloading: $percent%\nডাউনলোড হচ্ছে: $percent%"
                                }

                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    cursor.close()
                                    progressDialog.dismiss()

                                    // ✅ Thumbnail ডাউনলোড
                                    downloadThumbnailImage(
                                        activityContext,
                                        book.thumbnail_url,
                                        book.title
                                    )

                                    val downloadedFile = File(
                                        activityContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                        "pdf_books/$fileName"
                                    )

                                    if (downloadedFile.exists()) {
                                        val viewIntent =
                                            Intent(activityContext, PdfViewerActivity::class.java)
                                        viewIntent.putExtra(
                                            "pdf_file",
                                            downloadedFile.absolutePath
                                        )
                                        activityContext.startActivity(viewIntent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Download failed or file missing",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    return
                                }
                            }
                            cursor?.close()
                            checkDownloadHandler.postDelayed(this, 1000)
                        }
                    })

                    Toast.makeText(
                        context,
                        "Download started… Your internet speed may affect the time.\n" +
                                "ডাউনলোড শুরু হয়েছে… আপনার ইন্টারনেট স্পিড অনুযায়ী সময় লাগতে পারে।",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun getItemCount(): Int = bookList.size

    // Thumbnail ডাউনলোড
    private fun downloadThumbnailImage(context: Context, imageUrl: String, title: String) {
        try {
            val fileName = title.replace(" ", "_") + ".jpg"
            val request = DownloadManager.Request(Uri.parse(imageUrl))
            request.setTitle("Downloading Thumbnail")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            request.setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_PICTURES,
                "book_thumbnails/$fileName"
            )

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Log.d("THUMB_DOWNLOAD", "Started thumbnail download: $fileName")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("THUMB_DOWNLOAD", "Thumbnail download failed: ${e.localizedMessage}")
        }
    }

    // dp → px helper
    private fun dpToPx(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}
