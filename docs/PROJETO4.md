# Projeto 4 — processo e decisões

## O que mudou

No Trabalho 3, `AppState` concentrava o acesso ao banco e os dados das telas. Agora o caminho é Tela → ViewModel → Repository → DAO. O DAO devolve Flow e a lista muda automaticamente quando o banco muda.

A primeira adaptação tinha organização além do necessário para a aula. Após o pedido de simplificação, foram removidos `DadosRepository`, `AppUiState`, `FormularioHabito`, `FormularioRecompensa` e `FocoUiState`. Também saíram `combine`, `Mutex`, o executor genérico de ações e a escolha de formulário pelas palavras “novo”, “rapido” e “edicao”.

## Base das aulas

Foi usado o exemplo `banco-03-mvvm/versao_final_mvvm` de `Aula Banco.zip`, especialmente `FilmesRepository`, `FilmesUiState`, `FilmesViewModel` e `FilmesViewModelFactory`. O [material do professor](https://drive.google.com/drive/folders/1xcMGAc0RCkI-vUcyjHGCY6Kd5B8kW7KP) contém esses exemplos.

Como no exemplo de Filmes:

- O Repository recebe o DAO e oferece métodos de buscar, inserir, atualizar e remover.
- `buscarTodos()` devolve `Flow<List<...>>`.
- O ViewModel tem `_uiState` privado e `uiState` público.
- O `init` coleta cada lista com `viewModelScope.launch` e `collect`.
- Métodos como `onNomeChange`, `onEditar` e `onSalvar` atendem às ações do usuário.
- O estado é alterado com `copy`, e a Factory passa os Repositories ao ViewModel.

O aplicativo já tinha saldo e histórico em várias tabelas, diferente do cadastro de Filmes. Por isso, as operações de conclusão, recaída, foco e resgate ainda usam `withTransaction` nos Repositories. É o que mantém as gravações juntas; não há um coordenador criado para isso.

## Pacotes

`ui` tem as telas. `data` tem as Entities, os DAOs e o Database. `data.repository` tem um Repository por Entity. `viewmodel` tem os dois ViewModels, seus estados e suas Factories. `navigation` mantém as rotas existentes.

O Repository separa o acesso ao banco do ViewModel. A tela não usa DAO nem cria coroutines para gravar dados.

## O que cada UiState guarda

`HabitosUiState` guarda as listas de hábitos, recompensas e registros, o perfil, os campos do formulário de hábito, o hábito em edição, os campos do cadastro rápido e o estado do foco. Também conserva os cálculos de pontos, sequência e estatísticas que já existiam.

`RecompensasUiState` guarda a lista de recompensas, o perfil, nome, custo, recompensa em edição, aba e mensagens. Os campos são diretos, como `nome`, `descricao` e `filmeEmEdicao` no exemplo de Filmes.

Os dois estados guardam os indicadores usados para mostrar carregamento, gravação ou erro. O item em edição determina se `onSalvar` insere ou atualiza.

## Testes e dificuldades

A simplificação exigiu ajustar as telas e os testes que liam os formulários aninhados. O percurso final de teste confere o CRUD, o saldo, o foco, o histórico, a migração e a reabertura do banco. Também recria a Activity com um formulário preenchido. Os resultados executados estão em [VALIDACAO_PROJETO4.md](VALIDACAO_PROJETO4.md), com capturas da versão simplificada.

O relato de participação e de dificuldades pessoais dos integrantes não foi informado. Cada integrante deve acrescentar aqui sua participação real, com suas palavras. As capturas do Robolectric não substituem o vídeo de apresentação em aparelho.
