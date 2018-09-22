package chooser.com.example.eloem.chooser.helperClasses

import android.os.Parcelable
import chooser.com.example.eloem.chooser.util.randomInt
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class ListObj(val id: Int, var title: String, var items: @RawValue Array<Item>, var currentPos: Int = 0, var mode: Int): Parcelable {
    val currentItem get() = if (currentPos < items.size) items[currentPos]
                                else Item("", -1)
    
    val hasNoItems get() = items.isEmpty()
    
    fun restart(){
        when (mode) {
            MODE_RANDOM_ORDER -> {
                currentPos = 0
                items = items.toList().shuffled().toTypedArray()
                
            }
            MODE_SINGLE_PICK ->currentPos = randomInt(0, items.size)
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
    data class Item(var name: String, val id: Int):  Parcelable
    
    companion object {
        const val MODE_RANDOM_ORDER = 1
        const val MODE_SINGLE_PICK = 2
    }
}