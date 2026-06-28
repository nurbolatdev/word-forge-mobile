package com.wordforge.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordforge.data.remote.AnswerRequest
import com.wordforge.data.repository.AuthRepository
import com.wordforge.data.repository.ListsRepository
import com.wordforge.data.repository.QuizRepository
import com.wordforge.domain.model.QuizQuestion
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

enum class QuizPhase { IDLE, LOADING, QUESTION, RESULT, DONE }

sealed class QuizEvent {
    object Unauthorized : QuizEvent()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val listsRepository: ListsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // List selection
    private val _lists = MutableStateFlow<List<WordList>>(emptyList())
    val lists: StateFlow<List<WordList>> = _lists.asStateFlow()

    private val _selectedListIds = MutableStateFlow<Set<Long>?>(null) // null = All
    val selectedListIds: StateFlow<Set<Long>?> = _selectedListIds.asStateFlow()

    // Mode selection
    private val _direction = MutableStateFlow("EN_TO_RU")
    val direction: StateFlow<String> = _direction.asStateFlow()

    // Quiz state
    private val _phase = MutableStateFlow(QuizPhase.IDLE)
    val phase: StateFlow<QuizPhase> = _phase.asStateFlow()

    private val _question = MutableStateFlow<QuizQuestion?>(null)
    val question: StateFlow<QuizQuestion?> = _question.asStateFlow()

    private val _answerResult = MutableStateFlow<com.wordforge.domain.model.AnswerResult?>(null)
    val answerResult: StateFlow<com.wordforge.domain.model.AnswerResult?> = _answerResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _events = MutableSharedFlow<QuizEvent>()
    val events: SharedFlow<QuizEvent> = _events.asSharedFlow()

    private var roundId: Long = -1
    private var questionStartMs: Long = 0L

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            when (val result = listsRepository.getLists()) {
                is Result.Success -> _lists.value = result.data
                is Result.Error -> handleError(result.message)
                else -> Unit
            }
        }
    }

    fun toggleListSelection(listId: Long) {
        val current = _selectedListIds.value?.toMutableSet() ?: mutableSetOf()
        if (listId in current) current.remove(listId) else current.add(listId)
        _selectedListIds.value = if (current.isEmpty()) null else current
    }

    fun selectAll() { _selectedListIds.value = null }

    fun setDirection(dir: String) { _direction.value = dir }

    fun startRound(modality: String) {
        viewModelScope.launch {
            _phase.value = QuizPhase.LOADING
            _question.value = null
            _answerResult.value = null

            val listIds = _selectedListIds.value?.toList()
                ?: _lists.value.map { it.id }

            val cardIdsResult = quizRepository.getCardIds(listIds)
            if (cardIdsResult is Result.Error) {
                handleError(cardIdsResult.message)
                _phase.value = QuizPhase.IDLE
                return@launch
            }
            val cardIds = (cardIdsResult as Result.Success).data
            if (cardIds.isEmpty()) {
                _error.value = "No cards in selected lists"
                _phase.value = QuizPhase.IDLE
                return@launch
            }

            when (val roundResult = quizRepository.startRound(cardIds, modality, _direction.value)) {
                is Result.Success -> {
                    roundId = roundResult.data.id
                    fetchQuestion()
                }
                is Result.Error -> {
                    handleError(roundResult.message)
                    _phase.value = QuizPhase.IDLE
                }
                else -> Unit
            }
        }
    }

    fun submitMcq(translationId: Long) {
        val cardId = _question.value?.cardId ?: return
        submit(AnswerRequest(cardId = cardId, chosenTranslationId = translationId, responseTimeMs = elapsed()))
    }

    fun submitTyped(text: String) {
        val cardId = _question.value?.cardId ?: return
        submit(AnswerRequest(cardId = cardId, typedAnswer = text, responseTimeMs = elapsed()))
    }

    fun next() {
        val result = _answerResult.value ?: return
        when {
            result.roundFinished -> _phase.value = QuizPhase.DONE
            result.nextQuestion != null -> {
                _question.value = result.nextQuestion
                _answerResult.value = null
                questionStartMs = System.currentTimeMillis()
                _phase.value = QuizPhase.QUESTION
            }
            else -> viewModelScope.launch { fetchQuestion() }
        }
    }

    fun resetQuiz() {
        _phase.value = QuizPhase.IDLE
        _question.value = null
        _answerResult.value = null
        roundId = -1
    }

    fun clearError() { _error.value = null }

    private fun submit(request: AnswerRequest) {
        viewModelScope.launch {
            when (val result = quizRepository.submitAnswer(roundId, request)) {
                is Result.Success -> {
                    _answerResult.value = result.data
                    _phase.value = QuizPhase.RESULT
                }
                is Result.Error -> handleError(result.message)
                else -> Unit
            }
        }
    }

    private suspend fun fetchQuestion() {
        when (val result = quizRepository.getQuestion(roundId)) {
            is Result.Success -> {
                _question.value = result.data
                questionStartMs = System.currentTimeMillis()
                _phase.value = QuizPhase.QUESTION
            }
            is Result.Error -> {
                handleError(result.message)
                _phase.value = QuizPhase.IDLE
            }
            else -> Unit
        }
    }

    private suspend fun handleError(message: String) {
        if (message == "UNAUTHORIZED") {
            authRepository.signOut()
            _events.emit(QuizEvent.Unauthorized)
        } else {
            _error.value = message
        }
    }

    private fun elapsed() = System.currentTimeMillis() - questionStartMs
}
