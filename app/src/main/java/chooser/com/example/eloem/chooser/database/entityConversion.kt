package chooser.com.example.eloem.chooser.database

import chooser.com.example.eloem.chooser.chooser.ChooserItem
import chooser.com.example.eloem.chooser.chooser.ChooserItemChooser
import chooser.com.example.eloem.chooser.chooser.parsType
import chooser.com.example.eloem.chooser.chooser.weight
import chooser.com.example.eloem.chooser.chooser.WeightedChooserItem
import chooser.com.example.eloem.chooser.chooser.OrderChooser
import chooser.com.example.eloem.chooser.chooser.PickChooser
import chooser.com.example.eloem.chooser.chooser.WeightedChooser
import java.util.*

fun ChooserItemChooser<out ChooserItem>.getSqlItems(): List<ChooserItemSql> {
    return items.mapIndexed { index, item ->
        ChooserItemSql(item.name, id, index, item.originalPos, item.weight)
    }
}

fun ChooserItemChooser<out ChooserItem>.getSqlChooser(): ChooserItemChooserSql {
    return ChooserItemChooserSql(id, title, currentPos, parsType)
}

fun ChooserWithChooserItems.toStandardChooserItemChooser(): ChooserItemChooser<out ChooserItem> {
    val items = items!!
            .sortedBy { it.position }
            .map { WeightedChooserItem(it.name, it.originalPosition, it.weight) }
            .toMutableList()
    
    val c = chooser
    return when(c?.type) {
        OrderChooser.PARS_TYPE -> OrderChooser(c.id,
                c.name,
                items,
                c.currentPos)
        PickChooser.PARS_TYPE -> PickChooser(c.id,
                c.name,
                items,
                c.currentPos)
        WeightedChooser.PARS_TYPE -> WeightedChooser(c.id,
                c.name,
                items,
                c.currentPos)
        else -> throw UnknownFormatFlagsException("Unknown type string: $chooser.type")
    }
}

fun ChooserItemChooser<out ChooserItem>.toSqlType(): ChooserItemChooserSql {
    return ChooserItemChooserSql(id, title, currentPos, parsType)
}

fun ChooserItemChooser<out ChooserItem>.itemsToSqlType(): List<ChooserItemSql> {
    return items.mapIndexed { index, item ->
        ChooserItemSql(item.name, id, index, item.originalPos, item.weight)
    }
}