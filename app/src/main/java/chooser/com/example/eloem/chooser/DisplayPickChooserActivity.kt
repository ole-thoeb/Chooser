package chooser.com.example.eloem.chooser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import chooser.com.example.eloem.chooser.chooser.PickChooser
import chooser.com.example.eloem.chooser.chooser.parsType
import chooser.com.example.eloem.chooser.database.deleteChooserItemChooser
import chooser.com.example.eloem.chooser.database.getChooserItemChooser
import chooser.com.example.eloem.chooser.database.updateJustChooserItemChooser
import chooser.com.example.eloem.chooser.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_display_item.*
import kotlinx.android.synthetic.main.display_item_bottom_sheet.*

open class DisplayPickChooserActivity<T: PickChooser<*>>: AppCompatActivity() {
    
    lateinit var data: T
    lateinit var mBottomSheetAdapter: ListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentTheme)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_item)
        
        val chooserId = intent.getIntExtra(CHOOSER_ID_EXTRA, -1)
        if (chooserId == -1) finish()
        
        val chooser = getChooserItemChooser(this, chooserId)
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
        
        nextItemFAB.setOnClickListener { onFabPressed() }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_display_pick_chooser, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.delete -> {
            deleteChooser()
            true
        }
        R.id.edit -> {
            editChooser()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    open fun deleteChooser(){
        showDeleteDialog(this) {
            deleteChooserItemChooser(this, data.id)
            NavUtils.navigateUpFromSameTask(this)
        }
    }
    
    open fun editChooser(){
        val intent = Intent(this, DefaultAddListActivity::class.java)
                .putExtra(DefaultAddListActivity.CHOOSER_ID_EXTRA, this.data.id)
                .putExtra(DefaultAddListActivity.CHOOSER_TYPE_EXTRA, this.data.parsType())
        startActivity(intent)
    }
    
    open fun onFabPressed() {
        if (!data.hasNoItems) { // show nextItem Item
            data.nextItem()
            updateJustChooserItemChooser(this, data)
            updateUiWithData()
        }
    }
    
    open fun updateUiWithData() {
        progressTV.text = if (data.hasNoItems) {
            resources.getString(R.string.noItem)
        } else {
            itemNameTV.text = data.currentItem.name
            mBottomSheetAdapter.notifyDataSetChanged()
            
            ""
        }
    }
    
    inner class ListAdapter(val context: Context) : BaseAdapter() {
        
        private val accentColor = context.getAttribute(R.attr.colorAccent).data
        private val backgroundColor = context.getAttribute(R.attr.background).data
        
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