package com.example.findme

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.findme.databinding.ActivityMainBinding
import com.example.findme.ui.home.HomeFragment
import com.example.findme.ui.myreports.MyReportsFragment
import com.example.findme.ui.report.ReportFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment by lazy { HomeFragment.newInstance() }
    private val reportFragment by lazy { ReportFragment.newInstance() }
    private val myReportsFragment by lazy { MyReportsFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { _, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())

            // Apply status bar top padding to AppBarLayout
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)

            // When keyboard is visible, hide bottom navigation so it doesn't float above keyboard.
            // When keyboard is hidden, restore bottom navigation with navigation bar padding.
            if (isImeVisible) {
                binding.bottomNavigation.visibility = View.GONE
                binding.mainRoot.setPadding(systemBars.left, 0, systemBars.right, ime.bottom)
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
                binding.mainRoot.setPadding(systemBars.left, 0, systemBars.right, 0)
            }

            windowInsets
        }

        setupNavigation()

        if (savedInstanceState == null) {
            switchFragment(homeFragment, getString(R.string.title_home))
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFragment(homeFragment, getString(R.string.title_home))
                    true
                }
                R.id.navigation_report -> {
                    switchFragment(reportFragment, getString(R.string.title_report))
                    true
                }
                R.id.navigation_my_reports -> {
                    switchFragment(myReportsFragment, getString(R.string.title_my_reports))
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(fragment: Fragment, title: String) {
        binding.topToolbar.title = "FindMe • $title"
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_container, fragment)
            .commit()
    }
}
