package com.sample.imagedownloader.ui.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<D> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items = arrayListOf<D>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val isEmpty: Boolean
        get() = items.isEmpty()

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItems(list: List<D>) {
        items.addAll(list)
        notifyDataSetChanged()
        notifyItemRangeInserted(items.size, list.size)
    }

    fun getItem(index: Int): D? {
        return items.getOrNull(index)
    }

    fun clear() {
        items.clear()
        notifyItemRangeRemoved(0, items.size)
    }

}

