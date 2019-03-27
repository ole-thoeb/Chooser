package chooser.com.example.eloem.chooser.chooser


open class ChooserItem(var name: String, var originalPos: Int) {
    init {
        require(originalPos >= 0) { "originalPos must be > 0 but is $originalPos" }
    }
}
private const val NO_ITEM_NAME = "No Item #ERROR"
internal object NO_CHOOSER_ITEM: ChooserItem(NO_ITEM_NAME, 0)


open class WeightedChooserItem(name: String, originalPos: Int, val weight: Int = 1): ChooserItem(name, originalPos) {
    init {
        require(weight > 0) { "weight must be > 0 but is $weight" }
    }
}

internal object NO_CHOOSER_ITEM_WEIGHTED: WeightedChooserItem(NO_ITEM_NAME, 0, 1)

fun ChooserItem.toWeightedChooserItem(): WeightedChooserItem =
        if (this is WeightedChooserItem) this
        else WeightedChooserItem(name, originalPos)

val ChooserItem.weight: Int get() {
    return if (this is WeightedChooserItem) weight
    else 1
}

fun MutableList<out ChooserItem>.toWeightedChooserItems(): List<WeightedChooserItem> {
    return map { it.toWeightedChooserItem() }
}

private data class ConverterWeightedChooserItem(val name: String, val orgPos: Int, val randomPos: Int, val weight: Int)

data class MutableWeightedChooserItem(var name: String, var randomPos: Int, var weight: Int = 1)

fun List<ChooserItem>.toMutableChooserItems(): List<MutableWeightedChooserItem> {
    return mapIndexed { index, item ->
        ConverterWeightedChooserItem(item.name, item.originalPos, index, item.weight)
    }.sortedBy { it.orgPos }
            .map { MutableWeightedChooserItem(it.name, it.randomPos, it.weight) }
}

fun List<MutableWeightedChooserItem>.toWeightedChooserItem(): List<WeightedChooserItem> {
    return mapIndexed { index, item ->
        ConverterWeightedChooserItem(item.name, index, item.randomPos, item.weight)
    }.sortedBy { it.randomPos }
            .map { WeightedChooserItem(it.name, it.orgPos, it.weight) }
}
