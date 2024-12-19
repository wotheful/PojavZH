package com.movtery.zalithlauncher.feature.notice

import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.ZHTools
import com.movtery.zalithlauncher.utils.http.CallUtils
import com.movtery.zalithlauncher.utils.http.CallUtils.CallbackListener
import com.movtery.zalithlauncher.utils.path.UrlManager
import com.movtery.zalithlauncher.utils.stringutils.StringUtils
import net.kdt.pojavlaunch.Tools
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Objects

class CheckNewNotice {
    companion object {
        @JvmStatic
        var noticeInfo: NoticeInfo? = null
        private var isChecking = false

        private fun checkCooling(): Boolean {
            return ZHTools.getCurrentTimeMillis() - AllSettings.noticeCheck.getValue() > 2 * 60 * 1000 //2分钟冷却
        }

        @JvmStatic
        fun checkNewNotice(listener: CheckNoticeListener) {
            if (isChecking) {
                return
            }
            isChecking = true

            noticeInfo?.let {
                listener.onSuccessful(noticeInfo)
                isChecking = false
                return
            }

            if (!checkCooling()) {
                return
            } else {
                AllSettings.noticeCheck.put(ZHTools.getCurrentTimeMillis()).save()
            }

            CallUtils(object : CallbackListener {
                override fun onFailure(call: Call?) {
                    isChecking = false
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response?) {
                    if (!response!!.isSuccessful) {
                        Logging.e("CheckNewNotice", "Unexpected code ${response.code}")
                    } else {
                        runCatching {
                            Objects.requireNonNull(response.body)
                            val responseBody = response.body!!.string()

                            val originJson = JSONObject(responseBody)
                            val rawBase64 = originJson.getString("content")
                            //base64解码，因为这里读取的是一个经过Base64加密后的文本
                            val rawJson = StringUtils.decodeBase64(rawBase64)

                            val noticeJson = Tools.GLOBAL_GSON.fromJson(rawJson, NoticeJsonObject::class.java)

                            //获取通知消息
                            val language = ZHTools.getSystemLanguage()
                            val title = getLanguageText(language, noticeJson.title)
                            val content = getLanguageText(language, noticeJson.content)

                            noticeInfo = NoticeInfo(title, content, noticeJson.date, noticeJson.numbering)
                            listener.onSuccessful(noticeInfo)
                        }.getOrElse { e ->
                            Logging.e("Check New Notice", e.toString())
                        }
                    }
                    isChecking = false
                }
            }, "${UrlManager.URL_GITHUB_HOME}launcher_notice.json", null).enqueue()
        }

        private fun getLanguageText(language: String, text: NoticeJsonObject.Text): String {
            return when (language) {
                "zh_cn" -> text.zhCN
                "zh_tw" -> text.zhTW
                else -> text.enUS
            }
        }
    }
}
