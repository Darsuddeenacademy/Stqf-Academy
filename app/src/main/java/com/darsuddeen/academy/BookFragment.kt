package com.darsuddeen.academy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.databinding.FragmentBookBinding


class BookFragment : Fragment() {

    // ViewBinding setup
    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout with ViewBinding
        _binding = FragmentBookBinding.inflate(inflater, container, false)

        // Setup WebView
        binding.bookWebView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl("https://darsuddeenacademy.com/books")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        }
}