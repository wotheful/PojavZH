package com.movtery.zalithlauncher.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.databinding.FragmentVersionBinding
import com.movtery.zalithlauncher.ui.subassembly.versionlist.VersionSelectedListener
import com.movtery.zalithlauncher.ui.subassembly.versionlist.VersionType
import com.movtery.zalithlauncher.utils.ZHTools
import net.kdt.pojavlaunch.Tools

class VersionSelectorFragment : FragmentWithAnim(R.layout.fragment_version) {
    companion object {
        const val TAG: String = "FileSelectorFragment"
    }

    private lateinit var binding: FragmentVersionBinding
    private var release: TabLayout.Tab? = null
    private var snapshot: TabLayout.Tab? = null
    private var beta: TabLayout.Tab? = null
    private var alpha: TabLayout.Tab? = null
    private var versionType: VersionType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVersionBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindTab()

        binding.apply {
            refresh(versionTab.getTabAt(versionTab.selectedTabPosition))

            versionTab.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    refresh(tab)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })

            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

            version.setVersionSelectedListener(object : VersionSelectedListener() {
                override fun onVersionSelected(version: String?) {
                    if (version == null) {
                        Tools.backToMainMenu(requireActivity())
                    } else {
                        val bundle = Bundle()
                        bundle.putString(InstallGameFragment.BUNDLE_MC_VERSION, version)
                        ZHTools.swapFragmentWithAnim(this@VersionSelectorFragment, InstallGameFragment::class.java, InstallGameFragment.TAG, bundle)
                    }
                }
            })
        }
    }

    private fun refresh(tab: TabLayout.Tab?) {
        binding.apply {
            setVersionType(tab)
            version.setVersionType(versionType)
        }
    }

    private fun setVersionType(tab: TabLayout.Tab?) {
        versionType = when (tab) {
            release -> VersionType.RELEASE
            snapshot -> VersionType.SNAPSHOT
            beta -> VersionType.BETA
            alpha -> VersionType.ALPHA
            else -> VersionType.RELEASE
        }
    }

    private fun bindTab() {
        binding.apply {
            release = versionTab.newTab().setText(getString(R.string.generic_release))
            snapshot = versionTab.newTab().setText(getString(R.string.version_snapshot))
            beta = versionTab.newTab().setText(getString(R.string.version_beta))
            alpha = versionTab.newTab().setText(getString(R.string.version_alpha))

            versionTab.addTab(release!!)
            versionTab.addTab(snapshot!!)
            versionTab.addTab(beta!!)
            versionTab.addTab(alpha!!)

            versionTab.selectTab(release)
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.versionLayout, Animations.BounceInDown))
            .apply(AnimPlayer.Entry(binding.operateLayout, Animations.BounceInLeft))
            .apply(AnimPlayer.Entry(binding.returnButton, Animations.FadeInLeft))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.versionLayout, Animations.FadeOutUp))
            .apply(AnimPlayer.Entry(binding.operateLayout, Animations.FadeOutRight))
    }
}
