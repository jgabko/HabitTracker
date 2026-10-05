package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habittracker.ui.theme.Contorno
import com.example.habittracker.ui.theme.Coral
import com.example.habittracker.ui.theme.FundoEscuro
import com.example.habittracker.ui.theme.HabitTrackerTheme
import com.example.habittracker.ui.theme.Superficie
import com.example.habittracker.ui.theme.SuperficieAlta
import com.example.habittracker.ui.theme.TextoPrincipal
import com.example.habittracker.ui.theme.VerdeSalvia

@Composable
fun PaletaPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "HabitTracker",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tema escuro · verde-sálvia e coral",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        TituloSecao("Paleta")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AmostraCor("Sálvia", VerdeSalvia, modifier = Modifier.weight(1f))
            AmostraCor("Coral", Coral, modifier = Modifier.weight(1f))
            AmostraCor("Fundo", FundoEscuro, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AmostraCor("Superfície", Superficie, modifier = Modifier.weight(1f))
            AmostraCor("Elevada", SuperficieAlta, modifier = Modifier.weight(1f))
            AmostraCor("Texto", TextoPrincipal, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))
        TituloSecao("Componentes base")
        CartaoPadrao {
            Text(
                text = "Cartão padrão",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Usado nas listas de hábitos e recompensas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarraProgresso(progresso = 0.65f)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Barra de progresso · 65%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AmostraCor(
    nome: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cor)
                .border(1.dp, Contorno, RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = nome,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121410, showSystemUi = true)
@Composable
fun PaletaPreviewTela() {
    HabitTrackerTheme {
        PaletaPreview()
    }
}
