package chooser.com.example.eloem.chooser.chooser

import android.content.Context
import chooser.com.example.eloem.chooser.util.newMultiDiceId
import chooser.com.example.eloem.chooser.util.randomInt


open class Dice(
        override val id: Int,
        override val title: String,
        val sides: Int,
        cur: Int = randomInt(1..sides)
): NumberRangeChooser {
    
    
    override var current: Int = cur
        set(value) {
            require(value in 1..sides) { "current must be between 1 and $sides but is $value" }
            field = value
        }
    
    override fun next(): Int {
        current = randomInt(range)
        return current
    }
    
    override val range: IntRange = 1..sides
}

open class MultiDice(
        override val id: Int,
        val sides: Int,
        val times: Int,
        val color: Int,
        cur: List<Int> = List(times) { randomInt(1..sides) }
): Chooser<List<Int>>, List<Dice> {
    
    init {
        require(times > 0) { "times must be > 0 but is $times" }
        checkCurrent(cur)
    }
    
    override var current = cur
        set(value) {
            checkCurrent(value)
            field = value
        }
    
    private fun checkCurrent(value: List<Int>) {
        require(value.size == times) {
            "value must have same size as times: size ${value.size}, times $times"
        }
        value.forEach { require(it in 1..sides) {
            "all values in current must be between 1 and $sides but value $it was found"
        } }
    }
    
    override val title: String = "NO_TITLE"
    
    override fun next(): List<Int> {
        current = List(times) { randomInt(1..sides) }
        return current
    }
    
    override val size: Int = sides
    
    override operator fun contains(element: Dice): Boolean = element is ShadowDice && element.current in current
    
    override fun containsAll(elements: Collection<Dice>): Boolean = elements.all { contains(it) }
    
    override operator fun get(index: Int): Dice = ShadowDice(current[index])
    
    override fun indexOf(element: Dice): Int {
        if (element !is ShadowDice || element.sides != sides) return -1
        return indexOfFirst { element.current == it.current }
    }
    
    override fun isEmpty(): Boolean = false
    
    private val diceList get() = List(times) { ShadowDice(current[it]) }
    
    override fun iterator(): Iterator<Dice> = diceList.iterator()
    
    override fun lastIndexOf(element: Dice): Int {
        if (element !is ShadowDice || element.sides != sides) return -1
        return indexOfLast { element.current == it.current }
    }
    
    override fun listIterator(): ListIterator<Dice> = diceList.listIterator()
    
    override fun listIterator(index: Int): ListIterator<Dice> = diceList.listIterator(index)
    
    override fun subList(fromIndex: Int, toIndex: Int): List<Dice> = diceList.subList(fromIndex, toIndex)
    
    private inner class ShadowDice(current: Int): Dice(-1, "Shadow Dice", sides, current)
}

open class MultiDiceList(
        override val id: Int,
        override val title: String,
        list: List<MultiDice>
): List<MultiDice> by list, Chooser<List<List<Int>>> {
    
    override val current: List<List<Int>>
        get() = map { it.current }
    
    override fun next(): List<List<Int>> {
        return map { it.next() }
    }
}

data class MutableMultiDice(
        val id: Int,
        var sides: Int,
        var times: Int,
        var color: Int
) {
    companion object {
        const val NEW_ID = -123
    }
}

fun MultiDice.toMutableMultiDice() = MutableMultiDice(id, sides, times, color)

fun MutableMultiDice.toMultiDice(context: Context)
        = MultiDice(if (id == MutableMultiDice.NEW_ID) newMultiDiceId(context) else id , sides, times, color)