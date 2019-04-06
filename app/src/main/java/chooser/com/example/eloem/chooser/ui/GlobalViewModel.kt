package chooser.com.example.eloem.chooser.ui

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import chooser.com.example.eloem.chooser.R
import chooser.com.example.eloem.chooser.chooser.*
import chooser.com.example.eloem.chooser.database.*
import chooser.com.example.eloem.chooser.util.CombinedLiveData
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GlobalViewModel(application: Application): AndroidViewModel(application) {
    
    private val repository: ChooserRepository
    
    val allChooserItemChoosers: LiveData<List<ChooserItemChooser<out ChooserItem>>>
    
    val allMultiDiceLists: LiveData<List<MultiDiceList>>
    
    val allChoosers: LiveData<List<Chooser<*>>>
    
    init {
        val chooserDao = ChooserRoomDatabase.getDatabase(getApplication()).chooserDao()
        repository = ChooserRepository(chooserDao)
        
        allChooserItemChoosers = repository.allChooserItemChooser
        allMultiDiceLists = repository.allDiceList
        
        allChoosers = CombinedLiveData(allChooserItemChoosers, allMultiDiceLists) { allChoosers, allMultiDices ->
            val list = mutableListOf<Chooser<*>>()
            if (allChoosers != null && allMultiDices != null) {
                list.addAll(allChoosers)
                list.addAll(allMultiDices)
            }
            list.sortedBy { it.id }
        }
    }
    
    private var parentJob = Job()
    
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    
    private val scope = CoroutineScope(coroutineContext)
    
    /*fun getChooserItemChooser(diceId: Int): ChooserItemChooser<out ChooserItem> {
        return allChooserItemChoosers.value?.find { it.diceId == diceId } ?:
                handOverChooserItemChooser?.let {
                    if (it.diceId == diceId) it
                    else null
                } ?: throw Exception("diceId: $diceId is not Valid. Valid ids are " +
                "${allChooserItemChoosers.value?.map { it.diceId }?.joinToString()}")
    }*/
    
    fun deleteChooser(chooser: Chooser<*>) {
        when(chooser) {
            is ChooserItemChooser<*> -> deleteChooserItemChooser(chooser.id)
            is MultiDiceList -> deleteMultiDiceList(chooser)
        }
    }
    
    fun insertChooser(chooser: Chooser<*>) {
        when(chooser) {
            is ChooserItemChooser<*> -> insertChooserItemChooser(chooser)
            is MultiDiceList -> insertMultiDiceList(chooser)
        }
    }
    
    fun getChooserItemChooser(chooserId: Int) = repository.getChooser(chooserId)
    
    var handOverChooserItemChooser: ChooserItemChooser<out ChooserItem>? = null
    
    fun insertChooserItemChooser(chooser: ChooserItemChooser<out ChooserItem>) = scope.launch(Dispatchers.IO) {
        repository.insertChooserItemChooser(chooser)
    }
    
    fun deleteChooserItemChooser(chooserId: Int) = scope.launch(Dispatchers.IO) {
        repository.deleteChooserItemChooser(chooserId)
    }
    
    fun updateChooserItemChooser(chooser: ChooserItemChooser<out ChooserItem>) = scope.launch(Dispatchers.IO) {
        repository.updateChooserItemChooser(chooser)
    }
    
    fun updateChooserAfterRestart(chooser: ChooserItemChooser<out ChooserItem>) = scope.launch(Dispatchers.IO) {
        repository.updateChooserAfterRestart(chooser)
    }
    
    fun updateChooserAfterNext(chooser: ChooserItemChooser<out ChooserItem>) = scope.launch(Dispatchers.IO) {
        repository.updateChooserAfterNext(chooser)
    }
    
    fun deleteChooserUiFeedBack(chooser: ChooserItemChooser<out ChooserItem>, rootView: View, anchorView: View? = null) {
        deleteChooserItemChooser(chooser.id)
        Snackbar.make(rootView,
                getApplication<Application>().resources.getQuantityString(R.plurals.deletedChooserMessage, 1),
                Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .setAction(R.string.undo) {
                    insertChooserItemChooser(chooser)
                }
                .show()
    }
    
    fun restartChooserUiFeedBack(chooser: OrderChooser<out ChooserItem>, rootView: View, anchorView: View? = null) {
        val beforeRestart = chooser.deepCopy()
        updateChooserAfterRestart(chooser)
        Snackbar.make(rootView,
                getApplication<Application>().getString(R.string.restartedChooserMessage),
                Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .setAction(R.string.undo) {
                    updateChooserAfterRestart(beforeRestart)
                }
                .show()
    }
    
    /**#############################################################################*/

    fun transformChooserToMultiDiceList(chooserId: Int) {
        val chooser = PickChooser(chooserId, "", mutableListOf())
        transformChooserToMultiDiceList(chooser)
    }
    
    fun transformChooserToMultiDiceList(chooser: ChooserItemChooser<out ChooserItem>) {
        deleteChooserItemChooser(chooser.id)
        val mDiceList = MultiDiceList(chooser.id, chooser.title, listOf())
        insertMultiDiceList(mDiceList)
    }
    
    fun transformMultiDiceListToChooser(diceId: Int, type: String) {
        val diceList = MultiDiceList(diceId, "", mutableListOf())
        transformMultiDiceListToChooser(diceList, type)
    }
    
    fun transformMultiDiceListToChooser(multiDiceList: MultiDiceList, type: String) {
        deleteMultiDiceList(multiDiceList)
        val chooser = ChooserItemChooser(multiDiceList.id, multiDiceList.title, mutableListOf(), 0, type)
        insertChooserItemChooser(chooser)
    }
    
    /**#############################################################################*/
    
    fun getMultiDiceList(id: Int): LiveData<MultiDiceList?> {
        return Transformations.map(allMultiDiceLists) { input ->
            input.find { it.id == id }
        }
    }
    
    fun insertMultiDiceList(multiDiceList: MultiDiceList) = scope.launch(Dispatchers.IO) {
        repository.insertMultiDiceList(multiDiceList)
    }
    
    fun deleteMultiDiceList(multiDiceList: MultiDiceList) = scope.launch(Dispatchers.IO) {
        repository.deleteMultiDiceList(multiDiceList)
    }
    
    fun updateMultiDiceList(multiDiceList: MultiDiceList) = scope.launch(Dispatchers.IO) {
        repository.updateMultiDiceList(multiDiceList)
    }
    
    fun updateMultiDiceListAfterRole(multiDiceList: MultiDiceList) = scope.launch(Dispatchers.IO) {
        repository.updateMultiDiceListAfterRole(multiDiceList)
    }
    
    fun deleteDiceUiFeedback(diceList: MultiDiceList, rootView: View, anchorView: View? = null) {
        deleteMultiDiceList(diceList)
        Snackbar.make(rootView,
                getApplication<Application>().resources.getQuantityString(R.plurals.deletedChooserMessage, 1),
                Snackbar.LENGTH_LONG)
                .setAnchorView(anchorView)
                .setAction(R.string.undo) {
                    insertMultiDiceList(diceList)
                }
                .show()
    }
    
    
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}