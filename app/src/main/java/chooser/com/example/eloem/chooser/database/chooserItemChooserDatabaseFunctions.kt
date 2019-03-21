package chooser.com.example.eloem.chooser.database

import android.content.Context
import chooser.com.example.eloem.chooser.chooser.ChooserItem
import chooser.com.example.eloem.chooser.chooser.WeightedChooserItem
import chooser.com.example.eloem.chooser.chooser.*
import chooser.com.example.eloem.chooser.util.ListContract
import org.jetbrains.anko.db.*
import java.util.*

//deletes the List, including all Items, that belong to the list
fun deleteChooserItemChooser(context: Context, entryId: Int) = context.database.use {
    delete(ListContract.ListEntry.TABLE_NAME,
            "${ListContract.ListEntry.COLUMN_NAME_ID} = {id}", "id" to entryId)
    
    deleteChooserItems(context, entryId)
}
//deletes the Item witch belong to the given list
fun deleteChooserItems(context: Context, listId: Int) = context.database.use {
    delete(ListContract.ItemEntry.TABLE_NAME,
            "${ListContract.ItemEntry.COLUMN_NAME_LIST_ID} = {id}", "id" to listId)
}
//newly adds the List including all items
fun insertChooserItemChooser(context: Context, cItemChooser: ChooserItemChooser<out ChooserItem>) {
    when(cItemChooser){
        is WeightedChooser<out WeightedChooserItem> -> {
            insertWeightedItems(context, cItemChooser.id, cItemChooser.items)
        }
        is OrderChooser<out ChooserItem> -> {
            insertChooserItems(context, cItemChooser.id, cItemChooser.items)
        }
        is PickChooser<out ChooserItem> -> {
            insertChooserItems(context, cItemChooser.id, cItemChooser.items)
        }
    }
    
    insertJustChooserItemChooser(context, cItemChooser)
}

private fun insertJustChooserItemChooser(context: Context, cItemChooser: ChooserItemChooser<out ChooserItem>) {
    
    fun insertChooser(context: Context, oc: OrderChooser<*>, typeFlag: String): Unit = context.database.use {
        insert(ListContract.ListEntry.TABLE_NAME,
                ListContract.ListEntry.COLUMN_NAME_ID to oc.id,
                ListContract.ListEntry.COLUMN_NAME_NAME to oc.title,
                ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to oc.currentPos,
                ListContract.ListEntry.COLUMN_NAME_TYPE to typeFlag)
    }
    
    if (cItemChooser is OrderChooser<*>) insertChooser(context, cItemChooser, cItemChooser.parsType())
}
//newly adds all items of the given list
fun insertChooserItems(context: Context, id: Int, items: List<ChooserItem>): Unit = context.database.use {
    items.forEachIndexed { index, item ->
        insert(ListContract.ItemEntry.TABLE_NAME,
                ListContract.ItemEntry.COLUMN_NAME_NAME to item.name,
                ListContract.ItemEntry.COLUMN_NAME_LIST_ID to id,
                ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION to item.originalPos,
                ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION to index)
    }
}

fun insertWeightedItems(context: Context, id: Int, items: List<WeightedChooserItem>): Unit = context.database.use {
    items.forEachIndexed { index, item ->
        insert(ListContract.ItemEntry.TABLE_NAME,
                ListContract.ItemEntry.COLUMN_NAME_NAME to item.name,
                ListContract.ItemEntry.COLUMN_NAME_LIST_ID to id,
                ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION to index,
                ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION to item.originalPos,
                ListContract.ItemEntry.COLUMN_NAME_WEIGHT to item.weight)
    }
}

// updates the list entry, including all items
fun updateChooserItemChooser(context: Context, cItemChooser: ChooserItemChooser<out ChooserItem>) {
    updateJustChooserItemChooser(context, cItemChooser)
    //TODO use update and handel when nothing to update
    //updateItem(context, cItemChooser.items)
    deleteChooserItems(context, cItemChooser.id)
    when(cItemChooser){
        is WeightedChooser<*> -> insertWeightedItems(context, cItemChooser.id, cItemChooser.items)
        is OrderChooser<*> -> insertChooserItems(context, cItemChooser.id, cItemChooser.items)
        is PickChooser<*> -> insertChooserItems(context, cItemChooser.id, cItemChooser.items)
    }
}

