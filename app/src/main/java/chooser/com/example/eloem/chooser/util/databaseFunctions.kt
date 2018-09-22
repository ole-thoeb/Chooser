package chooser.com.example.eloem.chooser.util

import android.content.Context
import chooser.com.example.eloem.chooser.helperClasses.ListObj
import chooser.com.example.eloem.chooser.helperClasses.database
import org.jetbrains.anko.db.*

//deletes the List, including all Items, with the given id
fun deleteListEntry(context: Context, entryId: Int) = context.database.use {
    delete(ListContract.ListEntry.TABLE_NAME, "${ListContract.ListEntry.COLUMN_NAME_ID} = {id}",
            "id" to entryId)
    
    deleteItems(context, entryId)
}
//deletes the Item with the given id
fun deleteItems(context: Context, listId: Int)= context.database.use {
    delete(ListContract.ItemEntry.TABLE_NAME, "${ListContract.ItemEntry.COLUMN_NAME_LIST} = {id}",
            "id" to listId)
}
//newly adds the List including all items
fun insertListEntry(context: Context, listObj: ListObj) = context.database.use {
    insert(ListContract.ListEntry.TABLE_NAME,
            ListContract.ListEntry.COLUMN_NAME_ID to listObj.id,
            ListContract.ListEntry.COLUMN_NAME_NAME to listObj.title,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to listObj.currentPos,
            ListContract.ListEntry.COLUMN_NAME_MODE to  listObj.mode)
    
    insertItems(context, listObj)
}
//newly adds all items of the given list
fun insertItems(context: Context, listObj: ListObj) = context.database.use {
    listObj.items.forEachIndexed { index, item ->
        insert(ListContract.ItemEntry.TABLE_NAME,
                ListContract.ItemEntry.COLUMN_NAME_ID to item.id,
                ListContract.ItemEntry.COLUMN_NAME_NAME to item.name,
                ListContract.ItemEntry.COLUMN_NAME_LIST to listObj.id,
                ListContract.ItemEntry.COLUMN_NAME_POSITION to index)
    }
}
// updates the list entry, including all items
fun updateListEntryComplete(context: Context, listObj: ListObj){
    updateJustList(context, listObj)
    //TODO use update and handel then nothing to update
    //updateItem(context, listObj.items)
    deleteItems(context, listObj.id)
    insertItems(context, listObj)
}

fun updateItem(context: Context, items: Array<ListObj.Item>) = context.database.use {
    items.forEachIndexed{index, item ->
        update(ListContract.ItemEntry.TABLE_NAME, ListContract.ItemEntry.COLUMN_NAME_POSITION to index,
                ListContract.ItemEntry.COLUMN_NAME_NAME to item.name)
                .whereArgs("${ListContract.ItemEntry.COLUMN_NAME_ID} = {id}", "id" to item.id)
                .exec()
    }
    
}
// updates just the list. doesn't change the items
fun updateJustList(context: Context, listObj: ListObj) = context.database.use {
    update(ListContract.ListEntry.TABLE_NAME, ListContract.ListEntry.COLUMN_NAME_NAME to listObj.title,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to listObj.currentPos)
            .whereArgs("${ListContract.ListEntry.COLUMN_NAME_ID} = {id}", "id" to listObj.id)
            .exec()
}
//returns all lists as mutableList
fun getAllListObj(context: Context) = context.database.use {
    val listObjs = mutableListOf<ListObj>()
    
    select(ListContract.ListEntry.TABLE_NAME, ListContract.ListEntry.COLUMN_NAME_ID,
            ListContract.ListEntry.COLUMN_NAME_NAME,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS,
            ListContract.ListEntry.COLUMN_NAME_MODE)
            .parseList(object : MapRowParser<MutableList<ListObj>> {
                
                override fun parseRow(columns: Map<String, Any?>): MutableList<ListObj> {
                    val title = columns[ListContract.ListEntry.COLUMN_NAME_NAME].toString()
                    val id = columns[ListContract.ListEntry.COLUMN_NAME_ID].toString().toInt()
                    val curPos = columns[ListContract.ListEntry.COLUMN_NAME_CURRENT_POS].toString().toInt()
                    val mode = columns[ListContract.ListEntry.COLUMN_NAME_MODE].toString().toInt()
                    
                    val listObj = ListObj(id, title, getItemsFromOneList(context, id), curPos, mode)
                    
                    listObjs.add(listObj)
                    
                    return listObjs
                }
            })
    
    listObjs
}
//returns all items from a single list as an array
fun getItemsFromOneList(context: Context, listId: Int) = context.database.use {
    val items = mutableListOf<ListObj.Item>()
    
    select(ListContract.ItemEntry.TABLE_NAME, ListContract.ItemEntry.COLUMN_NAME_ID,
            ListContract.ItemEntry.COLUMN_NAME_NAME)
            .whereArgs("${ListContract.ItemEntry.COLUMN_NAME_LIST} = {listId}", "listId" to listId)
            .orderBy(ListContract.ItemEntry.COLUMN_NAME_POSITION)
            .parseList(object :MapRowParser<MutableList<ListObj.Item>>{
                override fun parseRow(columns: Map<String, Any?>): MutableList<ListObj.Item> {
                    val name = columns[ListContract.ItemEntry.COLUMN_NAME_NAME].toString()
                    val id = columns[ListContract.ItemEntry.COLUMN_NAME_ID].toString().toInt()
                    
                    val item = ListObj.Item(name, id)
                    items.add(item)
                    
                    return items
                }
            })
    items.toTypedArray()
}

fun createTables(context: Context) = context.database.use {
    createTable(ListContract.ListEntry.TABLE_NAME, true,
            ListContract.ListEntry.COLUMN_NAME_ID to INTEGER + PRIMARY_KEY + UNIQUE,
            ListContract.ListEntry.COLUMN_NAME_NAME to TEXT,
            ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to INTEGER,
            ListContract.ListEntry.COLUMN_NAME_MODE to INTEGER)
    
    createTable(ListContract.ItemEntry.TABLE_NAME, true,
            ListContract.ItemEntry.COLUMN_NAME_ID to INTEGER + PRIMARY_KEY + UNIQUE,
            ListContract.ItemEntry.COLUMN_NAME_NAME to TEXT,
            ListContract.ItemEntry.COLUMN_NAME_LIST to INTEGER,
            ListContract.ItemEntry.COLUMN_NAME_POSITION to INTEGER)
}
