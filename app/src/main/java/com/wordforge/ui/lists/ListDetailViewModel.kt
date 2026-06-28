package com.wordforge.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordforge.data.repository.AuthRepository
import com.wordforge.data.repository.ListDetailRepository
import com.wordforge.domain.model.Card
import com.wordforge.domain.model.TranslateResult
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

sealed class DetailEvent {
    object Unauthorized : DetailEvent()
}

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val repository: ListDetailRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _searchResult = MutableStateFlow<TranslateResult?>(null)
    val searchResult: StateFlow<TranslateResult?> = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    private var listId: Long = -1

    fun loadCards(id: Long) {
        listId = id
        viewModelScope.launch {
            _isLoading.value = true
            handle(repository.getCards(id)) { _cards.value = it }
            _isLoading.value = false
        }
    }

    fun searchWord(lemma: String, sourceLang: String, targetLang: String) {
        if (lemma.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _searchResult.value = null
            handle(repository.searchWord(lemma.trim(), sourceLang, targetLang)) {
                _searchResult.value = it
            }
            _isSearching.value = false
        }
    }

    fun addCard(lemma: String, wordId: Long, translationIds: List<Long>) {
        viewModelScope.launch {
            when (val cardResult = repository.addCard(listId, wordId, lemma)) {
                is Result.Success -> {
                    val card = cardResult.data
                    val finalCard = if (translationIds.isNotEmpty()) {
                        when (val tResult = repository.updateTranslations(listId, card.id, translationIds)) {
                            is Result.Success -> tResult.data
                            else -> card
                        }
                    } else card
                    _cards.value = _cards.value + finalCard
                    _searchResult.value = null
                }
                is Result.Error -> handleError(cardResult.message)
                else -> Unit
            }
        }
    }

    fun removeCard(cardId: Long) {
        viewModelScope.launch {
            handle(repository.deleteCard(listId, cardId)) {
                _cards.value = _cards.value.filter { it.id != cardId }
            }
        }
    }

    fun clearSearch() { _searchResult.value = null }
    fun clearError() { _error.value = null }

    private suspend fun <T> handle(result: Result<T>, onSuccess: suspend (T) -> Unit) {
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> handleError(result.message)
            else -> Unit
        }
    }

    private suspend fun handleError(message: String) {
        if (message == "UNAUTHORIZED") {
            authRepository.signOut()
            _events.emit(DetailEvent.Unauthorized)
        } else {
            _error.value = message
        }
    }
}
