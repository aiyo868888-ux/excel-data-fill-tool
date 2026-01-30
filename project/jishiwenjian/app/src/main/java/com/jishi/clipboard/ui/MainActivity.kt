package com.jishi.clipboard.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jishi.clipboard.R
import com.jishi.clipboard.repository.UnifiedContentRepository
import com.jishi.clipboard.ui.fragments.InsightFragment
import com.jishi.clipboard.ui.fragments.InspirationFragment
import com.jishi.clipboard.ui.fragments.SettingsFragment
import com.jishi.clipboard.ui.fragments.TodoFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 主界面 - 底部导航栏架构
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var unifiedRepository: UnifiedContentRepository

    private val bottomNav: BottomNavigationView by lazy { findViewById(R.id.bottomNav) }

    companion object {
        private const val REQUEST_CODE_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        supportActionBar?.hide()
        setupBottomNavigation(savedInstanceState)
        initializeRepository()
    }

    private fun initializeRepository() {
        lifecycleScope.launch {
            runCatching { 
                unifiedRepository.initialize() 
            }.onSuccess {
                Timber.d("Repository 初始化完成")
            }.onFailure { error ->
                Timber.e(error, "Repository 初始化失败")
            }
        }
    }

    private fun requestStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                requestManageAllFilesPermission()
            }
            else -> {
                requestLegacyStoragePermission()
            }
        }
    }

    private fun requestManageAllFilesPermission() {
        if (Environment.isExternalStorageManager()) return
        
        runCatching {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
            Toast.makeText(this, "请授予存储权限", Toast.LENGTH_LONG).show()
        }.onFailure { error ->
            Timber.e(error, "无法打开权限设置")
        }
    }

    private fun requestLegacyStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_CODE_STORAGE
        )
    }

    private fun setupBottomNavigation(savedInstanceState: Bundle?) {
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_inspiration -> InspirationFragment.newInstance()
                R.id.navigation_insight -> InsightFragment.newInstance()
                R.id.navigation_todo -> TodoFragment.newInstance()
                R.id.navigation_settings -> SettingsFragment.newInstance()
                else -> return@setOnItemSelectedListener false
            }
            replaceFragment(fragment)
            true
        }

        // 默认显示灵感页面
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navigation_inspiration
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

