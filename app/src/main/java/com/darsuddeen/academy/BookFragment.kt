package com.darsuddeen.academy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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

        // Static book list from assets (if you have thumbnails, we'll add later)
        val bookList = listOf(
            BookModel("DDA সহজ কুরআন শিক্ষা কায়দা", "Book1.pdf"),
            BookModel("DDA ইসলাম শিক্ষা", "Book2.pdf"),
            BookModel("কুরআনের ৫০০ শব্দ", "Book3.pdf"),
            BookModel("৫০০ শব্দের অনুশীলনী", "Book4.pdf"),
            BookModel("Everyday English", "Book5.pdf")
        )

        // ✅ Grid layout with 2 columns
        adapter = BookAdapter(requireContext(), bookList)
        binding.bookRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.bookRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}