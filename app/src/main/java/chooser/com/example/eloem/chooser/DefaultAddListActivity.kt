package chooser.com.example.eloem.chooser

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import chooser.com.example.eloem.chooser.helperClasses.ChooserItem
import chooser.com.example.eloem.chooser.helperClasses.OrderChooser
import chooser.com.example.eloem.chooser.util.*
import emil.beothy.widget.BetterEditText
import kotlinx.android.synthetic.main.actionbar_layout.*
import kotlinx.android.synthetic.main.activity_add_list.*

class DefaultAddListActivity<T: OrderChooser<ChooserItem>> : AppCompatActivity() {
    
    private lateinit var data: T
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentTheme)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_list)
        
        //set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.actionbar_layout)
        
        val chooserId = intent.getIntExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, -1)
        if (chooserId == -1) finish()
    
        val chooser = getChooser(this, chooserId)
        try {
            @Suppress("UNCHECKED_CAST")
            data = chooser as T
        } catch (e: TypeCastException) {
            finish()
        }
        
        //set data to UI
        list.apply {
            val converterList = data.items.mapIndexed { index, item ->
                ConverterChooserItem(item.name, item.originalPos, index)
            }.toMutableList()
            adapter = MyListAdapter(context,
                    converterList.apply { sortBy { it.orgPos } }
                            .map { MyListAdapter.MutableChooserItem(it.name, it.randomPos) }
                            .toMutableList())
            layoutManager = LinearLayoutManager(context)
        }
        
        actionBarText.setText(data.title)
    }
    
    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this, currentFocus)
        
        //update List object
        val cleanedItems = (list.adapter as MyListAdapter).values
                .filter { it.name != "" }
                .mapIndexed { index, item -> ConverterChooserItem(item.name, index, item.randomPos) }
                .sortedBy { it.randomPos }
        val title = actionBarText.text.toString()
        //when nothing was filled in -> safe nothing | discard list
        if (cleanedItems.isEmpty() && title == "") return
        
        data.items.apply {
            clear()
            addAll(cleanedItems.map { ChooserItem(it.name, it.orgPos) })
        }
        data.title = title
        
        //write/update data to database
        updateListEntryComplete(this, data)
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_list, menu)
        return true
    }
    /*
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.mode)?.title = when (data.mode){
            ListObj.MODE_RANDOM_ORDER -> resources.getString(R.string.modeRandomOrder)
            ListObj.MODE_SINGLE_PICK -> resources.getString(R.string.modeRandomPick)
            
            else -> resources.getString(R.string.error)
        }
        return true
    }*/
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId){
        R.id.restart -> {
            /*val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialogRestartListMessage)
                    .setNegativeButton(R.string.dialogNegative) {_, _ ->
                        //nothing
                    }
                    .setPositiveButton(R.string.dialogPositive) { _, _ ->
                        data.restart()
                        
                        updateListEntryComplete(this, data)
                    }
                    .show()*/
            true
        }
        R.id.delete -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialogDeleteListMessage)
                    .setNegativeButton(R.string.dialogNegative) {_, _ ->
                        //nothing
                    }
                    .setPositiveButton(R.string.dialogPositive) { _, _ ->
                        deleteListEntry(this, data.id)
    
                        NavUtils.navigateUpFromSameTask(this)
                    }
                    .show()
            true
        }
        R.id.mode -> {
            /*AlertDialog.Builder(this)
                    .setTitle(R.string.chooseMode)
                    .setItems(R.array.modeArray){ _, which ->
                        data.mode = when(which){
                            0 -> ListObj.MODE_SINGLE_PICK
                            1 -> ListObj.MODE_RANDOM_ORDER
                            
                            else -> ListObj.MODE_RANDOM_ORDER
                        }
                    }
                    .show()*/
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        //navigate always to main activity (parent activity)
        NavUtils.navigateUpFromSameTask(this)
    }
    
    data class ConverterChooserItem(val name: String, val orgPos: Int, val randomPos: Int)
    
    class MyListAdapter(private val context: Context, var values: MutableList<MutableChooserItem>):
            RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        
        data class MutableChooserItem(var name: String, var randomPos: Int)
        
        private lateinit var mRecyclerView: RecyclerView
        
        class ViewHolder1(layout: View): RecyclerView.ViewHolder(layout){
            val itemNameET: BetterEditText = layout.findViewById(R.id.itemName)
            val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
        }
        
        class ViewHolder2(layout: View): RecyclerView.ViewHolder(layout){
            val linLayout: LinearLayout = layout.findViewById(R.id.linLayout)
        }
        
        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            mRecyclerView = recyclerView
            super.onAttachedToRecyclerView(recyclerView)
        }
        
        override fun getItemCount(): Int {
            return values.size + 1
        }
        
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType){
                0 -> {
                    val realHolder = holder as ViewHolder1
                    with(realHolder.itemNameET) {
                        setText(values[position].name, TextView.BufferType.EDITABLE)
                        onTextChangeListener = { charSequence, betterEditText ->
                            val pos = realHolder.adapterPosition
                            if (pos < values.size) {
                                values[pos].name = charSequence.toString()
                            }
                        }
                        onLineBreakListener = { subStrings, view ->
                            val pos = realHolder.adapterPosition
                            if (subStrings.size == 1) addNewItem(pos + 1, subStrings.first())
                            else {
                                subStrings.forEachIndexed { index, s ->
                                    values.add(pos + index + 1, MutableChooserItem(s, values.size))
                                }
                                val lastPos = pos + subStrings.size + 1
                                notifyItemRangeInserted(pos + 1, subStrings.size)
                                mRecyclerView.scrollToPosition(lastPos)
                            }
                        }
                        onDelAtStartListener = { restString, view ->
                            val pos = realHolder.adapterPosition
                            removeItem(pos, restString)
                        }
                        onFocusChangeListener = View.OnFocusChangeListener { tv, hasFocus ->
                            with(realHolder.deleteButton){
                                if (hasFocus){
                                    setImageDrawable(resources.getDrawable(R.drawable.ic_clear, context.theme))
                                    isClickable = true
                                }else {
                                    setImageDrawable(resources.getDrawable(R.drawable.transparent, context.theme))
                                    isClickable = false
                                }
                            }
                        }
                        
                        //set Focus to newly added textViews and show keyboard
                        focusAndShowKeyboard()
                        setSelection(values[position].name.length)
                    }
    
                    realHolder.deleteButton.setOnClickListener { removeItem(realHolder.adapterPosition) }
                }
                1 -> {
                    val realHolder = holder as ViewHolder2
                    realHolder.linLayout.setOnClickListener { addNewItem(values.size) }
                }
            }
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType){
                0 ->{
                    val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_list_row, parent,false)
                    ViewHolder1(layout)
                }
                else ->{
                    val layout = LayoutInflater.from(parent.context).inflate(R.layout.new_item_row, parent,false)
                    ViewHolder2(layout)
                }
            }
        }
        
        override fun getItemViewType(position: Int) = if (position < values.size) 0
            else 1
        
        private fun addNewItem(pos: Int, startString: String = ""){
            values.add(pos, MutableChooserItem(startString, values.size))
            notifyItemInserted(pos)
            mRecyclerView.scrollToPosition(pos)
        }
        
        private fun removeItem(pos: Int, remainingText: String = ""){
            val gvH = mRecyclerView.findViewHolderForAdapterPosition(pos) ?: return
            val vH =  gvH as ViewHolder1
            if (pos > 0){
                val posBefore = pos -1
                val beforeVH = mRecyclerView.findViewHolderForAdapterPosition(posBefore) as ViewHolder1
                //if deleted textView had focus switch it to the one before
                if (beforeVH.itemNameET.text.isNotEmpty() && remainingText != "")
                    beforeVH.itemNameET.append(" $remainingText")
                else
                    beforeVH.itemNameET.append(remainingText)
                if (vH.itemNameET.hasFocus()){
                    mRecyclerView.scrollToPosition(posBefore)
                    beforeVH.itemNameET.requestFocus()
                    beforeVH.itemNameET.setSelection(beforeVH.itemNameET.text.length - remainingText.length)
                }
            }else {
                //if it was the last text view don't set focus and hide keyboard
                hideSoftKeyboard(context, vH.itemNameET)
            }
            
            values.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
        
        private const val TAG = "DefaultAddListActivity"
    }
}