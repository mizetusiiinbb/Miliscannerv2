package com.miliconfig.ipv6scanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ScanResultEntity>)

    @Query("SELECT * FROM scan_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_results WHERE sessionId = :sessionId ORDER BY port ASC")
    suspend fun getResultsForSession(sessionId: Long): List<ScanResultEntity>

    @Query("DELETE FROM scan_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM scan_sessions")
    suspend fun clearAllSessions()
}
