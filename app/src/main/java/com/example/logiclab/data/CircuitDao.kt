package com.example.logiclab.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CircuitDao {

    @Query("SELECT * FROM saved_circuits ORDER BY updatedAt DESC")
    fun getAllCircuits(): Flow<List<CircuitEntity>>

    @Query("SELECT * FROM saved_circuits WHERE id = :id LIMIT 1")
    suspend fun getCircuitById(id: Long): CircuitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCircuit(circuit: CircuitEntity): Long

    @Query("DELETE FROM saved_circuits WHERE id = :id")
    suspend fun deleteCircuitById(id: Long)
}
