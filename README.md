# HabitTracker Room

Evolução do **projeto completo do Trabalho 2**, fornecido no arquivo `HabitTracker(3).zip`, com persistência local em SQLite/Room. Projeto individual de **jgabko**.

Esta versão mantém as 12 telas, o tema escuro, as cinco abas, as conquistas, o calendário demonstrativo, as estatísticas, o perfil e o foco simulado do ZIP. O Room foi integrado aos mesmos formulários, cartões e regras do Trabalho 2.

## Como executar

- Android Studio compatível com AGP 9.3.2 e JDK 25.
- Android SDK Platform 37.0 e Build Tools 36.0.0.
- Aparelho ou emulador Android API 24 ou superior.

1. Abra este repositório no Android Studio.
2. Selecione o JDK 25 para o Gradle e configure o Android SDK.
3. Aguarde a sincronização. Room e KSP já estão configurados.
4. Execute o módulo `app` em um aparelho ou emulador.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:installDebug
```

Em Linux/macOS, use `./gradlew`.

## Funcionalidades preservadas e integradas

| Tela ou fluxo do Trabalho 2 | Integração nesta versão |
| --- | --- |
| Início e lista de hábitos | Carregam os hábitos do banco; conclusão altera histórico e saldo em transação. |
| Novo hábito | Mantém tipo Fazer/Evitar, categoria, dias e período; salva no Room antes de voltar. |
| Detalhe do hábito | Consulta pelo ID no banco, mantém estatísticas e edição, e permite excluir com confirmação. |
| Loja e conquistas | Mantém as duas abas; cadastro, exclusão e resgate persistem. |
| Detalhe da recompensa | Consulta por ID, mantém os dados de resgate e acrescenta edição de nome e custo. |
| Foco | Mantém a simulação por passos de cinco minutos; uma sessão concluída grava histórico e credita saldo. |
| Progresso e estatísticas | Usam o histórico carregado do Room para os mesmos cálculos e gráficos do Trabalho 2. |
| Perfil | Calcula pontos, nível e progresso com o histórico persistido. |
| Configurações e Sobre | Persistem a preferência simulada de notificações; restauração de exemplos exige confirmação. |

O calendário continua sendo o mês demonstrativo de 30 dias com hoje no dia 15. O foco continua simulado e a preferência de notificações não agenda notificações reais, conforme o escopo do Trabalho 2.

## Dados e atualização de instalações anteriores

O arquivo continua sendo `habittracker.db`. Na primeira instalação, os exemplos originais são gravados uma única vez. Encerrar o app ou excluir todos os hábitos não recria exemplos. Apenas a restauração confirmada nas configurações substitui os cadastros pelo conjunto inicial.

O banco usa **versão 2**, com migração explícita da versão 1 da implementação simplificada anterior. Isso é necessário porque o ZIP possui campos e categorias que aquela implementação não tinha. A migração preserva IDs, cadastros, saldo, histórico e progressão; não utiliza recriação destrutiva do banco.

O APK usa o mesmo `applicationId` e `versionCode = 2`. Para atualizar mantendo os dados, instale com a mesma assinatura do aplicativo anterior. Dados do Trabalho 2 que existiam somente em memória e já foram perdidos ao encerrar aquele processo não podem ser recuperados do ZIP.

Sem login, servidor, API ou sincronização na nuvem.

## Estrutura

- `model`: as classes originais Habito, Recompensa e Registro adaptadas para Entity; Perfil preserva saldo e preferência.
- `data`: AppDatabase Singleton, DAOs, conversores, migração e AppState com coroutines IO.
- `navigation`: rotas e navegação originais do ZIP.
- `ui`: tema, componentes e telas originais, integrados ao estado persistente.

Os DAOs de hábitos e recompensas têm Insert, Query, Update e Delete suspend, além de consulta filtrada por ID. As listas carregam em `LaunchedEffect(Unit)`. As operações de banco são executadas em `CoroutineScope(Dispatchers.IO).launch`, com cancelamento, transações e tratamento de erros com `try/catch` e `Log.e`.

## Documentação e validação

- [Processo, decisões e autoria individual](docs/PROCESSO_E_DECISOES.md).
- [Origem dos arquivos e integração das versões](docs/INTEGRACAO_TRABALHO2.md).
- [Resultados da validação](docs/VALIDACAO.md).
- [Roteiro de prints, vídeo e persistência](docs/ROTEIRO_DE_EVIDENCIAS.md).

```powershell
.\gradlew.bat :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

O segundo comando exige aparelho ou emulador. No Windows, `scripts/testar-persistencia.ps1` executa o percurso de CRUD nas telas originais, força a parada entre as etapas de persistência e copia as capturas reais para `docs/evidencias`. A opção `-GravarVideo` grava também o CRUD e a persistência com `adb screenrecord`.

**A demonstração no aparelho e seu vídeo ainda precisam ser executados.** Os testes locais e a compilação não substituem essa evidência obrigatória.
