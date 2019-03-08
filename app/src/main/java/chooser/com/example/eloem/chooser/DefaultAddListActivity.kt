package chooser.com.example.eloem.chooser

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.app.NavUtils
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chooser.com.example.eloem.chooser.helperClasses.*
import chooser.com.example.eloem.chooser.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import emil.beothy.widget.BetterEditText
import kotlinx.android.synthetic.main.actionbar_layout.*
import kotlinx.android.synthetic.main.fragment_add_order_chooser.*
import kotlinx.android.synthetic.main.weight_dialog.view.*
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
            WeightedChooser.PARS_TYPE -> AddWeightedChooserFragment()
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
                supportFragmentManager
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
            showDeleteDialog(this) {
                deleteListEntry(this, chooserId)
    
                NavUtils.navigateUpFromSameTask(this)
            }
            true
        }
        R.id.mode -> {
            MaterialAlertDialogBuilder(this)
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
        /*currentFragment?.let {
            it.saveChooser()
            Toast.makeText(this, R.string.messageSafedChooser, Toast.LENGTH_SHORT).show()
        }*/
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
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
        const val CHOOSER_TYPE_EXTRA = "extraChooserType"
        
        private const val TAG = "DefaultAddListActivity"
    }
}

abstract class AddChooserFragment: Fragment(){
    abstract fun saveChooser()
    
    val parentActivity: DefaultAddListActivity by lazy { activity as DefaultAddListActivity }
}

open class AddOrderChooserFragment: AddChooserFragment(){
    
    open val chooser: OrderChooser<ChooserItem> by lazy {
        val ctx = context
        val arg = arguments
        if (ctx != null &&  arg != null) {
            val chooserId = arg.getInt(DefaultAddListActivity.CHOOSER_ID_EXTRA)
            getChooser(ctx, chooserId).toOrderChooser()
        }else throw Error("could not get chooser id")
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_add_order_chooser, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        //set data to UI
        list.apply {
            adapter = OrderAdapter(chooser.items.toMutableChooserItems().toMutableList())
            layoutManager = LinearLayoutManager(context)
        }
        
        parentActivity.chooserTitle = chooser.title
    }
    
    override fun saveChooser() {
        Log.d(TAG, "on Pause called. Type ${chooser::class}")
        val ctx = context ?: return
        hideSoftKeyboard(ctx, view?.findFocus())
        
        //update List object
        val cleanedItems = (list.adapter as OrderAdapter).values
                .filter { it.name != "" }
        val title = parentActivity.chooserTitle
        //when nothing was filled in -> discard list
        if (cleanedItems.isEmpty() && title == "") {
            deleteListEntry(ctx, chooser.id)
            return
        }
        
        chooser.items.apply {
            clear()
            addAll(cleanedItems.toChooserItems())
        }
        chooser.title = title
        
        //write/update data to database
        updateListEntryComplete(ctx, chooser)
    }
    
