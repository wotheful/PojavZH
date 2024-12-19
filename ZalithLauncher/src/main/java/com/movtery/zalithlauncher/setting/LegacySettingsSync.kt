package com.movtery.zalithlauncher.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.movtery.zalithlauncher.feature.log.Logging

/**
 * 将废弃的旧设置数据，同步为新的设置数据
 */
class LegacySettingsSync {
    companion object {
        @JvmStatic
        fun check(context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)

            if (preferences.all.isEmpty()) {
                setSynced(preferences)
            } else if (!preferences.getBoolean("isSynced", false)) {
                start(preferences)
            }
        }

        @SuppressLint("CheckResult")
        private fun start(pref: SharedPreferences) {
            Logging.i("LegacySettingsSync", "Start syncing legacy settings data!")
            val builder = Settings.Manager.SettingBuilder()
            pref.all.forEach { (key, value) ->
                value?.let { builder.put(key, it) }
            }
            builder.save()

            setSynced(pref)
        }

        private fun setSynced(pref: SharedPreferences) {
            pref.edit().putBoolean("isSynced", true).apply()
        }
    }
}