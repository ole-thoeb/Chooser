package chooser.com.example.eloem.chooser.database

import android.util.Log
import chooser.com.example.eloem.chooser.chooser.*
import java.lang.Exception
import java.util.*

fun ChooserWithChooserItems.toStandardChooserItemChooser(): ChooserItemChooser<out ChooserItem> {
    val items = items!!
            .sortedBy { it.position }
            .map { WeightedChooserItem(it.name, it.originalPosition, it.weight) }
            .toMutableList()
    
    val c = chooser!!
    return ChooserItemChooser(c.id, c.name, items, c.currentPos, c.type)
}

fun ChooserItemChooser<out ChooserItem>.toSqlType(): ChooserItemChooserSql {
    return ChooserItemChooserSql(id, title, currentPos, parsType)
}

fun ChooserItemChooser<out ChooserItem>.itemsToSqlType(): List<ChooserItemSql> {
    return items.mapIndexed { index, item ->
        ChooserItemSql(item.name, id, index, item.originalPos, item.weight)
    }
}

/*fun MultiDiceListWithDice.toStandardMultiDiceList(): MultiDiceList {
    return MultiDiceList(multiDiceList!!.id,
            multiDiceList!!.title,
            dices!!.map { it.toStandardMultiDice() })
}*/

fun MultiDiceWithCurrent.toStandardMultiDice(): MultiDice {
    val mDice = multiDice!!
    
    return try {
        MultiDice(mDice.diceId,
                mDice.sides,
                mDice.times,
                mDice.color,
                current!!.map { cur -> cur.value })
    } catch (e: Exception) {
        Log.e("Conversion", e.localizedMessage)
        MultiDice(mDice.diceId,
                mDice.sides,
                mDice.times,
                mDice.color,
                List(mDice.times) { 1 })
    }
}

fun MultiDiceList.toSqlType(): MultiDiceListSql {
    return MultiDiceListSql(id, title)
}

fun MultiDice.toSqlType(listId: Int): MultiDiceSql {
    return MultiDiceSql(id, listId, sides, times, color)
}

fun MultiDice.currentToSqlType(): List<MultiDiceCurrentSql> {
    return current.map { MultiDiceCurrentSql(id, it) }
}