package chooser.com.example.eloem.chooser.chooser

import android.util.Log
import chooser.com.example.eloem.chooser.util.randomInt

sealed class ChooserItemChooser<T: ChooserItem>(
        override val id: Int,
        override val title: String,
        override val items: MutableList<T>,
        cPos: Int = 0
): Chooser<ChooserItem>, ItemRandomizer<ChooserItem> {
    
    override var currentPos: Int = cPos
        get() = if (field in items.indices) {
            field
        } else {
            field = items.lastIndex
            field
        }
    
    abstract fun copy(pId: Int = id,
                      pTitle: String = title,
                      pItems: MutableList<T> = items,
                      pCurrentPos: Int = currentPos): ChooserItemChooser<T>
    
    companion object {
        val EMPTY: ChooserItemChooser<ChooserItem> get() = PickChooser(-1, "", mutableListOf())
    }
}

fun ChooserItemChooser<out ChooserItem>.copy(
        pId: Int = id,
        pTitle: String = title,
        pItems: MutableList<out ChooserItem> = items,
        pCurrentPos: Int = currentPos
): ChooserItemChooser<out ChooserItem> {
    
    return when (this) {
        is OrderChooser<*> -> OrderChooser(pId, pTitle, pItems.toWeightedChooserItems().toMutableList(), pCurrentPos)
        is WeightedChooser<*> -> WeightedChooser(pId, pTitle, pItems.toWeightedChooserItems().toMutableList(), pCurrentPos)
        is PickChooser<*> -> PickChooser(pId, pTitle, pItems.toWeightedChooserItems().toMutableList(), pCurrentPos)
    }
}


fun ChooserItemChooser<*>.parsType(): String = when(this){
    is WeightedChooser<*> -> WeightedChooser.PARS_TYPE
    is OrderChooser<*> -> OrderChooser.PARS_TYPE
    is PickChooser<*> -> PickChooser.PARS_TYPE
}

val ChooserItemChooser<*>.parsType: String get() = parsType()

open class PickChooser<T: ChooserItem>(id: Int,
                                       title: String,
                                       items: MutableList<T>,
                                       currentPos: Int = 0):
        ChooserItemChooser<T>(id, title, items, currentPos){
    
    override fun nextItem(): ChooserItem {
        currentPos = randomInt(0 until items.size)
        return currentItem
    }
    
    override val hasNoItems: Boolean get() = items.isEmpty()
    
    override val currentItem: ChooserItem
        get() =
            if (hasNoItems) NO_CHOOSER_ITEM
            else items[currentPos]
    
    override val current: ChooserItem get() = currentItem
    
    override fun next(): ChooserItem = nextItem()
    
    override fun copy(pId: Int, pTitle: String, pItems: MutableList<T>, pCurrentPos: Int): PickChooser<T> {
        return PickChooser(pId, pTitle, pItems, pCurrentPos)
    }
    
    companion object {
        const val PARS_TYPE = "PickChooser"
    }
}

open class OrderChooser<T: ChooserItem>(id: Int,
                                        title: String,
                                        items: MutableList<T>,
                                        currentPos: Int = 0):
        PickChooser<T>(id, title, items, currentPos), Depletable<T> {
    
    override val hasNextItem: Boolean get() =  currentPos < items.size - 1
    
    override fun nextItem(): ChooserItem {
        if (currentPos < items.lastIndex) currentPos++
        return items[currentPos]
    }
    
    override fun restart(){
        currentPos = 0
        items.shuffle()
    }
    
    override fun copy(pId: Int, pTitle: String, pItems: MutableList<T>, pCurrentPos: Int): OrderChooser<T> {
        return OrderChooser(pId, pTitle, pItems, pCurrentPos)
    }
    
    fun deepCopy() = copy(pItems = items.toList().toMutableList())
    
    companion object {
        const val PARS_TYPE = "OrderChooser"
    }
}

open class WeightedChooser<T: WeightedChooserItem>(id: Int, title: String, items: MutableList<T>, curPos: Int = 0):
        PickChooser<T>(id, title, items, curPos) {
    
    override fun nextItem(): WeightedChooserItem {
        val weightSum = items.sumBy { it.weight }
        var weightedPos = randomInt(1..weightSum)
        Log.d("WeightedChooser", "random weighted position = $weightedPos")
        items.forEachIndexed { index, item ->
            weightedPos -= item.weight
            if (weightedPos <= 0) {
                currentPos = index
                return currentItem
            }
        }
        
        throw Error("could not determine nextItem Item")
    }
    
    override val currentItem: WeightedChooserItem
        get() =
            if (hasNoItems) NO_CHOOSER_ITEM_WEIGHTED
            else items[currentPos]
    
    
    override fun copy(pId: Int, pTitle: String, pItems: MutableList<T>, pCurrentPos: Int): WeightedChooser<T> {
        return WeightedChooser(pId, pTitle, pItems, pCurrentPos)
    }
    
    companion object {
        const val PARS_TYPE = "WeightedChooser"
    }
}