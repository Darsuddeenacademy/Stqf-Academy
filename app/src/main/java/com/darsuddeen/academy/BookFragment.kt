package com.darsuddeen.academy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

        // Static book list from assets
        val bookList = listOf(
            Book("Book 1", "Book1.pdf"),
            Book("Book 2", "Book2.pdf"),
            Book("Book 3", "Book3.pdf"),
            Book("Book 4", "Book4.pdf"),
            Book("Book 5", "Book5.pdf")
        )

        // Setup RecyclerView
        adapter = BookAdapter(requireContext(), bookList)
        binding.booksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.booksRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        }
}