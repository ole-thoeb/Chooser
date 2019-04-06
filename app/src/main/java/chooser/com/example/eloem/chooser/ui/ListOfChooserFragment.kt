package chooser.com.example.eloem.chooser.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.chooser.*
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.util.*
import com.example.eloem.dartCounter.recyclerview.BottomSpacingAdapter
import com.example.eloem.dartCounter.recyclerview.ContextAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_list_of_choosers.*
import java.lang.Error
import kotlin.jvm.internal.Ref

class ListOfChooserFragment : ChildFragment() {
    
    private val vm: ListOfChooserViewModel by fragmentViewModel()
    private val globalModel: GlobalViewModel by activityViewModel()
    private lateinit var mRecyclerAdapter: MainListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        /*setTheme(currentColoredTheme)
        /**new theme was applied*/
        writeRecreateMain(this, false)*/
        super.onCreate(savedInstanceState)
        
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_of_choosers, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    
        hostActivity.mainFab.setOnClickListener {
            val id = newListId(requireContext())
            val newChooser = OrderChooser(id,
                    "",
                    mutableListOf(ChooserItem("", 0)))
    
            globalModel.insertChooserItemChooser(newChooser)
            globalModel.handOverChooserItemChooser = newChooser
    
            findNavController().navigate(
                    ListOfChooserFragmentDirections.actionGlobalAddChooserItemChooserFragment(id))
        }
        hostActivity.showFab()
        hostActivity.mainFab.animateToIcon(AnimatedIconFab.Icon.ADD)
        
        hostActivity.hideBottomSheet()
        
        val selectedPos = vm.selectedChooserPositions
    
        mRecyclerAdapter = MainListAdapter(hostActivity, mutableListOf(), selectedPos, vm.cabActive, globalModel)
    
