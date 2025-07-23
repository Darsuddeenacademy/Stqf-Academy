package com.darsuddeen.academy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.databinding.FragmentVideosBinding

class VideoClassFragment : Fragment() {

    // ViewBinding object for fragment_videos.xml
    private var _binding: FragmentVideosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using ViewBinding
        _binding = FragmentVideosBinding.inflate(inflater, container, false)

        // Reference to the WebView from binding
        val webView: WebView = binding.videoWebView

        // Enable WebView settings
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false

        // Set WebView clients
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()

        // Load your YouTube playlist or video
        webView.loadUrl("https://youtube.com/playlist?list=PLdZH8A1MbXdcpwNcQlcOqeMDo-C9ItSlx&si=kn7zGWvmFhEpE1ac")

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memoryleaks
        }
}