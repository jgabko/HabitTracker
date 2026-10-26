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

/** Percorre as telas originais do ZIP, sem substituir o visual por telas de teste. */
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [35])
@org.robolectric.annotation.SQLiteMode(org.robolectric.annotation.SQLiteMode.Mode.LEGACY)
class CrudInterfaceLocalTest {
    @get:Rule val tela = createAndroidComposeRule<MainActivity>()

    private fun aguardarBanco() {
        tela.waitForIdle()
        tela.waitUntil(30_000) {
            tela.onAllNodesWithTag("operacao-banco").fetchSemanticsNodes().isEmpty()
        }
        tela.waitForIdle()
    }
    private fun localizar(texto: String): SemanticsNodeInteraction {
        aguardarBanco()
        val seletor = hasText(texto) and !hasSetTextAction()
        if (tela.onAllNodes(seletor).fetchSemanticsNodes().isEmpty() &&
            tela.onAllNodes(hasScrollToNodeAction()).fetchSemanticsNodes().isNotEmpty()) {
            tela.onNode(hasScrollToNodeAction()).performScrollToNode(seletor)
        }
        tela.waitUntil(30_000) { tela.onAllNodes(seletor).fetchSemanticsNodes().isNotEmpty() }
        val node = tela.onNode(seletor)
        try { node.assertIsDisplayed() } catch (_: AssertionError) { node.performScrollTo() }
        return node
    }
    private fun clicar(texto: String) { localizar(texto).performClick(); aguardarBanco() }
    private fun aba(descricao: String) {
        aguardarBanco()
        tela.onNodeWithContentDescription(descricao, useUnmergedTree = true).performClick()
        aguardarBanco()
    }
    private fun preencher(rotulo: String, valor: String) {
        aguardarBanco()
        tela.onNode(hasSetTextAction() and hasText(rotulo)).performScrollTo()
            .performTextClearance()
        tela.onNode(hasSetTextAction() and hasText(rotulo)).performTextInput(valor)
    }
    // Este teste JVM verifica a árvore e as ações reais do Compose.
    // Não produz prints ou vídeo e não substitui o teste no aparelho.
    private fun capturar(nome: String) {
        aguardarBanco()
        tela.onRoot().assertExists()
    }
    @Test fun etapa01CrudPelasTelas() {
        capturar("00-inicio-trabalho2.png")
        clicar("Ver todos")
        clicar("Formulário completo")
        preencher("Nome do hábito", "Hábito do teste CRUD")
        clicar("Salvar hábito")
        localizar("Hábito do teste CRUD").assertExists()
        capturar("01-habito-adicionado.png")
        clicar("Hábito do teste CRUD")
        preencher("Nome do hábito", "Hábito editado no Room")
        clicar("Salvar alterações")
        localizar("Hábito editado no Room").assertExists()
        capturar("02-habito-editado.png")
        clicar("Remover hábito")
        clicar("Remover")
        tela.onAllNodesWithText("Hábito editado no Room").assertCountEquals(0)
        capturar("03-habito-removido.png")
        tela.onNodeWithContentDescription("Voltar").performClick()
        aba("Loja")
        preencher("Nome da recompensa", "Recompensa do teste CRUD")
        preencher("Custo em minutos", "15")
        clicar("Adicionar")
        clicar("Recompensa do teste CRUD")
        preencher("Nome da recompensa", "Recompensa editada no Room")
        preencher("Custo em minutos", "20")
        clicar("Salvar alterações")
        localizar("Recompensa editada no Room").assertExists()
        capturar("04-recompensa-editada.png")
        clicar("Remover recompensa")
        clicar("Remover")
        tela.onAllNodesWithText("Recompensa editada no Room").assertCountEquals(0)
        aba("Progresso")
        capturar("07-progresso-preservado.png")
        clicar("Ver estatísticas")
        capturar("10-estatisticas-preservadas.png")
        tela.onNodeWithContentDescription("Voltar").performClick()
        aba("Foco")
        capturar("08-foco-preservado.png")
        clicar("Iniciar")
        repeat(5) { clicar("+5 min") }
        localizar("Sessão concluída!").assertExists()
        aba("Perfil")
        capturar("09-perfil-preservado.png")
        clicar("Configurações")
        tela.onNode(isToggleable()).performClick()
        aguardarBanco()
        clicar("Sobre o app")
        localizar("Versão 2.0").assertExists()
    }
}
