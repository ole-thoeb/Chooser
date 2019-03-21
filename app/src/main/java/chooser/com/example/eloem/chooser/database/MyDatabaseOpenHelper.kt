package chooser.com.example.eloem.chooser.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(context: Context): ManagedSQLiteOpenHelper(context, "MyDatabase", null, 4) {
    override fun onCreate(db: SQLiteDatabase?) {
        createTables(db)
    }
    
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        dropTables(db)
        createTables(db)
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