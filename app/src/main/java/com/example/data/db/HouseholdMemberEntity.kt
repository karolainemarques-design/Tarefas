package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_members")
data class HouseholdMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarColorHex: String = "#3F51B5",
    val createdAt: Long = System.currentTimeMillis()
)
