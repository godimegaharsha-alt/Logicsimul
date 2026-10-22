package com.example.logiclab.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_circuits")
data class CircuitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val circuitJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
