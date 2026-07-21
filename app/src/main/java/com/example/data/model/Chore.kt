package com.example.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ChoreDefinition(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val deadlineHour: Int,
    val deadlineMinute: Int,
    val points: Int = 5
) {
    val formattedDeadline: String
        get() = String.format("%02dh%02d", deadlineHour, deadlineMinute)

    val is18hGroup: Boolean
        get() = deadlineHour <= 18
}

val DEFAULT_CHORES = listOf(
    ChoreDefinition(
        id = "limpar_casa",
        title = "Limpar casa",
        category = "Casa",
        description = "Organizar e limpar a sala, quartos e áreas comuns",
        deadlineHour = 18,
        deadlineMinute = 0,
        points = 5
    ),
    ChoreDefinition(
        id = "lavar_louca",
        title = "Lavar louça",
        category = "Cozinha",
        description = "Lavar, secar e guardar os pratos e panelas",
        deadlineHour = 18,
        deadlineMinute = 0,
        points = 5
    ),
    ChoreDefinition(
        id = "limpar_quintal",
        title = "Limpar quintal",
        category = "Exterior",
        description = "Varrer a varanda, quintal e recolher sujeiras",
        deadlineHour = 18,
        deadlineMinute = 0,
        points = 5
    ),
    ChoreDefinition(
        id = "alimentar_cachorros",
        title = "Alimentar os cachorros",
        category = "Pets",
        description = "Colocar ração limpa e trocar a água dos cães",
        deadlineHour = 21,
        deadlineMinute = 30,
        points = 5
    ),
    ChoreDefinition(
        id = "preparar_janta",
        title = "Preparar a janta",
        category = "Alimentação",
        description = "Cozinhar a refeição da noite para todos",
        deadlineHour = 21,
        deadlineMinute = 30,
        points = 5
    )
)
