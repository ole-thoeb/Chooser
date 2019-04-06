package chooser.com.example.eloem.chooser.database

import androidx.lifecycle.LiveData
import androidx.room.*
import chooser.com.example.eloem.chooser.chooser.MultiDiceList

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

    /**#############################################################################*/
    
    @Transaction
    fun insertMultiDiceList(multiDiceList: MultiDiceList) {
        insertSqlMultiDiceList(multiDiceList.toSqlType())
        multiDiceList.forEach {
            insertSqlMultiDice(it.toSqlType(multiDiceList.id))
            insertSqlMultiDiceCurrents(it.currentToSqlType())
        }
    }
    
    @Insert
    fun insertSqlMultiDiceList(diceList: MultiDiceListSql)
    
    @Insert
    fun insertSqlMultiDice(mDice: MultiDiceSql)
    
    @Insert
    fun insertSqlMultiDiceCurrents(currents: List<MultiDiceCurrentSql>)
    
    /*@Update
    fun updateSqlMultiDice(multiDice: MultiDiceSql)*/
    
    @Query("DELETE FROM MultiDiceListSql WHERE id = :id")
    fun deleteSqlMultiDiceList(id: Int)
    
    @Query("DELETE FROM MultiDiceSql WHERE listId = :listId")
    fun deleteMultiDiceFromList(listId: Int)
    
    @Query("DELETE FROM MultiDiceCurrentSql WHERE multiDiceId = :diceId")
    fun deletCurrentFromDice(diceId: Int)
    
    @Query("DELETE FROM MultiDiceCurrentSql WHERE multiDiceId = :multiDiceId")
    fun deleteSqlMultiDiceCurrent(multiDiceId: Int)
    
    @Update
    fun updateSqlMultiDiceList(multiDiceList: MultiDiceListSql)
    
    @Transaction
    fun updateMultiDiceListAfterRole(multiDiceList: MultiDiceList) {
        multiDiceList.forEach {
            deleteSqlMultiDiceCurrent(it.id)
            insertSqlMultiDiceCurrents(it.currentToSqlType())
        }
    }
    
    @Transaction
    fun updateMultiDiceList(multiDiceList: MultiDiceList) {
        deleteMultiDiceFromList(multiDiceList.id)
        multiDiceList.forEach {
            insertSqlMultiDice(it.toSqlType(multiDiceList.id))
            deleteSqlMultiDiceCurrent(it.id)
            insertSqlMultiDiceCurrents(it.currentToSqlType())
        }
        updateSqlMultiDiceList(multiDiceList.toSqlType())
    }
    
    @Query("SELECT * FROM MultiDiceListSql")
    fun getAllMultiDiceLists(): LiveData<List<MultiDiceListSql>>
    
    @Transaction
    @Query("SELECT * FROM MultiDiceSql")
    fun getAllMultiDices(): LiveData<List<MultiDiceWithCurrent>>
}