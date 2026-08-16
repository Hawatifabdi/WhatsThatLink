package com.whatsThatLink.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PhishingApi {
    @POST("predict")
    suspend fun predict(@Body request: PhishingRequest): Response<PhishingResponse>
}

data class PhishingRequest(
    val url: String
)
