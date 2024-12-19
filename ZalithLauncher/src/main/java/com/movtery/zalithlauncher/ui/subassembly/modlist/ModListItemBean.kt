package com.movtery.zalithlauncher.ui.subassembly.modlist

import androidx.recyclerview.widget.RecyclerView
import com.movtery.zalithlauncher.feature.download.enums.ModLoader
import java.util.concurrent.atomic.AtomicReference

class ModListItemBean(@JvmField val title: String, @JvmField val modloader: ModLoader?, adapter: RecyclerView.Adapter<*>) {
    private val adapter = AtomicReference(adapter)

    fun getAdapter(): RecyclerView.Adapter<*> {
        return adapter.get()
    }

    override fun toString() = "CollapsibleExpandItemBean{title='$title', modloader=$modloader, adapter=$adapter}"
}

