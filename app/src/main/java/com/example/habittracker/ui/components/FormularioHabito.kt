package com.example.habittracker.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.habittracker.dados.Atributo
import com.example.habittracker.dados.CategoriaHabito
import com.example.habittracker.dados.DiaDaSemana
import com.example.habittracker.dados.Habito
import com.example.habittracker.dados.PeriodoDoDia

@Composable
fun FormularioHabito(
    inicial: Habito? = null,
    habilitado: Boolean = true,
    aoSalvar: (Habito) -> Unit
) {
    var nome by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.nome.orEmpty()) }
    var categoria by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.categoria ?: CategoriaHabito.SAUDE) }
    var periodo by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.periodo ?: PeriodoDoDia.MANHA) }
    var atributo by rememberSaveable(inicial?.id) { mutableStateOf(inicial?.atributo ?: Atributo.VITALIDADE) }
    var dias by rememberSaveable(inicial?.id) { mutableStateOf((inicial?.diasDaSemana ?: DiaDaSemana.entries.toSet()).map { it.name }.toTypedArray()) }
    var minutos by rememberSaveable(inicial?.id) { mutableStateOf((inicial?.minutosGanho ?: 10).toString()) }
    val valor = minutos.toIntOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(nome, { nome = it }, label = { Text("Nome do hábito") }, modifier = Modifier.fillMaxWidth(), enabled = habilitado, singleLine = true)
        Text("Categoria")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CategoriaHabito.entries.forEach { item ->
                FilterChip(categoria == item, { categoria = item }, label = { Text(item.rotulo) }, enabled = habilitado)
            }
        }
        Text("Dias da semana")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DiaDaSemana.entries.forEach { dia ->
                FilterChip(dia.name in dias, {
                    dias = if (dia.name in dias) dias.filterNot { it == dia.name }.toTypedArray() else dias + dia.name
                }, label = { Text(dia.rotulo) }, enabled = habilitado)
            }
        }
        Text("Período")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PeriodoDoDia.entries.forEach { item ->
                FilterChip(periodo == item, { periodo = item }, label = { Text(item.rotulo) }, enabled = habilitado)
            }
        }
        Text("Atributo")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Atributo.entries.forEach { item ->
                FilterChip(atributo == item, { atributo = item }, label = { Text(item.rotulo) }, enabled = habilitado)
            }
        }
        OutlinedTextField(minutos, { minutos = it }, label = { Text("Minutos por conclusão") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = habilitado, isError = valor == null || valor <= 0)
        Button(
            onClick = { aoSalvar(Habito(inicial?.id ?: 0, nome.trim(), categoria, dias.map { DiaDaSemana.valueOf(it) }.toSet(), periodo, atributo, valor ?: 0)) },
            enabled = habilitado && nome.isNotBlank() && dias.isNotEmpty() && valor != null && valor > 0,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (inicial == null) "Salvar hábito" else "Salvar alterações") }
    }
}
