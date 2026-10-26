package com.example.habittracker.data

import com.example.habittracker.model.*

/** O mesmo conjunto de exemplos do ZIP do Trabalho 2. Usado apenas na primeira
 * instalação e na restauração explicitamente confirmada nas configurações. */
internal class DadosExemplo {
    val habitos = mutableListOf<Habito>()
    val recompensas = mutableListOf<Recompensa>()
    val registros = mutableListOf<Registro>()
    private var proximoIdRegistro = 1
    private fun registrarEmDia(dia: Int, tipo: TipoRegistro, id: Int, minutos: Int = 0) {
        val habito = habitos.first { it.id == id }
        registros.add(Registro(proximoIdRegistro++, dia, tipo, id, habito.nome, habito.categoria, if (tipo == TipoRegistro.CONCLUSAO) habito.minutosRecompensa else minutos))
    }
    private fun concluirNosDias(id: Int, dias: List<Int>) {
        dias.forEach { registrarEmDia(it, TipoRegistro.CONCLUSAO, id) }
    }
    init {
        habitos.addAll(
            listOf(
                Habito(id = 1, nome = "Meditar 10 min", tipo = TipoHabito.FAZER, categoria = Categoria.MENTE,
                    emoji = "🧘", periodo = Periodo.MANHA, minutosRecompensa = 10),
                Habito(id = 2, nome = "Leitura", tipo = TipoHabito.FAZER, categoria = Categoria.MENTE,
                    emoji = "📖", periodo = Periodo.NOITE, minutosRecompensa = 10),
                Habito(id = 3, nome = "Exercício", tipo = TipoHabito.FAZER, categoria = Categoria.CORPO,
                    emoji = "🏋️", periodo = Periodo.MANHA, minutosRecompensa = 15),
                Habito(id = 4, nome = "Estudar idioma", tipo = TipoHabito.FAZER, categoria = Categoria.MENTE,
                    emoji = "🌎", periodo = Periodo.TARDE, minutosRecompensa = 10),
                Habito(id = 5, nome = "Beber 2L de água", tipo = TipoHabito.FAZER, categoria = Categoria.SAUDE,
                    emoji = "💧", periodo = Periodo.TARDE, minutosRecompensa = 5),
                Habito(id = 6, nome = "Sem redes sociais após 22h", tipo = TipoHabito.EVITAR,
                    categoria = Categoria.DISCIPLINA, emoji = "📵", periodo = Periodo.NOITE,
                    minutosRecompensa = 15, ultimaRecaida = 10),
                Habito(id = 7, nome = "Sem refrigerante", tipo = TipoHabito.EVITAR,
                    categoria = Categoria.DISCIPLINA, emoji = "🥤", periodo = Periodo.TARDE,
                    minutosRecompensa = 10, ultimaRecaida = 3),
                Habito(id = 8, nome = "Estudar o projeto", tipo = TipoHabito.FAZER, categoria = Categoria.TRABALHO,
                    emoji = "💻", periodo = Periodo.TARDE, minutosRecompensa = 10)
            )
        )

        // Dias (do mês fictício) em que cada hábito foi concluído
        concluirNosDias(1, listOf(3, 5, 6, 10, 11, 12, 13, 14, 15))
        concluirNosDias(2, (3..14).toList())
        concluirNosDias(3, listOf(2, 4, 6, 8, 9, 11, 13))
        concluirNosDias(4, listOf(5, 6, 9, 10, 11, 12, 13, 14, 15))
        concluirNosDias(5, (1..14).filter { it != 4 && it != 9 })
        concluirNosDias(6, (1..9).toList() + (11..14).toList())
        concluirNosDias(7, (4..14).toList())
        concluirNosDias(8, listOf(2, 3, 4, 5, 6, 8, 9, 10, 12, 13))

        // Recaídas dos hábitos EVITAR
        registrarEmDia(10, TipoRegistro.RECAIDA, 6)
        registrarEmDia(3, TipoRegistro.RECAIDA, 7)

        // Sessões de foco de exemplo (dia, id do hábito, minutos)
        registrarEmDia(3, TipoRegistro.FOCO, 8, 25)
        registrarEmDia(5, TipoRegistro.FOCO, 8, 25)
        registrarEmDia(6, TipoRegistro.FOCO, 8, 50)
        registrarEmDia(8, TipoRegistro.FOCO, 8, 25)
        registrarEmDia(9, TipoRegistro.FOCO, 8, 25)
        registrarEmDia(10, TipoRegistro.FOCO, 8, 25)
        registrarEmDia(12, TipoRegistro.FOCO, 8, 50)
        registrarEmDia(13, TipoRegistro.FOCO, 8, 25)
        registrarEmDia(14, TipoRegistro.FOCO, 2, 25)

        recompensas.addAll(
            listOf(
                Recompensa(id = 1, nome = "30 min de YouTube", emoji = "📺", custoMinutos = 30, vezesResgatada = 2),
                Recompensa(id = 2, nome = "1 episódio de série", emoji = "🎬", custoMinutos = 25, vezesResgatada = 1),
                Recompensa(id = 3, nome = "1h de videogame", emoji = "🎮", custoMinutos = 60),
                Recompensa(id = 4, nome = "15 min de redes sociais", emoji = "📱", custoMinutos = 15, vezesResgatada = 3)
            )
        )
    }
}
