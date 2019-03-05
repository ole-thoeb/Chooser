package chooser.com.example.eloem.chooser

import android.app.AlertDialog
import android.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageButton
import chooser.com.example.eloem.chooser.helperClasses.*
import chooser.com.example.eloem.chooser.util.*
import emil.beothy.widget.BetterEditText
import kotlinx.android.synthetic.main.actionbar_layout.*
import kotlinx.android.synthetic.main.fragment_add_order_chooser.*
import java.util.*

open class DefaultAddListActivity : AppCompatActivity() {
    
    private lateinit var modeOption: MenuItem
    private val chooserId by lazy {
        val c = intent.getIntExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, -1)
        if (c == -1) finish()
        c
    }
    private var currentType: String? = null
    private var currentFragment: AddChooserFragment? = null
    //val CURRENT_FRAGMENT_TAG = "fragmentTag"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentTheme)
    
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_add_list)
    
        //set up action bar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.actionbar_layout)
        }
    
        val type = intent.getStringExtra(CHOOSER_TYPE_EXTRA)
        switchFragment(type)
    }
    
    private fun getTypedFragmentWithId(type: String): AddChooserFragment{
        val fragment = when (type) {
            OrderChooser.PARS_TYPE -> AddOrderChooserFragment()
            PickChooser.PARS_TYPE -> AddPickChooserFragment()
            WeightedChooser.PARS_TYPE -> throw NotImplementedError()
            else -> throw UnknownFormatFlagsException("Unknown Flag for chooser type: $type")
        }
        fragment.arguments = Bundle().apply {
            putInt(CHOOSER_ID_EXTRA, chooserId)
        }
        return fragment
    }
    
    private fun switchFragment(type: String){
        if (currentType != type) {
            currentFragment?.saveChooser()
            getTypedFragmentWithId(type).let {
                currentFragment = it
                currentType = type
                fragmentManager
                        .beginTransaction()
                        .replace(android.R.id.content, it)
                        .commit()
            }
        }
    }
    
    private fun setIcon(type: String){
        modeOption.icon = when(type){
            OrderChooser.PARS_TYPE -> resources.getDrawable(R.drawable.ic_order, theme)
            PickChooser.PARS_TYPE -> resources.getDrawable(R.drawable.ic_pick, theme)
            WeightedChooser.PARS_TYPE -> resources.getDrawable(R.drawable.ic_weighted, theme)
            else -> throw UnknownFormatFlagsException("Unknown Flag for chooser type: $type")
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_list, menu)
        if (menu != null) {
            modeOption = menu.findItem(R.id.mode)
            currentType?.let { setIcon(it) }
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId){
        R.id.delete -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialogDeleteListMessage)
                    .setNegativeButton(R.string.dialogNegative) {_, _ ->
                        //nothing
                    }
                    .setPositiveButton(R.string.dialogPositive) { _, _ ->
                        deleteListEntry(this, chooserId)
    
                        NavUtils.navigateUpFromSameTask(this)
                    }
                    .show()
            true
        }
        R.id.mode -> {
            AlertDialog.Builder(this)
                    .setTitle(R.string.chooseMode)
                    .setItems(R.array.modeArray){ _, which ->
                        val newType = when(which){
                            0 -> OrderChooser.PARS_TYPE
                            1 -> PickChooser.PARS_TYPE
                            2 -> WeightedChooser.PARS_TYPE
                            else -> throw Error("Unknown option for mode: $which")
                        }
                        switchFragment(newType)
                        setIcon(newType)
                    }
                    .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    override fun onPause() {
        super.onPause()
        currentFragment?.saveChooser()
    }
    
    override fun onBackPressed() {
        //navigate always to main activity (parent activity)
        NavUtils.navigateUpFromSameTask(this)
    }
    
    //for child fragments to access edit text in actionbar
    var chooserTitle: String
        set(value) { actionBarText.setText(value) }
        get() = actionBarText.text.toString()
    
    data class ConverterChooserItem(val name: String, val orgPos: Int, val randomPos: Int)
    
    data class MutableChooserItem(var name: String, var randomPos: Int)
    
    /*class MyListAdapter(private val context: Context, var values: MutableList<MutableChooserItem>):
            RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        
        
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
        
        override fun getItemViewType(position: Int) =
                if (position < values.size) 0
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
    }*/
    
    abstract class AddChooserFragment: Fragment(){
        abstract fun saveChooser()
    }
    
    open class AddOrderChooserFragment: AddChooserFragment(){
        
        open val chooser: OrderChooser<ChooserItem> by lazy {
            val chooserId = arguments.getInt(CHOOSER_ID_EXTRA)
            getChooser(context, chooserId).toOrderChooser()
        }
        
        private val parentActivity: DefaultAddListActivity by lazy { activity as DefaultAddListActivity }
        
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            return inflater.inflate(R.layout.fragment_add_order_chooser, container, false)
        }
        
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            
            //set data to UI
            list.apply {
                val converterList = chooser.items.mapIndexed { index, item ->
                    ConverterChooserItem(item.name, item.originalPos, index)
                }.toMutableList()
                adapter = OrderAdapter(converterList.apply { sortBy { it.orgPos } }
                                .map { MutableChooserItem(it.name, it.randomPos) }
                                .toMutableList())
                layoutManager = LinearLayoutManager(context)
            }
    
            parentActivity.chooserTitle = chooser.title
        }
    
        override fun saveChooser() {
            Log.d(TAG, "on Pause called. Type ${chooser::class}")
            hideSoftKeyboard(context, view.findFocus())
        
            //update List object
            val cleanedItems = (list.adapter as OrderAdapter).values
                    .filter { it.name != "" }
                    .mapIndexed { index, item -> ConverterChooserItem(item.name, index, item.randomPos) }
                    .sortedBy { it.randomPos }
            val title = parentActivity.chooserTitle
            //when nothing was filled in -> discard list
            if (cleanedItems.isEmpty() && title == "") {
                deleteListEntry(context, chooser.id)
                return
            }
        
            chooser.items.apply {
                clear()
                addAll(cleanedItems.map { ChooserItem(it.name, it.orgPos) })
            }
            chooser.title = title
        
            //write/update data to database
            updateListEntryComplete(context, chooser)
        }
        
        class OrderAdapter(values: MutableList<MutableChooserItem>):
                EditListAdapter<MutableChooserItem>(values) {
            
            class EditViewHolder(layout: View): EditRowVH(layout){
                override val itemNameET: BetterEditText = layout.findViewById(R.id.itemName)
                override val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
            }
            
            class FootViewHolder(layout: View): RecyclerView.ViewHolder(layout){
                val root: ViewGroup = layout.findViewById(R.id.linLayout)
            }
            
            override fun writeEditContent(pos: Int, content: String) {
                values[pos].name = content
            }
    
            override fun readEditContent(pos: Int): String = values[pos].name
    
            override fun newItem(pos: Int, s: String): MutableChooserItem =
                    MutableChooserItem(s, values.size)
    
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
                    = when(viewType){
                VIEW_TYPE_EDIT_ROW -> EditViewHolder(
                        LayoutInflater
                                .from(context)
                                .inflate(R.layout.item_list_row, parent,false)
                )
                1 -> FootViewHolder(
                        LayoutInflater
                                .from(context)
                                .inflate(R.layout.new_item_row, parent,false)
                )
                else -> FootViewHolder(
                        LayoutInflater
                                .from(context)
                                .inflate(R.layout.new_item_row, parent,false)
                )
            }
    
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                if (holder.itemViewType == 1){
                    val realHolder = holder as FootViewHolder
                    with(realHolder){
                        root.setOnClickListener { addNewItem(values.size) }
                    }
                }
                else super.onBindViewHolder(holder, position)
            }
    
            override fun getItemCount(): Int = values.size + 1
    
            override fun getItemViewType(position: Int): Int = when(position){
                values.size -> 1
                else -> VIEW_TYPE_EDIT_ROW
            }
        }
    }
    
    class AddPickChooserFragment: AddOrderChooserFragment(){
        override val chooser: PickChooser<ChooserItem> by lazy {
            val chooserId = arguments.getInt(CHOOSER_ID_EXTRA)
            getChooser(context, chooserId).toPickChooser()
        }
    }
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
        const val CHOOSER_TYPE_EXTRA = "extraChooserType"
        
        private const val TAG = "DefaultAddListActivity"
    }
}
