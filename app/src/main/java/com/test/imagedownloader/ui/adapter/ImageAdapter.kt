package com.test.imagedownloader.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.test.imagedownloader.R
import com.test.imagedownloader.manager.rest.DataObject
import com.test.library.CircleTransformation
import com.test.library.ImDn
import com.test.library.RoundTransformation
import kotlinx.android.synthetic.main.list_item.view.*

class ImageAdapter : BaseAdapter<DataObject>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder = holder as ViewHolder
        val item = getItem(position)
        if (item != null) {
            viewHolder?.bind(item)
        }
    }

    override fun getItemCount(): Int = when(mList) {
        null -> 0
        else -> mList!!.count()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        fun bind(item: DataObject) {
            ImDn.init(mView.mItemImage.context)
                    .holder(R.drawable.loading_progress_anim)
                    .error(R.drawable.ic_error_black_24dp)
                    .transform(CircleTransformation())
                    .path(item.avatarUrl!!)
                    .into(mView.mItemImage)

            mView.mItemLoginValue.setText(item.login)
            mView.mItemIdValue.setText(item.id)
        }
    }

}