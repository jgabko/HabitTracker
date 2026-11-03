package com.example.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habittracker.data.AppDatabase
import com.example.habittracker.viewmodel.*
import com.example.habittracker.navigation.AppNavigation
import com.example.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras do sistema transparentes com ícones claros (app sempre escuro)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            HabitTrackerTheme {
                val repository = androidx.compose.runtime.remember {
                    com.example.habittracker.data.repository.DadosRepository(
                        AppDatabase.getInstance(applicationContext))
                }
                val habitos: HabitosViewModel = viewModel(factory = HabitosViewModelFactory(repository))
                val recompensas: RecompensasViewModel = viewModel(factory = RecompensasViewModelFactory(repository))
                AppNavigation(viewModel = habitos, recompensasViewModel = recompensas)
            }
        }
    }
}
