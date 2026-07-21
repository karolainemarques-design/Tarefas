package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {

    // Member Queries
    @Query("SELECT * FROM household_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<HouseholdMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: HouseholdMemberEntity): Long

    @Query("DELETE FROM household_members WHERE id = :memberId")
    suspend fun deleteMember(memberId: Long)

    @Query("SELECT COUNT(*) FROM household_members")
    suspend fun getMemberCount(): Int

    // Completion Queries
    @Query("SELECT * FROM chore_completions WHERE dateString = :dateString")
    fun getCompletionsForDate(dateString: String): Flow<List<ChoreCompletionEntity>>

    @Query("SELECT * FROM chore_completions WHERE dateString = :dateString AND choreId = :choreId LIMIT 1")
    suspend fun getCompletionForChoreAndDate(choreId: String, dateString: String): ChoreCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: ChoreCompletionEntity)

    @Query("DELETE FROM chore_completions WHERE choreId = :choreId AND dateString = :dateString")
    suspend fun deleteCompletion(choreId: String, dateString: String)

    @Query("SELECT * FROM chore_completions ORDER BY completedAtMillis DESC LIMIT 50")
    fun getAllCompletionsHistory(): Flow<List<ChoreCompletionEntity>>

    @Query("DELETE FROM chore_completions WHERE id = :completionId")
    suspend fun deleteCompletionById(completionId: Long)
}
