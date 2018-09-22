package chooser.com.example.eloem.chooser.util

import android.content.Context
import android.preference.PreferenceManager

const val ITEM_ID_ID = "itemIdId"

fun newItemId(context: Context): Int{
    fun readItemId(context: Context): Int{
        val pM = PreferenceManager.getDefaultSharedPreferences(context)
        return pM.getInt(ITEM_ID_ID, 0)
    }
    
    fun writeItemId(context: Context, value: Int){
        val pM = PreferenceManager.getDefaultSharedPreferences(context).edit()
        pM.putInt(ITEM_ID_ID, value).apply()
    }
    
    val id = readItemId(context)
    writeItemId(context, id + 1)
    return id
}

const val LIST_ID_ID = "listIdId"

fun newListId(context: Context): Int{
    fun readListId(context: Context): Int{
        val pM = PreferenceManager.getDefaultSharedPreferences(context)
        return pM.getInt(LIST_ID_ID, 0)
    }
    
    fun writeListId(context: Context, value: Int){
        val pM = PreferenceManager.getDefaultSharedPreferences(context).edit()
        pM.putInt(LIST_ID_ID, value).apply()
    }
    
    val id = readListId(context)
    writeListId(context, id + 1)
    return id
}