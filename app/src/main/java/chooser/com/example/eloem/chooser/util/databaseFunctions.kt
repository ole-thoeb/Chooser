package chooser.com.example.eloem.chooser.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import chooser.com.example.eloem.chooser.helperClasses.*
import org.jetbrains.anko.db.*
import java.util.*

fun createTables(db: SQLiteDatabase?){
    db?.createTable(ListContract.ListEntry.TABLE_NAME, true,
            ListContract.ListEntry.COLUMN_NAME_ID to INTEGER + PRIMARY_KEY,
            ListContract.ListEntry.COLUMN_NAME_NAME to TEXT,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to INTEGER,
            ListContract.ListEntry.COLUMN_NAME_TYPE to TEXT)
    
    db?.createTable(ListContract.ItemEntry.TABLE_NAME, true,
            ListContract.ItemEntry.COLUMN_NAME_NAME to TEXT,
            ListContract.ItemEntry.COLUMN_NAME_LIST_ID to INTEGER,
            ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION to INTEGER,
            ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION to INTEGER,
            ListContract.ItemEntry.COLUMN_NAME_WEIGHT to INTEGER + DEFAULT("1"))
}

fun dropTables(db: SQLiteDatabase?) {
    db?.dropTable(ListContract.ListEntry.TABLE_NAME, true)
    db?.dropTable(ListContract.ItemEntry.TABLE_NAME, true)
}
//deletes the List, including all Items, that belong to the list
fun deleteListEntry(context: Context, entryId: Int) = context.database.use {
    delete(ListContract.ListEntry.TABLE_NAME, "${ListContract.ListEntry.COLUMN_NAME_ID} = {id}",
            "id" to entryId)
    
    deleteItems(context, entryId)
}
//deletes the Item witch belong to the given list
fun deleteItems(context: Context, listId: Int) = context.database.use {
    delete(ListContract.ItemEntry.TABLE_NAME, "${ListContract.ItemEntry.COLUMN_NAME_LIST_ID} = {id}",
            "id" to listId)
}
//newly adds the List including all items
fun insertListEntry(context: Context, listObj: ChooserObj) {
    when(listObj){
        is WeightedChooser<*> -> {
            insertWeightedItems(context, listObj.id, listObj.items)
        }
        is OrderChooser<*> -> {
            insertItems(context, listObj.id, listObj.items)
        }
    }
    
    insertJustChooser(context, listObj)
}

private fun insertJustChooser(context: Context, listObj: ChooserObj) {
    
    fun insertChooser(context: Context, oc: OrderChooser<*>, typeFlag: String) = context.database.use {
        insert(ListContract.ListEntry.TABLE_NAME,
                ListContract.ListEntry.COLUMN_NAME_ID to oc.id,
                ListContract.ListEntry.COLUMN_NAME_NAME to oc.title,
                ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to oc.currentPos,
                ListContract.ListEntry.COLUMN_NAME_TYPE to typeFlag)
    }
    
    if (listObj is OrderChooser<*>) insertChooser(context, listObj, listObj.parsType())
}
//newly adds all items of the given list
fun insertItems(context: Context, id: Int, items: List<ChooserItem>) = context.database.use {
    items.forEachIndexed { index, item ->
        insert(ListContract.ItemEntry.TABLE_NAME,
                ListContract.ItemEntry.COLUMN_NAME_NAME to item.name,
                ListContract.ItemEntry.COLUMN_NAME_LIST_ID to id,
                ListContract.ItemEntry.COLUMN_NAME_ORIGINAL_POSITION to item.originalPos,
                ListContract.ItemEntry.COLUMN_NAME_CURRENT_POSITION to index)
    }
}

fun insertWeightedItems(context: Context, id: Int, items: List<WeightedChooserItem>) = context.database.use {
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
fun updateListEntryComplete(context: Context, listObj: ChooserObj){
    updateJustList(context, listObj)
    //TODO use update and handel when nothing to update
    //updateItem(context, listObj.items)
    deleteItems(context, listObj.id)
    when(listObj){
        is WeightedChooser<*> -> insertWeightedItems(context, listObj.id, listObj.items)
        is PickChooser<*> -> insertItems(context, listObj.id, listObj.items)
        is OrderChooser<*> -> insertItems(context, listObj.id, listObj.items)
    }
}

fun updateItems(context: Context, items: List<ChooserItem>, listId: Int) = context.database.use {
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
fun updateJustList(context: Context, listObj: ChooserObj): Unit = context.database.use {
    when(listObj) {
        is OrderChooser<*> -> update(ListContract.ListEntry.TABLE_NAME,
                ListContract.ListEntry.COLUMN_NAME_NAME to listObj.title,
                ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to listObj.currentPos,
                ListContract.ListEntry.COLUMN_NAME_TYPE to listObj.parsType())
                .whereArgs("${ListContract.ListEntry.COLUMN_NAME_ID} = {id}", "id" to listObj.id)
                .exec()
    }
}

fun chooserParser(context: Context) = rowParser<Int, String, Int, String, ChooserObj> {
        id: Int, title: String, curPos: Int, type: String ->
    
    when(type){
        OrderChooser.PARS_TYPE ->
            OrderChooser(id, title, getItemsFromOneList(context, id).toMutableList(), curPos)
        PickChooser.PARS_TYPE ->
            PickChooser(id, title, getItemsFromOneList(context, id).toMutableList(), curPos)
        WeightedChooser.PARS_TYPE ->
            WeightedChooser(id, title, getWeightedItemsFromOneList(context, id).toMutableList(), curPos)
        else -> throw UnknownFormatFlagsException("Unknown type string: $type")
    }
}
/**return the [ChooserObj] with the given [id]*/
fun getChooser(context: Context, id: Int) = context.database.use {
    select(ListContract.ListEntry.TABLE_NAME,
            ListContract.ListEntry.COLUMN_NAME_ID,
            ListContract.ListEntry.COLUMN_NAME_NAME,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS,
            ListContract.ListEntry.COLUMN_NAME_TYPE)
            .whereArgs("${ListContract.ListEntry.COLUMN_NAME_ID} = {id}", "id" to id)
            .parseSingle(chooserParser(context))
}
//returns all Choosers in a list
fun getAllListObj(context: Context): List<ChooserObj> = context.database.use {
    select(ListContract.ListEntry.TABLE_NAME,
            ListContract.ListEntry.COLUMN_NAME_ID,
            ListContract.ListEntry.COLUMN_NAME_NAME,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS,
            ListContract.ListEntry.COLUMN_NAME_TYPE)
            .parseList(chooserParser(context))
}
//returns all items from a single list as an array
fun getItemsFromOneList(context: Context, listId: Int) = context.database.use {
    
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

fun getWeightedItemsFromOneList(context: Context, listId: Int) = context.database.use {
   
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