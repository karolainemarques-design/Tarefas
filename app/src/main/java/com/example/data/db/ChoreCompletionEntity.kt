package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chore_completions")
data class ChoreCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val choreId: String,
    val dateString: String, // Format: YYYY-MM-DD
    val completedByMemberId: Long,
    val completedByMemberName: String,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val formattedTime: String, // e.g. "14:35"
    val pointsEarned: Int = 5,
    val photoUri: String? = null // Path to captured photo proof
)
