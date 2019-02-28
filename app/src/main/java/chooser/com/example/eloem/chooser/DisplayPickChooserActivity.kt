package chooser.com.example.eloem.chooser

import android.view.Menu
import android.view.MenuItem
import chooser.com.example.eloem.chooser.helperClasses.PickChooser
import kotlinx.android.synthetic.main.activity_display_item.*

class DisplayPickChooserActivity<T: PickChooser<*>>: DisplayOrderChooserActivity<T>() {
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_display_pick_chooser, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId) {
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
    
    override fun updateUiWithData() {
        progressTV.text = if (data.hasNoItems) resources.getString(R.string.noItem)
        else {
            itemNameTV.text = data.currentItem.name
            mBottomSheetAdapter.notifyDataSetChanged()
        
            ""
        }
    }
}