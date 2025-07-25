package com.darsuddeen.academy

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNowOnHome = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())
        binding.bottomNavigationView.selectedItemId = R.id.menu_home

        // Bottom navigation click
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

        // Back button behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

                // যদি এখন হোমে না থাকি, তাহলে হোমে নিয়ে যাবে
                if (!isNowOnHome) {
                    isNowOnHome = true
                    loadFragment(HomeFragment())
                    binding.bottomNavigationView.selectedItemId = R.id.menu_home
                    return
                }

                // এখন হোমে আছি — তাই Exit Dialog দেখাবে
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