package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habittracker.data.repository.*

class HabitosViewModelFactory(
    private val habitos: HabitoRepository,
    private val recompensas: RecompensaRepository,
    private val registros: RegistroRepository,
    private val perfil: PerfilRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitosViewModel(habitos, recompensas, registros, perfil) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
