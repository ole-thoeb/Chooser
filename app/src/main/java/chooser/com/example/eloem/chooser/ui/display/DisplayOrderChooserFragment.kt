package chooser.com.example.eloem.chooser.ui.display

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import chooser.com.example.eloem.chooser.chooser.ChooserItem
import chooser.com.example.eloem.chooser.chooser.ChooserItemChooser
import chooser.com.example.eloem.chooser.chooser.Depletable
import chooser.com.example.eloem.chooser.chooser.OrderChooser
import kotlinx.android.synthetic.main.fragment_display_chooser.*
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.R

open class DisplayOrderChooserFragment<T: OrderChooser<*>> : DisplayPickChooserFragment<T>() {
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_display_item, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        chooser?.let {
            if (it is OrderChooser<*>) {
                it.restart()
                globalViewModel.restartChooserUiFeedBack(it, hostActivity.rootView, hostActivity.mainFab)
            }
        }
    }
    
    override fun onFabPressed() {
        chooser?.let {
            if (it is Depletable<*>) {
                if (it.hasNextItem) {
                    super.onFabPressed()
                } else {
                    restartChooser()
                }
            } else {
                super.onFabPressed()
            }
        }
    }
    
    override fun updateUiWithData(newChooser: ChooserItemChooser<out ChooserItem>) {
        super.updateUiWithData(newChooser)
        
        if (newChooser !is Depletable<*>) return
        
        if (!newChooser.hasNoItems) {
            progressTV.text = resources.getString(R.string.progressString, newChooser.currentPos + 1, newChooser.items.size)
            hostActivity.mainFab.animateToIcon(if (!newChooser.hasNextItem) {
                AnimatedIconFab.Icon.REFRESH
            } else {
                AnimatedIconFab.Icon.NEXT
            })
        }
    }
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
    }
}
