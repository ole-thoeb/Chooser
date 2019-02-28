package chooser.com.example.eloem.chooser

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.*
import android.widget.*
import chooser.com.example.eloem.chooser.helperClasses.*
import chooser.com.example.eloem.chooser.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    
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
            divider = resources.getDrawable(R.drawable.transparent, theme)
            emptyView = empty
        }
    }
    
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
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
    }
    
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
    
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }
    
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val listObj = getItem(position)
            
            val vH = convertView?: layoutInflater.inflate(R.layout.main_activity_list_item, parent, false)
            
            vH.findViewById<TextView>(R.id.progressTV).text = when(listObj){
                is OrderChooser<*> -> {
                    if (listObj.hasNoItems) resources.getString(R.string.noItem)
                    else resources.getString(R.string.progressString, listObj.currentPos + 1, listObj.items.size)
                }
                is PickChooser<*> -> resources.getString(R.string.randomPick)
                
                else -> resources.getString(R.string.error)
            }
            
            vH.findViewById<TextView>(R.id.titleTV).text = listObj.title
            vH.findViewById<TextView>(R.id.currentItemTV).text = listObj.currentItem.name

            vH.findViewById<CardView>(R.id.card).setOnClickListener{
                val chooser = values[position]
                when(chooser){
                    is PickChooser<*> -> startActivity(Intent(context, DisplayPickChooserActivity::class.java).apply {
                        putExtra(DisplayOrderChooserActivity.CHOOSER_ID_EXTRA, chooser.id)
                    })
                    is OrderChooser<*> -> startActivity(Intent(context, DisplayOrderChooserActivity::class.java).apply {
                        putExtra(DisplayOrderChooserActivity.CHOOSER_ID_EXTRA, chooser.id)
                    })
                }
            }
        
            vH.findViewById<ImageButton>(R.id.menuButton).setOnClickListener { button ->
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
            }
            return vH
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
                val intent = Intent(context, DefaultAddListActivity::class.java).apply {
                    putExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, values[pos].id)
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
}
