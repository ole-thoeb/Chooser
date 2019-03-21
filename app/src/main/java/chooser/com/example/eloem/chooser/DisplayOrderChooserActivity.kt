package chooser.com.example.eloem.chooser

import android.view.Menu
import android.view.MenuItem
import chooser.com.example.eloem.chooser.chooser.OrderChooser
import chooser.com.example.eloem.chooser.util.showRestartDialog
import chooser.com.example.eloem.chooser.database.updateChooserItems
import chooser.com.example.eloem.chooser.database.updateJustChooserItemChooser
import kotlinx.android.synthetic.main.activity_display_item.*

open class DisplayOrderChooserActivity<T: OrderChooser<*>> : DisplayPickChooserActivity<T>() {
    
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
    
    open fun restartChooser(){
        showRestartDialog(this) {
            data.restart()
            updateJustChooserItemChooser(this, data)
            updateChooserItems(this, data.items, data.id)
            updateUiWithData()
        }
    }
    
    override fun onFabPressed() {
        if (data.hasNextItem) {
            super.onFabPressed()
        } else {
            restartChooser()
        }
    }
    
    override fun updateUiWithData() {
        super.updateUiWithData()
        
        if (!data.hasNoItems) {
            progressTV.text = resources.getString(R.string.progressString, data.currentPos + 1, data.items.size)
            if (!data.hasNextItem) {
                nextItemFAB.setImageResource(R.drawable.ic_refresh)
            } else {
                nextItemFAB.setImageResource(R.drawable.ic_arrow_forward)
            }
        }
    }
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
    }
}
