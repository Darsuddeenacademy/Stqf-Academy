package com.darsuddeen.academy.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darsuddeen.academy.Model.BookModel
import com.darsuddeen.academy.Utils.PDFUtils
import com.darsuddeen.academy.databinding.ItemBookBinding

import com.darsuddeen.academy.PdfViewerActivity

class BookAdapter(
    private val context: Context,
    private val bookList: List<BookModel>
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.binding.bookTitle.text = book.title
        holder.binding.bookDescription.text = "Click to open ${book.title}"

        // Load first page thumbnail
        val thumbnail: Bitmap? = PDFUtils.getFirstPageThumbnail(context, book.assetFileName)
        thumbnail?.let {
            holder.binding.bookThumbnail.setImageBitmap(it)
        }

        // Show ripple then open PDF
        holder.binding.root.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, PdfViewerActivity::class.java)
                intent.putExtra("pdf_file", book.assetFileName)
                context.startActivity(intent)
            }, 150)
        }
    }

    override fun getItemCount(): Int = bookList.size
}