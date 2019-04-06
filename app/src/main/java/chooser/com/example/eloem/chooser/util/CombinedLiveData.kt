package chooser.com.example.eloem.chooser.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class CombinedLiveData<T1, T2, S>(
        source1: LiveData<T1>,
        source2: LiveData<T2>,
        private val combine: (data1: T1?, data2: T2?) -> S
) : MediatorLiveData<S>() {
    
    private var data1: T1? = null
    private var data2: T2? = null
    
    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2)
        }
    }
    
    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException()
    }
    
    override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}