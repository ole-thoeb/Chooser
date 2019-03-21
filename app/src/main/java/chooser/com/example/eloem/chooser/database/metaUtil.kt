package chooser.com.example.eloem.chooser.database

import android.database.sqlite.SQLiteDatabase
import chooser.com.example.eloem.chooser.util.ListContract
import org.jetbrains.anko.db.*

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