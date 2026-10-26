package com.example.habittracker.data

import com.example.habittracker.model.TipoHabito

/** Uma medalha. Só leitura: é calculada a partir do histórico, não é guardada em lugar nenhum. */
data class Conquista(
    val emoji: String,
    val titulo: String,
    val descricao: String,
    val desbloqueada: Boolean
)

/** Lista de medalhas do app, com o status atual de cada uma (calculado a partir do AppState). */
fun listaDeConquistas(estado: AppState): List<Conquista> {
    val resgates = estado.recompensas.sumOf { it.vezesResgatada }
    return listOf(
        Conquista("🌱", "Primeiro passo", "Conclua 1 hábito", estado.totalConclusoesGeral() >= 1),
        Conquista("🔥", "Semana de fogo", "7 dias seguidos com atividade", estado.ofensivaGeral() >= 7),
        Conquista("💯", "Meio cento", "50 conclusões no total", estado.totalConclusoesGeral() >= 50),
        Conquista("🎯", "Foco total", "100 min de foco no total", estado.minutosFocoTotal() >= 100),
        Conquista("⏱️", "Mestre Pomodoro", "5 sessões de foco", estado.totalSessoesFoco() >= 5),
        Conquista(
            "🛡️", "Autocontrole", "7 dias sem um hábito de evitar",
            estado.habitos.any { it.tipo == TipoHabito.EVITAR && estado.diasSem(it) >= 7 }
        ),
        Conquista("🎁", "Merecido", "Resgate 1 recompensa", resgates >= 1),
        Conquista("⭐", "Nível 3", "Chegue ao nível 3", estado.nivel() >= 3)
    )
}
