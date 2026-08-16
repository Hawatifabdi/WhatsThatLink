package com.whatsThatLink.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class PhishingResponse(
    val url: String,
    val prediction: String,
    @SerializedName("phishing_probability") val phishingProbability: Double,
    @SerializedName("legitimate_probability") val legitimateProbability: Double,
    val risk: String,
    val virustotal: VirusTotalData?
)

data class VirusTotalData(
    val available: Boolean,
    val status: String,
    val malicious: Int,
    val suspicious: Int,
    val harmless: Int,
    val undetected: Int
)

@Entity(tableName = "recent_scans")
data class RecentScan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val prediction: String,
    val phishingProbability: Double,
    val legitimateProbability: Double,
    val risk: String,
    val vtMalicious: Int,
    val vtSuspicious: Int,
    val vtAvailable: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
