package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.dados.AppDatabase
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.navegacao.AppNavegacao
import com.example.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                val estado: AppState = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        require(modelClass.isAssignableFrom(AppState::class.java))
                        return AppState(AppDatabase.getInstance(applicationContext)) as T
                    }
                })
                AppNavegacao(estado = estado)
            }
        }
    }
}
