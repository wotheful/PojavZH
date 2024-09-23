package com.movtery.pojavzh.feature.mod.modloader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.utils.anim.ViewAnimUtils
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.modloaders.FabricVersion
import net.kdt.pojavlaunch.modloaders.OptiFineUtils.OptiFineVersion
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener

open class BaseModVersionListAdapter(private val mData: List<*>?) :
    RecyclerView.Adapter<BaseModVersionListAdapter.ViewHolder>(), TaskCountListener {
    private var mTasksRunning = false
    private var onItemClickListener: OnItemClickListener? = null
    private var iconDrawable = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(mData!![position])
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    open fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }

    fun setIconDrawable(iconDrawable: Int) {
        this.iconDrawable = iconDrawable
    }

    fun interface OnItemClickListener {
        fun onClick(version: Any?)
    }

    inner class ViewHolder(private val mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val context: Context
        private val versionName: TextView

        init {
            val icon = itemView.findViewById<ImageView>(R.id.zh_file_image)
            context = itemView.context
            versionName = itemView.findViewById(R.id.zh_file_name)

            if (iconDrawable != 0) icon.setImageResource(iconDrawable)
            itemView.findViewById<View>(R.id.zh_file_check).visibility = View.GONE
        }

        fun setView(version: Any?) {
            when (version) {
                is OptiFineVersion -> versionName.text = version.versionName
                is FabricVersion -> versionName.text = version.version
                is String -> versionName.text = version
            }
            mainView.setOnClickListener { _: View? ->
                if (mTasksRunning) {
                    ViewAnimUtils.setViewAnim(mainView, Animations.Shake)
                    Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                onItemClickListener?.onClick(version)
            }
        }
    }

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }
}
