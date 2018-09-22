package emil.beothy.adapter

import android.support.v7.widget.RecyclerView

abstract class AddListItemsAdapter <T> (var values: MutableList<T>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    lateinit var mRecyclerView: RecyclerView
    
    abstract fun defaultItem(): T
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
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