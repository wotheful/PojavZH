package com.movtery.zalithlauncher.feature.login

import android.content.Context
import com.google.gson.Gson
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.feature.log.Logging.e
import com.movtery.zalithlauncher.utils.path.UrlManager.Companion.TIME_OUT
import com.movtery.zalithlauncher.utils.path.UrlManager.Companion.createRequestBuilder
import com.movtery.zalithlauncher.utils.stringutils.StringUtilsKt
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.value.MinecraftAccount
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Objects
import java.util.UUID

object OtherLoginApi {
    private var client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(TIME_OUT.first, TIME_OUT.second)
        .connectTimeout(TIME_OUT.first, TIME_OUT.second)
        .readTimeout(TIME_OUT.first, TIME_OUT.second)
        .writeTimeout(TIME_OUT.first, TIME_OUT.second)
        .build()
    private var baseUrl: String? = null

    fun setBaseUrl(baseUrl: String) {
        var url = baseUrl
        if (baseUrl.endsWith("/")) {
            url = baseUrl.substring(0, baseUrl.length - 1)
        }
        this.baseUrl = url
    }

    @Throws(IOException::class)
    fun login(context: Context, userName: String?, password: String?, listener: Listener) {
        if (Objects.isNull(baseUrl)) {
            listener.onFailed(context.getString(R.string.other_login_baseurl_not_set))
            return
        }
        val agent = AuthRequest.Agent().apply {
            this.name = "Minecraft"
            this.version = 1.0
        }
        val authRequest = AuthRequest().apply {
            this.username = userName
            this.password = password
            this.agent = agent
            this.requestUser = true
            this.clientToken = UUID.randomUUID().toString().lowercase()
        }
        val data = Gson().toJson(authRequest)
        callLogin(data, "/authserver/authenticate", listener)
    }

    @Throws(IOException::class)
    fun refresh(context: Context, account: MinecraftAccount, select: Boolean, listener: Listener) {
        if (Objects.isNull(baseUrl)) {
            listener.onFailed(context.getString(R.string.other_login_baseurl_not_set))
            return
        }
        val refresh = Refresh().apply {
            this.clientToken = account.clientToken
            this.accessToken = account.accessToken
        }
        if (select) {
            val selectedProfile = Refresh.SelectedProfile().apply {
                this.name = account.username
                this.id = account.profileId
            }
            refresh.selectedProfile = selectedProfile
        }
        val data = Gson().toJson(refresh)
        callLogin(data, "/authserver/refresh", listener)
    }

    @Throws(IOException::class)
    private fun callLogin(data: String, url: String, listener: Listener) {
        val body = data.toRequestBody("application/json".toMediaTypeOrNull())
        val call = client.newCall(createRequestBuilder(baseUrl + url, body).build())

        call.execute().use { response ->
            val res = response.body?.string()
            if (response.code == 200) {
                val result = Gson().fromJson(res, AuthResult::class.java)
                listener.onSuccess(result)
            } else {
                var errorMessage: String = res ?: "null"
                runCatching {
                    if (errorMessage == "null") return@runCatching

                    val jsonObject = JSONObject(errorMessage)
                    errorMessage = if (jsonObject.has("errorMessage")) {
                        jsonObject.getString("errorMessage")
                    } else if (jsonObject.has("message")) {
                        jsonObject.getString("message")
                    } else {
                        e("Other Login", "The error message returned by the server could not be retrieved.")
                        return@runCatching
                    }

                    if (errorMessage.contains("\\u"))
                        errorMessage = StringUtilsKt.decodeUnicode(errorMessage.replace("\\\\u", "\\u"))
                }.getOrElse { e -> e("Other Login", Tools.printToString(e)) }
                listener.onFailed("(${response.code}) $errorMessage")
            }
        }
    }

    fun getServeInfo(context: Context, url: String): String? {
        val call = client.newCall(createRequestBuilder(url).get().build())
        runCatching {
            call.execute().use { response ->
                val res = response.body?.string()
                if (response.code == 200) return res
            }
        }.getOrElse { e -> Tools.showError(context, e) }
        return null
    }

    interface Listener {
        fun onSuccess(authResult: AuthResult)
        fun onFailed(error: String)
    }
}