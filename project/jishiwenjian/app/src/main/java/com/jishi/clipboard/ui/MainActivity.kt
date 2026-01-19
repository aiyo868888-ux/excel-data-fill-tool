package com.jishi.clipboard.ui

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jishi.clipboard.R
import com.jishi.clipboard.ui.dialog.ClipboardEditDialogFragment
import com.jishi.clipboard.ui.fragments.HistoryFragment
import com.jishi.clipboard.ui.fragments.HomeFragment
import com.jishi.clipboard.ui.fragments.SettingsFragment
import com.jishi.clipboard.ui.fragments.TagsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主界面 - 底部导航栏架构
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        android.util.Log.d("MainActivity", "========== MainActivity.onCreate 开始 ==========")
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "super.onCreate 完成")
        setContentView(R.layout.activity_main)
        android.util.Log.d("MainActivity", "setContentView 完成")

        initViews()
        android.util.Log.d("MainActivity", "initViews 完成")
        setupBottomNavigation()
        android.util.Log.d("MainActivity", "setupBottomNavigation 完成")

        // 隐藏 ActionBar
        supportActionBar?.hide()
        android.util.Log.d("MainActivity", "========== MainActivity.onCreate 完成 ==========")
    }

    private fun initViews() {
        bottomNav = findViewById(R.id.bottomNav)
        frameLayout = findViewById(R.id.fragmentContainer)
    }

    private fun setupBottomNavigation() {
        // 设置底部导航监听
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment.newInstance())
                    true
                }
                R.id.navigation_history -> {
                    replaceFragment(HistoryFragment.newInstance())
                    true
                }
                R.id.navigation_tags -> {
                    replaceFragment(TagsFragment.newInstance())
                    true
                }
                R.id.navigation_settings -> {
                    replaceFragment(SettingsFragment.newInstance())
                    true
                }
                else -> false
            }
        }

        // 默认显示历史页面
        bottomNav.selectedItemId = R.id.navigation_history
        replaceFragment(HistoryFragment.newInstance())
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

