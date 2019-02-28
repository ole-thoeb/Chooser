package chooser.com.example.eloem.chooser.helperClasses

import android.os.Parcelable
import chooser.com.example.eloem.chooser.util.randomInt
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.jetbrains.anko.db.SqlType
import java.lang.Error

/*@Parcelize
data class ListObj(val id: Int, var title: String, var items: @RawValue Array<Item>, var currentPos: Int = 0, var mode: Int): Parcelable {
    val currentItem get() = if (currentPos < items.size) items[currentPos]
                                else Item("", -1)
    
    val hasNoItems get() = items.isEmpty()
    
    val hasNext: Boolean get() = when(mode) {
        MODE_RANDOM_ORDER -> currentPos + 1 < items.size
        MODE_SINGLE_PICK -> true
        else -> false
    }
    
    fun restart(){
        when (mode) {
            MODE_RANDOM_ORDER -> {
                currentPos = 0
                items = items.toList().shuffled().toTypedArray()
            
            }
            MODE_SINGLE_PICK -> currentPos = randomInt(0, items.size)
        }
    }
    
    
    fun nextItem(): Boolean = when(mode){
        MODE_RANDOM_ORDER ->  {
            currentPos ++
            if (currentPos < items.size){
                 true
            }else{
                currentPos --
                false
            }
        }
        MODE_SINGLE_PICK ->{
            currentPos = randomInt(0, items.size)
            true
        }
        else -> false
    }
    
    
    @Parcelize
    data class Item(var name: String, val id: Int): Parcelable
    
    companion object {
        const val MODE_RANDOM_ORDER = 1
        const val MODE_SINGLE_PICK = 2
    }
}*/
interface ChooserObj: Parcelable {
    val id: Int
    var title: String
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

@Parcelize
open class OrderChooser<T: ChooserItem>(override val id: Int,
                                        override var title: String,
                                        val items: @RawValue MutableList<T>,
                                        var currentPos: Int = 0): ChooserObj {
    
    override val hasNextItem: Boolean get() =  currentPos < items.size - 1
    
    override fun nextItem(): ChooserItem = items[currentPos++]
    
    override val hasNoItems: Boolean get() = items.isEmpty()
    
    override val currentItem: ChooserItem get() = items[currentPos]
    
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

open class WeightedChooserItem(name: String, originalPos: Int, val weight: Int): ChooserItem(name, originalPos) {
    init {
        require(weight > 0) { "weight must be > 0 but is $weight" }
    }
}

open class WeightedChooser<T: WeightedChooserItem>(id: Int, title: String, items: MutableList<T>, curPos: Int = 0):
        PickChooser<T>(id, title, items, curPos) {
    
    override fun nextItem(): ChooserItem {
        val weightSum = items.sumBy { it.weight }
        var weightedPos = randomInt(0 until weightSum)
        items.forEachIndexed { index, item ->
            weightedPos -= item.weight
            if (weightedPos <= 0){
                currentPos = index
                return currentItem
            }
        }
        
        throw Error("could not determine next Item")
    }
    
    companion object {
        const val PARS_TYPE = "WeightedChooser"
    }
}