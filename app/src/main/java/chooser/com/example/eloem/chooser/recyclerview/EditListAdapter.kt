package chooser.com.example.eloem.chooser.recyclerview

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import chooser.com.example.eloem.chooser.util.focusAndShowKeyboard
import chooser.com.example.eloem.chooser.util.hideSoftKeyboard
import emil.beothy.widget.BetterEditText

abstract class EditListAdapter<T>(var values: MutableList<T>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    
    lateinit var recyclerView: RecyclerView
    val context: Context by lazy { recyclerView.context }
    
    private var lastInsertedPos: Int? = null
    
    /**
     * called after item was inserted to underling list.
     * parameters are added item and the position it was added*/
    var onAddItemListener: ((T, Int) -> Unit)? = null
    
    /**
     * called after item was removed to underling list.
     * parameters are removed item and the position it was removed*/
    var onRemoveItemListener: ((T, Int) -> Unit)? = null
    
    
    open var valuesOffsetStart: Int = 0
    
    abstract class EditRowVH(layout: View): RecyclerView.ViewHolder(layout){
        abstract val itemNameET: BetterEditText
        abstract val deleteButton: ImageButton
    }
    
    override fun onAttachedToRecyclerView(rV: RecyclerView) {
        recyclerView = rV
        super.onAttachedToRecyclerView(recyclerView)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_EDIT_ROW) {
            val realHolder = holder as EditRowVH
            with(realHolder.itemNameET) {
                setText(readEditContent(position - valuesOffsetStart), TextView.BufferType.EDITABLE)
                onTextChangeListener = { charSequence, betterEditText ->
                    val valuePos = realHolder.adapterPosition - valuesOffsetStart
                    if (valuePos < values.size) {
                        writeEditContent(valuePos, charSequence.toString())
                    }
                }
                onLineBreakListener = { subStrings, view ->
                    val pos = realHolder.adapterPosition
                    if (subStrings.size == 1) addNewItem(pos + 1 - valuesOffsetStart, subStrings.first())
                    else {
                        subStrings.forEachIndexed { index, s ->
                            val insertPos = pos + index + 1
                            values.add(insertPos, newItem(insertPos, s))
                        }
                        val lastPos = pos + subStrings.size + 1
                        notifyItemRangeInserted(pos + 1, subStrings.size)
                        recyclerView.scrollToPosition(lastPos)
                    }
                }
                onDelAtStartListener = { restString, view ->
                    val pos = realHolder.adapterPosition
                    removeItem(pos - valuesOffsetStart, restString)
                }
                onFocusChangeListener = View.OnFocusChangeListener { tv, hasFocus ->
                    with(realHolder.deleteButton) {
                        if (hasFocus) {
                            visibility = View.VISIBLE
                            isClickable = true
                        } else {
                            visibility = View.INVISIBLE
                            isClickable = false
                        }
                    }
                }
                
                //set Focus to newly added textViews and show keyboard
                if (lastInsertedPos == position) {
                    Log.d(TAG, "requesting focus")
                    focusAndShowKeyboard()
                    setSelection(text.length)
                }
                lastInsertedPos = null
            }
            
            realHolder.deleteButton.setOnClickListener {
                removeItem(realHolder.adapterPosition - valuesOffsetStart)
            }
        }
    }
    
    abstract fun writeEditContent(pos: Int, content: String)
    
    abstract fun readEditContent(pos: Int): String
    
    abstract fun newItem(pos: Int, s: String = ""): T
    
    //pos is position in values
    fun addNewItem(pos: Int, startString: String = ""){
        lastInsertedPos = pos + valuesOffsetStart
        val newItem = newItem(pos, startString)
        values.add(pos, newItem)
        notifyItemInserted(pos + valuesOffsetStart)
        onAddItemListener?.invoke(newItem, pos)
        recyclerView.scrollToPosition(pos + valuesOffsetStart)
    }
    
    //pos is position in values
    fun removeItem(pos: Int, remainingText: String = ""){
        val gvH = recyclerView.findViewHolderForAdapterPosition(pos + valuesOffsetStart) ?: return
        val vH =  gvH as EditRowVH
        if (pos > 0){
            val posBefore = pos -1 + valuesOffsetStart
            val beforeVH = recyclerView.findViewHolderForAdapterPosition(posBefore) as EditRowVH
            //if deleted textView had focus switch it to the one before
            if (beforeVH.itemNameET.text.isNotEmpty() && remainingText != "")
                beforeVH.itemNameET.append(" $remainingText")
            else
                beforeVH.itemNameET.append(remainingText)
            if (vH.itemNameET.hasFocus()){
                recyclerView.scrollToPosition(posBefore)
                beforeVH.itemNameET.requestFocus()
                beforeVH.itemNameET.setSelection(beforeVH.itemNameET.text.length - remainingText.length)
            }
        }else {
            //if it was the last text view don't set focus and hide keyboard
            hideSoftKeyboard(context, vH.itemNameET)
        }
        val removedItem = values[pos]
        values.removeAt(pos)
        notifyItemRemoved(pos + valuesOffsetStart)
        onRemoveItemListener?.invoke(removedItem, pos)
    }
    
    companion object {
        const val VIEW_TYPE_EDIT_ROW = 546789
        
        private const val TAG = "EditListAdapter"
    }
}