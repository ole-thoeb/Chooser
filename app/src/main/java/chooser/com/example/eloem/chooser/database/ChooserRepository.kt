package chooser.com.example.eloem.chooser.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import chooser.com.example.eloem.chooser.chooser.*
import chooser.com.example.eloem.chooser.util.CombinedLiveData
import chooser.com.example.eloem.chooser.util.filterAndRemove

class ChooserRepository(private val chooserDao: ChooserDao) {
    
    val allChooserItemChooser: LiveData<List<ChooserItemChooser<out ChooserItem>>>
        = Transformations.map(chooserDao.getAllChooserWithChooserItems()) { list ->
            list.map { bundle ->
                bundle.toStandardChooserItemChooser()
            }
    }
    
    
    fun getChooser(id: Int): LiveData<ChooserItemChooser<out ChooserItem>?> {
        return Transformations.map(allChooserItemChooser) { list ->
            list.find { it.id == id }
        }
    }

    @WorkerThread
    suspend fun insertChooserItemChooser(chooser: ChooserItemChooser<out ChooserItem>) {
        chooserDao.insertSqlChooserItemChooser(chooser.toSqlType())
        chooserDao.insertSqlChooserItems(chooser.itemsToSqlType())
    }
    
    @WorkerThread
    suspend fun deleteChooserItemChooser(chooserId: Int) {
        chooserDao.deleteSqlChooserItemChooser(chooserId)
        chooserDao.deleteSqlChooserItemsFromChooser(chooserId)
    }
    
    @WorkerThread
    suspend fun updateChooserItemChooser(chooser: ChooserItemChooser<out ChooserItem>) {
        chooserDao.updateSqlChooserItemChooser(chooser.toSqlType())
        
        chooserDao.deleteSqlChooserItemsFromChooser(chooser.id)
        chooserDao.insertSqlChooserItems(chooser.itemsToSqlType())
    }
    
    @WorkerThread
    suspend fun updateChooserAfterRestart(chooser: ChooserItemChooser<out ChooserItem>) {
        chooserDao.updateSqlChooserItems(chooser.itemsToSqlType())
        chooserDao.updateSqlChooserItemChooser(chooser.toSqlType())
    }
    
    @WorkerThread
    suspend fun updateChooserAfterNext(chooser: ChooserItemChooser<out ChooserItem>) {
        chooserDao.updateSqlChooserItemChooser(chooser.toSqlType())
    }
    
    
    val allDiceList: LiveData<List<MultiDiceList>>
    
    
    init {
    
        allDiceList = CombinedLiveData(chooserDao.getAllMultiDiceLists(), chooserDao.getAllMultiDices()) { lists, dices ->
            if (lists == null || dices == null) return@CombinedLiveData emptyList<MultiDiceList>()
            
            val mutableDices = dices.toMutableList()
            lists.map { multiDiceList ->
                MultiDiceList(multiDiceList.id, multiDiceList.title,
                        mutableDices.filterAndRemove { it.multiDice?.listId == multiDiceList.id }
                                .map { it.toStandardMultiDice() })
            }
        }
    }
    
    @WorkerThread
    suspend fun deleteMultiDiceList(multiDiceList: MultiDiceList) {
        chooserDao.deleteSqlMultiDiceList(multiDiceList.id)
        chooserDao.deleteMultiDiceFromList(multiDiceList.id)
        multiDiceList.forEach { chooserDao.deletCurrentFromDice(it.id) }
    }
    
    @WorkerThread
    suspend fun updateMultiDiceList(multiDiceList: MultiDiceList) {
        chooserDao.updateMultiDiceList(multiDiceList)
    }
    
    @WorkerThread
    suspend fun updateMultiDiceListAfterRole(multiDiceList: MultiDiceList) {
        chooserDao.updateMultiDiceListAfterRole(multiDiceList)
    }
    
    @WorkerThread
    suspend fun insertMultiDiceList(multiDiceList: MultiDiceList) {
        chooserDao.insertMultiDiceList(multiDiceList)
    }
}