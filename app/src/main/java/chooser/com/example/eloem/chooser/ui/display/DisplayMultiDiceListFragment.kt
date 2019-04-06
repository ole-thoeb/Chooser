package chooser.com.example.eloem.chooser.ui.display

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.chooser.MultiDiceList
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.ui.ChildFragment
import chooser.com.example.eloem.chooser.ui.GlobalViewModel
import chooser.com.example.eloem.chooser.util.activityViewModel
import com.example.eloem.dartCounter.recyclerview.BottomSpacingAdapter
import com.example.eloem.dartCounter.recyclerview.ContextAdapter
import com.thebluealliance.spectrum.internal.ColorUtil
import kotlinx.android.synthetic.main.fragment_display_multi_dice_list.*

class DisplayMultiDiceListFragment: ChildFragment() {
    
    private val arg: DisplayMultiDiceListFragmentArgs by navArgs()
    private val diceId: Int by lazy { arg.diceListId }
    private val globalViewModel: GlobalViewModel by activityViewModel()
    
    private lateinit var mAdapter: DisplayAdapter
    private var multiDiceList: MultiDiceList? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_display_multi_dice_list, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        mAdapter = DisplayAdapter(MultiDiceList(-1, "", listOf()))
        
        recyclerView.apply {
            adapter = BottomSpacingAdapter(mAdapter, resources.getDimensionPixelSize(R.dimen.paddingBottomRecyclerView))
            layoutManager = LinearLayoutManager(requireContext())
        }
        globalViewModel.getMultiDiceList(diceId).observe(viewLifecycleOwner, Observer {
            multiDiceList = it
            if (it != null) {
                mAdapter.mDiceList = it
                hostActivity.supportActionBar?.title = it.title
                mAdapter.notifyDataSetChanged()
            }
        })
        
        hostActivity.supportActionBar?.apply {
            setDisplayShowCustomEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        
        hostActivity.hideBottomSheet()
        hostActivity.mainFab.animateToIcon(AnimatedIconFab.Icon.NEXT)
        hostActivity.mainFab.setOnClickListener {
            multiDiceList?.let {
                it.next()
                
                
                globalViewModel.updateMultiDiceListAfterRole(it)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_display_pick_chooser, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.delete -> {
            multiDiceList?.let {
                globalViewModel.deleteDiceUiFeedback(it, hostActivity.rootView, hostActivity.mainFab)
            }
            findNavController().navigateUp()
            true
        }
        R.id.edit -> {
            findNavController()
                    .navigate(DisplayMultiDiceListFragmentDirections
                            .actionDisplayMultiDiceListFragmentToDiceEditorFragment(diceId))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    
    class DisplayAdapter(var mDiceList: MultiDiceList): ContextAdapter<DisplayAdapter.MultiDiceVH>() {
    
        class MultiDiceVH(layout: View): RecyclerView.ViewHolder(layout) {
            val root: ViewGroup = layout.findViewById(R.id.rootConstraint)
            val resultTV: TextView = layout.findViewById(R.id.resultTV)
            val sidesTV: TextView = layout.findViewById(R.id.sidesTV)
            val timesTV: TextView = layout.findViewById(R.id.timesTV)
        }
    
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiDiceVH {
            return MultiDiceVH(inflate(R.layout.item_display_dice_list_dice, parent))
        }
    
        override fun getItemCount(): Int = mDiceList.size
    
        override fun onBindViewHolder(holder: MultiDiceVH, position: Int) {
            val multiDice = mDiceList[position]
            val textColor = if (ColorUtil.isColorDark(multiDice.color)) Color.WHITE else Color.BLACK
            holder.apply {
                root.backgroundTintList = ColorStateList.valueOf(multiDice.color)
                resultTV.text = multiDice.sortedBy { it.current }.joinToString { it.current.toString() }
                sidesTV.text = context.getString(R.string.diceSides, multiDice.sides)
                timesTV.text = context.getString(R.string.diceTimes, multiDice.times)
                
                resultTV.setTextColor(textColor)
                sidesTV.setTextColor(textColor)
                timesTV.setTextColor(textColor)
            }
        }
    }
}