package com.example.eloem.dartCounter.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewEmptySupport : RecyclerView {
    var emptyView: View? = null
        set(value) {
            field = value
            checkIfEmpty()
        }
    
    var emptyThreshold: Int = 0
    
    private val observer = object : AdapterDataObserver() {
        override fun onChanged() {
            checkIfEmpty()
        }
        
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
        
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }
    
    constructor(context: Context) : super(context)
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    
    private fun checkIfEmpty() {
        emptyView?.let {
            val cAdapter = adapter
            if (cAdapter != null) {
                val emptyViewVisible = cAdapter.itemCount <= emptyThreshold
                it.visibility = if (emptyViewVisible) VISIBLE else GONE
                visibility = if (emptyViewVisible) GONE else VISIBLE
            }
        }
    }
    
    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        
        checkIfEmpty()
    }
}