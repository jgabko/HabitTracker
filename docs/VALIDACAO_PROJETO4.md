# Validação da versão simplificada

Data: 3 de novembro de 2026.

```text
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest
BUILD SUCCESSFUL in 57s
```

**15 testes executados, sem falhas, erros ou testes ignorados.** O APK do aplicativo e o APK de testes instrumentados foram compilados.

| Classe | Testes | Verificação |
| --- | ---: | --- |
| ConversoresTest | 2 | Enums e dias da semana. |
| RoomLocalTest | 2 | CRUD, persistência em arquivo, histórico e rollback. |
| MigracaoBancoTest | 2 | Migração da base versão 1, com e sem perfil. |
| MvvmIntegracaoTest | 7 | Exemplos, cálculos, formulários, CRUD, Flow, saldo, foco, recaída, reabertura e resgates simultâneos. |
| CrudInterfaceLocalTest | 1 | Percurso pelas telas reais do Compose e recriação da Activity com o formulário preenchido. |
| ExampleUnitTest | 1 | Teste simples já existente. |

No teste de resgates simultâneos, duas tentativas de gastar 30 minutos partem do saldo de 45. Somente uma é concluída: o saldo termina em 15 e há um único resgate no histórico. Isso verifica a proteção da transação do Room depois da remoção do Mutex.

O JSON do esquema versão 2 continua idêntico ao da versão anterior. As telas continuam sem acesso direto ao DAO e sem coroutines de banco. Não foram acrescentadas dependências. Os avisos de APIs obsoletas existentes e de acesso nativo do Java não impediram a execução.

As [nove capturas](evidencias/projeto4/README.md) foram atualizadas pela execução desta versão no Robolectric. Elas não são capturas de celular. O vídeo em aparelho continua pendente.

Ambiente: Windows, JDK 25, Gradle 9.5.0, AGP 9.3.2, SDK 37 e Robolectric 4.17/API 35.

O [relatório JSON](RESULTADOS_PROJETO4.json) registra os casos executados. SHA-256 do APK:

```text
fdf2c8c80271015e39e133c5b845403b3ed4d1c9edc25eb322f631f6b718030c
```

A versão simplificada é entregue na `main` do mesmo repositório. Os documentos `VALIDACAO.md` e `RESULTADOS_DOS_TESTES.json` são históricos do Trabalho 3.
