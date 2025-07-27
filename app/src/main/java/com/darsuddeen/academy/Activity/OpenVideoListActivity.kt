package com.darsuddeen.academy.Activity

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class OpenVideoListActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val playlistUrl = intent.getStringExtra("playlist_url")

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        playlistUrl?.let {
            webView.loadUrl(it)
        }

        // Handle back press for WebView history
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack() // go to previous video in playlist
                } else {
                    finish() // exit activity and return to RecyclerView
                }
            }
            })
        }
}