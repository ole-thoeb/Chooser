package chooser.com.example.eloem.chooser

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import chooser.com.example.eloem.chooser.chooser.ChooserItem
import chooser.com.example.eloem.chooser.chooser.ChooserItemChooser
import chooser.com.example.eloem.chooser.chooser.OrderChooser
import chooser.com.example.eloem.chooser.database.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class GlobalViewModel(application: Application): AndroidViewModel(application) {
    
    private val repository: ChooserRepository
    
    val allChooserItemChoosers: LiveData<List<ChooserItemChooser<out ChooserItem>>>
    
    init {
        val chooserDao = ChooserRoomDatabase.getDatabase(getApplication()).chooserDao()
        repository = ChooserRepository(chooserDao)
        
        allChooserItemChoosers = repository.allChooserItemChooser
    }
    
    private var parentJob = Job()
    
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    
    private val scope = CoroutineScope(coroutineContext)
    
    /*fun getChooserItemChooser(id: Int): ChooserItemChooser<out ChooserItem> {
        return allChooserItemChoosers.value?.find { it.id == id } ?:
                handOverChooserItemChooser?.let {
                    if (it.id == id) it
                    else null
                } ?: throw Exception("id: $id is not Valid. Valid ids are " +
                "${allChooserItemChoosers.value?.map { it.id }?.joinToString()}")
    }*/
    
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
    
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}