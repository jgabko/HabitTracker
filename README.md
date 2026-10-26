# HabitTracker com Room

Evolução do [HabitTracker do Trabalho 2](https://github.com/jgabko/HabitTracker), em Kotlin e Jetpack Compose, com persistência local em SQLite/Room.

## Como rodar

Requisitos:

- Android Studio compatível com Android Gradle Plugin 9.3.2.
- JDK 25, conforme o arquivo de toolchain já existente na base.
- Android SDK Platform 37.0 e Build Tools 36.0.0.
- Emulador ou celular Android com API 24 ou superior.

1. Abra a pasta do projeto em **File → Open** no Android Studio.
2. Configure o JDK do Gradle e instale o SDK pelo SDK Manager, se solicitado.
3. Aguarde a sincronização. O plugin KSP e o compilador Room já estão configurados.
4. Conecte um aparelho com depuração USB ou inicie um emulador.
5. Execute o módulo `app`.

Pelo terminal, com Java e Android SDK configurados:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

Em Linux/macOS, use `./gradlew` no lugar de `.\gradlew.bat`.

O banco é criado no primeiro acesso. A primeira execução começa vazia; nenhum cadastro de exemplo é recriado automaticamente.

## Demonstração do CRUD

- **Início → Novo hábito:** cadastro completo, com dias, categoria, período, atributo e minutos.
- **Início → Ver todos:** lista de hábitos, cadastro rápido, conclusão e exclusão.
- **Cartão de um hábito:** detalhe consultado pelo ID, edição e remoção.
- **Loja:** cadastro e lista de recompensas.
- **Cartão de uma recompensa:** detalhe consultado pelo ID, edição, remoção e resgate quando houver saldo.

Os formulários validam campos vazios e valores numéricos. Uma gravação com erro mantém os dados do formulário e exibe a falha.

## Persistência

O arquivo `habittracker.db` fica no armazenamento interno do aplicativo. Fechar a Activity ou encerrar o processo não deve apagar os dados. Desinstalar o aplicativo ou limpar seu armazenamento apaga o banco local.

O Room é a fonte dos dados. As listas reativas do Compose são uma representação do conteúdo lido do banco e são recarregadas ao entrar nas telas e após gravações.

## Estrutura de dados

| Componente | Responsabilidade |
| --- | --- |
| `Habito` e `HabitoDao` | Cadastro, leitura, edição e exclusão de hábitos; consulta por ID. |
| `Recompensa` e `RecompensaDao` | Cadastro, leitura, edição e exclusão de recompensas; consulta por ID. |
| `Registro` e `RegistroDao` | Histórico de conclusões, recaídas, foco e resgates. |
| `Perfil` e `PerfilDao` | Saldo em minutos e atributos. |
| `Conversores` | Conversão dos enums e do conjunto de dias para os campos do banco. |
| `AppDatabase` | Banco Room versão 1, com instância Singleton. |
| `AppState` | Coroutines em IO, transações, tratamento de erros e atualização da interface na Main. |

## Documentação e evidências

- [Processo e decisões](docs/PROCESSO_E_DECISOES.md): origem, mudanças, entidades, queries e desafios técnicos.
- [Roteiro de demonstração](docs/ROTEIRO_DE_EVIDENCIAS.md): CRUD e teste de encerramento completo do app.
- [Registro de validação](docs/VALIDACAO.md): comandos executados, resultados e eventuais pendências.
- Evidências reais da execução ficam em `docs/evidencias`, quando disponíveis.

Os relatos pessoais e as contribuições dos integrantes precisam ser fornecidos por eles; não foram inventados.

## Testes

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

O segundo comando exige aparelho ou emulador. Os testes instrumentados abrangem:

- CRUD e consultas por ID dos dois cadastros.
- Persistência com fechamento e reabertura de um banco em arquivo.
- Preservação do saldo e do histórico.
- Rollback de transação.
- Percurso pela interface com criação, edição e remoção.

Para demonstrar a persistência entre processos, siga o roteiro: cadastrar, forçar a parada do app, reabrir pelo ícone e mostrar o mesmo item. Não limpar os dados entre as etapas.

No Windows, o roteiro automatizado está em `scripts/testar-persistencia.ps1`. Execute `powershell -File scripts/testar-persistencia.ps1 -AdbPath "CAMINHO_DO_ADB"` com um único dispositivo de teste conectado. Ele executa o CRUD pela interface, força a parada do app entre os testes de persistência e copia os prints gerados.

Os testes locais também incluem `RoomLocalTest`, que usa Robolectric e os DAOs gerados pelo Room com banco em arquivo. Esses testes já passaram, mas não substituem a demonstração em dispositivo e seu vídeo; veja o registro de validação.

## Escopo

Sem login, servidor, API ou sincronização na nuvem. Foram preservados o tema e a navegação da base. Foco, progresso, estatísticas e configurações que já eram telas provisórias continuam fora do escopo desta migração. O dia atual ainda é o valor fixo do projeto original.
