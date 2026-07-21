package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.ChoreCompletionEntity
import com.example.data.db.HouseholdMemberEntity
import com.example.data.model.ChoreDefinition
import com.example.data.model.DEFAULT_CHORES
import com.example.data.repository.ChoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CurrentTimeState(
    val hour: Int,
    val minute: Int,
    val isSimulated: Boolean = false
) {
    val totalMinutes: Int
        get() = hour * 60 + minute

    val formatted: String
        get() = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

data class GoogleUserAccount(
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isSignedIn: Boolean = false
)

data class ChoreUiModel(
    val chore: ChoreDefinition,
    val isCompleted: Boolean,
    val completionInfo: ChoreCompletionEntity? = null,
    val isOverdue: Boolean = false
)

data class ReminderAlertState(
    val group18hOverdueCount: Int = 0,
    val group21h30OverdueCount: Int = 0,
    val overdueTasks: List<ChoreDefinition> = emptyList(),
    val showBanner: Boolean = false
)

class ChoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChoreRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _selectedDateString = MutableStateFlow(todayDateString)
    val selectedDateString: StateFlow<String> = _selectedDateString.asStateFlow()

    private val _simulatedTime = MutableStateFlow<CurrentTimeState?>(null)
    private val _realTime = MutableStateFlow(getCurrentSystemTime())
    val currentTimeState: StateFlow<CurrentTimeState> = combine(_simulatedTime, _realTime) { sim, real ->
        sim ?: real
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getCurrentSystemTime())

    val members: StateFlow<List<HouseholdMemberEntity>>

    val completionsForSelectedDate: StateFlow<List<ChoreCompletionEntity>>

    val historyCompletions: StateFlow<List<ChoreCompletionEntity>>

    val choreUiList: StateFlow<List<ChoreUiModel>>

    val reminderAlertState: StateFlow<ReminderAlertState>

    val memberPointsMap: StateFlow<Map<Long, Int>>

    val dailyScore: StateFlow<Int>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChoreRepository(database.choreDao())

        viewModelScope.launch {
            repository.ensureDefaultMembers()
        }

        // Timer loop to update real time every minute
        viewModelScope.launch {
            while (true) {
                _realTime.value = getCurrentSystemTime()
                delay(30000) // update every 30 seconds
            }
        }

        members = repository.members.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        historyCompletions = repository.history.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        completionsForSelectedDate = _selectedDateString.flatMapLatest { date ->
            repository.getCompletionsForDate(date)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        choreUiList = combine(
            completionsForSelectedDate,
            currentTimeState,
            _selectedDateString
        ) { completions, currentTime, selectedDate ->
            val isToday = (selectedDate == todayDateString)
            DEFAULT_CHORES.map { chore ->
                val completion = completions.find { it.choreId == chore.id }
                val isCompleted = completion != null

                val choreDeadlineMinutes = chore.deadlineHour * 60 + chore.deadlineMinute
                val isOverdue = !isCompleted && isToday && (currentTime.totalMinutes >= choreDeadlineMinutes)

                ChoreUiModel(
                    chore = chore,
                    isCompleted = isCompleted,
                    completionInfo = completion,
                    isOverdue = isOverdue
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        reminderAlertState = combine(choreUiList, _selectedDateString) { chores, date ->
            val isToday = (date == todayDateString)
            if (!isToday) {
                ReminderAlertState()
            } else {
                val overdue18h = chores.filter { it.chore.is18hGroup && it.isOverdue }
                val overdue21h30 = chores.filter { !it.chore.is18hGroup && it.isOverdue }
                val allOverdue = chores.filter { it.isOverdue }.map { it.chore }

                ReminderAlertState(
                    group18hOverdueCount = overdue18h.size,
                    group21h30OverdueCount = overdue21h30.size,
                    overdueTasks = allOverdue,
                    showBanner = allOverdue.isNotEmpty()
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ReminderAlertState()
        )

        dailyScore = completionsForSelectedDate.combine(choreUiList) { completions, _ ->
            completions.sumOf { it.pointsEarned }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        memberPointsMap = historyCompletions.combine(members) { completionsList, memberList ->
            val map = HashMap<Long, Int>()
            memberList.forEach { map[it.id] = 0 }
            completionsList.forEach { comp ->
                map[comp.completedByMemberId] = (map[comp.completedByMemberId] ?: 0) + comp.pointsEarned
            }
            map
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )
    }

    private fun getCurrentSystemTime(): CurrentTimeState {
        val calendar = Calendar.getInstance()
        return CurrentTimeState(
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            isSimulated = false
        )
    }

    private val _googleUser = MutableStateFlow(
        GoogleUserAccount(
            name = "Karolaine Trabajo",
            email = "karolainetrabalho6@gmail.com",
            isSignedIn = true
        )
    )
    val googleUser: StateFlow<GoogleUserAccount> = _googleUser.asStateFlow()

    fun signInWithGoogle(name: String = "Karolaine Trabajo", email: String = "karolainetrabalho6@gmail.com") {
        _googleUser.value = GoogleUserAccount(
            name = name,
            email = email,
            isSignedIn = true
        )
    }

    fun signOutGoogle() {
        _googleUser.value = GoogleUserAccount(
            name = "",
            email = "",
            isSignedIn = false
        )
    }

    fun markChoreCompleted(choreId: String, member: HouseholdMemberEntity, photoUri: String? = null) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
            repository.markChoreCompleted(
                choreId = choreId,
                dateString = _selectedDateString.value,
                memberId = member.id,
                memberName = member.name,
                formattedTime = timeStr,
                photoUri = photoUri
            )
        }
    }

    fun unmarkChoreCompleted(choreId: String) {
        viewModelScope.launch {
            repository.unmarkChoreCompleted(choreId, _selectedDateString.value)
        }
    }

    fun addMember(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addMember(name.trim(), colorHex)
        }
    }

    fun deleteMember(memberId: Long) {
        viewModelScope.launch {
            repository.deleteMember(memberId)
        }
    }

    fun setSelectedDate(dateString: String) {
        _selectedDateString.value = dateString
    }

    fun setSimulatedTime(hour: Int, minute: Int) {
        _simulatedTime.value = CurrentTimeState(hour = hour, minute = minute, isSimulated = true)
    }

    fun resetSimulatedTime() {
        _simulatedTime.value = null
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteCompletionById(id)
        }
    }

    fun getFormattedSelectedDate(): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(_selectedDateString.value)
            val cal = Calendar.getInstance()
            val todayStr = todayDateString
            if (_selectedDateString.value == todayStr) {
                "Hoje (${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)})"
            } else {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date!!)
            }
        } catch (e: Exception) {
            _selectedDateString.value
        }
    }
}
