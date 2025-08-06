package com.stqf.academy.Activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.stqf.academy.BookFragment
import com.stqf.academy.Fragment.HomeFragment
import com.stqf.academy.Fragment.VideoClassFragment
import com.stqf.academy.R
import com.stqf.academy.databinding.ActivityMainBinding
import com.stqf.academy.fragment.DashBoardFragment
import com.stqf.academy.fragment.LiveBooksFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var justNavigatedToHome = false
    private var exitDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // App launch হলে একবারই এই ফ্ল্যাগ হবে
        justNavigatedToHome = true

        loadFragment(HomeFragment())
        binding.bottomNavigationView.selectedItemId = R.id.menu_home

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    loadFragment(HomeFragment())
                }
                R.id.menu_books -> {
                    loadFragment(BookFragment())
                }
                R.id.menu_videos -> {
                    loadFragment(VideoClassFragment())
                }
                R.id.menu_dashboard -> {
                    loadFragment(DashBoardFragment())
                }
            }
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                // HomeFragment এর WebView ব্যাক নেভিগেশন
                if (currentFragment is HomeFragment && currentFragment.canGoBack()) {
                    currentFragment.goBack()
                    return
                }

                // LiveBooksFragment → DashBoardFragment
                if (currentFragment is LiveBooksFragment) {
                    loadFragment(DashBoardFragment())
                    binding.bottomNavigationView.selectedItemId = R.id.menu_dashboard
                    return
                }

                // যেকোনো Fragment থেকে → HomeFragment
                if (currentFragment !is HomeFragment) {
                    loadFragment(HomeFragment())
                    binding.bottomNavigationView.selectedItemId = R.id.menu_home
                    return
                }

                // এখন যদি HomeFragment-এ থাকি
                if (justNavigatedToHome) {
                    justNavigatedToHome = false
                } else {
                    if (!exitDialogShown) {
                        showExitDialog()
                    }
                }
            }
        })
    }

    private fun showExitDialog() {
        exitDialogShown = true
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No") { _, _ -> exitDialogShown = false }
            .setCancelable(false)
            .show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
