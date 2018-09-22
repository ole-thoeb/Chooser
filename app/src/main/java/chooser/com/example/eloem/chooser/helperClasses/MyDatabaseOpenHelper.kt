package chooser.com.example.eloem.chooser.helperClasses

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import chooser.com.example.eloem.chooser.util.ListContract
import chooser.com.example.eloem.chooser.util.createTables
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(context: Context): ManagedSQLiteOpenHelper(context, "MyDatabase", null, 2) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.createTable(ListContract.ListEntry.TABLE_NAME, true,
                ListContract.ListEntry.COLUMN_NAME_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                ListContract.ListEntry.COLUMN_NAME_NAME to TEXT,
                ListContract.ListEntry.COLUMN_NAME_CURRENT_POS to INTEGER,
                ListContract.ListEntry.COLUMN_NAME_MODE to INTEGER)
        
        db?.createTable(ListContract.ItemEntry.TABLE_NAME, true,
                ListContract.ItemEntry.COLUMN_NAME_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                ListContract.ItemEntry.COLUMN_NAME_NAME to TEXT,
                ListContract.ItemEntry.COLUMN_NAME_LIST to INTEGER,
                ListContract.ItemEntry.COLUMN_NAME_POSITION to INTEGER)
    }
    
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.dropTable(ListContract.ListEntry.TABLE_NAME, true)
        db?.dropTable(ListContract.ItemEntry.TABLE_NAME, true)
        
        onCreate(db)
    }
    
    companion object {
        private var instance: MyDatabaseOpenHelper? = null
    
        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }
}

val Context.database get() = MyDatabaseOpenHelper.getInstance(applicationContext)