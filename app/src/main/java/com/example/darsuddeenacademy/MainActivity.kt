package com.darsuddeen.academy

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.darsuddeen.academy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Hide default action bar
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // সাদা ব্যাকগ্রাউন্ড রাখো যেন কালো স্ক্রিন না আসে
        binding.root.setBackgroundColor(android.graphics.Color.WHITE)

        val webView = binding.webview
        val progressBar = binding.progressBar

        // ✅ WebView সেটিংস
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.loadsImagesAutomatically = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        webView.webChromeClient = WebChromeClient()

        // ✅ WebViewClient সহ Error & JavaScript Injection
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE


            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    view.loadUrl("file:///android_asset/error.html")
                    progressBar.visibility = View.GONE
                }
            }
        }

        // ✅ Load URL
        if (isInternetAvailable()) {
            webView.loadUrl("https://darsuddeenacademy.com")
        } else {
            webView.loadUrl("file:///android_asset/error.html")
        }

        // ✅ Back Press হ্যান্ডেল
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    // ✅ ইন্টারনেট চেকার ফাংশন
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
}