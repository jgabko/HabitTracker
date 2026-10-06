# HabitTracker

App Android de hábitos e foco feito em **Kotlin + Jetpack Compose**, para o **Trabalho 2** de Desenvolvimento de Aplicativos Móveis.

**Ideia:** concluir hábitos e fazer sessões de foco gera **minutos de tempo livre**, que podem ser trocados por recompensas na loja. O app tem tema escuro e um avatar com nível e atributos que evoluem com o uso.

**Autor:** João Gabriel Mota Kochla

---

## Como rodar

**Requisitos:** Android Studio recente (com JDK embutido) e um emulador ou celular Android com API 24 ou superior.

1. Abra a pasta do projeto no Android Studio (**File > Open**) e aguarde o *Gradle Sync* terminar.
2. Crie ou abra um emulador (Device Manager) ou conecte um celular com depuração USB.
3. Selecione o módulo `app` e clique em **Run ▶**.

Pela linha de comando: `./gradlew installDebug` (no Windows, `gradlew.bat installDebug`).

> **Dados só na memória:** ao fechar o app, tudo volta aos dados de exemplo. Guardar os dados é assunto do próximo trabalho.

**Tecnologias:** Kotlin 2.2.10, Jetpack Compose (Material 3, BOM 2026.02.01), Navigation Compose 2.9.3, Android Gradle Plugin 9.3.2.

---

## Documentação do processo e das decisões

A documentação, com prints de cada etapa e as respostas da seção 4 do enunciado, está no Google Slides:
https://drive.google.com/drive/folders/1l4ykIoI8mic6v1ow-0GJpbFkHDKKSnLH?usp=sharing

Resumo das decisões:

- **Tema:** uso apps de hábitos e quis fazer o meu. Hábitos e foco geram minutos de tempo livre (a única "moeda" do app), gastos em recompensas.
- **Estado único:** a classe `AppState` guarda as listas (`mutableStateListOf`) e as regras (concluir hábito, registrar recaída, resgatar recompensa, registrar sessão de foco). As telas só chamam as funções dela.
- **Rotas:** o `object Rotas` concentra todas as rotas nomeadas. Os detalhes recebem só o **id** pela rota (`habito/{id}`, `recompensa/{id}`) e buscam o item no `AppState`.
- **Navegação:** `NavHost`, `Scaffold`, `TopAppBar` e `NavigationBar` ficam juntos em `AppNavigation`. A barra inferior só aparece nas 5 abas; as telas internas têm botão de voltar (`popBackStack()`).
- **Complexidade extra no Detalhe do Hábito:** calcula ofensiva atual, melhor ofensiva, total de conclusões e taxa dos últimos 7 dias; permite editar o hábito na própria tela; e abre o Foco já com aquele hábito. Escolhi essa tela porque é onde o usuário vê se o hábito está funcionando e age sobre ele.
- **Simulações (placeholders):** o Foco não usa temporizador real (o botão "+5 min" avança o tempo), o "hoje" é um dia fixo (dia 15 de 30), as notificações são só um Switch, e o avatar é um círculo com emoji.

---

## Telas

O app tem **12 telas**: 5 abas na barra inferior e 7 telas internas.

| Tela | Tipo | O que faz |
|---|---|---|
| Início | Aba | Saudação, saldo de tempo livre e hábitos de hoje com check. Engrenagem abre Configurações, "Ver todos" abre Meus Hábitos e o botão "+" abre Novo Hábito. |
| Progresso | Aba | Ofensiva atual, calendário do mês com cor por intensidade, "dias sem" dos hábitos de evitar e botão para Estatísticas. |
| Foco | Aba | Pomodoro simulado: escolhe hábito e duração (15, 25 ou 45 min), Iniciar, "+5 min", Resetar. Ao completar, registra a sessão e soma tempo livre. |
| Loja | Aba | Duas abas internas: **Conquistas** (lista de medalhas) e **Loja** (Lista 2: recompensas). |
| Perfil | Aba | Avatar com emoji, nível e 5 atributos (Força, Vitalidade, Sabedoria, Foco, Disciplina). Botão para Configurações. |
| Meus Hábitos | Interna | **Lista 1:** hábitos com formulário rápido, Checkbox, lixeira e clique que abre o Detalhe. |
| Novo Hábito | Interna | Formulário completo: nome, tipo, categoria, dias da semana, período e recompensa em minutos. |
| Detalhe do Hábito | Interna | Estatísticas calculadas, edição na própria tela, "Registrar recaída" (hábitos de evitar) e "Focar neste hábito". |
| Detalhe da Recompensa | Interna | Custo, saldo, quanto falta, vezes resgatada e botão Resgatar. |
| Estatísticas | Interna | Gráfico de barras feito com `Box`, tempo total de foco, média diária, taxa de conclusão e melhor ofensiva. |
| Configurações | Interna | Switch de notificações (simulado), "Sobre o app" e "Resetar dados de exemplo" com confirmação. |
| Sobre | Interna | Informações do app. |

