package com.darsuddeen.academy.Activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.BookFragment
import com.darsuddeen.academy.Fragment.DashBoardFragment
import com.darsuddeen.academy.Fragment.HomeFragment
import com.darsuddeen.academy.Fragment.VideoClassFragment
import com.darsuddeen.academy.R
import com.darsuddeen.academy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNowOnHome = true
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())
        binding.bottomNavigationView.selectedItemId = R.id.menu_home

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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                if (currentFragment is HomeFragment && currentFragment.canGoBack()) {
                    currentFragment.goBack()
                    return
                }

                if (!isNowOnHome) {
                    isNowOnHome = true
                    loadFragment(HomeFragment())
                    binding.bottomNavigationView.selectedItemId = R.id.menu_home
                    return
                }

                // শুধু ডায়লগ বক্স
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}