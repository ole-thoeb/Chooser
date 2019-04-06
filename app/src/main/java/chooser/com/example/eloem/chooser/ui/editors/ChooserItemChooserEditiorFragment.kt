package chooser.com.example.eloem.chooser.ui.editors

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chooser.com.example.eloem.chooser.ui.GlobalViewModel
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.chooser.*
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.helperClasses.WeightProgressDrawable
import chooser.com.example.eloem.chooser.recyclerview.EditListAdapter
import chooser.com.example.eloem.chooser.util.*
import com.example.eloem.dartCounter.recyclerview.BottomSpacingAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import emil.beothy.widget.BetterEditText
import kotlinx.android.synthetic.main.fragment_add_order_chooser.*
import java.util.*

open class ChooserItemChooserEditorFragment : EditorFragment() {
    
    private val arg: ChooserItemChooserEditorFragmentArgs by navArgs()
    private val chooserId: Int by lazy { arg.chooserId }
    private var currentType: String? = null
    private var currentFragment: AddChooserFragment? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.container, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        hostActivity.mainFab.setOnClickListener {
            if (currentType == OrderChooser.PARS_TYPE) {
                findNavController()
                        .navigate(ChooserItemChooserEditorFragmentDirections.actionChooserItemChooserEditorFragmentToDisplayOrderChooserFragment(chooserId, true))
            } else {
                findNavController()
                        .navigate(ChooserItemChooserEditorFragmentDirections.actionChooserItemChooserEditorFragmentToDisplayPickChooserFragment(chooserId, true))
            }
        }
        hostActivity.mainFab.animateToIcon(AnimatedIconFab.Icon.CHECK)
        
        var first = true
        globalViewModel.getChooserItemChooser(chooserId).observe(viewLifecycleOwner, Observer {
            if (it != null && first) {
                first = false
                switchFragment(it.parsType, false)
            }
        })
    }
    
    private fun getTypedFragmentWithId(type: String): AddChooserFragment {
        val fragment = when (type) {
            OrderChooser.PARS_TYPE -> AddOrderChooserFragment()
            PickChooser.PARS_TYPE -> AddPickChooserFragment()
            WeightedChooser.PARS_TYPE -> AddWeightedChooserFragment()
            else -> throw UnknownFormatFlagsException("Unknown Flag for chooser type: $type")
        }
        fragment.arguments = Bundle().apply {
            putInt(CHOOSER_ID_EXTRA, chooserId)
        }
        return fragment
    }
    
    private fun switchFragment(type: String, safe: Boolean = true){
        if (currentType != type) {
            if (safe) currentFragment?.saveChooser()
            getTypedFragmentWithId(type).also {
                currentFragment = it
                currentType = type
                childFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, it)
                        .commit()
            }
        }
    }
    
    private fun setIcon(type: String){
        modeOption.setIcon(when(type){
            OrderChooser.PARS_TYPE -> R.drawable.ic_order
            PickChooser.PARS_TYPE -> R.drawable.ic_pick
            WeightedChooser.PARS_TYPE -> R.drawable.ic_weighted
            else -> throw UnknownFormatFlagsException("Unknown Flag for chooser type: $type")
        })
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        currentType?.let { setIcon(it) }
    }
    
    private var deletionExit = false
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId){
        R.id.delete -> {
            val chooser = currentFragment?.finaliseChooser()
            //currentFragment?.gChooser = null
            deletionExit = true
            
            if (chooser != null) {
                globalViewModel.deleteChooserUiFeedBack(chooser, hostActivity.rootView)
                findNavController().popBackStack(R.id.listOfChooserFragment, false)
            }
        
            true
        }
        R.id.mode -> {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.chooseMode)
                    .setItems(R.array.modeArray){ _, which ->
                        val newType = when(which){
                            0 -> OrderChooser.PARS_TYPE
                            1 -> PickChooser.PARS_TYPE
                            2 -> WeightedChooser.PARS_TYPE
                            3 -> {
                                val chooser = currentFragment?.finaliseChooser()
                                deletionExit = true
    
                                if (chooser != null) {
                                    globalViewModel.transformChooserToMultiDiceList(chooser)
                                } else {
                                    globalViewModel.transformChooserToMultiDiceList(chooserId)
                                }
                                findNavController()
                                        .navigate(ChooserItemChooserEditorFragmentDirections
                                                .actionChooserItemChooserEditorFragmentToDiceEditorFragment(chooserId))
                                return@setItems
                            }
                            else -> throw Error("Unknown option for mode: $which")
                        }
                        switchFragment(newType)
                        setIcon(newType)
                    }
                    .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    override fun onPause() {
        super.onPause()
        view?.let {
            hideSoftKeyboard(requireContext(), it.windowToken)
        }
        if (!deletionExit) currentFragment?.saveChooser(true)
    }
    
    companion object {
        const val CHOOSER_ID_EXTRA = "extraChooserId"
        const val CHOOSER_TYPE_EXTRA = "extraChooserType"
        
        private const val TAG = "ChooserItemChooserEditorFragment"
    }
}

