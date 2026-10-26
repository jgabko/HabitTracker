package com.example.habittracker.model

/** Os 5 atributos do avatar. */
enum class Atributo(val rotulo: String, val emoji: String) {
    FORCA("Força", "💪"),
    VITALIDADE("Vitalidade", "❤️"),
    SABEDORIA("Sabedoria", "🧠"),
    FOCO("Foco", "🎯"),
    DISCIPLINA("Disciplina", "🛡️")
}

/** Categoria de um hábito. Cada categoria alimenta um atributo do avatar. */
enum class Categoria(val rotulo: String, val emoji: String, val atributo: Atributo) {
    CORPO("Corpo", "🏋️", Atributo.FORCA),
    SAUDE("Saúde", "💧", Atributo.VITALIDADE),
    MENTE("Mente", "📖", Atributo.SABEDORIA),
    TRABALHO("Trabalho", "💻", Atributo.FOCO),
    DISCIPLINA("Disciplina", "🛡️", Atributo.DISCIPLINA)
}
