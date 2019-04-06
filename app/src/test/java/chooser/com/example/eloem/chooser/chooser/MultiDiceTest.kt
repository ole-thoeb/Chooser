package chooser.com.example.eloem.chooser.chooser

import org.junit.Assert.*
import org.junit.Test

class MultiDiceTest {
    
    @Test
    fun currentRequireTest() {
        val dice = MultiDice(1, 6, 1, 0xFF000000.toInt(), listOf(7))
    }
}