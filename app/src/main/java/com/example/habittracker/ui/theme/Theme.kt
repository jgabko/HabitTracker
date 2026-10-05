package com.example.habittracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PaletaEscura = darkColorScheme(
    primary = VerdeSalvia,
    onPrimary = SobreVerde,
    secondary = Coral,
    onSecondary = SobreCoral,
    tertiary = VerdeSalvia,
    onTertiary = SobreVerde,
    background = FundoEscuro,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    outline = Contorno,
    error = Coral,
    onError = SobreCoral
)

@Composable
fun HabitTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PaletaEscura,
        typography = Typography,
        content = content
    )
}
