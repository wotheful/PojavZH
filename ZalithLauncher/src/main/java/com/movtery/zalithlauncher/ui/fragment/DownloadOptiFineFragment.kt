package com.movtery.zalithlauncher.ui.fragment

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.event.sticky.SelectInstallTaskEvent
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.feature.mod.modloader.ModVersionListAdapter
import com.movtery.zalithlauncher.task.TaskExecutors
import com.movtery.zalithlauncher.ui.subassembly.modlist.ModListFragment
import com.movtery.zalithlauncher.utils.ZHTools
import net.kdt.pojavlaunch.Tools
import com.movtery.zalithlauncher.feature.mod.modloader.OptiFineDownloadTask
import com.movtery.zalithlauncher.feature.version.Addon
import com.movtery.zalithlauncher.ui.fragment.InstallGameFragment.Companion.BUNDLE_MC_VERSION
import net.kdt.pojavlaunch.modloaders.OptiFineUtils
import net.kdt.pojavlaunch.modloaders.OptiFineUtils.OptiFineVersion
import net.kdt.pojavlaunch.modloaders.OptiFineUtils.OptiFineVersions
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadOptiFineFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadOptiFineFragment"
    }

    override fun init() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, R.drawable.ic_optifine))
        setTitleText("OptiFine")
        setLink("https://www.optifine.net/home")
        setMCMod("https://www.mcmod.cn/class/36.html")
        setReleaseCheckBoxGone()
        super.init()
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    private fun refresh(force: Boolean): Future<*> {
        return TaskExecutors.getDefault().submit {
            runCatching {
                TaskExecutors.runInUIThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val optiFineVersions = OptiFineUtils.downloadOptiFineVersions(force)
                processModDetails(optiFineVersions)
            }.getOrElse { e ->
                TaskExecutors.runInUIThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadOptiFineFragment", Tools.printToString(e))
            }
        }
    }

    private fun empty() {
        TaskExecutors.runInUIThread {
            componentProcessing(false)
            setFailedToLoad(getString(R.string.version_install_no_versions))
        }
    }

    private fun processModDetails(optiFineVersions: OptiFineVersions?) {
        optiFineVersions ?: run {
            empty()
            return
        }

        val mcVersion = arguments?.getString(BUNDLE_MC_VERSION) ?: throw IllegalArgumentException("The Minecraft version is not passed")

        val mOptiFineVersions: MutableMap<String, MutableList<OptiFineVersion>> = HashMap()
        optiFineVersions.optifineVersions.forEach(Consumer<List<OptiFineVersion>> { optiFineVersionList: List<OptiFineVersion> ->  //通过版本列表一层层遍历并合成为 Minecraft版本 + Optifine版本的Map集合
            currentTask?.apply { if (isCancelled) return@Consumer }

            optiFineVersionList.forEach(Consumer Consumer2@{ optiFineVersion: OptiFineVersion ->
                currentTask?.apply { if (isCancelled) return@Consumer2 }
                addIfAbsent(mOptiFineVersions, optiFineVersion.minecraftVersion.removePrefix("Minecraft").trim(), optiFineVersion)
            })
        })

        if (currentTask!!.isCancelled) return

        val mcOptiFineVersions = mOptiFineVersions[mcVersion] ?: mOptiFineVersions[mcVersion] ?: run {
            empty()
            return
        }

        val adapter = ModVersionListAdapter(R.drawable.ic_optifine, mcOptiFineVersions)
        adapter.setOnItemClickListener { version: Any ->
            if (isTaskRunning()) return@setOnItemClickListener false

            val optifineVersion = version as OptiFineVersion
            EventBus.getDefault().postSticky(
                SelectInstallTaskEvent(
                    Addon.OPTIFINE,
                    optifineVersion.versionName,
                    OptiFineDownloadTask(optifineVersion)
                )
            )
            ZHTools.onBackPressed(requireActivity())
            true
        }

        currentTask?.apply { if (isCancelled) return }

        TaskExecutors.runInUIThread {
            val recyclerView = recyclerView
            runCatching {
                recyclerView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                recyclerView.adapter = adapter
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            recyclerView.scheduleLayoutAnimation()
        }
    }
}
