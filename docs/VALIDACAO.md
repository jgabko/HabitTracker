# Registro de validação da integração

Data: 27 de outubro de 2026.

## Resultado final executado

```text
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest
BUILD SUCCESSFUL in 44s
79 actionable tasks: 8 executed, 71 up-to-date
```

O resultado corresponde ao código integrado do ZIP, incluindo a atualização das coroutines, a migração de banco e as telas originais.

| Verificação | Resultado |
| --- | --- |
| APK de depuração | Compilado com sucesso. |
| APK de testes instrumentados | Compilado com sucesso. |
| Testes locais | 12 executados, 0 falhas, 0 erros, 0 ignorados. |
| ConversoresTest | 2 testes de enums e dias da semana. |
| RoomLocalTest | 2 testes de CRUD, persistência em arquivo, histórico e rollback. |
| MigracaoBancoTest | 2 testes de atualização da base versão 1, com e sem perfil existente. |
| IntegracaoTrabalho2Test | 4 testes de exemplos, cálculos, CRUD, reabertura, saldo, foco, recaída, resgate, configuração e validação. |
| CrudInterfaceLocalTest | 1 percurso executado nas telas Compose com Robolectric. |
| ExampleUnitTest | 1 teste simples já existente na base. |

O percurso de interface cria, edita e remove hábitos e recompensas; abre progresso e estatísticas; conclui uma sessão simulada de foco; abre perfil, altera a preferência de notificações e verifica a tela Sobre. Ele interage com a Activity, navegação, formulários e DAOs reais do projeto no ambiente Android simulado do Robolectric.

Esse percurso JVM verifica ações e a árvore de interface; não gera screenshots nem vídeo e não equivale a uma execução no aparelho. Os detalhes aguardam a consulta assíncrona antes das verificações.

O relatório estruturado está em [RESULTADOS_DOS_TESTES.json](RESULTADOS_DOS_TESTES.json).

## Ambiente

- Windows x64.
- Java 25 do Android Studio, Gradle 9.5.0 e AGP 9.3.2.
- Android SDK Platform 37.0 e Build Tools 36.0.0.
- Room 2.8.5, KSP 2.3.12 e Robolectric 4.17 com API 35.
- A compilação emite avisos de APIs de teste e TabRow obsoletas e de acesso nativo no Java; nenhum deles impediu o build.

## Migração verificada

Os testes criam um arquivo com o esquema exportado da versão 1 e abrem esse mesmo banco com a nova AppDatabase. A validação do esquema pelo Room e a leitura dos dados confirmam preservação de IDs, nomes, categorias convertidas, dias, saldo, histórico e contadores de resgate. A ausência de perfil no banco antigo não apaga cadastros nem dispara restauração de exemplos.

## Pendente em aparelho

Não havia aparelho Android conectado e o emulador instalado não inicia neste Windows: código 0xC0000135 e ausência de MFPlat.dll. O modo sem janela e sem áudio também havia falhado na verificação do ambiente.

Portanto, ainda precisam ser executados os testes instrumentados em dispositivo e a gravação contínua de cadastrar, encerrar completamente e reabrir o app com o cadastro persistido. Não há prints ou vídeos reais desta execução apresentados como concluídos.

O roteiro está em [ROTEIRO_DE_EVIDENCIAS.md](ROTEIRO_DE_EVIDENCIAS.md); o script `scripts/testar-persistencia.ps1 -GravarVideo` prepara as capturas e gravações com um dispositivo disponível. A sintaxe PowerShell do script foi verificada; sua execução ADB depende desse dispositivo.

## Limite da conferência do enunciado

O banco usa versão 2 para preservar instalações da versão 1; a justificativa está no documento de decisões. O novo repositório é HabitTracker-Room e a autoria é individual. O prazo do Blackboard e a apresentação em sala dependem da turma e do autor. Evidências reais continuam necessárias para completar a entrega.
