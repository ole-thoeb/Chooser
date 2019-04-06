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
    var currentPos: Int
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


/*open class DicesChooser(override val diceId: Int,
                        override var title: String,
                        private val items: List<Dice>): ListChooser<Int, Dice>, List<Dice> by items {
    
    override val current: List<Int> = map { it.current }
    
    override fun next(): List<Int> {
        forEach { it.next() }
        return current
    }
}*/