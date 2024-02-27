package com.sample.imagedownloader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sample.imagedownloader.R
import com.sample.imagedownloader.manager.DataObject
import com.sample.library.CircleTransformation
import com.sample.library.ImDn

class ImageAdapter : BaseAdapter<DataObject>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).let { holder ->
            getItem(position)?.let { item ->
                holder.bind(item)
            }
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        fun bind(item: DataObject) {
            val itemImage = mView.findViewById<ImageView>(R.id.itemImage)
            val loginValueText = mView.findViewById<TextView>(R.id.itemLoginValue)
            val idValueText = mView.findViewById<TextView>(R.id.itemIdValue)

            ImDn.init(itemImage.context)
                    .holder(R.drawable.loading_progress_anim)
                    .error(R.drawable.ic_error_black_24dp)
                    .transform(CircleTransformation())
                    .path(item.avatar_url ?: throw IllegalArgumentException("Bad json format"))
                    .into(itemImage)
            loginValueText.text = item.login
            idValueText.text = item.id
        }
    }

}