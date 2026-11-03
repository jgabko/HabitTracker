# HabitTracker — Projeto 4

O mesmo aplicativo do Trabalho 3, organizado com o MVVM mostrado na aula de Filmes.

## Como executar

Abra a pasta no Android Studio, configure JDK 25 e Android SDK 37 e execute o módulo `app`.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

## Organização

- `data`: Entity, DAO, Database e conversores.
- `data.repository`: HabitoRepository, RecompensaRepository, RegistroRepository e PerfilRepository.
- `viewmodel`: dois ViewModels, dois UiStates e duas Factories.
- `ui`: as mesmas telas e componentes.
- `navigation`: as mesmas rotas.

As telas leem `uiState` com `collectAsState()` e chamam os métodos do ViewModel. Cada ViewModel coleta os Flows no `init`, igual ao exemplo do professor. `onSalvar` verifica o formulário e decide entre inserir e atualizar.

A versão simplificada usa campos diretos nos UiStates. Foram retirados o repositório coordenador, os formulários aninhados, a classe separada do foco, o `combine`, o `Mutex` e o executor genérico de ações. As transações do Room continuam nas operações de saldo e histórico.

As telas, os cadastros, o foco simulado, as conquistas e a persistência foram mantidos. O banco continua na versão 2 com a migração já existente. O calendário continua demonstrativo, com hoje no dia 15.

## Documentação

- [Processo e decisões](docs/PROJETO4.md).
- [Testes desta versão](docs/VALIDACAO_PROJETO4.md).
- [Capturas no Robolectric](docs/evidencias/projeto4/README.md).

Para testar em um aparelho conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
.\scripts\testar-persistencia.ps1 -GravarVideo
```

O vídeo em aparelho e o relato pessoal dos integrantes precisam ser feitos pelos autores antes da apresentação.
