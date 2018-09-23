package chooser.com.example.eloem.chooser

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import chooser.com.example.eloem.chooser.helperClasses.ListObj
import chooser.com.example.eloem.chooser.util.*
import emil.beothy.widget.BetterEditText
import kotlinx.android.synthetic.main.actionbar_layout.*
import kotlinx.android.synthetic.main.activity_add_list.*

class AddListActivity : AppCompatActivity() {
    private var isNewList:Boolean = true
    private lateinit var data: ListObj
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (readCurrentThem(this)){
            R.style.DarkAppTheme
        }else{
            R.style.LightAppTheme
        })
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_list)
        
        //set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.actionbar_layout)
        
        //TODO: get data from Intent (could crash if no extra)
        data = intent.getParcelableExtra(LIST_OBJ_EXTRA) as ListObj
        isNewList = intent.getBooleanExtra(IS_NEW_LIST_FLAG, true)
        
        //set data to UI
        list.apply {
            adapter = MyListAdapter(context, data.items.toMutableList())
            layoutManager = LinearLayoutManager(context)
        }
        
        actionBarText.setText(data.title)
    }
    
    override fun onPause() {
        super.onPause()
        hideSoftKeyboard(this, currentFocus)
        
        //update List object
        val cleanedItems = (list.adapter as MyListAdapter).values.filter { it.name != "" }.toTypedArray()
        val title = actionBarText.text.toString()
        //when nothing was filled in -> safe nothing | discard list
        if (cleanedItems.isEmpty() && title == "") return
        
        data.items = cleanedItems
        data.title = title
        
        //write/update data to database
        if (isNewList)insertListEntry(this, data)
        else updateListEntryComplete(this, data)
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_list, menu)
        return true
    }
    
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu?.findItem(R.id.mode)?.title = when (data.mode){
            ListObj.MODE_RANDOM_ORDER -> resources.getString(R.string.modeRandomOrder)
            ListObj.MODE_SINGLE_PICK -> resources.getString(R.string.modeRandomPick)
            
            else -> resources.getString(R.string.error)
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId){
        R.id.restart -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialogRestartListMessage)
                    .setNegativeButton(R.string.dialogNegative) {_, _ ->
                        //nothing
                    }
                    .setPositiveButton(R.string.dialogPositive) { _, _ ->
                        data.restart()
    
                        if (isNewList) insertListEntry(this, data)
                        else updateListEntryComplete(this, data)
                    }
                    .show()
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
            AlertDialog.Builder(this)
                    .setTitle(R.string.chooseMode)
                    .setItems(R.array.modeArray){ _, which ->
                        data.mode = when(which){
                            0 -> ListObj.MODE_SINGLE_PICK
                            1 -> ListObj.MODE_RANDOM_ORDER
                            
                            else -> ListObj.MODE_RANDOM_ORDER
                        }
                    }
                    .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    override fun onBackPressed() {
        //navigate always to main activity (parent activity)
        NavUtils.navigateUpFromSameTask(this)
    }
    
    class MyListAdapter(private val context: Context, var values: MutableList<ListObj.Item>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private lateinit var mRecyclerView: RecyclerView
        
        class ViewHolder1(layout: View): RecyclerView.ViewHolder(layout){
            val itemName: BetterEditText = layout.findViewById(R.id.playerName)
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
                0 ->{
                    val realHolder = holder as ViewHolder1
                    with(realHolder.itemName) {
                        setText(values[position].name, TextView.BufferType.EDITABLE)
                        setTextChangeListener = { charSequence, betterEditText ->
                            val pos = realHolder.adapterPosition
                            if (pos < values.size) {
                                values[pos].name = charSequence.toString()
                            }
                        }
                        setEnterKeyListener = {afterEnterString ->
                            addNewItem(realHolder.adapterPosition +  1, afterEnterString)
                        }
                        setOnKeyListener { view, i, keyEvent ->
                            //handel special actions on backspace
                            if (keyEvent.action == KeyEvent.ACTION_UP) {
                                when (keyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DEL -> {
                                        removeItem(realHolder.adapterPosition)
                                        return@setOnKeyListener true
                                    }
                                }
                            }
                            false
                        }
                        //set Focus to newly added textViews
                        requestFocus()
                    }
                    //show keyboard and not hide it if it is visible
                    val ipm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    ipm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)
    
                    realHolder.deleteButton.setOnClickListener{ removeItem(realHolder.adapterPosition) }
                }
                
                1 ->{
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
            values.add(pos, ListObj.Item(startString, newItemId(context)))
            this.notifyItemInserted(pos)
            mRecyclerView.scrollToPosition(pos)
        }
        
        private fun removeItem(pos: Int){
            val gvH = mRecyclerView.findViewHolderForAdapterPosition(pos)?: return
            val vH =  gvH as ViewHolder1
            if (vH.itemName.hasFocus()){
                if (pos > 0){
                    //if deleted textView had focus switch it to the one before
                    val vH2 = mRecyclerView.findViewHolderForAdapterPosition(pos -1) as ViewHolder1
                    vH2.itemName.requestFocus()
                }else{
                    //if it was the last text view don't set focus and hide keyboard
                    hideSoftKeyboard(context, vH.itemName)
                }
            }
            
            values.removeAt(pos)
            this.notifyItemRemoved(pos)
        }
    }
    
    companion object {
        const val LIST_OBJ_EXTRA = "extraListObj"
        const val IS_NEW_LIST_FLAG = "flagNewList"
    }
}
