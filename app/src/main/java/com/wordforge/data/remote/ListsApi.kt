package com.wordforge.data.remote

import com.wordforge.domain.model.Card
import com.wordforge.domain.model.TranslateResult
import com.wordforge.domain.model.WordList
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CreateListRequest(val title: String, val sourceLang: String, val targetLang: String)
data class AddCardRequest(val wordId: Long, val lemma: String)
data class UpdateTranslationsRequest(val translationIds: List<Long>)

interface ListsApi {
    @GET("api/lists")
    suspend fun getLists(): List<WordList>

    @POST("api/lists")
    suspend fun createList(@Body request: CreateListRequest): WordList

    @DELETE("api/lists/{id}")
    suspend fun deleteList(@Path("id") id: Long)

    @GET("api/lists/{id}/cards")
    suspend fun getCards(@Path("id") listId: Long): List<Card>

    @POST("api/lists/{id}/cards")
    suspend fun addCard(@Path("id") listId: Long, @Body request: AddCardRequest): Card

    @PATCH("api/lists/{id}/cards/{cid}/translations")
    suspend fun updateTranslations(
        @Path("id") listId: Long,
        @Path("cid") cardId: Long,
        @Body request: UpdateTranslationsRequest
    ): Card

    @DELETE("api/lists/{id}/cards/{cid}")
    suspend fun deleteCard(@Path("id") listId: Long, @Path("cid") cardId: Long)
}
