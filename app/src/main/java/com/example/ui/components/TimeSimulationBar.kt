package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.CurrentTimeState

@Composable
fun TimeSimulationBar(
    currentTimeState: CurrentTimeState,
    onSetSimulatedTime: (Int, Int) -> Unit,
    onResetTime: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("time_simulation_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentTimeState.isSimulated)
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Relógio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Horário do Sistema: ${currentTimeState.formatted}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (currentTimeState.isSimulated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "SIMULADO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentTimeState.isSimulated) {
                        IconButton(
                            onClick = onResetTime,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("reset_time_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restaurar Horário Real",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.testTag("toggle_time_sim_button")
                    ) {
                        Text(
                            text = if (expanded) "Ocultar Testes" else "Testar Horários",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Simule o horário para testar os lembretes do grupo das 18h00 e 21h30:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { onSetSimulatedTime(14, 0) },
                            label = { Text("14:00 (Normal)") },
                            modifier = Modifier.testTag("chip_14h")
                        )
                        AssistChip(
                            onClick = { onSetSimulatedTime(18, 15) },
                            label = { Text("18:15 (> 18h)") },
                            modifier = Modifier.testTag("chip_18h")
                        )
                        AssistChip(
                            onClick = { onSetSimulatedTime(21, 45) },
                            label = { Text("21:45 (> 21h30)") },
                            modifier = Modifier.testTag("chip_21h30")
                        )
                    }
                }
            }
        }
    }
}
