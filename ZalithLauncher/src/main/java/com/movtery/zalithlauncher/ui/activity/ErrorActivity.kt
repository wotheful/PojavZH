package com.movtery.zalithlauncher.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.movtery.zalithlauncher.InfoCenter
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.databinding.ActivityErrorBinding
import com.movtery.zalithlauncher.feature.version.VersionsManager
import com.movtery.zalithlauncher.utils.path.PathManager
import com.movtery.zalithlauncher.utils.file.FileTools.Companion.getLatestFile
import com.movtery.zalithlauncher.utils.file.FileTools.Companion.shareFile
import com.movtery.zalithlauncher.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.Tools
import java.io.File

class ErrorActivity : BaseActivity() {
    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        extras ?: run {
            finish()
            return
        }

        binding.errorConfirm.setOnClickListener { finish() }
        binding.errorRestart.setOnClickListener {
            startActivity(Intent(this@ErrorActivity, SplashActivity::class.java))
        }

        if (extras.getBoolean(BUNDLE_IS_ERROR, true)) {
            showError(extras)
        } else {
            //如果不是应用崩溃，那么这个页面就不允许截图
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            showCrash(extras)
        }
    }

    private fun showCrash(extras: Bundle) {
        val code = extras.getInt(BUNDLE_CODE, 0)
        if (code == 0) {
            finish()
            return
        }

        binding.errorButtons.visibility = View.GONE
        binding.errorTitle.setText(R.string.generic_wrong_tip)

        val crashReportFile = getLatestFile(extras.getString(BUNDLE_CRASH_REPORTS_PATH), 15)
        val logFile = File(PathManager.DIR_GAME_HOME, "latestlog.txt")

        val message = if (extras.getBoolean(BUNDLE_IS_SIGNAL)) R.string.game_singnal_message else R.string.game_exit_message

        binding.errorText.apply {
            text = getString(message, code)
            textSize = 14f
        }
        binding.errorTip.visibility = View.VISIBLE
        binding.errorNoScreenshot.visibility = View.VISIBLE
        binding.crashShareCrashReport.visibility = if ((crashReportFile?.exists() == true)) View.VISIBLE else View.GONE
        binding.crashShareLog.visibility = if (logFile.exists()) View.VISIBLE else View.GONE

        crashReportFile?.let { file ->
            binding.crashShareCrashReport.setOnClickListener {
                shareFile(this, file)
            }
        }
        binding.crashShareLog.setOnClickListener { Tools.shareLog(this) }
    }

    private fun showError(extras: Bundle) {
        binding.errorTitle.text = InfoCenter.replaceName(this, R.string.error_fatal)
        binding.crashButtons.visibility = View.GONE

        val throwable = extras.getSerializable(BUNDLE_THROWABLE) as Throwable?
        val stackTrace = if (throwable != null) Tools.printToString(throwable) else "<null>"
        val strSavePath = extras.getString(BUNDLE_SAVE_PATH)
        val errorText = "$strSavePath :\r\n\r\n$stackTrace"

        binding.errorText.text = errorText
        binding.errorCopy.setOnClickListener { StringUtils.copyText("error", stackTrace, this@ErrorActivity) }
        strSavePath?.let{
            val crashFile = File(strSavePath)
            binding.errorShare.setOnClickListener {
                shareFile(this, crashFile)
            }
        }
    }

    companion object {
        private const val BUNDLE_IS_ERROR = "is_error"
        private const val BUNDLE_IS_SIGNAL = "is_signal"
        private const val BUNDLE_CODE = "code"
        private const val BUNDLE_CRASH_REPORTS_PATH = "crash_reports_path"
        private const val BUNDLE_THROWABLE = "throwable"
        private const val BUNDLE_SAVE_PATH = "save_path"

        @JvmStatic
        fun showError(ctx: Context, savePath: String?, th: Throwable?) {
            val intent = Intent(ctx, ErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(BUNDLE_THROWABLE, th)
            intent.putExtra(BUNDLE_SAVE_PATH, savePath)
            intent.putExtra(BUNDLE_IS_ERROR, true)
            ctx.startActivity(intent)
        }

        @JvmStatic
        @JvmOverloads
        fun showExitMessage(
            ctx: Context,
            code: Int,
            isSignal: Boolean,
            crashReportsPath: String? = getCrashReportsPath()?.absolutePath
        ) {
            val intent = Intent(ctx, ErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(BUNDLE_CODE, code)
            intent.putExtra(BUNDLE_IS_ERROR, false)
            intent.putExtra(BUNDLE_IS_SIGNAL, isSignal)
            intent.putExtra(BUNDLE_CRASH_REPORTS_PATH, crashReportsPath)
            ctx.startActivity(intent)
        }

        private fun getCrashReportsPath(): File? {
            val gameDir: File = VersionsManager.getCurrentVersion()?.getGameDir() ?: return null
            return File(gameDir, "crash-reports")
        }
    }
}
