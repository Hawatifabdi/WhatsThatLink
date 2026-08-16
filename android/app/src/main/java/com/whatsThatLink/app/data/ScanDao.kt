package com.whatsThatLink.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM recent_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<RecentScan>>

    @Insert
    suspend fun insertScan(scan: RecentScan): Long

    @Query("SELECT * FROM recent_scans WHERE id = :id")
    suspend fun getScanById(id: Long): RecentScan?

    @Query("DELETE FROM recent_scans")
    suspend fun clearHistory()
}
