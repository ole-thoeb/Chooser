package com.example.eloem.dartCounter.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BottomSpacingAdapter<VH: RecyclerView.ViewHolder>(private val delegate: RecyclerView.Adapter<VH>,
                                                        private val spacing: Int,
                                                        private val spanCount: Int = 1):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    class PaddingVH(layout: View): RecyclerView.ViewHolder(layout)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == PADDING_VIEW_TYPE) {
            val view = View(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(0, spacing)
            PaddingVH(view)
        } else {
            delegate.onCreateViewHolder(parent, viewType)
        }
    }
    
    override fun getItemCount(): Int = delegate.itemCount + spanCount
    
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != PADDING_VIEW_TYPE) {
            delegate.onBindViewHolder(holder as VH, position)
        }
    }
    
    override fun getItemId(position: Int): Long {
        return delegate.getItemId(position)
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (position >= delegate.itemCount) PADDING_VIEW_TYPE
        else delegate.getItemViewType(position)
    }
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        delegate.onAttachedToRecyclerView(recyclerView)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (holder.itemViewType != PADDING_VIEW_TYPE) {
            delegate.onBindViewHolder(holder as VH, position, payloads)
        }
    }
    
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        delegate.onDetachedFromRecyclerView(recyclerView)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return if (holder.itemViewType != PADDING_VIEW_TYPE) {
            delegate.onFailedToRecycleView(holder as VH)
        } else {
            false
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType != PADDING_VIEW_TYPE) {
            delegate.onViewAttachedToWindow(holder as VH)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType != PADDING_VIEW_TYPE) {
            delegate.onViewDetachedFromWindow(holder as VH)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType != PADDING_VIEW_TYPE) {
            delegate.onViewRecycled(holder as VH)
        }
    }
    
    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        delegate.registerAdapterDataObserver(observer)
    }
    
    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
        delegate.setHasStableIds(hasStableIds)
    }
    
    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        delegate.unregisterAdapterDataObserver(observer)
    }
    
    override fun equals(other: Any?): Boolean =
            other is BottomSpacingAdapter<*> &&
            other.delegate == delegate &&
            other.spacing == spacing
    
    override fun hashCode(): Int {
        return delegate.hashCode() + spacing.hashCode()
    }
    
    companion object {
        private const val PADDING_VIEW_TYPE = 94863971
    }
}