@file:Suppress("UNCHECKED_CAST")

package chooser.com.example.eloem.chooser.chooser

import java.util.*

fun ChooserItemChooser<*>.toOrderChooser(): OrderChooser<ChooserItem> {
    return OrderChooser(id, title, items as MutableList<ChooserItem>, currentPos)
}

/*fun Chooser<*>.toOrderChooser(): OrderChooser<ChooserItem>
        = requireItemBased(this).toOrderChooser()*/

fun ChooserItemChooser<*>.toPickChooser(): PickChooser<ChooserItem> {
    return PickChooser(id, title, items as MutableList<ChooserItem>, currentPos)
}

/*fun Chooser<*>.toPickChooser(): PickChooser<ChooserItem>
        = requireItemBased(this).toPickChooser()*/

fun ChooserItemChooser<*>.toWeightedChooser(): WeightedChooser<WeightedChooserItem> {
    return WeightedChooser(id,
            title,
            (items as MutableList<ChooserItem>)
                    .map { it.toWeightedChooserItem() }
                    .toMutableList(),
            currentPos)
}

@Suppress("FunctionName")
fun ChooserItemChooser(id: Int,
                       title: String,
                       items: MutableList<WeightedChooserItem>,
                       currentPos: Int = 0,
                       type: String): ChooserItemChooser<out ChooserItem> {
    return when(type) {
        OrderChooser.PARS_TYPE -> OrderChooser(id,
                title,
                items,
                currentPos)
        PickChooser.PARS_TYPE -> PickChooser(id,
                title,
                items,
                currentPos)
        WeightedChooser.PARS_TYPE -> WeightedChooser(id,
                title,
                items,
                currentPos)
        else -> throw UnknownFormatFlagsException("Unknown type string: $type")
    }
}

/*fun Chooser<*>.toWeightedChooser(): WeightedChooser<WeightedChooserItem>
        = requireItemBased(this).toWeightedChooser()

private fun requireItemBased(chooser: Chooser<*>): ItemRandomizer<*> {
    return if (chooser is ItemRandomizer<*>) (chooser as ItemRandomizer<*>).toWeightedChooser()
    else throw Error("chooser is not item based")
}*/