# Roteiro de demonstração e evidências

## Preparação

1. Abrir o projeto no Android Studio e sincronizar o Gradle.
2. Conectar um aparelho ou iniciar um emulador Android com API 24 ou superior.
3. Instalar e executar o módulo `app`.
4. Gravar a tela do aparelho ou do emulador. Não usar imagens de mockup como prova de execução.

## Cadastro edição e exclusão de hábito

1. Abrir **Novo hábito** pela tela Início.
2. Informar um nome identificável, como **Caminhar para o teste**.
3. Selecionar categoria, dias, período, atributo e minutos. Salvar.
4. Abrir **Ver todos** e mostrar o novo cartão.
5. Abrir o cartão, tocar em **Editar hábito**, alterar o nome e os minutos e salvar.
6. Voltar para a lista e mostrar os novos valores.
7. Remover esse hábito e mostrar que ele saiu da lista.

## Cadastro edição e exclusão de recompensa

1. Abrir **Loja**.
2. Criar uma recompensa com nome e custo positivo.
3. Abrir seu cartão e editar nome e custo.
4. Salvar e mostrar os valores atualizados.
5. Remover e mostrar que o cartão saiu da lista.

## Persistência com encerramento completo

1. Criar o hábito **Teste de persistência Room**.
2. Mostrar o cartão na lista e registrar um print.
3. Encerrar o app completamente. Em um aparelho de teste, usar **Configurações → Apps → HabitTracker → Forçar parada**, ou o comando de teste `adb shell am force-stop com.example.habittracker`.
4. Não limpar o armazenamento e não desinstalar o app.
5. Abrir o app novamente pelo ícone e mostrar o mesmo cartão, com o mesmo conteúdo.
6. Abrir o detalhe, confirmar os dados e registrar outro print.
7. Salvar o vídeo completo, sem cortar o trecho entre o encerramento e a reabertura.

Repetir com uma recompensa e, se possível, com uma edição e uma exclusão: após reabrir, a edição deve continuar salva e o item excluído não deve reaparecer.

## Organização dos arquivos

Armazenar capturas e gravações reais em `docs/evidencias`. Nomes sugeridos:

- `01-habito-adicionado.png`
- `02-habito-editado.png`
- `03-habito-removido.png`
- `04-recompensa-editada.png`
- `05-antes-de-fechar.png`
- `06-depois-de-reabrir.png`
- `persistencia-fechar-reabrir.mp4`

Relacionar cada arquivo ao passo demonstrado e informar o dispositivo, a versão do Android e a data do teste. Caso o vídeo esteja em um serviço externo, inserir um link acessível no README e conferir o acesso antes da entrega.
