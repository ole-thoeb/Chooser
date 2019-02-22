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
import chooser.com.example.eloem.chooser.helperClasses.ListObj
import chooser.com.example.eloem.chooser.util.*
import kotlinx.android.synthetic.main.activity_display_item.*
import kotlinx.android.synthetic.main.display_item_bottom_sheet.*

class DisplayItemActivity : AppCompatActivity() {
    
    lateinit var data: ListObj
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentTheme)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_item)
        
        data = intent.getParcelableExtra(LIST_OBJ_EXTRA) as ListObj
        
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
        list.adapter = ListAdapter(this)
        
        updateUiWithData()
        
        nextItemFAB.setOnClickListener {
            if (data.hasNext) { // show next Item
                data.nextItem()
                updateJustList(this, data)
                updateUiWithData()
            } else { // build dialog
                AlertDialog.Builder(this)
                        .setMessage(R.string.dialogRestartListMessage)
                        .setNegativeButton(R.string.dialogNegative) { _, _ ->
                            //nothing
                        }
                        .setPositiveButton(R.string.dialogPositive) { _, _ ->
                            data.restart()
                            updateItems(this, data.items)
                            updateJustList(this, data)
                            nextItemFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_arrow_forward))
                            updateUiWithData()
                        }
                        .show()
                
                /*val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.dialogNoMoreItemsMessage)
                        .setNegativeButton(R.string.dialogNoMoreItemsNegative) { dialog, which ->
                            deleteListEntry(this, data.id)
                            NavUtils.navigateUpFromSameTask(this)
                        }
                        .setPositiveButton(R.string.dialogNoMoreItemsPositive) { dialog, which ->
                            data.restart()
                            updateListEntryComplete(this, data)
                            updateUiWithData()
                        }
                        .show()*/
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_display_item, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.delete -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialogDeleteListMessage)
                    .setNegativeButton(R.string.dialogNegative) { _, _ ->
                        //nothing
                    }
                    .setPositiveButton(R.string.dialogPositive) { _, _ ->
                        deleteListEntry(this, data.id)
                        
                        NavUtils.navigateUpFromSameTask(this)
                    }
                    .show()
            true
        }
        R.id.restart -> {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialogRestartListMessage)
                    .setNegativeButton(R.string.dialogNegative) { _, _ ->
                        //nothing
                    }
                    .setPositiveButton(R.string.dialogPositive) { _, _ ->
                        data.restart()
                        updateItems(this, data.items)
                        updateUiWithData()
                    }
                    .show()
            true
        }
        R.id.edit -> {
            val intent = Intent(this, AddListActivity::class.java)
            intent.putExtra(AddListActivity.LIST_OBJ_EXTRA, data)
            intent.putExtra(AddListActivity.IS_NEW_LIST_FLAG, false)
            
            startActivity(intent)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    private fun updateUiWithData() {
        progressTV.text = if (data.hasNoItems) resources.getString(R.string.noItem)
        else when (data.mode) {
            ListObj.MODE_RANDOM_ORDER -> resources.getString(R.string.progressString, data.currentPos + 1, data.items.size)
            ListObj.MODE_SINGLE_PICK -> "" //show nothing
            
            else -> resources.getString(R.string.error)
        }
        
        itemNameTV.text = data.currentItem.name
        
        (list.adapter as ListAdapter).notifyDataSetChanged()
        
        if (!data.hasNext) nextItemFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_refresh))
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
        const val LIST_OBJ_EXTRA = "extraListObj"
    }
}
