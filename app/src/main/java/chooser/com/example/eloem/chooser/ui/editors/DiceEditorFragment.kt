package chooser.com.example.eloem.chooser.ui.editors


import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.chooser.*
import chooser.com.example.eloem.chooser.helperClasses.AnimatedIconFab
import chooser.com.example.eloem.chooser.util.hideSoftKeyboard
import com.example.eloem.dartCounter.recyclerview.BottomSpacingAdapter
import com.example.eloem.dartCounter.recyclerview.ContextAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.thebluealliance.spectrum.SpectrumDialog
import com.thebluealliance.spectrum.internal.ColorUtil
import kotlinx.android.synthetic.main.fragment_dice_editor.*

/**
 * A simple [Fragment] subclass.
 *
 */
class DiceEditorFragment : EditorFragment() {
    
    private val args: DiceEditorFragmentArgs by navArgs()
    private val diceId: Int by lazy { args.diceListId }
    lateinit var editorAdapter: DiceEditorAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dice_editor, container, false)
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        editorAdapter = DiceEditorAdapter(mutableListOf(), childFragmentManager)
        list.apply {
            adapter = BottomSpacingAdapter(editorAdapter, resources.getDimensionPixelSize(R.dimen.paddingBottomRecyclerView))
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        var first = true
        globalViewModel.getMultiDiceList(diceId).observe(viewLifecycleOwner, Observer {
            if (it != null && first) {
                first = false
                editorAdapter.values = it.map { dice -> dice.toMutableMultiDice() }.toMutableList()
                chooserTitle = it.title
            }
        })
        
        hostActivity.mainFab.setOnClickListener {
            findNavController()
                    .navigate(DiceEditorFragmentDirections
                            .actionDiceEditorFragmentToDisplayMultiDiceListFragment(diceId))
        }
        hostActivity.mainFab.animateToIcon(AnimatedIconFab.Icon.CHECK)
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        modeOption.icon = requireContext().getDrawable(R.drawable.ic_dice)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.delete -> {
            finalise()?.let {
                globalViewModel.deleteDiceUiFeedback(it, hostActivity.rootView, hostActivity.mainFab)
            }
            findNavController().navigateUp()
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
                            3 -> return@setItems
                            else -> throw Error("Unknown option for mode: $which")
                        }
                        val diceList = finalise()
                        if (diceList != null) {
                            globalViewModel.transformMultiDiceListToChooser(diceList, newType)
                        } else {
                            globalViewModel.transformMultiDiceListToChooser(diceId, newType)
                        }
                        findNavController()
                                .navigate(DiceEditorFragmentDirections
                                        .actionDiceEditorFragmentToChooserItemChooserEditorFragment(diceId))
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
        finalise(clean = true)?.let {
            globalViewModel.updateMultiDiceList(it)
        }
    }
    
    private fun finalise(clean: Boolean = false): MultiDiceList? {
        val dices = editorAdapter.values
        val title = chooserTitle
        if (clean && dices.isEmpty() && title == "") {
            globalViewModel.deleteMultiDiceList(MultiDiceList(diceId, title, mutableListOf()))
            return null
        }
        return MultiDiceList(diceId, chooserTitle, dices.map { it.toMultiDice(requireContext()) })
    }
    
    class DiceEditorAdapter(var values: MutableList<MutableMultiDice>, val fm: FragmentManager): ContextAdapter<RecyclerView.ViewHolder>() {
        
        class AddDiceVH(layout: View): RecyclerView.ViewHolder(layout) {
            val root: ViewGroup = layout.findViewById(R.id.linLayout)
        }
        
        class EditDiceVH(layout: View): RecyclerView.ViewHolder(layout) {
            val timesTV: TextView = layout.findViewById(R.id.timesTV)
            val sidesTV: TextView = layout.findViewById(R.id.sidesTV)
            val colorButton: ImageButton = layout.findViewById(R.id.colorButton)
            val deleteButton: ImageButton = layout.findViewById(R.id.deleteButton)
        }
    
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when(viewType) {
            1 -> EditDiceVH(inflate(R.layout.edit_multi_dice_row, parent))
            2 -> AddDiceVH(inflate(R.layout.item_dice_editor_add_item, parent))
            else -> throw Error("Unknown view type: $viewType")
        }
    
        override fun getItemViewType(position: Int): Int = when(position) {
            values.size -> 2
            else -> 1
        }
    
        override fun getItemCount(): Int = values.size + 1
    
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder.itemViewType) {
                1 -> {
                    val dice = values[position]
                    holder as EditDiceVH
                    holder.timesTV.apply {
                        text = context.getString(R.string.diceTimes, dice.times)
                        setOnClickListener {
                            val pos = holder.adapterPosition
    
                            showEditDialog(context,
                                    context.getString(R.string.hintSetTimes),
                                    values[pos].times.toString(),
                                    intNotGreaterZero,
                                    context.getString(R.string.messageTimesGreaterZero),
                                    { text, _, _ ->
                                        values[pos].times = text.toInt()
                                        notifyItemChanged(pos)
                                    },
                                    hideSoftInput = true)
                        }
                    }
                    holder.sidesTV.apply {
                        text = context.getString(R.string.diceSides, dice.sides)
                        setOnClickListener {
                            val pos = holder.adapterPosition
        
                            showEditDialog(context,
                                    context.getString(R.string.hintSetSides),
                                    values[pos].sides.toString(),
                                    intNotGreaterZero,
                                    context.getString(R.string.messageSidesGreaterZero),
                                    { text, _, _ ->
                                        values[pos].sides = text.toInt()
                                        notifyItemChanged(pos)
                                    },
                                    hideSoftInput = true)
                        }
                    }
                    holder.colorButton.setOnClickListener {
                        SpectrumDialog.Builder(context)
                                .setTitle(R.string.chooseColor)
                                .setSelectedColor(values[holder.adapterPosition].color)
                                .setColors(R.array.colorArray)
                                .setDismissOnColorSelected(true)
                                .setOnColorSelectedListener { positiveResult, color ->
                                    val pos = holder.adapterPosition
                                    values[pos].color = color
                                    setColors(holder.colorButton, color)
                                    notifyItemChanged(pos)
                                }
                                .build()
                                .show(fm, "dialog")
                    }
                    setColors(holder.colorButton, dice.color)
                    holder.deleteButton.setOnClickListener {
                        val pos = holder.adapterPosition
                        values.removeAt(pos)
                        notifyItemRemoved(pos)
                    }
                }
                2 -> {
                    holder as AddDiceVH
                    holder.root.setOnClickListener {
                        values.add(newDice())
                        notifyItemInserted(values.lastIndex)
                    }
                }
            }
        }
        
        private fun setColors(button: ImageButton, @ColorInt color: Int) {
            button.backgroundTintList = ColorStateList.valueOf(color)
            button.imageTintList = ColorStateList.valueOf(if (ColorUtil.isColorDark(color)) Color.WHITE else Color.BLACK)
        }
        
        private fun newDice() = MutableMultiDice(MutableMultiDice.NEW_ID, 6, 1, Color.WHITE)
    }
}
