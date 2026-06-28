package com.wordforge.data.repository

import com.wordforge.data.remote.CreateListRequest
import com.wordforge.data.remote.ListsApi
import com.wordforge.domain.model.WordList
import com.wordforge.util.Result
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListsRepository @Inject constructor(
    private val listsApi: ListsApi
) {
    suspend fun getLists(): Result<List<WordList>> =
        safeCall { listsApi.getLists() }

    suspend fun createList(title: String): Result<WordList> =
        safeCall { listsApi.createList(CreateListRequest(title, "EN", "RU")) }

    suspend fun deleteList(id: Long): Result<Unit> =
        safeCall { listsApi.deleteList(id) }

    private suspend fun <T> safeCall(call: suspend () -> T): Result<T> = try {
        Result.Success(call())
    } catch (e: HttpException) {
        when (e.code()) {
            401 -> Result.Error("UNAUTHORIZED")
            else -> Result.Error("Ошибка сервера: ${e.code()}")
        }
    } catch (e: IOException) {
        Result.Error("Cannot connect to server")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