        recyclerView.apply {
            adapter = BottomSpacingAdapter(mRecyclerAdapter, resources.getDimensionPixelSize(R.dimen.paddingBottomRecyclerView))
            emptyThreshold = 1
            emptyView = empty
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        globalModel.allChoosers.observe(viewLifecycleOwner, Observer {
            mRecyclerAdapter.values = it.toMutableList()
            mRecyclerAdapter.notifyDataSetChanged()
        })
        
        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            title = resources.getString(R.string.app_name)
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowCustomEnabled(false)
        }
    }
    
    /*override fun onResume() {
        super.onResume()
        
        mRecyclerAdapter.values = getAllChooserItemChooser(requireContext()).toMutableList()
        mRecyclerAdapter.notifyDataSetChanged()
    }*/
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_list_of_chooser, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_global_settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    class MainListAdapter(
            private val host: HostActivity,
            var values: MutableList<Chooser<*>>,
            private val selectedChoosersPos: MutableList<Int>,
            private val cabActive: Ref.BooleanRef,
            private val gVm: GlobalViewModel
    ): ContextAdapter<MainListAdapter.ViewHolder1>(){
        
        private var actionMode: ActionMode? = null
        
        private val actionModeCallback = object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem?): Boolean {
                // Respond to clicks on the actions in the CAB
                return when (item?.itemId) {
                    R.id.delete -> {
                        //dereference so it is still there then the lambda is called
                        val selectedPos = selectedChoosersPos.toList()
                        deleteSelectedItems(selectedPos)
                        //showDeleteDialog(context) { deleteSelectedItems(selectedPos) }
                        mode.finish() // Action picked, so close the CAB
                        true
                    }
                    R.id.restart -> {
                        //dereference so it is still there then the lambda is called
                        val selectedPos = selectedChoosersPos.toList()
                        restartLists(selectedPos)
                        /*showRestartDialog(context) {
                            selectedPos.forEach { restartLists(it) }
                        }*/
                        mode.finish()
                        true
                    }
                    R.id.edit -> {
                        if (selectedChoosersPos.size == 1){
                            editItem(selectedChoosersPos.first())
                        }
                        mode.finish()
                        true
                    }
                    else -> false
                }
            }
    
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Inflate the menu for the CAB
                val menuInflater = mode.menuInflater
                menuInflater.inflate(R.menu.context_main, menu)
                host.window.statusBarColor = context.getAttribute(R.attr.colorPrimary).data
                return true
            }
    
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }
    
            override fun onDestroyActionMode(mode: ActionMode?) {
        
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                actionMode = null
                cabActive.element = false
                host.window.statusBarColor = context.getAttribute(R.attr.statusBarColor).data
                selectedChoosersPos.clear()
                notifyDataSetChanged()
            }
        }
    
        override fun onAttachedToRecyclerView(rv: RecyclerView) {
            super.onAttachedToRecyclerView(rv)
            
            if (cabActive.element) {
                if (selectedChoosersPos.isNotEmpty()) {
                    actionMode = host.startActionMode(actionModeCallback)?.also {
                        updateCab(it)
                    }
                } else {
                    cabActive.element = false
                }
            } else {
                selectedChoosersPos.clear()
            }
        }
    
        private fun itemInCABPressed(position: Int, view: View) {
            Log.d(TAG, "Item $position in CAB pressed")
            
            val mode = actionMode ?: host.startActionMode(actionModeCallback).also {
                actionMode = it
                cabActive.element = true
            }
            
            if (isItemChecked(position)) {
                deselectItem(position, view)
            } else {
                selectItem(position, view)
            }
            
            updateCab(mode)
        }
        
        private fun updateCab(mode: ActionMode) {
            if (selectedChoosersPos.size == 1){
                mode.menu.findItem(R.id.edit).run {
                    isVisible = true
                    isEnabled = true
                }
            } else {
                mode.menu.findItem(R.id.edit).run {
                    isVisible = false
                    isEnabled = false
                }
            }
    
            if (selectedChoosersPos.isEmpty()){
                mode.finish()
            } else {
                mode.title = context.resources.getQuantityString(R.plurals.titleCAB,
                        selectedChoosersPos.size, selectedChoosersPos.size)
            }
        }
    
        private fun selectItem(position: Int, view: View){
            selectedChoosersPos.add(position)
            view.isSelected = true
        }
    
        private fun deselectItem(position: Int, view: View){
            selectedChoosersPos.remove(position)
            view.isSelected = false
        }
    
        private fun isItemChecked(position: Int) = position in selectedChoosersPos
        
        class ViewHolder1(layout: View): RecyclerView.ViewHolder(layout){
            val titleTV: TextView = layout.findViewById(R.id.titleTV)
            val currentItemTV: TextView = layout.findViewById(R.id.currentItemTV)
            val progressTV: TextView = layout.findViewById(R.id.progressTV)
            val card: MaterialCardView = layout.findViewById(R.id.card)
        }
    
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder1 {
            return ViewHolder1(inflate(R.layout.item_list_of_chooser, parent))
        }
        
        override fun onBindViewHolder(holder: ViewHolder1, position: Int) {
            val chooser = values[position]
            
            with(holder){
                progressTV.text = when(chooser){
                    is MultiDiceList -> context.getString(R.string.diceAmount, chooser.sumBy { it.size })
                    is OrderChooser<*> -> {
                        if (chooser.hasNoItems) context.getString(R.string.noItem)
                        else context.getString(R.string.progressString, chooser.currentPos + 1, chooser.items.size)
                    }
                    is PickChooser<*> -> context.getString(R.string.randomPick)
                    else -> throw Error("unknown chooser: $chooser")
                }
        
                titleTV.text = chooser.title
                currentItemTV.text = when(chooser) {
                    is ChooserItemChooser<*> -> {
                        if (chooser.hasNoItems) ""
                        else chooser.current.name
                    }
                    is MultiDiceList -> ""
                    else -> throw Error("unknown chooser: $chooser")
                }
        
                card.setNoDoubleClickListener {
                    val pos = holder.adapterPosition
                    Log.d(TAG, "NormalPress")
                    if (actionMode != null){
                        itemInCABPressed(pos, it)
                    } else {
                        val c = values[pos]
                        
                        when (c) {
                            is OrderChooser<*> ->
                                recyclerView.findNavController()
                                        .navigate(ListOfChooserFragmentDirections.actionListOfChooserFragmentToDisplayOrderChooserFragment(c.id))
                            is PickChooser<*> ->
                                recyclerView.findNavController()
                                        .navigate(ListOfChooserFragmentDirections.actionListOfChooserFragmentToDisplayPickChooserFragment(c.id))
                            is MultiDiceList ->
                                recyclerView.findNavController()
                                        .navigate(ListOfChooserFragmentDirections.actionListOfChooserFragmentToDisplayMultiDiceListFragment(c.id))
                        }
                    }
                }
                card.setOnLongClickListener {
                    itemInCABPressed(holder.adapterPosition, it)
                    true
                }
                card.isSelected = position in selectedChoosersPos
            }
        }
    
        override fun getItemId(position: Int): Long = position.toLong()
    
        override fun getItemCount(): Int  = values.size
        
        fun deleteSelectedItems(positions: List<Int>){
            val choosers = values.filterIndexed { index, _ -> index in positions }
            //choosers.forEach { deleteChooserItemChooser(context, it.diceId) }
            choosers.forEach { gVm.deleteChooser(it) }
            values.removeAll(choosers)
            Snackbar.make(host.rootView,
                    context.resources.getQuantityString(R.plurals.deletedChooserMessage, choosers.size, choosers.size),
                    Snackbar.LENGTH_LONG)
                    .setAnchorView(host.mainFab)
                    .setAction(R.string.undo) {
                        choosers.forEach { gVm.insertChooser(it) }
                    }
                    .show()
            /*if (choosers.size == 1) notifyItemRemoved(positions.first())
            else notifyDataSetChanged()*/
        }
        
        /*fun deleteItem(pos: Int){
            if (pos < itemCount){
                deleteChooserItemChooser(context, values[pos].diceId)
                values.removeAt(pos)
                notifyDataSetChanged()
            }
        }*/
        
        fun editItem(pos: Int) {
            if (pos < itemCount){
                val chooser = values[pos]
                when (chooser) {
                    is ChooserItemChooser<*> -> recyclerView
                            .findNavController()
                            .navigate(ListOfChooserFragmentDirections
                                    .actionGlobalAddChooserItemChooserFragment(chooser.id))
                    is MultiDiceList -> recyclerView
                            .findNavController()
                            .navigate(ListOfChooserFragmentDirections
                                    .actionListOfChooserFragmentToDiceEditorFragment(chooser.id))
                }
            }
        }
        
        private var lastRestarted: List<OrderChooser<out ChooserItem>>? = null
        
        fun restartLists(positions: List<Int>){
            val restarted = mutableListOf<OrderChooser<out ChooserItem>>()
            positions.forEach {
                val chooser = values[it]
                if (chooser is OrderChooser<*>){
                    restarted.add(chooser.deepCopy())
                    
                    chooser.restart()
                    gVm.updateChooserAfterRestart(chooser)
                }
            }
            if (restarted.isNotEmpty()) {
                lastRestarted = restarted
                Snackbar.make(host.rootView,
                        context.getString(R.string.restartedChooserMessage),
                        Snackbar.LENGTH_LONG)
                        .setAnchorView(host.mainFab)
                        .setAction(R.string.undo) {
                            lastRestarted?.forEach { gVm.updateChooserAfterRestart(it) }
                        }
                        .show()
            }
        }
    }
    
    class ListOfChooserViewModel: ViewModel() {
        
        val cabActive: Ref.BooleanRef = Ref.BooleanRef().apply { element = false }
        val selectedChooserPositions: MutableList<Int> = mutableListOf()
    }
    
    companion object {
        private const val TAG = "ListOfChooserFragment"
    }
}
