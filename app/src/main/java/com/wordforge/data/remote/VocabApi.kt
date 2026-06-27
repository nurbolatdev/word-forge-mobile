package com.wordforge.data.remote

import com.wordforge.domain.model.TranslateResult
import retrofit2.http.GET
import retrofit2.http.Query

interface VocabApi {
    @GET("api/vocabulary/words/translate")
    suspend fun translate(
        @Query("lemma") lemma: String,
        @Query("sourceLang") sourceLang: String,
        @Query("targetLang") targetLang: String
    ): TranslateResult
}
