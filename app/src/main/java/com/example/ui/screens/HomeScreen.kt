package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.R
import com.example.data.db.HouseholdMemberEntity
import com.example.data.model.ChoreDefinition
import com.example.ui.components.AddMemberDialog
import com.example.ui.components.ChoreCard
import com.example.ui.components.HistoryDialog
import com.example.ui.components.MemberSelectionDialog
import com.example.ui.components.RankingCard
import com.example.ui.components.ReminderBanner
import com.example.ui.components.TimeSimulationBar
import com.example.ui.viewmodel.ChoreUiModel
import com.example.ui.viewmodel.ChoreViewModel

import androidx.compose.material.icons.filled.AccountCircle
import com.example.ui.components.CameraProofDialog
import com.example.ui.components.GoogleAccountDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChoreViewModel,
    modifier: Modifier = Modifier
) {
    val choreList by viewModel.choreUiList.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val memberPointsMap by viewModel.memberPointsMap.collectAsStateWithLifecycle()
    val reminderState by viewModel.reminderAlertState.collectAsStateWithLifecycle()
    val currentTimeState by viewModel.currentTimeState.collectAsStateWithLifecycle()
    val dailyScore by viewModel.dailyScore.collectAsStateWithLifecycle()
    val historyList by viewModel.historyCompletions.collectAsStateWithLifecycle()
    val googleUser by viewModel.googleUser.collectAsStateWithLifecycle()
    val selectedDateFormatted = viewModel.getFormattedSelectedDate()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "18H", "21H"

    var activeChoreForMemberSelect by remember { mutableStateOf<ChoreDefinition?>(null) }
    var pendingCameraProof by remember { mutableStateOf<Pair<ChoreDefinition, HouseholdMemberEntity>?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tarefas Domésticas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = selectedDateFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Google Account Action Chip
                    Surface(
                        onClick = { showGoogleDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF4285F4).copy(alpha = 0.15f),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("google_account_top_bar_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4285F4),
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (googleUser.isSignedIn) googleUser.name.split(" ").first() else "Entrar",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A73E8)
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Histórico",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMemberDialog = true },
                modifier = Modifier.testTag("fab_add_member"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar membro",
                    tint = Color.White
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .drawVerticalScrollbar(listState),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_chores_1784657344797),
                            contentDescription = "Banner da casa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Organização Diária",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "5 tarefas • 5 pontos cada • Total 25 pts",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Quick Jump & Filter Navigation Chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "ALL",
                            onClick = {
                                selectedCategoryFilter = "ALL"
                                coroutineScope.launch { listState.animateScrollToItem(0) }
                            },
                            label = { Text("Todas (5)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "18H",
                            onClick = {
                                selectedCategoryFilter = "18H"
                                coroutineScope.launch { listState.animateScrollToItem(4) }
                            },
                            label = { Text("Até 18:00 (3)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "21H",
                            onClick = {
                                selectedCategoryFilter = "21H"
                                coroutineScope.launch { listState.animateScrollToItem(8) }
                            },
                            label = { Text("Até 21:30 (2)") }
                        )
                    }
                }
            }

            // Time Simulation Control Bar
            item {
                TimeSimulationBar(
                    currentTimeState = currentTimeState,
                    onSetSimulatedTime = { h, m -> viewModel.setSimulatedTime(h, m) },
                    onResetTime = { viewModel.resetSimulatedTime() }
                )
            }

            // Active Reminder Banner
            item {
                ReminderBanner(
                    reminderState = reminderState,
                    currentTimeFormatted = currentTimeState.formatted
                )
            }

            // Household Leaderboard & Ranking Card
            item {
                RankingCard(
                    members = members,
                    memberPointsMap = memberPointsMap,
                    dailyScore = dailyScore,
                    onAddMemberClick = { showAddMemberDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Section Header: Group 18h00
            if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "18H") {
                item {
                    SectionHeader(
                        title = "Grupo 18h00 (Até às 18:00)",
                        subtitle = "Limpar casa, Lavar louça e Limpar quintal",
                        badgeColor = MaterialTheme.colorScheme.primary
                    )
                }

                // Group 18h00 Tasks
                val group18h = choreList.filter { it.chore.is18hGroup }
                items(group18h, key = { "chore_${it.chore.id}" }) { choreModel ->
                    ChoreCard(
                        choreModel = choreModel,
                        onCompleteClick = { activeChoreForMemberSelect = choreModel.chore },
                        onUncompleteClick = { viewModel.unmarkChoreCompleted(choreModel.chore.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Section Header: Group 21h30
            if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == "21H") {
                item {
                    SectionHeader(
                        title = "Grupo 21h30 (Até às 21:30)",
                        subtitle = "Alimentar os cachorros e Preparar a janta",
                        badgeColor = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Group 21h30 Tasks
                val group21h30 = choreList.filter { !it.chore.is18hGroup }
                items(group21h30, key = { "chore_${it.chore.id}" }) { choreModel ->
                    ChoreCard(
                        choreModel = choreModel,
                        onCompleteClick = { activeChoreForMemberSelect = choreModel.chore },
                        onUncompleteClick = { viewModel.unmarkChoreCompleted(choreModel.chore.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    // Dialog: Member Selection
    activeChoreForMemberSelect?.let { chore ->
        MemberSelectionDialog(
            choreTitle = chore.title,
            members = members,
            onMemberSelect = { member ->
                val currentChore = activeChoreForMemberSelect
                activeChoreForMemberSelect = null
                if (currentChore != null) {
                    pendingCameraProof = Pair(currentChore, member)
                }
            },
            onAddNewMemberClick = {
                activeChoreForMemberSelect = null
                showAddMemberDialog = true
            },
            onDismiss = { activeChoreForMemberSelect = null }
        )
    }

    // Dialog: Camera Photo Proof Capture (Mandatory live photo without gallery)
    pendingCameraProof?.let { (chore, member) ->
        CameraProofDialog(
            chore = chore,
            member = member,
            onPhotoConfirmed = { photoUri ->
                viewModel.markChoreCompleted(
                    choreId = chore.id,
                    member = member,
                    photoUri = photoUri
                )
                pendingCameraProof = null
            },
            onDismiss = { pendingCameraProof = null }
        )
    }

    // Dialog: Google Account Sign-In
    if (showGoogleDialog) {
        GoogleAccountDialog(
            googleUser = googleUser,
            onSignInClick = { viewModel.signInWithGoogle() },
            onSignOutClick = { viewModel.signOutGoogle() },
            onDismiss = { showGoogleDialog = false }
        )
    }

    // Dialog: Add Member
    if (showAddMemberDialog) {
        AddMemberDialog(
            onConfirm = { name, colorHex ->
                viewModel.addMember(name, colorHex)
                showAddMemberDialog = false
            },
            onDismiss = { showAddMemberDialog = false }
        )
    }

    // Dialog: History Log
    if (showHistoryDialog) {
        HistoryDialog(
            historyList = historyList,
            onDeleteHistoryItem = { id -> viewModel.deleteHistoryRecord(id) },
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    badgeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 6.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 18.dp)
        )
    }
}

private fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    color: Color = Color(0xFF1E88E5),
    trackColor: Color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
    width: Dp = 6.dp
): Modifier = this.drawWithContent {
    drawContent()

    val totalItems = state.layoutInfo.totalItemsCount
    if (totalItems <= 0) return@drawWithContent

    val visibleItemsInfo = state.layoutInfo.visibleItemsInfo
    if (visibleItemsInfo.isEmpty()) return@drawWithContent

    val viewportHeight = size.height
    val trackWidthPx = width.toPx()
    val xOffset = size.width - trackWidthPx - 2.dp.toPx()

    // Draw scrollbar background track
    drawRoundRect(
        color = trackColor,
        topLeft = Offset(xOffset, 4.dp.toPx()),
        size = Size(trackWidthPx, viewportHeight - 8.dp.toPx()),
        cornerRadius = CornerRadius(trackWidthPx / 2, trackWidthPx / 2)
    )

    // Calculate thumb height and offset
    val visibleItemsCount = visibleItemsInfo.size
    val heightFraction = (visibleItemsCount.toFloat() / totalItems.toFloat()).coerceIn(0.15f, 1.0f)
    val thumbHeight = (viewportHeight - 8.dp.toPx()) * heightFraction

    val firstItemIndex = visibleItemsInfo.first().index
    val maxScrollIndex = (totalItems - visibleItemsCount).coerceAtLeast(1)
    val scrollProgress = (firstItemIndex.toFloat() / maxScrollIndex.toFloat()).coerceIn(0f, 1f)
    val thumbOffsetY = 4.dp.toPx() + (viewportHeight - 8.dp.toPx() - thumbHeight) * scrollProgress

    // Draw active scrollbar thumb
    drawRoundRect(
        color = color,
        topLeft = Offset(xOffset, thumbOffsetY),
        size = Size(trackWidthPx, thumbHeight),
        cornerRadius = CornerRadius(trackWidthPx / 2, trackWidthPx / 2)
    )
}