abstract class AddChooserFragment: Fragment(){
    
    val globalViewModel: GlobalViewModel by activityViewModel()
    val typedParentFragment: ChooserItemChooserEditorFragment by lazy { parentFragment as ChooserItemChooserEditorFragment }
    open var gChooser: ChooserItemChooser<out ChooserItem>? = null
    lateinit var recyclerAdapter: ChooserItemAdapter
    
    abstract fun typeChooser(chooser: ChooserItemChooser<out ChooserItem>): ChooserItemChooser<out ChooserItem>
    
    open fun getAdapter(values: MutableList<MutableWeightedChooserItem>): ChooserItemAdapter
            = ChooserItemAdapter(values)
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_add_order_chooser, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    
        recyclerAdapter = getAdapter(mutableListOf())
        //set data to UI
        list.apply {
            adapter = BottomSpacingAdapter(recyclerAdapter, resources.getDimensionPixelSize(R.dimen.paddingBottomRecyclerView) / 2)
            layoutManager = LinearLayoutManager(context)
        }
        
        val arg = arguments
        if (arg != null) {
            val chooserId = arg.getInt(ChooserItemChooserEditorFragment.CHOOSER_ID_EXTRA)
            globalViewModel.getChooserItemChooser(chooserId).observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    val typedChooser = typeChooser(it)
                    gChooser = typedChooser
                    typedParentFragment.chooserTitle = typedChooser.title
                    recyclerAdapter.values = typedChooser.items
                            .toMutableChooserItems()
                            .toMutableList()
                    recyclerAdapter.notifyDataSetChanged()
                }
            })
        }
    }
    
    open fun finaliseChooser(clean: Boolean = false): ChooserItemChooser<out ChooserItem>? {
        val chooser = gChooser ?: return null
        //update List object
    
        val title = typedParentFragment.chooserTitle
        
        val items = recyclerAdapter.values.run {
            if(clean) {
                val cleaned = filter { it.name != "" }
    
                //when nothing was filled in -> discard list
                if (cleaned.isEmpty() && title == "") {
                    globalViewModel.deleteChooserItemChooser(chooser.id)
                    //deleteChooserItemChooser(ctx, chooser.diceId)
                    return null
                }
                cleaned
            } else this
        }
    
        //write/update data to database
        /*updateChooserItemChooser(ctx,
                chooser.copy(pTitle = title, pItems = cleanedItems.toChooserItems().toMutableList()))*/
        return chooser.copy(pTitle = title, pItems = items.toWeightedChooserItem().toMutableList())
    }
    
    open fun saveChooser(clean: Boolean = false) {
        val finalisedChooser = finaliseChooser(clean)
        if (finalisedChooser != null) globalViewModel.updateChooserItemChooser(finalisedChooser)
    }
    
    open class ChooserItemAdapter(values: MutableList<MutableWeightedChooserItem>):
            EditListAdapter<MutableWeightedChooserItem>(values) {
        
        class EditViewHolder(layout: View): EditRowVH(layout){
            override val itemNameET: BetterEditText = layout.findViewById(R.id.itemName)
            override val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
        }
        
        class FootViewHolder(layout: View): RecyclerView.ViewHolder(layout){
            val root: ViewGroup = layout.findViewById(R.id.linLayout)
        }
        
        override fun writeEditContent(pos: Int, content: String) {
            values[pos].name = content
        }
        
        override fun readEditContent(pos: Int): String = values[pos].name
        
        override fun newItem(pos: Int, s: String): MutableWeightedChooserItem =
                MutableWeightedChooserItem(s, values.size)
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
                = when(viewType){
            VIEW_TYPE_EDIT_ROW -> EditViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.edit_item_row, parent, false)
            )
            1 -> FootViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.item_chooser_item_chooser_editor_add_item, parent, false)
            )
            else -> FootViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.item_chooser_item_chooser_editor_add_item, parent, false)
            )
        }
        
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == 1){
                val realHolder = holder as FootViewHolder
                with(realHolder){
                    root.setOnClickListener { addNewItem(values.size) }
                }
            }
            else super.onBindViewHolder(holder, position)
        }
        
        override fun getItemCount(): Int = values.size + 1
        
        override fun getItemViewType(position: Int): Int = when(position){
            values.size -> 1
            else -> VIEW_TYPE_EDIT_ROW
        }
    }
}

