package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habittracker.data.repository.DadosRepository

class HabitosViewModelFactory(private val repository: DadosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitosViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: $modelClass")
    }
}
