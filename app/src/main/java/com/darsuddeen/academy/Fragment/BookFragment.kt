package com.darsuddeen.academy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.darsuddeen.academy.Adapter.BookAdapter

import com.darsuddeen.academy.Model.BookModel
import com.darsuddeen.academy.databinding.FragmentBookBinding

class BookFragment : Fragment() {

    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Book List
        val bookList = listOf(
            BookModel("Learn Surah Fatiha", "Book1.pdf"),
            BookModel("Learn Surah Yaseen", "Book2.pdf"),
            BookModel("Tajweed Rules", "Book3.pdf"),
            BookModel("500 Quranic Words", "Book4.pdf"),
            BookModel("Arabic Grammar", "Book5.pdf")
        )

        // ✅ RecyclerView setup
        adapter = BookAdapter(requireContext(), bookList)
        binding.booksRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.booksRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}