---

## Requisitos do trabalho (onde está cada um)

| Requisito | Onde |
|---|---|
| `NavigationBar` funcionando | `navigation/AppNavigation.kt` (5 abas) |
| Botões com `navController.navigate(...)` | Todas as telas; nenhum botão sem função |
| `TopAppBar` com voltar (`popBackStack()`) | `AppNavigation.kt`, em todas as telas internas |
| `object Rotas` | `navigation/Rotas.kt` |
| 2 data classes de itens | `model/Habito.kt` e `model/Recompensa.kt` (mais `Registro` para o histórico) |
| 2 listas com `LazyColumn` + `Card` + `mutableStateListOf` | `HabitosScreen.kt` e a aba Loja em `LojaScreen.kt` (listas em `data/AppState.kt`) |
| Adicionar por formulário (`OutlinedTextField` + `Button`) | Hábitos (nome e categoria) e Loja (nome e custo, campo numérico) |
| Remover ou marcar | Lixeira e Checkbox em Hábitos; lixeira e Resgatar na Loja |
| Detalhe do item certo, via id na rota | `DetalheHabitoScreen.kt` e `DetalheRecompensaScreen.kt` |
| Detalhe com algo a mais | Detalhe do Hábito (cálculos, edição e navegação ao Foco) |
| `Row`/`Column`/`Box`, `Scaffold`, `remember`/`mutableStateOf` | Em todo o app |

---

## Estrutura do código

```
app/src/main/java/com/example/habittracker/
├── MainActivity.kt            # cria o AppState e chama o AppNavigation
├── model/                     # data classes e enums (Habito, Recompensa, Registro, Atributos)
├── data/
│   ├── AppState.kt            # listas reativas, regras e dados de exemplo
│   └── Conquistas.kt          # lista de medalhas calculada a partir do estado
├── navigation/
│   ├── Rotas.kt               # object Rotas
│   └── AppNavigation.kt       # Scaffold, TopAppBar, NavigationBar e NavHost
└── ui/
    ├── theme/                 # paleta escura (darkColorScheme) e tipografia
    ├── components/            # cartão padrão, barra de progresso, seletor de dias, chips...
    └── screens/               # as 12 telas
```

---

## Limitações conhecidas

- Os dados não sobrevivem ao fechamento do app (previsto no enunciado).
- O dia "hoje" é fixo e não há botão para avançar o dia.
- Na tela Meus Hábitos, o Checkbox conclui o hábito **hoje** mesmo que ele não esteja agendado para o dia atual.
- Notificações, escudos de ofensiva, login e idioma ficaram fora do escopo.

---

## Status das etapas

- [x] Etapa 0 – Projeto e dependência de navegação
- [x] Etapa 1 – Tema escuro e componentes base
- [x] Etapa 2 – Modelos e estado
- [x] Etapa 3 – Navegação
- [x] Etapa 4 – Início
- [x] Etapa 5 – Lista de Hábitos
- [x] Etapa 6 – Novo Hábito
- [x] Etapa 7 – Detalhe do Hábito
- [x] Etapa 8 – Loja e Detalhe da Recompensa
- [x] Etapa 9 – Foco simulado
- [x] Etapa 10 – Progresso
- [x] Etapa 11 – Perfil
- [x] Etapa 12 – Estatísticas
- [x] Etapa 13 – Configurações
- [x] Etapa 14 – Testes finais e entrega
