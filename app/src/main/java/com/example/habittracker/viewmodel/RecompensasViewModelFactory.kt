package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habittracker.data.repository.*

class RecompensasViewModelFactory(
    private val recompensas: RecompensaRepository,
    private val perfil: PerfilRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecompensasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecompensasViewModel(recompensas, perfil) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}
