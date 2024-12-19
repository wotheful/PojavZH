package com.movtery.zalithlauncher.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.event.value.DownloadRecyclerEnableEvent
import com.movtery.zalithlauncher.feature.download.InfoViewModel
import com.movtery.zalithlauncher.feature.download.ScreenshotAdapter
import com.movtery.zalithlauncher.feature.download.VersionAdapter
import com.movtery.zalithlauncher.feature.download.enums.ModLoader
import com.movtery.zalithlauncher.feature.download.item.InfoItem
import com.movtery.zalithlauncher.feature.download.item.ModVersionItem
import com.movtery.zalithlauncher.feature.download.item.ScreenshotItem
import com.movtery.zalithlauncher.feature.download.item.VersionItem
import com.movtery.zalithlauncher.feature.download.platform.AbstractPlatformHelper
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.task.Task
import com.movtery.zalithlauncher.task.TaskExecutors
import com.movtery.zalithlauncher.ui.subassembly.modlist.ModListAdapter
import com.movtery.zalithlauncher.ui.subassembly.modlist.ModListFragment
import com.movtery.zalithlauncher.ui.subassembly.modlist.ModListItemBean
import com.movtery.zalithlauncher.ui.view.AnimButton
import com.movtery.zalithlauncher.utils.MCVersionRegex.Companion.RELEASE_REGEX
import com.movtery.zalithlauncher.utils.ZHTools
import com.movtery.zalithlauncher.utils.stringutils.StringUtilsKt
import net.kdt.pojavlaunch.Tools
import org.greenrobot.eventbus.EventBus
import org.jackhuang.hmcl.ui.versions.ModTranslations
import org.jackhuang.hmcl.util.versioning.VersionNumber
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadModFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadModFragment"
    }

    private lateinit var platformHelper: AbstractPlatformHelper
    private lateinit var mInfoItem: InfoItem
    private var linkGetSubmit: Future<*>? = null

    override fun init() {
        parseViewModel()
        linkGetSubmit = TaskExecutors.getDefault().submit {
            runCatching {
                val webUrl = platformHelper.getWebUrl(mInfoItem)
                fragmentActivity?.runOnUiThread { setLink(webUrl) }
            }.getOrElse { e ->
                Logging.e("DownloadModFragment", "Failed to retrieve the website link, ${Tools.printToString(e)}")
            }
        }
        super.init()
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    override fun onDestroy() {
        EventBus.getDefault().post(DownloadRecyclerEnableEvent(true))
        linkGetSubmit?.apply {
            if (!isCancelled && !isDone) cancel(true)
        }
        super.onDestroy()
    }

    private fun refresh(force: Boolean): Future<*> {
        return TaskExecutors.getDefault().submit {
            runCatching {
                TaskExecutors.runInUIThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val versions = platformHelper.getVersions(mInfoItem, force)
                processDetails(versions)
            }.getOrElse { e ->
                TaskExecutors.runInUIThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadModFragment", Tools.printToString(e))
            }
        }
    }

    private fun processDetails(versions: List<VersionItem>?) {
        val pattern = RELEASE_REGEX

        val releaseCheckBoxChecked = releaseCheckBox.isChecked
        //在Key内同时记录MC版本，与Mod加载器信息，以便之后细分Mod加载器
        val mModVersionsByMinecraftVersion: MutableMap<Pair<String, ModLoader?>, MutableList<VersionItem>> = HashMap()

        versions?.forEach(Consumer { versionItem ->
            currentTask?.apply { if (isCancelled) return@Consumer }

            for (mcVersion in versionItem.mcVersions) {
                currentTask?.apply { if (isCancelled) return@Consumer }

                if (releaseCheckBoxChecked) {
                    val matcher = pattern.matcher(mcVersion)
                    if (!matcher.matches()) {
                        //如果不是正式版本，将继续检测下一项
                        continue
                    }
                }

                if (versionItem is ModVersionItem) {
                    val modloaders = versionItem.modloaders
                    if (modloaders.isNotEmpty()) {
                        modloaders.forEach {
                            addIfAbsent(mModVersionsByMinecraftVersion, Pair(mcVersion, it), versionItem)
                        }
                        //当这个版本是一个 ModVersionItem 的时候，则检查其Mod加载器是否不为空，如果不为空，则将版本支持的Mod加载器，放到不同的Mod加载器列表中
                        //这样会让用户更容易找到匹配自己需要的Mod加载器的版本
                        continue //已经分类完毕，没有必要再将这个版本加入进普通的版本列表中了
                    }
                }
                addIfAbsent(mModVersionsByMinecraftVersion, Pair(mcVersion, null), versionItem)
            }
        })

        currentTask?.apply { if (isCancelled) return }

        val mData: MutableList<ModListItemBean> = ArrayList()
        mModVersionsByMinecraftVersion.entries
            .sortedWith { entry1, entry2 ->
                -VersionNumber.compare(entry1.key.first, entry2.key.first)
            }
            .forEach { entry: Map.Entry<Pair<String, ModLoader?>, List<VersionItem>> ->
                currentTask?.apply { if (isCancelled) return }

                mData.add(
                    ModListItemBean(
                        entry.key.first,
                        entry.key.second,
                        VersionAdapter(this, mInfoItem, platformHelper, entry.value)
                    )
                )
            }

        currentTask?.apply { if (isCancelled) return }

        Task.runTask(TaskExecutors.getAndroidUI()) {
            val modVersionView = recyclerView
            runCatching {
                var mModAdapter = modVersionView.adapter as ModListAdapter?
                mModAdapter ?: run {
                    mModAdapter = ModListAdapter(this, mData)
                    modVersionView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                    modVersionView.adapter = mModAdapter
                    return@runCatching
                }
                mModAdapter?.updateData(mData)
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            modVersionView.scheduleLayoutAnimation()
        }.execute()
    }

    @SuppressLint("CheckResult")
    private fun parseViewModel() {
        val viewModel = ViewModelProvider(fragmentActivity!!)[InfoViewModel::class.java]
        platformHelper = viewModel.platformHelper
        mInfoItem = viewModel.infoItem

        mInfoItem.apply {
            val type = ModTranslations.getTranslationsByRepositoryType(classify)
            val mod = type.getModByCurseForgeId(slug)

            setTitleText(
                if (ZHTools.areaChecks("zh")) {
                    mod?.displayName ?: title
                } else title
            )
            setDescription(description)
            mod?.let {
                setMCMod(
                    StringUtilsKt.getNonEmptyOrBlank(type.getMcmodUrl(it))
                )
            }
            loadScreenshots()

            iconUrl?.apply {
                Glide.with(fragmentActivity!!).load(this).apply {
                    if (!AllSettings.resourceImageCache.getValue()) diskCacheStrategy(DiskCacheStrategy.NONE)
                }.into(getIconView())
            }
        }
    }

    private fun loadScreenshots() {
        val progressBar = createProgressView(fragmentActivity!!)
        addMoreView(progressBar)

        Task.runTask {
            platformHelper.getScreenshots(mInfoItem.projectId)
        }.ended(TaskExecutors.getAndroidUI()) { screenshotItems ->
            screenshotItems?.let addButton@{ items ->
                if (items.isEmpty()) return@addButton
                fragmentActivity?.let { activity ->
                    //添加一个按钮，通过点击这个按钮来加载屏幕截图数据
                    addMoreView(AnimButton(activity).apply {
                        layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        setText(R.string.download_info_load_screenshot)
                        setOnClickListener {
                            setScreenshotView(items)
                            AnimPlayer.play().apply(AnimPlayer.Entry(this, Animations.FadeOut))
                                .setOnEnd { removeMoreView(this) }
                                .start()
                        }
                    })
                }
            }
            removeMoreView(progressBar)
        }.onThrowable { e ->
            Logging.e(
                "DownloadModFragment",
                "Unable to load screenshots, ${Tools.printToString(e)}"
            )
        }.execute()
    }

    @SuppressLint("CheckResult")
    private fun setScreenshotView(screenshotItems: List<ScreenshotItem>) {
        fragmentActivity?.let { activity ->
            val recyclerView = RecyclerView(activity).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                layoutManager = LinearLayoutManager(activity)
                adapter = ScreenshotAdapter(screenshotItems)
            }

            addMoreView(recyclerView)
        }
    }

    private fun createProgressView(context: Context): ProgressBar {
        return ProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }
    }
}
