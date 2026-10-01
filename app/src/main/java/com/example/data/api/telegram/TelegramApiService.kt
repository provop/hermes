package com.example.data.api.telegram

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TelegramApiService {

    @GET("bot{token}/getMe")
    suspend fun getMe(
        @Path("token") token: String
    ): Response<TelegramResponse<TelegramUserDto>>

    @POST("bot{token}/sendMessage")
    suspend fun sendMessage(
        @Path("token") token: String,
        @Body request: SendMessageRequest
    ): Response<TelegramResponse<TelegramMessageDto>>

    @GET("bot{token}/getUpdates")
    suspend fun getUpdates(
        @Path("token") token: String,
        @Query("offset") offset: Long? = null,
        @Query("limit") limit: Int? = 10,
        @Query("timeout") timeout: Int? = 5
    ): Response<TelegramResponse<List<TelegramUpdateDto>>>
}
