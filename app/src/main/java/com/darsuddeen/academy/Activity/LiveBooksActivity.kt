package com.darsuddeen.academy.activity  // ✅ এটা ছোট হরফে হওয়া লাগবে

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.darsuddeen.academy.adapter.BookApiAdapter
import com.darsuddeen.academy.databinding.ActivityLiveBooksBinding
import com.darsuddeen.academy.model.BookApiResponse
import com.darsuddeen.academy.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LiveBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveBooksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView setup
        binding.liveBooksRecyclerView.layoutManager = GridLayoutManager(this, 2)

        // Load data
        loadBooksFromApi()
    }

    private fun loadBooksFromApi() {
        ApiClient.instance.getBooks().enqueue(object : Callback<BookApiResponse> {
            override fun onResponse(
                call: Call<BookApiResponse>,
                response: Response<BookApiResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val books = response.body()!!.books
                    val adapter = BookApiAdapter(this@LiveBooksActivity, books)
                    binding.liveBooksRecyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@LiveBooksActivity, "No books found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookApiResponse>, t: Throwable) {
                Toast.makeText(this@LiveBooksActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
