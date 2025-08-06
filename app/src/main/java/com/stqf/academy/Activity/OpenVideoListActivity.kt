package com.stqf.academy.Activity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.stqf.academy.R

class OpenVideoListActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_video_list)

        // Bind UI elements
        webView = findViewById(R.id.webView)
        backButton = findViewById(R.id.btnBackToList)

        // Setup WebView
        val playlistUrl = intent.getStringExtra("playlist_url")
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        if (isInternetAvailable(this)) {
            playlistUrl?.let {
                webView.loadUrl(it)
            }
        } else {
            webView.loadUrl("file:///android_asset/error.html")
        }

        // Handle physical back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        // Handle UI back button
        backButton.setOnClickListener {
            finish()
        }
    }

    // Internet check function
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
}