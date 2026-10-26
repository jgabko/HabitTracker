# Processo e decisões do HabitTracker Room

## Autoria individual

O projeto é desenvolvido individualmente por jgabko. Não há um trio: implementação, integração e preparação da documentação concentram-se em um único integrante. Os pontos abaixo descrevem decisões e problemas técnicos verificáveis no projeto, sem atribuir relatos pessoais de depuração que não foram informados.

## Trabalho 2 usado como base

A base correta é o projeto completo enviado em `HabitTracker(3).zip`. Ele contém início, hábitos, formulário completo, dois detalhes, foco, progresso, loja e conquistas, perfil, estatísticas, configurações e Sobre.

A implementação anterior com Room tinha sido construída sobre outra revisão, mais simples, com telas provisórias. Por isso, a versão nova não apresentava as funcionalidades do ZIP. A integração usa agora o ZIP como base visual e funcional, acrescentando persistência às suas classes e telas.

No Trabalho 2, AppState criava listas com `mutableStateListOf`, IDs por contadores e exemplos em memória. No Trabalho 3, o Room é a fonte persistente. As listas continuam sendo o estado visual do Compose e são atualizadas após a leitura do banco.

## Entidades e campos

- **Habito** mantém nome, tipo Fazer/Evitar, categoria, emoji, período, dias da semana, minutos por conclusão e última recaída. Esses campos preservam as regras e os formulários do Trabalho 2.
- **Recompensa** mantém nome, emoji, custo e quantidade de resgates. A edição persiste nome e custo preservando o ID e o contador.
- **Registro** mantém dia, tipo, referência ao hábito, nome e categoria no momento da atividade, minutos e referência opcional à recompensa. O snapshot preserva o histórico após a exclusão de um cadastro.
- **Perfil** preserva saldo e preferência de notificações. Campos de ajuste preservam a pontuação de bancos anteriores; novas instalações calculam seus pontos pelo histórico.

Todas possuem `@Entity` e `@PrimaryKey(autoGenerate = true)`, com `id: Int = 0`. IDs explícitos são utilizados somente para o conjunto original de exemplos e para preservar os registros migrados; cadastros comuns recebem IDs do banco.

## DAOs e consultas

HabitoDao e RecompensaDao possuem inserir, buscar todos, atualizar e remover, todos suspend. A busca por ID é usada pelos detalhes recebidos pela navegação. A ordenação por ID crescente preserva a ordem das listas do Trabalho 2.

RegistroDao consulta a conclusão de um hábito no dia para marcar, desmarcar ou registrar recaída sem duplicar créditos. PerfilDao lê e salva o saldo e a preferência. Queries de exclusão de todos os itens são restritas à restauração explícita de exemplos.

Conversores preservam os enums originais e a lista de dias entre domingo 0 e sábado 6.

## Database Singleton e migração

AppDatabase é uma classe abstrata RoomDatabase com companion object, @Volatile, synchronized e Room.databaseBuilder usando applicationContext.

A versão inicial da base Room já era 1. A integração dos modelos completos exige a versão 2, em vez de manter o número 1 do exemplo inicial do enunciado. A migração 1 para 2 converte categorias, dias e nomes de campos; preserva IDs, saldo e histórico; reconstitui contadores de resgates; e compensa pontos já existentes para evitar dupla contagem. Essa escolha permite atualizar instalações anteriores sem apagar seus dados.

Se o banco anterior não tiver um perfil, a migração cria o marcador do perfil sem semear exemplos sobre os cadastros existentes. Na instalação nova, o conjunto do ZIP é gravado uma única vez. A existência do perfil identifica essa inicialização: uma lista vazia por exclusões não volta a ser preenchida automaticamente.

## Coroutines e tratamento de erros

As ações do AppState executam em `CoroutineScope(Dispatchers.IO).launch`. O Job do ViewModel mantém o vínculo com o ciclo de vida. Leituras dos detalhes também usam esse formato, dentro de coroutineScope, e aguardam a consulta por join sem bloquear a thread.

O banco é acessado em IO; as mudanças do estado visual ocorrem na Main. `try/catch` registra falhas com `Log.e` e apresenta mensagens por Snackbar. CancellationException é propagada. O estado de carregamento é finalizado também em caso de cancelamento.

O formulário só é limpo, a tela só volta ou a mensagem de sucesso só aparece depois da gravação confirmada. Um indicador de progresso impede novas ações enquanto uma operação está em andamento.

## Regras preservadas e consistência

Concluir um hábito grava a atividade e credita seus minutos na mesma transação. O registro guarda o valor creditado, de modo que desmarcar depois de uma edição devolve exatamente aquele valor. Uma recaída remove a conclusão do dia, ajusta o saldo e atualiza a última recaída atomicamente.

Foco mantém a simulação do Trabalho 2: cinco minutos de foco geram um minuto de lazer, e dez minutos geram um ponto de Foco. O resgate desconta o custo, atualiza o contador e registra o evento na mesma transação. Mutex serializa as ações locais.

Estatísticas, calendário, ofensivas, conquistas e nível continuam calculados pelo histórico. Resgates não contam como dias de produtividade nem geram pontos. A exclusão de um cadastro preserva o histórico, como já fazia o modelo do ZIP.

## Dificuldades técnicas resolvidas

1. **Bases diferentes:** a interface completa do ZIP substitui as telas provisórias da implementação anterior.
2. **Modelos incompatíveis:** os modelos originais foram adaptados para entidades e os dados antigos receberam migração.
3. **Estado em memória:** saldo, histórico, resgates e preferências passaram a persistir junto aos cadastros.
4. **Operações assíncronas:** navegação e limpeza dos formulários passaram a aguardar sucesso.
5. **Créditos após edição:** a conclusão guarda o valor realmente creditado para desfazer sem inconsistência.
6. **Demonstração inicial:** os exemplos são gravados uma vez, sem sobrescrever dados ao reabrir.

## Validação e evidências

Os resultados efetivamente executados estão em [VALIDACAO.md](VALIDACAO.md). Há testes para CRUD, reabertura de banco em arquivo, migração do esquema anterior e regras integradas do Trabalho 2.

O teste ao vivo de fechar completamente e reabrir o app, os prints e o vídeo continuam necessários. O roteiro está em [ROTEIRO_DE_EVIDENCIAS.md](ROTEIRO_DE_EVIDENCIAS.md). Não há evidências de aparelho apresentadas como se já tivessem sido produzidas.

## Limites mantidos do Trabalho 2

Calendário demonstrativo de 30 dias com hoje no dia 15; foco por passos simulados; notificações apenas como preferência; avatar por emoji. Não foram introduzidos login, servidor ou sincronização na nuvem.
