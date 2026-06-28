package com.wordforge.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordforge.data.repository.AuthRepository
import com.wordforge.data.repository.ListsRepository
import com.wordforge.domain.model.WordList
import com.wordforge.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ListsEvent {
    object Unauthorized : ListsEvent()
}

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val listsRepository: ListsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _lists = MutableStateFlow<List<WordList>>(emptyList())
    val lists: StateFlow<List<WordList>> = _lists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _events = MutableSharedFlow<ListsEvent>()
    val events: SharedFlow<ListsEvent> = _events.asSharedFlow()

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            _isLoading.value = true
            handle(listsRepository.getLists()) { _lists.value = it }
            _isLoading.value = false
        }
    }

    fun createList(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            handle(listsRepository.createList(title.trim())) { newList ->
                _lists.value = _lists.value + newList
            }
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            handle(listsRepository.deleteList(listId)) {
                _lists.value = _lists.value.filter { it.id != listId }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private suspend fun <T> handle(result: Result<T>, onSuccess: suspend (T) -> Unit) {
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> {
                if (result.message == "UNAUTHORIZED") {
                    authRepository.signOut()
                    _events.emit(ListsEvent.Unauthorized)
                } else {
                    _error.value = result.message
                }
            }
            else -> Unit
        }
    }
}