    class OrderAdapter(values: MutableList<MutableChooserItem>):
            EditListAdapter<MutableChooserItem>(values) {
        
        class EditViewHolder(layout: View): EditRowVH(layout){
            override val itemNameET: BetterEditText = layout.findViewById(R.id.itemName)
            override val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
        }
        
        class FootViewHolder(layout: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(layout){
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
                            .inflate(R.layout.edit_item_row, parent,false)
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
        
        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
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

open class AddPickChooserFragment: AddOrderChooserFragment(){
    override val chooser: PickChooser<ChooserItem> by lazy {
        val ctx = context
        val arg = arguments
        if (ctx != null &&  arg != null) {
            val chooserId = arg.getInt(DefaultAddListActivity.CHOOSER_ID_EXTRA)
            getChooser(ctx, chooserId).toPickChooser()
        }else throw Error("could not get chooser id")
    }
}

open class AddWeightedChooserFragment: AddChooserFragment(){
    
    open val chooser: WeightedChooser<WeightedChooserItem> by lazy {
        val ctx = context
        val arg = arguments
        if (ctx != null &&  arg != null) {
            val chooserId = arg.getInt(DefaultAddListActivity.CHOOSER_ID_EXTRA)
            getChooser(ctx, chooserId).toWeightedChooser()
        }else throw Error("could not get chooser id")
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_add_order_chooser, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        //set data to UI
        list.apply {
            adapter = WeightAdapter(chooser.items.toMutableWeightedChooserItems().toMutableList())
            layoutManager = LinearLayoutManager(context)
        }
        parentActivity.chooserTitle = chooser.title
    }
    
    override fun saveChooser() {
        Log.d(TAG, "on Pause called. Type ${chooser::class}")
        val ctx = context ?: return
        hideSoftKeyboard(context, view?.findFocus())
        
        //update List object
        val cleanedItems = (list.adapter as WeightAdapter).values
                .filter { it.name != "" }
        val title = parentActivity.chooserTitle
        //when nothing was filled in -> discard list
        if (cleanedItems.isEmpty() && title == "") {
            deleteListEntry(ctx, chooser.id)
            return
        }
        
        chooser.items.apply {
            clear()
            addAll(cleanedItems.toWeightedChooserItem())
        }
        chooser.title = title
        
        //write/update data to database
        updateListEntryComplete(ctx, chooser)
    }
    
    open class WeightAdapter(values: MutableList<MutableWeightedChooserItem>):
            EditListAdapter<MutableWeightedChooserItem>(values){
        
        private var maxWeight: Int = values.maxBy { it.weight }?.weight ?: 1
        
        class EditViewHolder(layout: View): EditRowVH(layout){
            override val itemNameET: BetterEditText = layout.findViewById(R.id.itemName)
            override val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
            val weightButton: ImageButton = layout.findViewById(R.id.weightButton)
        }
        
        class FootViewHolder(layout: View): RecyclerView.ViewHolder(layout){
            val root: ViewGroup = layout.findViewById(R.id.linLayout)
        }
        
        override fun onAttachedToRecyclerView(rV: RecyclerView) {
            super.onAttachedToRecyclerView(rV)
            onRemoveItemListener = { item, pos ->
                if (item.weight == maxWeight){
                    maxWeight = values.maxBy { it.weight }?.weight ?: 1
                    updateProgressPercent()
                }
            }
        }
        
        override fun writeEditContent(pos: Int, content: String) {
            values[pos].name = content
        }
        
        override fun readEditContent(pos: Int): String = values[pos].name
        
        override fun newItem(pos: Int, s: String): MutableWeightedChooserItem =
                MutableWeightedChooserItem(s, values.size, 1)
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
                = when(viewType){
            VIEW_TYPE_EDIT_ROW -> EditViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.edit_weighted_item_row, parent,false)
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
            when(holder.itemViewType) {
                1 -> {
                    (holder as FootViewHolder).apply{
                        root.setOnClickListener { addNewItem(values.size) }
                    }
                }
                VIEW_TYPE_EDIT_ROW -> {
                    holder as EditViewHolder
                    
                    val animDraw = WeightProgressDrawable(
                            context.getAttribute(R.attr.colorAccent).data,
                            context.getAttribute(R.attr.colorOnBackground).data)
                            .apply {
                                
                                progressPercent = (values[position].weight / maxWeight.toFloat()) * 100
                            }
                    holder.weightButton.setImageDrawable(animDraw)
                    holder.weightButton.setOnClickListener {
                        val pos = holder.adapterPosition
                        
                        @SuppressLint("InflateParams")
                        val custView = LayoutInflater
                                .from(context)
                                .inflate(R.layout.weight_dialog, null, false)
                        custView.weightET.apply {
                            val sString = values[pos].weight.toString()
                            setText(sString)
                            setSelection(sString.length)
                            addTextChangedListener(afterTextChanged { s ->
                                val string = s.toString()
                                error = if (!string.matches("""\d+""".toRegex()) || string == "0")
                                    resources.getString(R.string.messageWeightGreaterZero)
                                else null
                            })
                            
                            showSoftInputOnFocus = true
                            focusAndShowKeyboard()
                        }
                        
                        MaterialAlertDialogBuilder(context)
                                .setView(custView)
                                .setPositiveButton(R.string.dialogPositive) { _, _ ->
                                    val text = custView.weightET.text.toString()
                                    if (!text.matches("""\d+""".toRegex()) || text == "0"){
                                        Toast.makeText(context,
                                                R.string.messageWeightGreaterZero,
                                                Toast.LENGTH_SHORT)
                                                .show()
                                        return@setPositiveButton
                                    }
                                    val newWeight = text.toInt()
                                    values[pos].weight = newWeight
                                    
                                    val newMax = values.maxBy { it.weight }?.weight ?: 1
                                    if (newMax != maxWeight){
                                        maxWeight = newWeight
                                        updateProgressPercent()
                                    }else animDraw.progressPercent = (newWeight / maxWeight.toFloat()) * 100
                                }
                                .setNegativeButton(R.string.dialogNegative) { _, _ -> }
                                .show()
                        
                        //custView.weightET.focusAndShowKeyboard()
                    }
                }
            }
            super.onBindViewHolder(holder, position)
        }
        
        override fun getItemViewType(position: Int): Int = when(position){
            values.size -> 1
            else -> VIEW_TYPE_EDIT_ROW
        }
        
        override fun getItemCount(): Int = values.size + 1
        
        private fun updateProgressPercent(){
            for (pos in 0 until itemCount - 1) {
                //can be null then position is currently not shown
                val vH = recyclerView.findViewHolderForAdapterPosition(pos) as EditViewHolder?
                if (vH != null){
                    (vH.weightButton.drawable as WeightProgressDrawable).progressPercent =
                            (values[pos].weight / maxWeight.toFloat()) * 100
                }
            }
        }
    }
}

private const val TAG = "AddListActivity"
