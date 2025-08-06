package com.stqf.academy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.stqf.academy.Adapter.BookAdapter
import com.stqf.academy.Model.BookModel
import com.stqf.academy.databinding.FragmentBookBinding

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

        // ✅ Book List with thumbnails
        val bookList = listOf(
            BookModel("DDA সহজ কুরআন শিক্ষা কায়দা", "Book1.pdf", R.drawable.book1_thumb),
            BookModel("DDA ইসলাম শিক্ষা", "Book2.pdf", R.drawable.book2_thumb),
            BookModel("Hafizi Quran", "book_6.pdf", R.drawable.book3_thumb),
            BookModel("কুরআনের ৫০০ শব্দ", "Book3.pdf", R.drawable.book4_thumb),
            BookModel("৫০০ শব্দের অনুশীলনী", "Book4.pdf", R.drawable.book5_thumb),
            BookModel("Everyday English", "Book5.pdf", R.drawable.book6_thumb)
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