package com.whatsThatLink.app.data

import android.content.Context
import com.whatsThatLink.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PhishingRepository(private val context: Context) {

    private val db = WhatsThatLinkDatabase.getDatabase(context)
    private val scanDao = db.scanDao()

    private val api: PhishingApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PhishingApi::class.java)
    }

    fun getAllScans() = scanDao.getAllScans()

    suspend fun scanUrl(url: String): Result<RecentScan> {
        return try {
            val response = api.predict(PhishingRequest(url))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val scan = RecentScan(
                    url = body.url,
                    prediction = body.prediction,
                    phishingProbability = body.phishingProbability,
                    legitimateProbability = body.legitimateProbability,
                    risk = body.risk,
                    vtMalicious = body.virustotal?.malicious ?: 0,
                    vtSuspicious = body.virustotal?.suspicious ?: 0,
                    vtAvailable = body.virustotal?.available ?: false
                )
                val id = scanDao.insertScan(scan)
                Result.success(scan.copy(id = id))
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScanById(id: Long) = scanDao.getScanById(id)
}
