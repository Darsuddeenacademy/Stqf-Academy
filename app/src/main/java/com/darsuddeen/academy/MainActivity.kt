package com.darsuddeen.academy

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var doubleBackToExitPressedOnce = false
    private var isNowOnHome = true // Track whether we are on Home

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())
        binding.bottomNavigationView.selectedItemId = R.id.menu_home

        // Bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    isNowOnHome = true
                    loadFragment(HomeFragment())
                }
                R.id.menu_books -> {
                    isNowOnHome = false
                    loadFragment(BookFragment())
                }
                R.id.menu_videos -> {
                    isNowOnHome = false
                    loadFragment(VideoClassFragment())
                }
                R.id.menu_dashboard -> {
                    isNowOnHome = false
                    loadFragment(DashBoardFragment())
                }
            }
            true
        }

        // BACK press handle
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                // 1. VideoClassFragment WebView
                if (currentFragment is VideoClassFragment) {
                    val webView = currentFragment.view?.findViewById<WebView>(R.id.videoWebView)
                    if (webView != null && webView.canGoBack()) {
                        webView.goBack()
                        return
                    }
                }

                // 2. HomeFragment WebView
                if (currentFragment is HomeFragment) {
                    val webView = currentFragment.view?.findViewById<WebView>(R.id.webview)
                    if (webView != null && webView.canGoBack()) {
                        webView.goBack()
                        return
                    }

                    // No more back in WebView — Show toast
                    if (doubleBackToExitPressedOnce) {
                        finish()
                    } else {
                        doubleBackToExitPressedOnce = true
                        Toast.makeText(
                            this@MainActivity,
                            "Press BACK again to exit",
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2000)
                    }
                    return
                }

                // 3. From any other fragment → go to HomeFragment
                if (!isNowOnHome) {
                    isNowOnHome = true
                    loadFragment(HomeFragment())
                    binding.bottomNavigationView.selectedItemId = R.id.menu_home
                }
            }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Optional internet check
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
}