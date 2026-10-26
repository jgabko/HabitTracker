# Registro de validação

Data: 27 de outubro de 2026.

## Executado com sucesso

| Verificação | Resultado |
| --- | --- |
| `:app:assembleDebug` | APK de depuração gerado. |
| `:app:assembleDebugAndroidTest` | APK dos testes instrumentados compilado. |
| `:app:testDebugUnitTest` | 5 testes, 0 falhas, 0 erros, 0 testes ignorados. |
| `ConversoresTest` | 2 testes de conversão dos enums e dos dias. |
| `RoomLocalTest` | 2 testes com os DAOs gerados pelo Room, em ambiente Android de testes da JVM. |
| `ExampleUnitTest` | 1 teste já existente na base. |
| `git diff --check` | Sem erros de espaços em branco. |

O teste de Room executado usa um banco em arquivo e confirma que cadastros e edições permanecem após fechar e reabrir o banco; que exclusões continuam aplicadas; que saldo e histórico são preservados; e que uma transação com falha desfaz suas gravações.

## Ambiente de compilação

- Windows 10 x64.
- JDK 25 e Gradle 9.5.0.
- Android Gradle Plugin 9.3.2.
- Android SDK Platform 37.0 e Build Tools 36.0.0.
- Room 2.8.5, KSP 2.3.12 e Robolectric 4.17.
- Os testes locais de Room usam o ambiente Android API 35 do Robolectric.

## Pendente em dispositivo

Os testes instrumentados e o percurso de CRUD pela interface foram compilados, mas não executados em aparelho ou emulador nesta validação. O emulador oficial não iniciou neste Windows porque o componente de sistema `MFPlat.dll` não está disponível. Não havia outro dispositivo disponível.

Assim, **o teste visual de adicionar, encerrar completamente o processo e reabrir o aplicativo ainda precisa ser executado e gravado**. O teste local de banco não foi apresentado como se fosse esse vídeo.

Para concluir as evidências, usar [ROTEIRO_DE_EVIDENCIAS.md](ROTEIRO_DE_EVIDENCIAS.md) e, em um aparelho ou emulador de teste, executar `scripts/testar-persistencia.ps1`. O script instala os APKs de debug, roda o CRUD pela UI, força a parada entre as etapas de persistência, verifica a reabertura e copia as capturas reais para `docs/evidencias`.

O relato das contribuições e dificuldades pessoais dos integrantes também precisa ser completado por eles, conforme a seção de documentação do enunciado.
