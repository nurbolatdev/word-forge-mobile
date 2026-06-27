package com.wordforge.data.remote

import com.wordforge.domain.model.AnswerResult
import com.wordforge.domain.model.QuizQuestion
import com.wordforge.domain.model.QuizRound
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class StartRoundRequest(val cardIds: List<Long>, val modality: String, val direction: String)
data class AnswerRequest(
    val cardId: Long,
    val chosenTranslationId: Long? = null,
    val typedAnswer: String? = null,
    val responseTimeMs: Long
)

interface QuizApi {
    @POST("api/quiz/rounds")
    suspend fun startRound(@Body request: StartRoundRequest): QuizRound

    @GET("api/quiz/rounds/{id}/question")
    suspend fun getQuestion(@Path("id") roundId: Long): QuizQuestion

    @POST("api/quiz/rounds/{id}/answer")
    suspend fun submitAnswer(@Path("id") roundId: Long, @Body request: AnswerRequest): AnswerResult
}
