package com.example.eloem.dartCounter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class ContextAdapter<VH: RecyclerView.ViewHolder>: RecyclerView.Adapter<VH>() {
    
    lateinit var recyclerView: RecyclerView
    val context: Context by lazy { recyclerView.context }
    
    val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }
    
    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        super.onAttachedToRecyclerView(rv)
        recyclerView = rv
    }
    
    fun inflate(@LayoutRes resource: Int, parent: ViewGroup): View =
            layoutInflater.inflate(resource, parent, false)
}