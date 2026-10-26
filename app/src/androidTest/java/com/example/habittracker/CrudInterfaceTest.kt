package com.example.habittracker

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import java.io.File

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CrudInterfaceTest {
    @get:Rule val tela = createAndroidComposeRule<MainActivity>()

    private fun aguardarTexto(texto: String) {
        tela.waitUntil(15_000) { tela.onAllNodesWithText(texto, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        tela.waitForIdle()
    }

    private fun clicar(texto: String) {
        aguardarTexto(texto)
        tela.onNodeWithText(texto).performClick()
    }

    private fun preencher(rotulo: String, valor: String) {
        tela.onNode(hasSetTextAction() and hasText(rotulo)).performScrollTo().performTextClearance()
        tela.onNode(hasSetTextAction() and hasText(rotulo)).performTextInput(valor)
    }

    private fun capturar(nome: String) {
        tela.waitForIdle()
        val instrumento = InstrumentationRegistry.getInstrumentation()
        val pasta = File(instrumento.targetContext.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        val bitmap = requireNotNull(instrumento.uiAutomation.takeScreenshot())
        File(pasta, nome).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun etapa01CrudPelasTelas() {
        clicar("Novo hábito")
        preencher("Nome do hábito", "Hábito do teste CRUD")
        tela.onNodeWithText("Salvar hábito").performScrollTo().performClick()
        clicar("Ver todos")
        aguardarTexto("Hábito do teste CRUD")
        capturar("01-habito-adicionado.png")
        clicar("Hábito do teste CRUD")
        clicar("Editar hábito")
        preencher("Nome do hábito", "Hábito editado no Room")
        tela.onNodeWithText("Salvar alterações").performScrollTo().performClick()
        aguardarTexto("Hábito editado no Room")
        capturar("02-habito-editado.png")
        tela.onNodeWithText("Remover hábito").performScrollTo().performClick()
        clicar("Remover")
        aguardarTexto("Meus hábitos")
        tela.waitUntil(15_000) { tela.onAllNodesWithText("Hábito editado no Room").fetchSemanticsNodes().isEmpty() }
        capturar("03-habito-removido.png")
        tela.onNodeWithContentDescription("Voltar").performClick()

        clicar("Loja")
        preencher("Nome da recompensa", "Recompensa do teste CRUD")
        preencher("Custo em minutos", "15")
        clicar("Adicionar recompensa")
        aguardarTexto("Recompensa do teste CRUD")
        clicar("Recompensa do teste CRUD")
        clicar("Editar recompensa")
        preencher("Nome da recompensa", "Recompensa editada no Room")
        preencher("Custo em minutos", "20")
        clicar("Salvar alterações")
        aguardarTexto("Recompensa editada no Room")
        capturar("04-recompensa-editada.png")
        clicar("Remover recompensa")
        clicar("Remover")
        aguardarTexto("Adicionar recompensa")
        tela.waitUntil(15_000) { tela.onAllNodesWithText("Recompensa editada no Room").fetchSemanticsNodes().isEmpty() }
    }

    @Test fun etapa02PrepararPersistencia() {
        clicar("Ver todos")
        preencher("Nome do hábito", "Teste de persistência Room")
        clicar("Adicionar hábito")
        aguardarTexto("Teste de persistência Room")
        capturar("05-antes-de-fechar.png")
    }

    // Também pode ser executado isoladamente após am force-stop e novo lançamento do app.
    @Test fun etapa03VerificarPersistencia() {
        clicar("Ver todos")
        aguardarTexto("Teste de persistência Room")
        capturar("06-depois-de-reabrir.png")
    }
}
