# Projeto 4: processo e decisões

## Antes e depois

No Trabalho 3, `data/AppState.kt` concentrava o estado das telas, os cálculos, as regras e as chamadas aos quatro DAOs. Usava listas mutáveis do Compose e fazia uma nova leitura de todas as tabelas depois de cada gravação. Algumas telas ainda consultavam detalhes por ID em `LaunchedEffect`, acompanhando um contador `versaoDados`.

No Projeto 4, o mesmo aplicativo usa o caminho `Tela -> ViewModel -> Repository -> DAO -> Room`. O retorno é reativo: `Room -> Flow -> UiState -> Tela`. Não foram acrescentadas telas, campos de cadastro ou funcionalidades. O dia 15 do calendário demonstrativo, o foco simulado, as regras de pontos e os exemplos continuam os mesmos.

## Referência do professor

A referência principal foi o código de `Aula Banco.zip`, na pasta `banco-03-mvvm/versao_final_mvvm`, conferido com a apresentação [Aula Banco - MVVM na prática](https://drive.google.com/file/d/1_w1mJ7Kx94MS6oj7d5LkGFs3sb5qSqW1/view). Foram seguidos os padrões do exemplo de Filmes:

- Repository concreto recebendo o DAO no construtor e repassando as operações.
- `buscarTodos()` sem `suspend`, retornando `Flow<List<...>>`; gravações continuam `suspend`.
- `MutableStateFlow` privado, `StateFlow` público com `asStateFlow()` e alterações com `update { it.copy(...) }`.
- Coleta do Flow iniciada no `init`, dentro de `viewModelScope.launch`.
- Eventos de formulário como `onNomeChange`, `onEditar` e `onSalvar`.
- Factory explícita implementando `ViewModelProvider.Factory`.
- Coleta com `collectAsState()`, permitida no enunciado, usando as dependências já existentes.

As pastas de [materiais](https://drive.google.com/drive/folders/1xcMGAc0RCkI-vUcyjHGCY6Kd5B8kW7KP) e [gravações](https://drive.google.com/drive/folders/1ReYclucWXi5ictbphukfZ3_-iQZc5Ygk) foram localizadas. A implementação foi baseada no código e nos slides; não se afirma que as gravações foram assistidas integralmente. Os PDFs anteriores são referências de Kotlin, Compose e navegação, e não substituem os requisitos do Projeto 4.

## Divisão dos pacotes

| Pacote | Responsabilidade |
| --- | --- |
| `ui` | Telas, componentes e tema. Observa o estado e encaminha eventos. |
| `data` | Habito, Recompensa, Registro, Perfil, enums, DAOs, Database, conversores e dados de exemplo. |
| `data.repository` | Um Repository por Entity, mais a coordenação das transações e dos Flows. |
| `viewmodel` | HabitosViewModel, RecompensasViewModel, seus UiStates, formulários e Factories. |
| `navigation` | As mesmas rotas e o mesmo NavHost do aplicativo anterior. |

Os arquivos de Entity foram movidos de `model` para `data`, como solicitado. As tabelas, os campos persistidos, a versão 2 do banco, o nome `habittracker.db` e a migração 1 -> 2 foram preservados.

## Por que os Repositories existem

Antes, mudar a forma de acessar os dados exigia mexer em `AppState`. Agora cada Entity tem seu Repository: `HabitoRepository`, `RecompensaRepository`, `RegistroRepository` e `PerfilRepository`. São eles que usam os respectivos DAOs. Os ViewModels não conhecem os DAOs nem recebem um Database.

`DadosRepository` monta esses quatro Repositories, combina seus Flows e coordena as operações que envolvem várias tabelas. Um exemplo é resgatar uma recompensa: é preciso descontar o saldo, aumentar o contador e registrar o histórico. Tudo continua em uma transação; se uma etapa falhar, o Room desfaz as anteriores. Um `Mutex` compartilhado pelos dois ViewModels impede que ações simultâneas leiam o mesmo saldo antes da atualização. A inicialização dos exemplos também passa por essa coordenação e só ocorre quando ainda não existe perfil.

Essa peça adicional é necessária porque o aplicativo tem regras entre quatro tabelas; o exemplo de Filmes da aula trabalha com uma tabela. Os quatro Repositories de Entity continuam simples, seguindo aquele exemplo.

## Conteúdo dos UiStates

`HabitosUiState` reúne os dados observados, o formulário rápido, o formulário completo, a edição de hábito, a sessão de foco simulada, a confirmação da restauração, os indicadores de carregamento/gravação e o erro. Os três formulários são separados para um rascunho de uma tela não sobrescrever outro.

`RecompensasUiState` reúne os dados observados, o formulário de cadastro, a edição de recompensa, a aba selecionada, a mensagem de resgate, os indicadores de carregamento/gravação e o erro.

`AppUiState` é o snapshot imutável compartilhado: hábitos, recompensas, registros e perfil. Os cálculos já existentes de sequência, pontos, nível, saldo e estatísticas ficaram nele como dados derivados. Não é um ViewModel e não acessa o banco.

`FormularioHabito` e `FormularioRecompensa` guardam os valores dos campos, o item em edição e os resultados de validação. A tela não monta mais a Entity editada nem decide se deve inserir ou atualizar: essa decisão ficou em `onSalvar`, como na aula. O formulário não é preenchido novamente a cada emissão do Room, para não apagar o texto enquanto a pessoa digita.

Os únicos estados locais que permanecem nos componentes são detalhes de apresentação, como a confirmação de exclusão e o estado de rolagem. Não há DAO, `CoroutineScope` nem `Dispatchers.IO` nos Composables.

## Problemas encontrados e validação

Na reorganização, a compilação inicial encontrou uma chave de fechamento ausente no executor das ações e imports do delegado `getValue` que faltavam nas telas que passaram a coletar estado. Esses pontos foram corrigidos antes da validação final.

A suíte de regressão verifica o CRUD, os dados de exemplo, os cálculos, o saldo, as transações, a migração e a persistência. Há um teste específico em que o Repository muda um hábito sem uma ação do ViewModel: os dois UiStates precisam receber a mudança pelo Flow. O teste de interface percorre os formulários, a navegação e as ações reais do Compose. Os resultados efetivamente executados ficam em [VALIDACAO_PROJETO4.md](VALIDACAO_PROJETO4.md).

As imagens de execução local, quando disponíveis, ficam em `evidencias/projeto4` e são identificadas como Robolectric. Elas não comprovam execução em celular. O vídeo curto e a demonstração em aparelho precisam ser gravados com o APK desta versão. Não havia aparelho conectado na preparação inicial.

## Relato dos integrantes

Este texto descreve as decisões técnicas implementadas. O material fornecido não informa a divisão do trio nem as dificuldades pessoais de cada integrante; esses relatos não foram inventados. Antes de entregar, os integrantes devem registrar aqui sua participação real, as dificuldades que tiveram e como as resolveram, com suas próprias palavras.

## Roteiro para apresentar

1. Mostrar `data/Daos.kt`, destacando `buscarTodos(): Flow<List<...>>`.
2. Abrir `data/repository/HabitoRepository.kt` e mostrar o DAO recebido no construtor.
3. Abrir `viewmodel/HabitosViewModel.kt`: `_uiState`, `uiState`, coleta no `init` e `onSalvar`.
4. Mostrar `HabitosUiState`, `RecompensasUiState` e as duas Factories.
5. Abrir uma tela e mostrar `collectAsState()` e chamadas de eventos.
6. Executar cadastro, edição, exclusão, conclusão, foco e resgate.
7. Criar um cadastro, encerrar completamente o aplicativo e reabri-lo para mostrar persistência.

O prazo do Blackboard não consta dos anexos recebidos e deve ser conferido pelo autor antes de publicar a entrega.
