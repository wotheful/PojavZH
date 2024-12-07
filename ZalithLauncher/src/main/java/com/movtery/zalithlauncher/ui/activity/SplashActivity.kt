package com.movtery.zalithlauncher.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.ContextExecutor
import com.movtery.zalithlauncher.databinding.ActivitySplashBinding
import com.movtery.zalithlauncher.feature.unpack.Components
import com.movtery.zalithlauncher.feature.unpack.Jre
import com.movtery.zalithlauncher.feature.unpack.UnpackComponentsTask
import com.movtery.zalithlauncher.feature.unpack.UnpackJreTask
import com.movtery.zalithlauncher.feature.unpack.UnpackSingleFilesTask
import com.movtery.zalithlauncher.task.Task
import com.movtery.zalithlauncher.ui.dialog.TipDialog
import com.movtery.zalithlauncher.utils.StoragePermissionsUtils
import net.kdt.pojavlaunch.LauncherActivity
import net.kdt.pojavlaunch.MissingStorageActivity
import net.kdt.pojavlaunch.Tools

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var isStarted: Boolean = false
    private lateinit var binding: ActivitySplashBinding
    private lateinit var installableAdapter: InstallableAdapter
    private val items: MutableList<InstallableItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItems()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val splashText = findViewById<TextView>(R.id.splash_text)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SplashActivity)
            adapter = installableAdapter
        }

        binding.startButton.apply {
            setOnClickListener {
                if (isStarted) return@setOnClickListener
                isStarted = true
                splashText.setText(R.string.splash_screen_installing)
                installableAdapter.startAllTasks()
            }
            isClickable = false
        }

        if (!Tools.checkStorageRoot()) {
            startActivity(Intent(this, MissingStorageActivity::class.java))
            finish()
            return
        }

        //如果安卓版本小于等于9，则检查存储权限（不是管理所有文件权限），拥有存储权限会保证文件、文件夹正常创建
        //但是并不强制要求用户必须授予权限，如果用户拒绝，那么之后产生的问题将由用户承担
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !StoragePermissionsUtils.hasStoragePermissions(this)) {
            TipDialog.Builder(this)
                .setMessage(R.string.permissions_write_external_storage)
                .setConfirmClickListener { requestStoragePermissions() }
                .setCancelClickListener { checkEnd() } //用户取消，那就跟随用户的意愿
                .buildDialog()
        } else {
            checkEnd()
        }
    }

    override fun onResume() {
        super.onResume()
        ContextExecutor.setActivity(this)
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            //无论用户是否授予了权限，都会完成检查，因为启动器并不强制要求权限
            //但是一旦因为存储权限出现了问题，那么将由用户自行承担后果
            checkEnd()
        }
    }

    private fun initItems() {
        Components.entries.forEach {
            val unpackComponentsTask = UnpackComponentsTask(this, it)
            if (!unpackComponentsTask.isCheckFailed()) {
                items.add(
                    InstallableItem(
                        it.displayName,
                        it.summary?.let { it1 -> getString(it1) },
                        unpackComponentsTask
                    )
                )
            }
        }
        Jre.entries.forEach {
            val unpackJreTask = UnpackJreTask(this, it)
            if (!unpackJreTask.isCheckFailed()) {
                items.add(
                    InstallableItem(
                        it.jreName,
                        getString(it.summary),
                        unpackJreTask
                    )
                )
            }
        }
        items.sort()
        installableAdapter = InstallableAdapter(items) {
            toMain()
        }
    }
    
    private fun checkEnd() {
        installableAdapter.checkAllTask()
        Task.runTask {
            UnpackSingleFilesTask(this).run()
        }.execute()

        binding.startButton.isClickable = true
    }

    private fun toMain() {
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE: Int = 100
    }
}