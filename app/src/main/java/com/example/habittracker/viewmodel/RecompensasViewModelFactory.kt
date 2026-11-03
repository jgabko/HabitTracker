package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habittracker.data.repository.DadosRepository

class RecompensasViewModelFactory(private val repository: DadosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecompensasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecompensasViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: $modelClass")
    }
}
