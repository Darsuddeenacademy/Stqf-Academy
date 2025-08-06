package com.stqf.academy.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.stqf.academy.adapter.BookApiAdapter
import com.stqf.academy.databinding.FragmentLiveBooksBinding
import com.stqf.academy.model.BookApiModel
import com.stqf.academy.model.BookApiResponse
import com.stqf.academy.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LiveBooksFragment : Fragment() {

    private var _binding: FragmentLiveBooksBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookAdapter: BookApiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveBooksBinding.inflate(inflater, container, false)

        setupRecyclerView()

        if (isNetworkAvailable(requireContext())) {
            loadBooksFromApi()
        } else {
            loadDownloadedBooks()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.liveBooksRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun loadBooksFromApi() {
        val call = ApiClient.instance.getBooks()
        call.enqueue(object : Callback<BookApiResponse> {
            override fun onResponse(call: Call<BookApiResponse>, response: Response<BookApiResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val books = response.body()!!.books
                    bookAdapter = BookApiAdapter(requireContext(), books)
                    binding.liveBooksRecyclerView.adapter = bookAdapter
                } else {
                    Toast.makeText(requireContext(), "লাইভ বই পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookApiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "ত্রুটি: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadDownloadedBooks() {
        val downloadDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pdf_books")
        val offlineBooks = mutableListOf<BookApiModel>()

        if (downloadDir.exists()) {
            downloadDir.listFiles()?.forEach { file ->
                if (file.extension == "pdf") {
                    val title = file.nameWithoutExtension.replace("_", " ")
                    val thumbFile = File(
                        requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "book_thumbnails/${file.nameWithoutExtension}.jpg"
                    )
                    val model = BookApiModel(
                        title = title,
                        description = "অফলাইনে ডাউনলোড করা বই",
                        pdf_url = "",
                        thumbnail_url = if (thumbFile.exists()) thumbFile.absolutePath else ""
                    )
                    offlineBooks.add(model)
                }
            }
        }

        if (offlineBooks.isNotEmpty()) {
            bookAdapter = BookApiAdapter(requireContext(), offlineBooks)
            binding.liveBooksRecyclerView.adapter = bookAdapter
            Toast.makeText(requireContext(), "ইন্টারনেট নেই, অফলাইন বই দেখানো হচ্ছে", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "কোনো বই ডাউনলোড করা নেই", Toast.LENGTH_LONG).show()
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
