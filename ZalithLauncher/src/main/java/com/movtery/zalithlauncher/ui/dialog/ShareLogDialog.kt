package com.movtery.zalithlauncher.ui.dialog

import android.content.Context
import android.view.View
import android.view.Window
import android.widget.TextView
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.task.Task
import com.movtery.zalithlauncher.task.TaskExecutors
import com.movtery.zalithlauncher.ui.dialog.DraggableDialog.DialogInitializationListener
import com.movtery.zalithlauncher.utils.path.PathManager
import com.movtery.zalithlauncher.utils.file.FileTools
import net.kdt.pojavlaunch.Tools
import java.io.File

class ShareLogDialog(context: Context) : FullScreenDialog(context), DialogInitializationListener {
    //启动器的日志文件集合，这里只会提取带有"log"名称的日志文件
    private val mLauncherLogFiles: Array<File>
        get() = PathManager.DIR_LAUNCHER_LOG.let { logDir ->
            File(logDir).listFiles()?.filter { it.name.contains("log") }?.toTypedArray()
        } ?: emptyArray()

    private val mGameLogFile = File(PathManager.DIR_GAME_HOME, "/latestlog.txt")

    init {
        setContentView(R.layout.dialog_share_log)

        findViewById<View>(R.id.zh_launcher_log)?.apply {
            val launcherLogFiles = mLauncherLogFiles

            setOnClickListener {
                if (launcherLogFiles.isNotEmpty()) {
                    Task.runTask {
                        val zipFile = File(PathManager.DIR_APP_CACHE, "logs.zip")
                        FileTools.packZip(launcherLogFiles, zipFile)
                        zipFile
                    }.ended(TaskExecutors.getAndroidUI()) { zipFile ->
                        zipFile?.let { FileTools.shareFile(context, it) }
                    }.onThrowable { e -> Logging.e("ShareLauncherLog", Tools.printToString(e)) }
                        .execute()
                    this@ShareLogDialog.dismiss()
                }
            }

            val mLauncherLogFilesPath = findViewById<TextView>(R.id.launcher_files_path)
            if (launcherLogFiles.isEmpty()) {
                setUnClickable(this)
                mLauncherLogFilesPath.setText(R.string.file_does_not_exist)
            } else mLauncherLogFilesPath.text = PathManager.DIR_LAUNCHER_LOG
        }

        findViewById<View>(R.id.zh_game_log)?.apply {
            setOnClickListener {
                if (mGameLogFile.exists()) {
                    FileTools.shareFile(context, mGameLogFile)
                    this@ShareLogDialog.dismiss()
                }
            }

            val mGameLogFilePath = findViewById<TextView>(R.id.log_file_path)
            if (!mGameLogFile.exists()) {
                setUnClickable(this)
                mGameLogFilePath.setText(R.string.file_does_not_exist)
            } else mGameLogFilePath.text = mGameLogFile.absolutePath
        }

        findViewById<View>(R.id.close_button)?.setOnClickListener { dismiss() }

        DraggableDialog.initDialog(this)
    }

    private fun setUnClickable(view: View) {
        view.isClickable = false
        view.alpha = 0.5f
    }

    override fun onInit(): Window? = window
}
