package chooser.com.example.eloem.chooser.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    ChooserItemChooserSql::class,
    ChooserItemSql::class,
    MultiDiceListSql::class,
    MultiDiceSql::class,
    MultiDiceCurrentSql::class
], version = 2, exportSchema = false)
abstract class ChooserRoomDatabase: RoomDatabase() {
    abstract fun chooserDao(): ChooserDao
    
    companion object {
        @Volatile
        private var INSTANCE: ChooserRoomDatabase? = null
    
        fun getDatabase(context: Context): ChooserRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ChooserRoomDatabase::class.java,
                        "chooser_database")
                        .fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}