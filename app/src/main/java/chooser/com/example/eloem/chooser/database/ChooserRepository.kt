package chooser.com.example.eloem.chooser.database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import chooser.com.example.eloem.chooser.chooser.*
import java.util.*

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
        chooserDao.insertSqlChooserItemChooser(chooser.getSqlChooser())
        chooserDao.insertSqlChooserItems(chooser.getSqlItems())
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
}