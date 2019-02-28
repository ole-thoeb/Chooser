package chooser.com.example.eloem.chooser.util

import android.provider.BaseColumns

object ListContract {
    object ListEntry: BaseColumns{
        const val TABLE_NAME = "lists"
        const val COLUMN_NAME_ID = "listId"
        const val COLUMN_NAME_NAME = "listName"
        const val COLUMN_NAME_CURRENT_POS = "currentPos"
        const val COLUMN_NAME_TYPE = "type"
    }
    
    object ItemEntry: BaseColumns{
        const val TABLE_NAME = "items"
        const val COLUMN_NAME_NAME = "itemNameET"
        const val COLUMN_NAME_LIST_ID = "listId"
        const val COLUMN_NAME_CURRENT_POSITION = "position"
        const val COLUMN_NAME_ORIGINAL_POSITION = "originalPosition"
        const val COLUMN_NAME_WEIGHT = "weight"
    }
}