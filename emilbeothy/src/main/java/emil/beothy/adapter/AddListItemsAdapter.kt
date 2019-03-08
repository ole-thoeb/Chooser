package emil.beothy.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class AddListItemsAdapter <T> (var values: MutableList<T>): androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(){
    lateinit var mRecyclerView: androidx.recyclerview.widget.RecyclerView
    
    abstract fun defaultItem(): T
    
    override fun onAttachedToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        mRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    open fun addNewItem(pos: Int){
        values.add(pos, defaultItem())
        this.notifyItemInserted(pos)
        mRecyclerView.scrollToPosition(pos)
    }
    
    open fun removeItem(pos: Int){
        values.removeAt(pos)
        this.notifyItemRemoved(pos)
    }
}