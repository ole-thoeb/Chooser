@file:Suppress("UNCHECKED_CAST")

package chooser.com.example.eloem.chooser.chooser

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

/*fun Chooser<*>.toWeightedChooser(): WeightedChooser<WeightedChooserItem>
        = requireItemBased(this).toWeightedChooser()

private fun requireItemBased(chooser: Chooser<*>): ItemRandomizer<*> {
    return if (chooser is ItemRandomizer<*>) (chooser as ItemRandomizer<*>).toWeightedChooser()
    else throw Error("chooser is not item based")
}*/