open class AddOrderChooserFragment: AddChooserFragment(){
    override fun typeChooser(chooser: ChooserItemChooser<out ChooserItem>): ChooserItemChooser<out ChooserItem> {
        return chooser.toOrderChooser()
    }
}

open class AddPickChooserFragment: AddChooserFragment(){
    override fun typeChooser(chooser: ChooserItemChooser<out ChooserItem>): ChooserItemChooser<out ChooserItem> {
        return chooser.toPickChooser()
    }
}

open class AddWeightedChooserFragment: AddChooserFragment(){
    
    override fun typeChooser(chooser: ChooserItemChooser<out ChooserItem>): ChooserItemChooser<WeightedChooserItem> {
        return chooser.toWeightedChooser()
    }
    
    override fun getAdapter(values: MutableList<MutableWeightedChooserItem>): ChooserItemAdapter {
        return WeightAdapter(values)
    }
    
    open class WeightAdapter(values: MutableList<MutableWeightedChooserItem>):
            ChooserItemAdapter(values) {
        
        private fun calculateMaxWeight() = values.maxBy { it.weight }?.weight ?: 1
        
        private val maxWeight: Int get() = calculateMaxWeight()
        
        private val holders: MutableSet<RecyclerView.ViewHolder> = mutableSetOf()
        
        class EditViewHolder(layout: View): EditRowVH(layout) {
            override val itemNameET: BetterEditText = layout.findViewById(R.id.itemName)
            override val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
            val weightButton: ImageButton = layout.findViewById(R.id.weightButton)
        }
        
        override fun onAttachedToRecyclerView(rV: RecyclerView) {
            super.onAttachedToRecyclerView(rV)
            onRemoveItemListener = { item, pos ->
                updateProgressPercent()
            }
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
                = when(viewType){
            VIEW_TYPE_EDIT_ROW -> EditViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.edit_weighted_item_row, parent, false)
            )
            1 -> FootViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.item_chooser_item_chooser_editor_add_item, parent, false)
            )
            else -> FootViewHolder(
                    LayoutInflater
                            .from(context)
                            .inflate(R.layout.item_chooser_item_chooser_editor_add_item, parent, false)
            )
        }
        
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holders.add(holder)
            when(holder.itemViewType) {
                1 -> {
                    (holder as FootViewHolder).apply{
                        root.setOnClickListener { addNewItem(values.size) }
                    }
                }
                VIEW_TYPE_EDIT_ROW -> {
                    holder as EditViewHolder
                    
                    val animDraw = WeightProgressDrawable(
                            context.getAttribute(R.attr.colorAccent).data,
                            context.getAttribute(R.attr.colorOnBackground).data)
                            .apply {
                                
                                progressPercent = (values[position].weight / maxWeight.toFloat()) * 100
                            }
                    
                    holder.weightButton.setImageDrawable(animDraw)
                    holder.weightButton.setOnClickListener {
                        val pos = holder.adapterPosition
                        
                        showEditDialog(context,
                                context.getString(R.string.hintSetWeight),
                                values[pos].weight.toString(),
                                intNotGreaterZero,
                                context.getString(R.string.messageWeightGreaterZero),
                                { text, _, _ ->
                                    val newWeight = text.toInt()
                                    values[pos].weight = newWeight
            
                                    updateProgressPercent()
                                })
                        //custView.weightET.focusAndShowKeyboard()
                    }
                }
            }
            super.onBindViewHolder(holder, position)
        }
    
        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            super.onViewRecycled(holder)
            holders.remove(holder)
        }
        
        private fun updateProgressPercent() {
            val maxWeightLocal = maxWeight.toFloat()
            holders.forEach {
                val pos = it.adapterPosition
                if (pos in values.indices) {
                    if (it.itemViewType == VIEW_TYPE_EDIT_ROW) {
                        it as EditViewHolder
                        (it.weightButton.drawable as WeightProgressDrawable).progressPercent =
                                (values[pos].weight / maxWeightLocal) * 100
                    }
                }
            }
        }
    }
}

private const val TAG = "AddListActivity"
