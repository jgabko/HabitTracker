# Integração do projeto original com Room

## Origem

- Base visual e funcional: `HabitTracker(3).zip`, fornecido pelo autor.
- SHA256 do ZIP: `BE7DE617C8A1292A5B4EEF36CB2A056F5700E9EF5A9DAB73AF321B3CA1507716`.
- Destino da integração: `jgabko/HabitTracker-Room`.
- Base anterior do destino: commit `eb0cb842bf864483248d351b4eb13d42591180e8`.

O ZIP foi tratado como fonte de código e referência da interface. Arquivos de IDE, builds, caches e configurações locais não foram incorporados.

## Preservado do ZIP

Todos os arquivos do tema; componentes de cartões, campos e seletores; as 12 telas; categorias, tipos de hábito e períodos; cinco abas; conquistas; cálculos de pontos, nível, ofensivas e estatísticas; o calendário e foco demonstrativos; dados de exemplo.

## Alterado para integrar o Trabalho 3

- Modelos originais anotados como entidades e IDs gerados pelo Room.
- AppState virou ViewModel e preservou sua API de consultas e cálculos para as telas.
- Mutações passaram a chamar DAOs em coroutines IO e transações.
- MainActivity fornece a instância Singleton do banco ao ViewModel.
- Listas carregam com LaunchedEffect(Unit).
- Detalhes consultam diretamente pelo ID recebido da navegação.
- Formulários aguardam sucesso antes de limpar dados ou navegar.
- Detalhe de recompensa recebeu edição, completando o CRUD das duas listas.
- Configurações persistem a preferência e restauram exemplos somente após confirmação.
- Mensagens da tela Sobre e README foram atualizadas para refletir a persistência.
- Testes e roteiro de evidências foram adaptados para os textos e fluxos do ZIP.

## Compatibilidade de dados

A migração 1 para 2 preserva o banco já instalado da implementação anterior com Room. Não há migração recuperável de dados que o Trabalho 2 já perdeu ao encerrar seu processo, pois aquela versão não os gravava em arquivo.

O applicationId foi preservado. A atualização por cima de um APK existente requer a mesma assinatura. Não desinstale nem limpe os dados para demonstrar a migração.

## Pendência externa

O emulador deste Windows falha antes de iniciar, com código 0xC0000135; MFPlat.dll está ausente. Sem outro aparelho conectado, não foi possível produzir o vídeo nem as capturas reais em dispositivo. O script e os testes instrumentados estão preparados para essa etapa.
