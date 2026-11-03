# Validação do Projeto 4

Data: 3 de novembro de 2026.

## Resultado executado

```text
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
BUILD SUCCESSFUL

gradlew.bat :app:testDebugUnitTest
BUILD SUCCESSFUL
```

**14 testes executados, zero falhas, zero erros, zero ignorados.** Os APKs do aplicativo e dos testes instrumentados foram compilados. Os testes instrumentados não foram executados em aparelho.

| Classe | Testes | O que verifica |
| --- | ---: | --- |
| ConversoresTest | 2 | Enums e dias da semana. |
| RoomLocalTest | 2 | CRUD, persistência em arquivo, histórico e rollback. |
| MigracaoBancoTest | 2 | Atualização da versão 1, com e sem perfil, preservando os dados. |
| MvvmIntegracaoTest | 6 | Exemplos e cálculos; CRUD reativo nos dois UiStates; mudanças externas recebidas pelo Flow; saldo, foco, conclusão, recaída e resgate; formulários e validação; reabertura sem recriar exemplos. |
| CrudInterfaceLocalTest | 1 | Percurso nas telas reais do Compose, com navegação, CRUD, foco, configurações e recriação da Activity com o formulário preenchido. |
| ExampleUnitTest | 1 | Teste simples já existente. |

Foram geradas e conferidas nove [capturas da execução local](evidencias/projeto4/README.md). São imagens do Android simulado pelo Robolectric, não de um celular.

## Conferências adicionais

- O JSON do esquema versão 2 é idêntico ao do commit base: mover as Entities para `data` não alterou as tabelas.
- Os Composables não usam DAO, `CoroutineScope` ou `Dispatchers.IO` para acessar o banco.
- Os ViewModels recebem Repository, não Database ou DAO.
- `buscarTodos()` dos quatro DAOs retorna Flow.
- Há dois ViewModels, dois UiStates próprios, `_uiState` privado, `uiState` público e duas Factories.
- `git diff --check` sem problemas de espaços.

## Ajustes encontrados na validação

A compilação inicial apontou uma chave de fechamento e imports ausentes na refatoração; foram corrigidos. Na suíte nova, o JUnit exigiu retorno `Unit` explícito em métodos com `runBlocking`. A captura padrão do Compose expirou no Robolectric; as capturas passaram a usar o desenho da View em um Canvas com gráficos nativos. O teste continuou verificando as ações reais da interface. A resolução simulada foi ajustada para mostrar uma tela de celular mais alta.

Não foi observada regressão funcional no percurso final executado. Os avisos restantes são de APIs obsoletas já usadas na base (`TabRow`, `SQLiteMode`) e acesso nativo no Java 25; não impediram o build nem os testes.

## Ambiente e rastreabilidade

Windows, JDK 25, Gradle 9.5.0, AGP 9.3.2, SDK 37, Room 2.8.5 e Robolectric 4.17/API 35. As versões de dependências do projeto foram preservadas.

O [relatório JSON](RESULTADOS_PROJETO4.json) contém os nomes dos casos e a identificação do APK. SHA-256 do APK:

```text
ff474d564ad123b492bd88765f20d95c3272fafc1fbd662cc0ab22a9cee81e0c
```

## Pendente para a apresentação

Não havia aparelho conectado na verificação por ADB. Ainda é necessário gravar o vídeo curto desta versão em Android, incluindo cadastro, encerramento completo e reabertura, e completar o relato verdadeiro dos integrantes em [PROJETO4.md](PROJETO4.md).

As alterações do Projeto 4 ficam na branch `codex/projeto4-mvvm` do mesmo repositório. Os relatórios antigos em `VALIDACAO.md` e `RESULTADOS_DOS_TESTES.json` são históricos do Trabalho 3.
