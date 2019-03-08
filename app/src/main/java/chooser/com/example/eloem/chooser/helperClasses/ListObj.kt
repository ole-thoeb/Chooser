package chooser.com.example.eloem.chooser.helperClasses

import android.os.Parcelable
import android.util.Log
import chooser.com.example.eloem.chooser.util.randomInt
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.lang.Error
import java.util.*

interface ChooserObj: Parcelable {
    val id: Int
    var title: String
    val items: Collection<ChooserItem>
    val hasNextItem: Boolean
    fun nextItem(): ChooserItem
    val currentItem: ChooserItem
    val hasNoItems: Boolean
}

@Parcelize
open class ChooserItem(var name: String, var originalPos: Int): Parcelable {
    init {
        require(originalPos >= 0) { "originalPos must be > 0 but is $originalPos" }
    }
}
private const val NO_ITEM_NAME = "No Item #ERROR"
private object NO_CHOOSER_ITEM: ChooserItem(NO_ITEM_NAME, 0)

@Parcelize
open class OrderChooser<T: ChooserItem>(override val id: Int,
                                        override var title: String,
                                        override val items: @RawValue MutableList<T>,
                                        var currentPos: Int = 0): ChooserObj {
    
    override val hasNextItem: Boolean get() =  currentPos < items.size - 1
    
    override fun nextItem(): ChooserItem = items[currentPos++]
    
    override val hasNoItems: Boolean get() = items.isEmpty()
    
    override val currentItem: ChooserItem get() =
        if (hasNoItems) NO_CHOOSER_ITEM
        else items[currentPos]
    
    open fun restart(){
        currentPos = 0
        items.shuffle()
    }
    
    companion object {
        const val PARS_TYPE = "OrderChooser"
    }
}

open class PickChooser<T: ChooserItem>(id: Int, title: String, items: MutableList<T>, curPos: Int = 0):
        OrderChooser<T>(id, title, items, curPos) {
    
    override val hasNextItem: Boolean get() = !hasNoItems
    
    override fun nextItem(): ChooserItem {
        currentPos = randomInt(0 until items.size)
        return currentItem
    }
    
    override fun restart() {
        //nothing
    }
    
    companion object {
        const val PARS_TYPE = "PickChooser"
    }
}

open class WeightedChooserItem(name: String, originalPos: Int, val weight: Int = 1): ChooserItem(name, originalPos) {
    init {
        require(weight > 0) { "weight must be > 0 but is $weight" }
    }
}

fun ChooserItem.toWeightedChooserItem(): WeightedChooserItem =
        if (this is WeightedChooserItem) this
        else WeightedChooserItem(name, originalPos)

private object NO_CHOOSER_ITEM_WEIGHTED: WeightedChooserItem(NO_ITEM_NAME, 0, 1)

open class WeightedChooser<T: WeightedChooserItem>(id: Int, title: String, items: MutableList<T>, curPos: Int = 0):
        PickChooser<T>(id, title, items, curPos) {
    
    override fun nextItem(): WeightedChooserItem {
        val weightSum = items.sumBy { it.weight }
        var weightedPos = randomInt(1..weightSum)
        Log.d("WeightedChooser", "random weighted position = $weightedPos")
        items.forEachIndexed { index, item ->
            weightedPos -= item.weight
            if (weightedPos <= 0){
                currentPos = index
                return currentItem
            }
        }
        
        throw Error("could not determine next Item")
    }
    
    override val currentItem: WeightedChooserItem get() =
        if (hasNoItems) NO_CHOOSER_ITEM_WEIGHTED
        else items[currentPos]
    
    companion object {
        const val PARS_TYPE = "WeightedChooser"
    }
}

/*fun <T: ChooserItem> OrderChooser<T>.toPickChooser(): PickChooser<T>{
    return PickChooser(id, title, items, currentPos)
}

fun <T: ChooserItem> PickChooser<T>.toOrderChooser(): OrderChooser<T>{
    return OrderChooser(id, title, items, currentPos)
}*/

fun ChooserObj.toOrderChooser(): OrderChooser<ChooserItem>{
    this as OrderChooser<ChooserItem>
    return OrderChooser(id, title, items, currentPos)
}

fun ChooserObj.toPickChooser(): PickChooser<ChooserItem>{
    this as OrderChooser<ChooserItem>
    return PickChooser(id, title, items, currentPos)
}

fun ChooserObj.toWeightedChooser(): WeightedChooser<WeightedChooserItem>{
    this as OrderChooser<ChooserItem>
    return WeightedChooser(id, title, items.map { it.toWeightedChooserItem() }.toMutableList(), currentPos)
}

fun ChooserObj.parsType(): String = when(this){
    is WeightedChooser<*> -> WeightedChooser.PARS_TYPE
    is PickChooser<*> -> PickChooser.PARS_TYPE
    is OrderChooser<*> -> OrderChooser.PARS_TYPE
    else -> throw TypeCastException("Can't determine type of Chooser: $this")
}

private data class ConverterChooserItem(val name: String, val orgPos: Int, val randomPos: Int)

data class MutableChooserItem(var name: String, var randomPos: Int)

private data class ConverterWeightedChooserItem(val name: String, val orgPos: Int, val randomPos: Int, val weight: Int)

data class MutableWeightedChooserItem(var name: String, var randomPos: Int, var weight: Int)

fun List<ChooserItem>.toMutableChooserItems(): List<MutableChooserItem> {
    return mapIndexed { index, item ->
        ConverterChooserItem(item.name, item.originalPos, index)
    }.sortedBy { it.orgPos }
            .map { MutableChooserItem(it.name, it.randomPos) }
}

fun List<WeightedChooserItem>.toMutableWeightedChooserItems(): List<MutableWeightedChooserItem> {
    return mapIndexed { index, item ->
        ConverterWeightedChooserItem(item.name, item.originalPos, index, item.weight)
    }.sortedBy { it.orgPos }
            .map { MutableWeightedChooserItem(it.name, it.randomPos, it.weight) }
}

fun List<MutableChooserItem>.toChooserItems(): List<ChooserItem> {
    return mapIndexed { index, item -> ConverterChooserItem(item.name, index, item.randomPos) }
            .sortedBy { it.randomPos }
            .map { ChooserItem(it.name, it.orgPos) }
}

fun List<MutableWeightedChooserItem>.toWeightedChooserItem(): List<WeightedChooserItem> {
    return mapIndexed { index, item ->
        ConverterWeightedChooserItem(item.name, index, item.randomPos, item.weight)
    }.sortedBy { it.randomPos }
            .map { WeightedChooserItem(it.name, it.orgPos, it.weight) }
}