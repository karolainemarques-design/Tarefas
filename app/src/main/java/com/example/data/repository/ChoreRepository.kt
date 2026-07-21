package com.example.data.repository

import com.example.data.db.ChoreCompletionEntity
import com.example.data.db.ChoreDao
import com.example.data.db.HouseholdMemberEntity
import kotlinx.coroutines.flow.Flow

class ChoreRepository(private val choreDao: ChoreDao) {

    val members: Flow<List<HouseholdMemberEntity>> = choreDao.getAllMembers()
    val history: Flow<List<ChoreCompletionEntity>> = choreDao.getAllCompletionsHistory()

    fun getCompletionsForDate(dateString: String): Flow<List<ChoreCompletionEntity>> {
        return choreDao.getCompletionsForDate(dateString)
    }

    suspend fun markChoreCompleted(
        choreId: String,
        dateString: String,
        memberId: Long,
        memberName: String,
        formattedTime: String,
        photoUri: String? = null
    ) {
        val completion = ChoreCompletionEntity(
            choreId = choreId,
            dateString = dateString,
            completedByMemberId = memberId,
            completedByMemberName = memberName,
            formattedTime = formattedTime,
            pointsEarned = 5,
            photoUri = photoUri
        )
        choreDao.insertCompletion(completion)
    }

    suspend fun unmarkChoreCompleted(choreId: String, dateString: String) {
        choreDao.deleteCompletion(choreId, dateString)
    }

    suspend fun addMember(name: String, colorHex: String) {
        val member = HouseholdMemberEntity(
            name = name,
            avatarColorHex = colorHex
        )
        choreDao.insertMember(member)
    }

    suspend fun deleteMember(memberId: Long) {
        choreDao.deleteMember(memberId)
    }

    suspend fun ensureDefaultMembers() {
        if (choreDao.getMemberCount() == 0) {
            choreDao.insertMember(HouseholdMemberEntity(name = "Karolaine", avatarColorHex = "#E91E63"))
            choreDao.insertMember(HouseholdMemberEntity(name = "João", avatarColorHex = "#2196F3"))
            choreDao.insertMember(HouseholdMemberEntity(name = "Maria", avatarColorHex = "#4CAF50"))
        }
    }

    suspend fun deleteCompletionById(id: Long) {
        choreDao.deleteCompletionById(id)
    }
}
