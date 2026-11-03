package com.example.findme

import android.os.Bundle
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainCoordinator) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupNavigation()

        if (savedInstanceState == null) {
            // Start with Home destination
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
