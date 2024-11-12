package com.movtery.zalithlauncher.ui.subassembly.hotbar

import com.movtery.zalithlauncher.setting.AllSettings.Companion.hotbarType

class HotbarUtils {
    companion object {
        @JvmStatic
        fun getCurrentType(): HotbarType {
            val hotbarType = hotbarType ?: return HotbarType.AUTO
            return when (hotbarType) {
                "manually" -> HotbarType.MANUALLY
                "auto" -> HotbarType.AUTO
                else -> HotbarType.AUTO
            }
        }

        @JvmStatic
        fun getCurrentTypeIndex(): Int {
            val hotbarType = hotbarType ?: return 0
            return when (hotbarType) {
                "manually" -> 1
                "auto" -> 0
                else -> 0
            }
        }
    }
}