package chooser.com.example.eloem.chooser.ui.display

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import chooser.com.example.eloem.chooser.ui.ChildFragment
import chooser.com.example.eloem.chooser.ui.GlobalViewModel
import chooser.com.example.eloem.chooser.chooser.ChooserItem
import chooser.com.example.eloem.chooser.chooser.ChooserItemChooser
import chooser.com.example.eloem.chooser.chooser.PickChooser
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.util.*
import chooser.com.example.eloem.chooser.R
import kotlinx.android.synthetic.main.fragment_display_chooser.*
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

open class DisplayPickChooserFragment<T: PickChooser<*>>: ChildFragment() {
    
    //lateinit var data: T
    lateinit var mBottomSheetAdapter: ListAdapter
    
    val globalViewModel: GlobalViewModel by activityViewModel()
    
    var chooser: ChooserItemChooser<out ChooserItem>? = null
    
    private val arg: DisplayPickChooserFragmentArgs by navArgs()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_display_chooser, container, false)
    }
    
    open val postponeBottomSheet get() = arg.postponeBottomSheet
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    
        //because of buggy animation of bottom sheet
        doAsync {
            if (postponeBottomSheet) Thread.sleep(200)
            uiThread {
                hostActivity.showBottomSheet()
            }
        }
        
        mBottomSheetAdapter = ListAdapter(requireContext(), ChooserItemChooser.EMPTY)
        hostActivity.bottomSheet.list.adapter = mBottomSheetAdapter
        
        val chooserId = arg.chooserId
        
        globalViewModel.getChooserItemChooser(chooserId).observe(viewLifecycleOwner, Observer {
            chooser = it
            if (it != null) {
                hostActivity.bottomSheet.listTitleTV.text = it.title
                mBottomSheetAdapter.chooser = it
                updateUiWithData(it)
            }
        })
    
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setDisplayShowTitleEnabled(false)
        
        hostActivity.mainFab.setOnClickListener { onFabPressed() }
        hostActivity.mainFab.animateToIcon(AnimatedIconFab.Icon.NEXT)
        
        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(false)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_display_pick_chooser, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
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
        chooser?.let {
            hostActivity.showBottomSheet()
            globalViewModel.deleteChooserUiFeedBack(it, hostActivity.rootView, hostActivity.mainFab)
            findNavController().navigateUp()
        }
    }
    
    open fun editChooser(){
        chooser?.let {
            findNavController()
                    .navigate(DisplayPickChooserFragmentDirections.actionGlobalAddChooserItemChooserFragment(it.id))
        }
    }
    
    open fun onFabPressed() {
        chooser?.let {
            if (!it.hasNoItems) { // show nextItem Item
                it.nextItem()
                globalViewModel.updateChooserAfterNext(it)
            }
        }
    }
    
    open fun updateUiWithData(newChooser: ChooserItemChooser<out ChooserItem>) {
        progressTV.text = if (newChooser.hasNoItems) {
            resources.getString(R.string.noItem)
        } else {
            itemNameTV.text = newChooser.currentItem.name
            mBottomSheetAdapter.notifyDataSetChanged()
            
            ""
        }
    }
    
    class ListAdapter(val context: Context, var chooser: ChooserItemChooser<out ChooserItem>) : BaseAdapter() {
        
        private val accentColor = context.getAttribute(R.attr.colorAccent).data
        private val backgroundColor = context.getAttribute(R.attr.background).data
        
        override fun getCount() = chooser.items.size
        
        override fun getItem(position: Int) = chooser.items[position]
        
        override fun getItemId(position: Int) = position.toLong()
        
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val vH = convertView
                    ?: LayoutInflater
                            .from(context)
                            .inflate(R.layout.display_item_list_item, parent, false)
            
            val tv = vH.findViewById<TextView>(R.id.itemTV)
            tv.text = context.getString(R.string.displayItemListString, position + 1, getItem(position).name)
            
            if (position == chooser.currentPos) {
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