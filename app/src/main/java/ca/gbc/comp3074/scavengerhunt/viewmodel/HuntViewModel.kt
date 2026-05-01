package ca.gbc.comp3074.scavengerhunt.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.scavengerhunt.data.Hunt
import ca.gbc.comp3074.scavengerhunt.data.HuntRepository
import ca.gbc.comp3074.scavengerhunt.data.HuntSort
import kotlinx.coroutines.launch

/**
 * Powers the list, detail and add/edit screens.
 *
 * The list is a tiny bit involved because it has to combine TWO things:
 *   - the search query (string from the search bar)
 *   - the sort order (set from the toolbar menu)
 *
 * MediatorLiveData is the Android-Architecture-Components-blessed way to merge
 * multiple LiveData inputs into one stream.
 */
class HuntViewModel(private val repo: HuntRepository) : ViewModel() {

    private val query = MutableLiveData("")
    private val sort  = MutableLiveData(HuntSort.NEWEST)

    /** Single LiveData the list activity observes. */
    val hunts: LiveData<List<Hunt>> = MediatorLiveData<List<Hunt>>().also { merged ->

        // We hold onto whichever inner LiveData is currently feeding the merged
        // one so we can disconnect it before subscribing to a new one. Otherwise
        // we'd get phantom updates from the old source.
        var current: LiveData<List<Hunt>>? = null

        fun rewire() {
            current?.let { merged.removeSource(it) }
            val q = query.value.orEmpty()
            val s = sort.value ?: HuntSort.NEWEST
            // search ignores sort (it always returns newest-first). felt fine —
            // if you're searching you usually only have a few results anyway.
            val src = if (q.isBlank()) repo.observe(s) else repo.search(q)
            merged.addSource(src) { merged.value = it }
            current = src
        }

        merged.addSource(query) { rewire() }
        merged.addSource(sort)  { rewire() }
    }

    fun setSearch(q: String) { query.value = q }
    fun setSort(s: HuntSort)  { sort.value = s }
    fun currentSort(): HuntSort = sort.value ?: HuntSort.NEWEST

    // ---- detail / add-edit ----

    val selectedHunt = MutableLiveData<Hunt?>()

    fun loadHunt(id: Long) {
        viewModelScope.launch { selectedHunt.postValue(repo.findById(id)) }
    }

    fun saveHunt(h: Hunt) {
        viewModelScope.launch {
            // id == 0L is Room's "not inserted yet" sentinel
            if (h.id == 0L) repo.insert(h) else repo.update(h)
        }
    }

    fun deleteHunt(h: Hunt) { viewModelScope.launch { repo.delete(h) } }

    /** Used by the snackbar undo on the list screen. */
    fun restore(h: Hunt) { viewModelScope.launch { repo.insert(h) } }

    fun toggleCompleted(h: Hunt) {
        viewModelScope.launch {
            repo.setCompleted(h.id, !h.completed)
            // re-load so the detail screen's UI reflects the change immediately
            selectedHunt.postValue(repo.findById(h.id))
        }
    }

    class Factory(private val repo: HuntRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HuntViewModel(repo) as T
    }
}
