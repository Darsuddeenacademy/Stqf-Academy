package com.stqf.academy.activity


import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.stqf.academy.BookFragment
import com.stqf.academy.fragment.HomeFragment
import com.stqf.academy.fragment.VideoClassFragment
import com.stqf.academy.R
import com.stqf.academy.databinding.ActivityMainBinding
import com.stqf.academy.fragment.ColorQuranFragment
import com.stqf.academy.fragment.DashBoardFragment
import com.stqf.academy.fragment.HafeziQuranFragment
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
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer)

            when (item.itemId) {
                R.id.menu_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.menu_books -> {
                    loadFragment(BookFragment())
                    true
                }

                R.id.menu_videos -> {
                    loadFragment(VideoClassFragment())
                    true
                }

                R.id.menu_dashboard -> {
                    // ✅ যদি এখন Color / Hafezi / LiveBooks-এর ভেতরে থাকি,
                    // তাহলে নতুন Dashboard লোড না করে শুধু back stack থেকে পুরনোটা ফিরিয়ে আনি
                    if (currentFragment is LiveBooksFragment ||
                        currentFragment is ColorQuranFragment ||
                        currentFragment is HafeziQuranFragment
                    ) {
                        supportFragmentManager.popBackStack()
                    } else {
                        loadFragment(DashBoardFragment())
                    }
                    true
                }

                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                // HomeFragment এর WebView ব্যাক নেভিগেশন
                if (currentFragment is HomeFragment && currentFragment.canGoBack()) {
                    currentFragment.goBack()
                    return
                }

                // ✅ Dashboard থেকে ওপেন হওয়া ৩টা Fragment:
                // LiveBooksFragment, ColorQuranFragment, HafeziQuranFragment
                if (currentFragment is LiveBooksFragment ||
                    currentFragment is ColorQuranFragment ||
                    currentFragment is HafeziQuranFragment
                ) {
                    supportFragmentManager.popBackStack()
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
