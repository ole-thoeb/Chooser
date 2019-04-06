package chooser.com.example.eloem.chooser.ui.editors

import android.annotation.SuppressLint
import android.app.ActionBar
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import chooser.com.example.eloem.chooser.ui.ChildFragment
import chooser.com.example.eloem.chooser.ui.GlobalViewModel
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.util.activityViewModel
import kotlinx.android.synthetic.main.actionbar_layout.view.*

open class EditorFragment: ChildFragment() {
    
    val globalViewModel: GlobalViewModel by activityViewModel()
    
    lateinit var modeOption: MenuItem
    
    private var toolbarBarText: EditText? = null
    
    var chooserTitle: String
        set(value) { toolbarBarText?.setText(value) }
        get() = toolbarBarText?.text?.toString() ?: ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    @SuppressLint("InflateParams")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    
        hostActivity.supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            val custView = layoutInflater.inflate(R.layout.actionbar_layout, null, false)
            toolbarBarText = custView.actionBarText
            val lParmas = androidx.appcompat.app.ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
            setCustomView(custView, lParmas)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    
        hostActivity.hideBottomSheet()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_list, menu)
        modeOption = menu.findItem(R.id.mode)
    }
}