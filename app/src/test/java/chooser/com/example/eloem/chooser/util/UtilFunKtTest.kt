package chooser.com.example.eloem.chooser.util

import org.junit.Assert.*
import org.junit.Test

class UtilFunKtTest {
    
    @Test
    fun testFilterAndRemove() {
        val list = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        val out = list.filterAndRemove { it % 2 == 0 }
        assertEquals(mutableListOf(2, 4, 6, 8), out)
        assertEquals(mutableListOf(1, 3, 5, 7, 9), list)
    }
}