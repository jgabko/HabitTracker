package com.example.habittracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// O app é nativamente escuro: existe apenas este esquema de cores (sem tema claro e sem cor dinâmica).
private val EsquemaEscuro = darkColorScheme(
    primary = Salvia,
    onPrimary = Color(0xFF0F1F14),
    primaryContainer = SalviaEscura,
    onPrimaryContainer = Color(0xFFD7EBDB),
    secondary = Coral,
    onSecondary = Color(0xFF2B0F08),
    secondaryContainer = CoralEscuro,
    onSecondaryContainer = Color(0xFFFFDBD2),
    tertiary = Amarelo,
    onTertiary = Color(0xFF2B2300),
    background = Fundo,
    onBackground = TextoPrincipal,
    surface = Fundo,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    surfaceContainerLowest = Fundo,
    surfaceContainerLow = Fundo,
    surfaceContainer = Superficie,
    surfaceContainerHigh = SuperficieAlta,
    surfaceContainerHighest = SuperficieAlta,
    outline = Contorno,
    outlineVariant = Contorno,
    error = Erro,
    onError = Color(0xFF2B0A0A)
)

@Composable
fun HabitTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaEscuro,
        typography = Typography,
        content = content
    )
}
