package com.wordforge.domain.model

data class WordList(
    val id: Long,
    val userId: Long,
    val title: String,
    val sourceLang: String,
    val targetLang: String,
    val wordCount: Int,
    val createdAt: String
)

data class Card(
    val id: Long,
    val listId: Long,
    val wordId: Long,
    val lemma: String,
    val chosenTranslationIds: List<Long>,
    val translations: List<String>,
    val status: String,
    val createdAt: String
)

data class TranslateResult(
    val wordId: Long,
    val lemma: String,
    val sourceLang: String,
    val targetLang: String,
    val suggestions: List<TranslateSuggestion>
)

data class TranslateSuggestion(
    val id: Long,
    val text: String,
    val provider: String
)

data class QuizRound(
    val id: Long,
    val totalCards: Int,
    val answeredCards: Int,
    val modality: String,
    val direction: String,
    val finished: Boolean
)

data class QuizQuestion(
    val cardId: Long,
    val lemma: String,
    val promptText: String,
    val direction: String,
    val questionIndex: Int,
    val totalCards: Int,
    val modality: String,
    val clozeText: String?,
    val options: List<QuizOption>
)

data class QuizOption(
    val translationId: Long,
    val text: String
)

data class AnswerResult(
    val cardId: Long,
    val correct: Boolean,
    val grade: Int,
    val correctTranslationId: Long,
    val correctTranslationText: String,
    val roundFinished: Boolean,
    val nextQuestion: QuizQuestion?
)