fun updateChooserItems(context: Context, items: List<ChooserItem>, listId: Int): Unit =
        context.database.use {
        
    items.forEachIndexed{index, item ->
        update(ListContract.ItemEntry.TABLE_NAME,
                ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION to index,
                ListContract.ItemEntry.COLUMN_NAME_NAME to item.name)
                .whereArgs("${ListContract.ItemEntry.COLUMN_NAME_LIST_ID} = {id} and " +
                        "${ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION} = {orgPos}",
                        "id" to listId, "orgPos" to item.originalPos)
                .exec()
    }
    
}
// updates just the list. doesn't change the items
fun updateJustChooserItemChooser(context: Context, listObj: ChooserItemChooser<out ChooserItem>): Unit =
        context.database.use {
        
    update(ListContract.ListEntry.TABLE_NAME,
        ListContract.ListEntry.COLUMN_NAME_NAME to listObj.title,
        ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to listObj.currentPos,
        ListContract.ListEntry.COLUMN_NAME_TYPE to listObj.parsType())
        .whereArgs("${ListContract.ListEntry.COLUMN_NAME_ID} = {id}", "id" to listObj.id)
        .exec()
}


fun chooserItemChooserParser(context: Context) = rowParser<Int, String, Int, String, ChooserItemChooser<out ChooserItem>> {
        id: Int, title: String, curPos: Int, type: String ->
    
    when(type){
        OrderChooser.PARS_TYPE ->
            OrderChooser(id, title, getChooserItemsFromOneChooserItemChooser(context, id).toMutableList(), curPos)
        PickChooser.PARS_TYPE ->
            PickChooser(id, title, getChooserItemsFromOneChooserItemChooser(context, id).toMutableList(), curPos)
        WeightedChooser.PARS_TYPE ->
            WeightedChooser(id, title, getWeightedItemsFromOneChooserItemChooser(context, id).toMutableList(), curPos)
        else -> throw UnknownFormatFlagsException("Unknown type string: $type")
    }
}
/**return the [ChooserItemChooser] with the given [id]*/
fun getChooserItemChooser(context: Context, id: Int): ChooserItemChooser<out ChooserItem> = context.database.use {
    select(ListContract.ListEntry.TABLE_NAME,
            ListContract.ListEntry.COLUMN_NAME_ID,
            ListContract.ListEntry.COLUMN_NAME_NAME,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS,
            ListContract.ListEntry.COLUMN_NAME_TYPE)
            .whereArgs("${ListContract.ListEntry.COLUMN_NAME_ID} = {id}", "id" to id)
            .parseSingle(chooserItemChooserParser(context))
}
//returns all Choosers in a list
fun getAllChooserItemChooser(context: Context): List<ChooserItemChooser<out ChooserItem>> = context.database.use {
    select(ListContract.ListEntry.TABLE_NAME,
            ListContract.ListEntry.COLUMN_NAME_ID,
            ListContract.ListEntry.COLUMN_NAME_NAME,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS,
            ListContract.ListEntry.COLUMN_NAME_TYPE)
            .parseList(chooserItemChooserParser(context))
}
//returns all items from a single list as an array
fun getChooserItemsFromOneChooserItemChooser(context: Context, listId: Int): List<ChooserItem> = context.database.use {
    
    val itemParser = rowParser { name: String, orgPos: Int ->
        ChooserItem(name, orgPos)
    }
    
    select(ListContract.ItemEntry.TABLE_NAME,
            ListContract.ItemEntry.COLUMN_NAME_NAME,
            ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION)
            .whereArgs("${ListContract.ItemEntry.COLUMN_NAME_LIST_ID} = {listId}", "listId" to listId)
            .orderBy(ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION)
            .parseList(itemParser)
}

fun getWeightedItemsFromOneChooserItemChooser(context: Context, listId: Int): List<WeightedChooserItem> = context.database.use {
   
    val weightedItemParser = rowParser { name: String, orgPos: Int, weight: Int ->
        WeightedChooserItem(name, orgPos, weight)
    }
    
    select(ListContract.ItemEntry.TABLE_NAME,
            ListContract.ItemEntry.COLUMN_NAME_NAME,
            ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION,
            ListContract.ItemEntry.COLUMN_NAME_WEIGHT)
            .whereArgs("${ListContract.ItemEntry.COLUMN_NAME_LIST_ID} = {listId}", "listId" to listId)
            .orderBy(ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION)
            .parseList(weightedItemParser)
}