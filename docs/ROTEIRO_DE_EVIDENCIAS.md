# Roteiro de demonstração e evidências

Use um aparelho ou emulador Android API 24 ou superior. Grave a tela real, sem limpar o armazenamento nem desinstalar entre o encerramento e a reabertura.

## CRUD de hábitos

1. No Início, toque no botão de adicionar ou entre em Ver todos e Formulário completo.
2. Preencha nome, tipo, categoria, dias e período. Toque em Salvar hábito.
3. Mostre o cadastro na lista e capture a tela.
4. Abra o cartão, role até Editar hábito, altere nome e minutos e salve.
5. Mostre o detalhe atualizado e capture a tela.
6. Use Remover hábito e confirme. Mostre que o item saiu da lista.

## CRUD de recompensas

1. Abra a aba inferior Loja e mantenha a aba interna Loja selecionada.
2. Cadastre nome e custo positivo. Mostre o cartão.
3. Abra o detalhe, role até Editar recompensa e altere nome e custo.
4. Salve e mostre os valores atualizados.
5. Use Remover recompensa, confirme e mostre a lista.

## Persistência entre processos

1. Crie o hábito Teste de persistência Room e uma recompensa identificável.
2. Capture os itens na lista.
3. Force a parada do app nas configurações do Android ou use:

```text
adb shell am force-stop com.example.habittracker
```

4. Abra novamente pelo ícone ou por `adb shell am start -n com.example.habittracker/.MainActivity`.
5. Mostre os mesmos cadastros e seus detalhes. Capture a tela.
6. Repita após uma edição e uma exclusão para verificar que os estados persistem.

## Funcionalidades do Trabalho 2

Mostre Início, Progresso, Foco, Loja, Conquistas, Perfil, Estatísticas e Configurações. Conclua um hábito e confira saldo e pontos. Conclua uma sessão simulada por passos de cinco minutos e resgate uma recompensa. Reabra o app e confira a preservação do saldo, histórico e resgates.

## Automação no Windows

Com Java e SDK configurados e um único dispositivo de teste autorizado:

```powershell
powershell -File scripts/testar-persistencia.ps1 -AdbPath "CAMINHO_DO_ADB" -GravarVideo
```

O script compila, instala por atualização, testa os DAOs e o percurso de CRUD, grava o CRUD e a persistência em vídeos separados e copia prints e vídeos para docs/evidencias. Durante o vídeo de persistência, a gravação continua enquanto o processo do app é encerrado e reaberto.

Testes via instrumentação são uma validação adicional. Na apresentação, demonstre também a abertura pelo ícone e o CRUD manual. Informe o modelo do dispositivo, a versão do Android e a data. Não apresente a compilação ou os testes JVM como se fossem evidências gravadas no aparelho.
