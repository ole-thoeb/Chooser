package chooser.com.example.eloem.chooser

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import chooser.com.example.eloem.chooser.helperClasses.*
import chooser.com.example.eloem.chooser.util.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_activity_list_item.view.*

class MainActivity : AppCompatActivity() {
    
    private var currentlyCABActive: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentColoredTheme)
        /**new theme was applied*/
        writeRecreateMain(this, false)
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_main)
        //registerForContextMenu(listView)
        /*database.use { dropTables(this) }
        database.use { createTables(this) }*/
        fab.setOnClickListener {
            val id = newListId(this)
            val listObj = OrderChooser(id,
                    "",
                    mutableListOf(ChooserItem("", 0)))
            
            val intent = Intent(this, DefaultAddListActivity::class.java).apply {
                putExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, id)
                putExtra(DefaultAddListActivity.CHOOSER_TYPE_EXTRA, listObj.parsType())
            }
            insertListEntry(this, listObj)
            startActivity(intent)
        }
    }
    
    
    override fun onResume() {
        if (readRecreateMain(this)) recreate()
        super.onResume()
        
        //get data from database
        val data = getAllListObj(this)
    
        val mAdapter = MainListAdapter(this, data.toMutableList())
    
        listView.apply {
            adapter = mAdapter
            emptyView = empty
            choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
            setMultiChoiceModeListener(object: AbsListView.MultiChoiceModeListener{
                
                override fun onActionItemClicked(mode: ActionMode, item: MenuItem?): Boolean {
                    // Respond to clicks on the actions in the CAB
                    return when (item?.itemId) {
                        R.id.delete -> {
                            mAdapter.deleteSelectedItems()
                            mode.finish() // Action picked, so close the CAB
                            true
                        }
                        R.id.restart -> {
                            mAdapter.selectedChoosersPos.forEach {
                                mAdapter.restartList(it)
                            }
                            mode.finish()
                            true
                        }
                        R.id.edit -> {
                            if (mAdapter.selectedChoosersPos.size == 1){
                                mAdapter.editItem(mAdapter.selectedChoosersPos.first())
                            }
                            mode.finish()
                            true
                        }
                        else -> false
                    }
                }
    
                override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
                    // Here you can do something when items are selected/de-selected,
                    // such as update the title in the CAB
                    Log.d(TAG, "onItemCheckStateChanged pos = $position, checked = $checked")
                    if (checked) {
                        if (mAdapter.selectedChoosersPos.size == 2){
                            mode.menu.findItem(R.id.edit).run {
                                isVisible = false
                                isEnabled = false
                            }
                        }
                    } else {
                        if (mAdapter.selectedChoosersPos.size == 1){
                            mode.menu.findItem(R.id.edit).run {
                                isVisible = true
                                isEnabled = true
                            }
                        }
                    }
                    mode.title = resources.getQuantityString(R.plurals.titleCAB,
                            mAdapter.selectedChoosersPos.size, mAdapter.selectedChoosersPos.size)
                }
    
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    // Inflate the menu for the CAB
                    val menuInflater = mode.menuInflater
                    menuInflater.inflate(R.menu.context_main, menu)
                    currentlyCABActive = true
                    return true
                }
    
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }
    
                override fun onDestroyActionMode(mode: ActionMode?) {
    
                    // Here you can make any necessary updates to the activity when
                    // the CAB is removed. By default, selected items are deselected/unchecked.
                    currentlyCABActive = false
    
                    mAdapter.selectedChoosersPos.clear()
                }
            })
        }
    }
    
    /*override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_main, menu)
    }
    
    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo
        val adapter = listView.adapter as MainListAdapter
        
        return when (item.itemId) {
            R.id.delete -> {
                adapter.deleteItem(info.position)
                true
            }
            R.id.edit -> {
                adapter.editItem(info.position)
                true
            }
            R.id.restart -> {
                adapter.restartList(info.position)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }*/
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //TODO: correct menu resource and actions
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    inner class MainListAdapter(private val context: Context, val values: MutableList<ChooserObj>): BaseAdapter(){
        
        val selectedChoosersPos = mutableListOf<Int>()
    
        fun itemInCABPressed(position: Int) {
            Log.d(TAG, "Item $position in CAB pressed")
            if (isItemChecked(position)) deselectItem(position)
            else selectItem(position)
        }
    
        fun selectItem(position: Int){
            selectedChoosersPos.add(position)
            listView.setItemChecked(position, true)
        }
    
        fun deselectItem(position: Int){
            selectedChoosersPos.remove(position)
            listView.setItemChecked(position, false)
        }
    
        private fun isItemChecked(position: Int) = position in selectedChoosersPos
        
        /*class ViewHolder1(layout: View): RecyclerView.ViewHolder(layout), View.OnCreateContextMenuListener{
            val titleTV: TextView = layout.findViewById(R.id.titleTV)
            val currentItemTV: TextView = layout.findViewById(R.id.currentItemTV)
            val progressTV: TextView = layout.findViewById(R.id.progressTV)
            init {
                layout.setOnCreateContextMenuListener(this)
            }
    
            override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }*/
    
        override fun getCount() = values.size
    
        /*override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val rHolder = holder as ViewHolder1
            
            val listObj = values[position]
            
            rHolder.currentItemTV.text = listObj.currentItem.name
            rHolder.progressTV.text = "${listObj.currentPos + 1}/${listObj.items.size}"
            rHolder.titleTV.text = listObj.title
            rHolder.onCreateContextMenu()
        }*/
    
        override fun getItem(position: Int) = values[position]
    
        override fun getItemId(position: Int): Long = position.toLong()
    
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val listObj = getItem(position)
            
            val vH = convertView?: layoutInflater.inflate(R.layout.main_activity_list_item, parent, false)
            
            with(vH){
                progressTV.text = when(listObj){
                    is PickChooser<*> -> resources.getString(R.string.randomPick)
                    is OrderChooser<*> -> {
                        if (listObj.hasNoItems) resources.getString(R.string.noItem)
                        else resources.getString(R.string.progressString, listObj.currentPos + 1, listObj.items.size)
                    }
        
                    else -> resources.getString(R.string.error)
                }
    
                titleTV.text = listObj.title
                currentItemTV.text =
                        if (listObj.hasNoItems) ""
                        else listObj.currentItem.name
    
                card.setNoDoubleClickListener{
                    Log.d(TAG, "NormalPress")
                    if (currentlyCABActive){
                        itemInCABPressed(position)
                    }else {
                        val chooser = values[position]
    
                        when (chooser) {
                            is PickChooser<*> ->
                                startActivity(Intent(context, DisplayPickChooserActivity::class.java).apply {
                                putExtra(DisplayOrderChooserActivity.CHOOSER_ID_EXTRA, chooser.id)
                            })
                            is OrderChooser<*> ->
                                startActivity(Intent(context, DisplayOrderChooserActivity::class.java).apply {
                                putExtra(DisplayOrderChooserActivity.CHOOSER_ID_EXTRA, chooser.id)
                            })
                        }
                    }
                }
                card.setOnLongClickListener {
                    Log.d(TAG, "LongPress")
                        itemInCABPressed(position)
                        true
                }
                card.setCardBackgroundColor(context.getAttribute(
                        if (isItemChecked(position)) R.attr.selectedItemBackground
                        else R.attr.mCardBackgroundColor,
                        true).data
                )
    
                /*menuButton.setOnClickListener { button ->
                    val popup = PopupMenu(context, button)
                    with(popup) {
                        menuInflater.inflate(R.menu.context_main, popup.menu)
            
                        setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.delete -> {
                                    AlertDialog.Builder(context)
                                            .setMessage(R.string.dialogDeleteListMessage)
                                            .setNegativeButton(R.string.dialogNegative) { _, _ ->
                                                //nothing
                                            }
                                            .setPositiveButton(R.string.dialogPositive) { _, _ ->
                                                deleteItem(position)
                                            }
                                            .show()
                                    true
                                }
                                R.id.edit -> {
                                    editItem(position)
                                    true
                                }
                                R.id.restart -> {
                                    AlertDialog.Builder(context)
                                            .setMessage(R.string.dialogRestartListMessage)
                                            .setNegativeButton(R.string.dialogNegative) { _, _ ->
                                                //nothing
                                            }
                                            .setPositiveButton(R.string.dialogPositive) { _, _ ->
                                                restartList(position)
                                            }
                                            .show()
                                    true
                                }
                                else -> false
                            }
                        }
            
                        show()
                    }
                }*/
            }
            
            return vH
        }
        
        fun deleteSelectedItems(){
            val choosers = values.filterIndexed { index, chooserObj -> index in selectedChoosersPos }
            choosers.forEach { deleteListEntry(context, it.id) }
            values.removeAll(choosers)
            notifyDataSetChanged()
        }
        
        fun deleteItem(pos: Int){
            if (pos < count){
                deleteListEntry(context, values[pos].id)
                values.removeAt(pos)
                notifyDataSetChanged()
            }
        }
        
        fun editItem(pos: Int){
            if (pos < count){
                val chooser = values[pos]
                val intent = Intent(context, DefaultAddListActivity::class.java).apply {
                    putExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, chooser.id)
                    putExtra(DefaultAddListActivity.CHOOSER_TYPE_EXTRA, chooser.parsType())
                }
                startActivity(intent)
            }
        }
        
        fun restartList(pos: Int){
            val chooser = values[pos]
            if (chooser is OrderChooser<*>){
                chooser.restart()
                updateJustList(context, chooser)
                updateItems(context, chooser.items, chooser.id)
                notifyDataSetChanged()
            }
        }
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}
