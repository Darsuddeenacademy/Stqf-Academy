package com.darsuddeen.academy

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darsuddeen.academy.databinding.ActivityPdfViewerBinding
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding
    private var totalPages = 0
    private var isToolbarVisible = false
    private var isPenMode = false
    private var isPenIconVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingPen.visibility = android.view.View.GONE

        val filePath = intent.getStringExtra("pdf_file") ?: return

        if (filePath.startsWith("/")) {
            // PDF from local storage (downloaded)
            val file = File(filePath)
            binding.pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange(onPageChangeListener)
                .onTap(tapListener)
                .load()
        } else {
            // PDF from assets folder
            binding.pdfView.fromAsset(filePath)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange(onPageChangeListener)
                .onTap(tapListener)
                .load()
        }

        // Page number tap to jump
        binding.pageNumberText.setOnClickListener {
            showPageInputDialog()
        }

        // Floating pen toggle
        binding.floatingPen.setOnClickListener {
            isToolbarVisible = !isToolbarVisible
            binding.drawingToolbar.visibility =
                if (isToolbarVisible) android.view.View.VISIBLE else android.view.View.GONE

            isPenMode = isToolbarVisible
            binding.drawingView.isDrawingEnabled = isPenMode
            binding.pdfView.setSwipeEnabled(!isPenMode)
        }

        // Color pickers
        binding.colorRed.setOnClickListener { binding.drawingView.setColor(Color.RED) }
        binding.colorBlue.setOnClickListener { binding.drawingView.setColor(Color.BLUE) }
        binding.colorGreen.setOnClickListener { binding.drawingView.setColor(Color.GREEN) }
        binding.colorBlack.setOnClickListener { binding.drawingView.setColor(Color.BLACK) }

        // Clear drawings
        binding.clearButton.setOnClickListener {
            binding.drawingView.clearDrawing()
        }
    }

    private val onPageChangeListener = OnPageChangeListener { page, pageCount ->
        totalPages = pageCount
        binding.pageNumberText.text = "${page + 1} / $pageCount"
    }

    private val tapListener = OnTapListener {
        togglePenIcon()
        true
    }

    private fun togglePenIcon() {
        isPenIconVisible = !isPenIconVisible
        binding.floatingPen.visibility =
            if (isPenIconVisible) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showPageInputDialog() {
        val input = EditText(this)
        input.hint = "Enter page number"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(this)
            .setTitle("Go to Page")
            .setView(input)
            .setPositiveButton("Go") { _, _ ->
                val pageNum = input.text.toString().toIntOrNull()
                if (pageNum != null && pageNum in 1..totalPages) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.pdfView.jumpTo(pageNum - 1, true)
                    }, 100)
                } else {
                    Toast.makeText(this, "Invalid page number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
