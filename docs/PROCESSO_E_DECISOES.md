# Evolução do HabitTracker para Room

## Como estava o Trabalho 2

A base é o código de [jgabko/HabitTracker](https://github.com/jgabko/HabitTracker), revisão `94924abfd73ec86f385f31c8c2b99b0705964328`.

Os modelos `Habito`, `Recompensa` e `Registro` já existiam. O `AppState` criava listas com `mutableStateListOf`, preenchia exemplos em seu construtor e gerava os IDs com contadores locais. Ao recriar o estado, esses dados voltavam ao início. As rotas de detalhes já recebiam um ID, mas procuravam o item na lista em memória.

Na revisão de origem, as telas de cadastro, listas e detalhes ainda usavam `TelaStub`. A descrição do README anterior antecipava funções que não estavam implementadas nessa revisão. Nesta evolução, as telas relacionadas ao CRUD foram implementadas de fato, preservando o tema, o pacote, a navegação e as classes do projeto.

## O que mudou

- Hábitos, recompensas, histórico e perfil passaram a ser armazenados em `habittracker.db`, no armazenamento interno do aplicativo.
- Os IDs passaram a ser gerados pelo SQLite/Room.
- As listas consultam o banco ao abrir, por `LaunchedEffect(Unit)` e `carregarDados()`.
- Cadastro, edição e exclusão chamam os DAOs e recarregam as listas após a operação.
- Os dois detalhes consultam o banco pelo ID recebido da navegação.
- A primeira execução começa sem dados; exemplos não são recriados automaticamente.
- `AppState` passou a ser um `ViewModel`, para que o escopo das gravações não dependa de uma recomposição da interface.

## Entidades e campos

| Entidade | Campos e motivo |
| --- | --- |
| `Habito` | ID, nome, categoria, dias da semana, período, atributo e minutos ganhos. Reaproveita o modelo existente e permite editar o agendamento e o ganho. |
| `Recompensa` | ID, nome e custo em minutos. São os dados apresentados na loja e usados no resgate. |
| `Registro` | ID, tipo, dia, hábito ou recompensa relacionada e minutos. Preserva as conclusões e os resgates para não perder o histórico ao reabrir. |
| `Perfil` | ID, saldo e valores dos cinco atributos. Evita manter saldo e progressão apenas na memória. |

Todas têm `@Entity(tableName = "...")` e `@PrimaryKey(autoGenerate = true)` com `id: Int = 0`. No perfil, a aplicação mantém um único registro de ID 1.

`Conversores` transforma os enums e o conjunto de dias em texto e os reconstrói na leitura. Os dias são armazenados em uma ordem estável. O banco exporta o esquema para `app/schemas`.

## DAOs e consultas

`HabitoDao` e `RecompensaDao` possuem os quatro métodos básicos, todos `suspend`: inserir, buscar todos, atualizar e remover. Também possuem `buscarPorId(id)` e a exclusão de todos os registros, usada somente pela restauração explícita de exemplos.

As listas são ordenadas por ID decrescente, para exibir o cadastro mais recente primeiro. A consulta por ID permite abrir o item certo sem transportar o objeto inteiro pela navegação.

`RegistroDao` inclui uma consulta por hábito, dia e tipo de conclusão. Ela permite concluir e desmarcar um hábito sem duplicar a conclusão do mesmo dia. `PerfilDao` lê o perfil e salva o saldo e os atributos.

## Database e execução fora da interface

`AppDatabase` reúne as quatro entidades, expõe seus DAOs e usa versão 1. A instância é criada com `Room.databaseBuilder`, `applicationContext`, `companion object`, `@Volatile` e `synchronized`.

As ações do `AppState` executam em `viewModelScope.launch(Dispatchers.IO)`. As consultas suspensas de detalhes usam `withContext(Dispatchers.IO)`, mantendo o mesmo requisito de executar fora da thread principal. A interface só é atualizada em `Dispatchers.Main`.

O escopo é ligado ao ciclo de vida do `ViewModel`. Um `Mutex` serializa as ações locais. Transações mantêm juntas as alterações relacionadas, como registrar uma conclusão e atualizar o saldo, ou registrar um resgate e descontar seu custo. Exceções são capturadas com `try/catch`, registradas com `Log.e` e mostradas na tela. Cancelamentos de coroutine são propagados.

Ao excluir um hábito ou uma recompensa, as chaves estrangeiras do histórico usam `SET_NULL`: o item sai da lista, mas os lançamentos anteriores e o saldo não desaparecem.

## Desafios da migração

1. **Listas reativas e persistência são coisas diferentes.** As listas do Compose foram mantidas para atualizar a tela, mas agora são preenchidas pela leitura do Room. Elas não são a fonte definitiva dos dados.
2. **IDs não podem reiniciar ao abrir o aplicativo.** Os contadores locais foram substituídos por chaves geradas pelo banco. A edição mantém o ID original.
3. **Enums e conjuntos precisam de uma representação persistível.** Foram adicionados conversores para preservar os valores dos modelos.
4. **Gravar e navegar precisam ocorrer na ordem correta.** O formulário só é limpo ou a tela fechada no retorno de sucesso da gravação. Erros mantêm o formulário disponível.
5. **Persistir somente os dois cadastros deixaria o estado inconsistente.** Histórico, saldo e atributos também passaram a ser gravados.
6. **Os detalhes não podem depender apenas do cache das listas.** Os dois detalhes fazem uma consulta filtrada ao banco quando abrem e após alterações.

## Testes e evidências

`RoomPersistenciaTest` usa um banco em arquivo, não apenas um banco em memória. Ele verifica CRUD das duas entidades, consultas por ID, conversores, fechamento e reabertura do arquivo e rollback de uma transação que falha.

`ConversoresTest` verifica a ida e volta dos enums e dos dias da semana. `CrudInterfaceTest` percorre os formulários de criação, edição e exclusão e gera capturas reais na pasta externa `evidencias` do aplicativo de teste.

O roteiro em [ROTEIRO_DE_EVIDENCIAS.md](ROTEIRO_DE_EVIDENCIAS.md) descreve a demonstração e a gravação do teste com encerramento completo do processo. A execução dos testes de banco não substitui a demonstração de fechar e reabrir o app pela interface.

## Relato dos integrantes

Os nomes, contribuições e dificuldades individuais devem ser registrados pelos próprios integrantes a partir do que ocorreu no desenvolvimento e na apresentação. Essa informação não está no código original nem no enunciado fornecido; não há relatos pessoais atribuídos neste documento.

## Limites desta evolução

O banco é local. Não foram adicionados login, servidor, API ou sincronização na nuvem. As telas de foco, progresso, estatísticas e configurações que já eram provisórias não foram transformadas em funcionalidades completas por esta migração. O calendário ainda usa o dia fixo definido no projeto de origem.
