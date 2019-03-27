package chooser.com.example.eloem.chooser.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlin.jvm.internal.Ref

@Dao
interface ChooserDao {
    
    @Insert
    fun insertSqlChooserItemChooser(chooser: ChooserItemChooserSql)
    
    @Insert
    fun insertSqlChooserItems(items: List<ChooserItemSql>)
    
    @Query("DELETE FROM ChooserItemSql WHERE chooserId = :id")
    fun deleteSqlChooserItemsFromChooser(id: Int)
    
    @Query("DELETE FROM ChooserItemChooserSql WHERE id = :id")
    fun deleteSqlChooserItemChooser(id: Int)
    
    @Update
    fun updateSqlChooserItemChooser(chooser: ChooserItemChooserSql)
    
    @Update
    fun updateSqlChooserItems(items: List<ChooserItemSql>)
    
    @Transaction
    @Query("SELECT * FROM ChooserItemChooserSql")
    fun getAllChooserWithChooserItems(): LiveData<List<ChooserWithChooserItems>>
    
    
}