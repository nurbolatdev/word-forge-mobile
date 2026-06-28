package com.wordforge.data.repository

import com.wordforge.data.remote.AnswerRequest
import com.wordforge.data.remote.ListsApi
import com.wordforge.data.remote.QuizApi
import com.wordforge.data.remote.StartRoundRequest
import com.wordforge.domain.model.AnswerResult
import com.wordforge.domain.model.QuizQuestion
import com.wordforge.domain.model.QuizRound
import com.wordforge.util.Result
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizApi: QuizApi,
    private val listsApi: ListsApi
) {
    suspend fun getCardIds(listIds: List<Long>): Result<List<Long>> = safeCall {
        listIds.flatMap { listsApi.getCards(it) }.map { it.id }
    }

    suspend fun startRound(
        cardIds: List<Long>,
        modality: String,
        direction: String
    ): Result<QuizRound> = safeCall {
        quizApi.startRound(StartRoundRequest(cardIds, modality, direction))
    }

    suspend fun getQuestion(roundId: Long): Result<QuizQuestion> =
        safeCall { quizApi.getQuestion(roundId) }

    suspend fun submitAnswer(roundId: Long, request: AnswerRequest): Result<AnswerResult> =
        safeCall { quizApi.submitAnswer(roundId, request) }

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
