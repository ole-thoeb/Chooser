package chooser.com.example.eloem.chooser.util

import android.provider.BaseColumns

object ListContract {
    object ListEntry: BaseColumns{
        const val TABLE_NAME = "lists"
        const val COLUMN_NAME_ID = "listId"
        const val COLUMN_NAME_NAME = "listName"
        const val COLUMN_NAME_CURRENT_POS = "currentPos"
        const val COLUMN_NAME_MODE = "mode"
    }
    
    object ItemEntry: BaseColumns{
        const val TABLE_NAME = "items"
        const val COLUMN_NAME_ID = "itemId"
        const val COLUMN_NAME_NAME = "itemName"
        const val COLUMN_NAME_LIST = "listId"
        const val COLUMN_NAME_POSITION = "position"
    }
}