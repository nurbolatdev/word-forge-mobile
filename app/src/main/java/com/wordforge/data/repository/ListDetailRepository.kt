package com.wordforge.data.repository

import com.wordforge.data.remote.AddCardRequest
import com.wordforge.data.remote.ListsApi
import com.wordforge.data.remote.UpdateTranslationsRequest
import com.wordforge.data.remote.VocabApi
import com.wordforge.domain.model.Card
import com.wordforge.domain.model.TranslateResult
import com.wordforge.util.Result
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListDetailRepository @Inject constructor(
    private val listsApi: ListsApi,
    private val vocabApi: VocabApi
) {
    suspend fun getCards(listId: Long): Result<List<Card>> =
        safeCall { listsApi.getCards(listId) }

    suspend fun searchWord(
        lemma: String,
        sourceLang: String,
        targetLang: String
    ): Result<TranslateResult> =
        safeCall { vocabApi.translate(lemma, sourceLang, targetLang) }

    suspend fun addCard(listId: Long, wordId: Long, lemma: String): Result<Card> =
        safeCall { listsApi.addCard(listId, AddCardRequest(wordId, lemma)) }

    suspend fun updateTranslations(
        listId: Long,
        cardId: Long,
        translationIds: List<Long>
    ): Result<Card> =
        safeCall { listsApi.updateTranslations(listId, cardId, UpdateTranslationsRequest(translationIds)) }

    suspend fun deleteCard(listId: Long, cardId: Long): Result<Unit> =
        safeCall { listsApi.deleteCard(listId, cardId) }

    private suspend fun <T> safeCall(call: suspend () -> T): Result<T> = try {
        Result.Success(call())
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> Result.Error("UNAUTHORIZED")
            else -> Result.Error("Server error: ${e.code()}")
        }
    } catch (e: IOException) {
        Result.Error("Cannot connect to server")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
