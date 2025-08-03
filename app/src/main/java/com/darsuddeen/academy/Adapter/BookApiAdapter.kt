package com.darsuddeen.academy.adapter

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.darsuddeen.academy.PdfViewerActivity
import com.darsuddeen.academy.databinding.ItemBookBinding
import com.darsuddeen.academy.model.BookApiModel
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.concurrent.thread

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

        Glide.with(context)
            .load(book.thumbnail_url)
            .into(holder.binding.bookThumbnail)

        holder.itemView.setOnClickListener {
            val isOnline = book.pdf_url.startsWith("http")
            val fileName = book.title.replace(" ", "_") + ".pdf"

            if (isOnline) {
                val localFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/pdf_books",
                    fileName
                )

                if (localFile.exists()) {
                    // File exists, open directly
                    val intent = Intent(context, PdfViewerActivity::class.java)
                    intent.putExtra("pdf_file", localFile.absolutePath)
                    context.startActivity(intent)
                } else {
                    AlertDialog.Builder(context)
                        .setTitle("Download Book")
                        .setMessage("Do you want to download this book for offline reading?")
                        .setPositiveButton("Yes") { _, _ ->
                            val progressDialog = ProgressDialog(context)
                            progressDialog.setTitle("Downloading")
                            progressDialog.setMessage("Please wait...")
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                            progressDialog.setCancelable(false)
                            progressDialog.show()

                            thread {
                                try {
                                    val url = URL(book.pdf_url)
                                    val connection = url.openConnection()
                                    connection.connect()

                                    val fileLength = connection.contentLength

                                    val input = url.openStream()
                                    val output = FileOutputStream(localFile)

                                    val data = ByteArray(1024)
                                    var total: Long = 0
                                    var count: Int

                                    while (input.read(data).also { count = it } != -1) {
                                        total += count
                                        output.write(data, 0, count)

                                        // Update progress bar
                                        val progress = (total * 100 / fileLength).toInt()
                                        progressDialog.progress = progress
                                    }

                                    output.flush()
                                    output.close()
                                    input.close()

                                    progressDialog.dismiss()

                                    // Open the file
                                    val intent = Intent(context, PdfViewerActivity::class.java)
                                    intent.putExtra("pdf_file", localFile.absolutePath)
                                    context.startActivity(intent)

                                } catch (e: Exception) {
                                    progressDialog.dismiss()
                                    e.printStackTrace()
                                }
                            }
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            } else {
                // Asset-based offline PDF
                val intent = Intent(context, PdfViewerActivity::class.java)
                intent.putExtra("pdf_file", book.pdf_url)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = bookList.size
}
