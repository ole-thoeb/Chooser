package chooser.com.example.eloem.chooser

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import chooser.com.example.eloem.chooser.helperClasses.OrderChooser
import chooser.com.example.eloem.chooser.helperClasses.parsType
import chooser.com.example.eloem.chooser.util.*
import kotlinx.android.synthetic.main.activity_display_item.*
import kotlinx.android.synthetic.main.display_item_bottom_sheet.*

open class DisplayOrderChooserActivity<T: OrderChooser<*>> : AppCompatActivity() {
    
    lateinit var data: T
    lateinit var mBottomSheetAdapter: ListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentTheme)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_item)
        
        val chooserId = intent.getIntExtra(CHOOSER_ID_EXTRA, -1)
        if (chooserId == -1) finish()
        
        val chooser = getChooser(this, chooserId)
        try {
            @Suppress("UNCHECKED_CAST")
            data = chooser as T
        }catch (e: TypeCastException){
            finish()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //set data in bottom sheet
        listTitleTV.text = data.title
        val sheetBehavior = BottomSheetBehavior.from(bottomSheet)
        listTitleTV.setOnClickListener {
            sheetBehavior.state = if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                BottomSheetBehavior.STATE_EXPANDED
            else BottomSheetBehavior.STATE_COLLAPSED
        }
        
        mBottomSheetAdapter = ListAdapter(this)
        list.adapter = mBottomSheetAdapter
        
        updateUiWithData()
        
        nextItemFAB.setOnClickListener {
            if (data.hasNextItem) { // show next Item
                data.nextItem()
                updateJustList(this, data)
                updateUiWithData()
            } else { // build dialog
                restartChooser()
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_display_item, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.delete -> {
            deleteChooser()
            true
        }
        R.id.restart -> {
            restartChooser()
            true
        }
        R.id.edit -> {
            editChooser()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    open fun deleteChooser(){
        AlertDialog.Builder(this)
                .setMessage(R.string.dialogDeleteListMessage)
                .setNegativeButton(R.string.dialogNegative) { _, _ ->
                    //nothing
                }
                .setPositiveButton(R.string.dialogPositive) { _, _ ->
                    deleteListEntry(this, data.id)
                
                    NavUtils.navigateUpFromSameTask(this)
                }
                .show()
    }
    
    open fun restartChooser(){
        AlertDialog.Builder(this)
                .setMessage(R.string.dialogRestartListMessage)
                .setNegativeButton(R.string.dialogNegative) { _, _ ->
                    //nothing
                }
                .setPositiveButton(R.string.dialogPositive) { _, _ ->
                    data.restart()
                    updateJustList(this, data)
                    updateItems(this, data.items, data.id)
                    updateUiWithData()
                }
                .show()
    }
    
    open fun editChooser(){
        val intent = Intent(this, DefaultAddListActivity::class.java).apply {
            putExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, this@DisplayOrderChooserActivity.data.id)
            putExtra(DefaultAddListActivity.CHOOSER_TYPE_EXTRA, this@DisplayOrderChooserActivity.data.parsType())
        }
        startActivity(intent)
    }
    
    open fun updateUiWithData() {
        progressTV.text = if (data.hasNoItems) resources.getString(R.string.noItem)
        else {
            itemNameTV.text = data.currentItem.name
            mBottomSheetAdapter.notifyDataSetChanged()
            
            resources.getString(R.string.progressString, data.currentPos + 1, data.items.size)
        }
        
        if (!data.hasNextItem) nextItemFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_refresh))
    }
    
    inner class ListAdapter(val context: Context) : BaseAdapter() {
        
        private val accentColor = context.getAttribute(R.attr.colorAccent, true).data
        private val textColor = context.getAttribute(R.attr.itemTextColor, true).data
        private val backgroundColor = context.getAttribute(R.attr.background, true).data
        
        override fun getCount() = data.items.size
        
        override fun getItem(position: Int) = data.items[position]
        
        override fun getItemId(position: Int) = position.toLong()
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val vH = convertView
                    ?: layoutInflater.inflate(R.layout.display_item_list_item, parent, false)
            
            val tv = vH.findViewById<TextView>(R.id.itemTV)
            tv.text = resources.getString(R.string.displayItemListString, position + 1, getItem(position).name)
            
            if (position == data.currentPos) {
                tv.setBackgroundColor(accentColor)
            } else {
                tv.setBackgroundColor(backgroundColor)
            }
            
            return vH
        }
    }
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
    }
}
