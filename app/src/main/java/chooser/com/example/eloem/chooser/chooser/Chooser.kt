package chooser.com.example.eloem.chooser.chooser

import chooser.com.example.eloem.chooser.util.randomInt

interface Chooser<T> {
    val id: Int
    val title: String
    val current: T
    fun next(): T
}

interface ItemRandomizer<T> {
    val id: Int
    val title: String
    val items: List<T>
    fun nextItem(): T
    val currentItem: T
    val currentPos: Int
    val hasNoItems: Boolean
    
    //fun copy(pId: Int, pTitle: String, pItems: List<T>, pCurrentPos: Int): ItemRandomizer<T>
}

interface Depletable<T> {
    val hasNextItem: Boolean
    fun restart()
}

interface NumberRangeChooser: Chooser<Int> {
    override val id: Int
    override val title: String
    override var current: Int
    override fun next(): Int
    val range: IntRange
}

open class Dice(override val id: Int,
                override val title: String,
                val sides: Int,
                override var current: Int = randomInt(1..sides)): NumberRangeChooser {
    
    override fun next(): Int {
        current = randomInt(range)
        return current
    }
    
    override val range: IntRange = 1..sides
}

interface ListChooser<T, C: Chooser<T>>: Chooser<List<T>>, List<C> {
    override val id: Int
    override var title: String
    
    override val current: List<T>
    override fun next(): List<T>
}

open class DicesChooser(override val id: Int,
                        override var title: String,
                        private val items: List<Dice>): ListChooser<Int, Dice>, List<Dice> by items {
    
    override val current: List<Int> = map { it.current }
    
    override fun next(): List<Int> {
        forEach { it.next() }
        return current
    }
}