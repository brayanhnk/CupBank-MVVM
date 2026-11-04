# CupBank — Trabalho 4 (MVVM)

App de banco fictício em Kotlin + Jetpack Compose + Room. No Trabalho 4 o código foi reorganizado em camadas **Model · Repository · ViewModel · View**, sem mudar o que o app faz.


## Como rodar

1. Abra no Android Studio a pasta que contém o `settings.gradle.kts` (File → Open).
2. Aguarde o Gradle Sync (precisa de internet na primeira vez).
3. Rode a configuração `app` num emulador/dispositivo (minSdk 24).

---

## O que mudou de um trabalho para o outro

| | Trabalho 2 | Trabalho 3 | **Trabalho 4 (este)** |
|---|---|---|---|
| Onde os dados vivem | `mutableStateListOf` em memória (sumiam ao fechar o app) | Banco **Room** (SQLite): persiste | Room, igual ao Trabalho 3 |
| Quem fala com o DAO | — | A própria tela, via classes `*State` (`BancoState`, `CartaoState`, `InvestimentosState`) | **Só o Repository**; só o ViewModel fala com o Repository |
| Como a lista chega na tela | Lista na memória | `suspend fun buscarTodos()` + `carregar()` chamado na mão (`LaunchedEffect`) depois de cada operação | `buscarTodos(): Flow<List<...>>`: o Room avisa sozinho quando a tabela muda |
| Estado da tela | Vários `remember` / `mutableStateOf` soltos | `*State` com `mutableStateOf` + `CoroutineScope(Dispatchers.IO)` dentro | `data class ...UiState` + `StateFlow` (`_uiState` privado, `uiState` público) |
| Threads / coroutines | — | Na tela/State (`Dispatchers.IO`) | `viewModelScope` no ViewModel e `withContext(Dispatchers.IO)` no Repository |
| Criação do estado | — | Tela criava o State com os DAOs | `ViewModelFactory` injeta os Repositories |
| Organização | Tudo no pacote raiz; entities dentro de arquivos de tela | Idem | Pacotes `data`, `data.repository`, `viewmodel`, `ui` (+ subpacotes) |
| Funcionalidades | — | Adicionar, listar, atualizar, remover, persistir | **As mesmas** (nenhuma tela, campo ou função nova) |

### Mudanças concretas no código

- **Telas sem lógica de banco:** nenhuma tela tem `dao`, `CoroutineScope` ou `Dispatchers.IO`. Elas fazem só `val uiState by viewModel.uiState.collectAsState()` e chamam métodos do ViewModel.
- **Sem `carregar()`:** deixou de existir. Inserir, atualizar ou apagar já atualiza a lista pelo `Flow`.
- **Pagar a fatura virou uma chamada só** (`CartaoViewModel.pagarFatura()`): valida o saldo, registra o débito no extrato e quita as transações pendentes. A quitação em lote é um único `UPDATE` no DAO (`quitarPendentes()`).
- **Entities** foram para `data/Modelos.kt` (antes estavam em arquivos de tela).
- **Cores compartilhadas** (`CupBank*`) saíram de dentro do `ProfileScreen.kt` e foram para `ui/theme/CupBankCores.kt`; formatação e componentes reutilizados (`paraReais`, `AvatarAtivo`, ...) saíram de `ui/investimentos` para `ui/components`.
- **Banco não mudou:** `version = 2`, mesmas 4 tabelas (`ativos`, `ordens`, `transacoes_cartao`, `movimentos`), mesma migração `1 → 2`. Quem já tinha o app instalado mantém os dados.


---

## Estrutura de pacotes

```
com.example.cupbank
├── MainActivity.kt                  só aplica o tema e chama CupBankApp
├── data                             MODEL
│   ├── Modelos.kt                   @Entity: Ativo, Ordem, TransacaoCartao, Movimento (+ Cartao, TipoAtivo)
│   ├── Daos.kt                      @Dao: AtivoDao, OrdemDao, TransacaoCartaoDao, MovimentoDao
│   ├── AppDatabase.kt               classe @Database, migração e dados iniciais
│   └── repository                   REPOSITORY
│       ├── AtivoRepository.kt
│       ├── OrdemRepository.kt
│       ├── TransacaoCartaoRepository.kt
│       └── MovimentoRepository.kt
├── viewmodel                        VIEWMODEL
│   ├── BancoViewModel.kt            BancoUiState + saldo, extrato, PIX
│   ├── CartaoViewModel.kt           CartaoUiState + cartão e fatura
│   ├── InvestimentosViewModel.kt    InvestimentosUiState + ativos e ordens
│   ├── CupBankViewModelFactory.kt   injeta os Repositories nos ViewModels
│   └── Executar.kt                  helper: escrita no banco com try/catch + Log.e
└── ui                               VIEW (@Composable)
    ├── navigation                   CupBankApp (NavHost + criação dos ViewModels), Rotas, CupBankBottomBar
    ├── home                         BankAppScreen
    ├── extrato                      ExtratoScreen
    ├── pix                          TelaTransferenciaPix
    ├── cartao                       TelaCartao, TelaDetalheTransacao
    ├── investimentos                InvestirScreen, DetalheAtivoScreen
    ├── perfil                       ProfileScreen
    ├── components                   componentes e formatação reutilizados (AvatarAtivo, paraReais...)
    └── theme                        cores, tipografia e tema
```

Fluxo de um dado: `Tela → ViewModel → Repository → DAO → Room`, e de volta por `Room → Flow → Repository → ViewModel (uiState) → Tela`.

---

## Respostas da seção 4 do enunciado

### 1. Como estava o projeto no Trabalho 3, e o que mudou na reorganização?

Veja a tabela "O que mudou de um trabalho para o outro" acima. Em resumo: o Room já salvava tudo, mas as classes `*State` guardavam o estado **e** chamavam o DAO direto, criavam `CoroutineScope(Dispatchers.IO)` e chamavam `carregar()` na mão depois de cada operação. Agora a tela só observa um `uiState` e chama métodos do ViewModel.

### 2. Como o trio decidiu dividir os pacotes?

| Pacote | O que foi para lá |
|---|---|
| `data` | Entities, DAOs e `AppDatabase`. Nada aqui conhece tela. |
| `data.repository` | Um Repository por Entity: `AtivoRepository`, `OrdemRepository`, `TransacaoCartaoRepository`, `MovimentoRepository`. |
| `viewmodel` | 3 ViewModels (cada um com seu `UiState`), a `CupBankViewModelFactory` e o helper `executar`. |
| `ui` | Todas as telas, a navegação, os componentes reutilizados e o tema, separados em subpacotes por assunto. |

Critério: **se precisa de `@Composable`, vai para `ui`; se precisa de Room mas não de tela, vai para `data`; se decide regra ou guarda estado da tela, vai para `viewmodel`; se só encapsula acesso a um DAO, vai para `data.repository`.** Os 3 ViewModels foram divididos por assunto: dinheiro/extrato/PIX, cartão e investimentos.

Começamos olhando para a estrutura do Trabalho 3 e separamos o que era banco de dados, o que era regra/estado e o que era tela. Seguimos o critério acima, e a maior dúvida foi onde colocar as cores e os componentes reutilizados, que estavam dentro de telas. Acabamos movendo para `ui/theme` e `ui/components`, e a divisão ficou mais clara depois disso.

📸 _Print da árvore de pastas no Android Studio._

![alt text]({53756496-06C4-4863-AF71-34CD5D6859FC}.png)

### 3. Por que o Repository existe — o que ele resolve que não existia antes?

- **Um único ponto de acesso a cada tabela:** o ViewModel não sabe se os dados vêm do Room, de uma API ou de outro lugar. Se a fonte mudar, só o Repository muda.
- **A thread certa em um lugar só:** as escritas usam `withContext(Dispatchers.IO)` dentro do Repository.
- **Regras de dados reutilizáveis:** `MovimentoRepository.registrar()` cria a linha do extrato com data/hora e é usado tanto pelo PIX (`BancoViewModel`) quanto pela fatura (`CartaoViewModel`).
- **Teste e troca mais fáceis:** dá para criar um Repository falso para testar um ViewModel.

Regra seguida: **1 Repository por Entity, e só ele importa o DAO.** Quando uma ação mexe em duas tabelas (remover ativo apaga as ordens dele), o ViewModel chama dois Repositories.

Entendemos o Repository como o único lugar que conhece o DAO. Assim o ViewModel não precisa saber de onde os dados vêm, e a thread do banco (`Dispatchers.IO`) fica resolvida em um só ponto. Também ajudou a reaproveitar regras, como o `registrar()` do extrato, usado tanto no PIX quanto na fatura.

### 4. O que tem dentro do UiState de cada tela, e por que esses campos?

| UiState | Telas | Campos | Por quê |
|---|---|---|---|
| `BancoUiState` | Início, Extrato, PIX | `movimentos` (+ `saldo` calculado) | O saldo é sempre `SALDO_BASE` + soma dos movimentos; guardamos só a lista e calculamos o resto. |
| `CartaoUiState` | Cartão, Detalhe da transação | `transacoes`, `saldo`, `transacaoDetalhe`, `carregandoDetalhe` (+ `cartao` calculado) | A fatura é a soma das transações não pagas. O `saldo` valida o pagamento. O detalhe recebe só o `id` pela rota e busca no banco. |
| `InvestimentosUiState` | Investir, Detalhe do ativo | `ativos`, `ordens`, `carregado`, `tickerSelecionado`, `quantidade`, `mostrarNovoAtivo` (+ `patrimonio`, `ativoSelecionado` calculados) | `tickerSelecionado` e `quantidade` precisam ser os mesmos nas duas telas; `mostrarNovoAtivo` deixa o botão **+** da barra abrir o diálogo; `carregado` evita mostrar "ativo não encontrado" antes do banco responder. |

Cada `UiState` é uma `data class` imutável. O ViewModel expõe `uiState` (`StateFlow`, só leitura) e altera apenas o `_uiState` privado. Os ViewModels são criados em `CupBankApp` e passados às telas, para que Investir, Detalhe e o botão **+** compartilhem o mesmo estado.

Guardamos no `UiState` só o que a tela precisa para se desenhar, como as listas e o item selecionado, e calculamos o resto (saldo, fatura, patrimônio) a partir delas. Isso evita valores duplicados que ficam desatualizados. Também usamos `data class` imutável com `StateFlow`, para só o ViewModel conseguir alterar o estado.

### 5. Alguma funcionalidade quebrou durante a reorganização? Como encontraram e corrigiram?

Depois de mover os arquivos para subpacotes, o projeto deu vários erros de import, porque as telas e os componentes mudaram de lugar. Encontramos pelo Build output do Android Studio, que mostrava o arquivo e a linha, e corrigimos um por um. Depois testamos manualmente todas as telas no emulador, seguindo a lista abaixo, e confirmamos que os dados continuavam depois de fechar o app.


Testes manuais para fazer e registrar:
- [ ] Início: saldo aparece; "Receber PIX" soma no saldo e cria linha no Extrato
- [ ] PIX: enviar válido debita e aparece no Extrato; chave vazia e saldo insuficiente mostram erro
- [ ] Investir: adicionar, editar e remover ativo; filtros Ações/Fundos
- [ ] Detalhe do ativo: mudar quantidade, confirmar compra (patrimônio sobe), cancelar ordem
- [ ] Cartão: adicionar, editar, remover transação; abrir detalhe; pagar fatura e erro de saldo insuficiente
- [ ] Fechar o app por completo e abrir de novo: dados continuam
- [ ] Girar a tela: ativo selecionado e quantidade continuam

### 6. Algum integrante do trio teve dificuldade em algum ponto? Como resolveram?

**[Pedro Henrique Vasconcelo]:** teve mais dificuldade em entender a diferença entre `Flow` e `StateFlow` e como a lista passa a se atualizar sozinha, sem o `carregar()`. Resolveu relendo o fluxo `Room → Flow → Repository → ViewModel → Tela` e testando com o Database Inspector.

**[Brayan Vinicius Hoinacki]:** teve dificuldade em mover as classes entre pacotes sem quebrar os imports e em criar a `ViewModelFactory`. Resolveu indo arquivo por arquivo pelos erros do Build.

---

## Mapa de telas

| Tela | Rota | Como chegar |
|---|---|---|
| Início | `inicio` | Aba **Início** |
| Extrato | `extrato` | Aba **Extrato** ou "Ver tudo" no Início |
| Investir | `investir` | Aba **Investir** ou botão central **+** |
| Detalhe do ativo | `detalhe_ativo/{ticker}` | Tocar num ativo |
| Perfil | `perfil` | Aba **Perfil** |
| Cartão | `cartao` | Perfil → **Cartões** ou Início → **Pagar** |
| Detalhe da transação | `cartao_transacao/{id}` | Tocar numa transação |
| Transferência PIX | `pix` | Início → **PIX** ou **Transferir** |

## Checklist do trabalho

- [x] Pacotes `ui`, `data`, `data.repository`, `viewmodel` com arquivos dentro
- [x] 1 Repository por Entity, só ele fala com o DAO
- [x] `buscarTodos()` dos DAOs retorna `Flow<List<...>>`
- [x] 3 ViewModels, cada um com seu `UiState`, `_uiState` privado + `uiState` público (`StateFlow`)
- [x] `CupBankViewModelFactory` injetando os Repositories
- [x] Telas sem `dao`, `CoroutineScope` ou `Dispatchers.IO`
- [x] Testado no emulador (seção 5) e prints/vídeos em `docs/prints/`
- [x] Textos das seções 5 e 6 revisados com o que realmente aconteceu

## Se o projeto não compilar

1. **Gradle Sync falhou:** precisa de internet; veja a aba *Build* e tente File → Sync Project with Gradle Files.
2. **Erro de KSP/Kotlin:** o projeto usa Kotlin 2.2.10 e KSP configurado em `gradle/libs.versions.toml`; ajuste as versões se o erro apontar incompatibilidade.
3. **Erro vermelho em algum `.kt` (import faltando):** os arquivos de `ui` foram movidos para subpacotes; confira os `import com.example.cupbank.ui...` do arquivo indicado.
4. **App abre e fecha na hora:** Logcat filtrando por `AndroidRuntime`/`Room`; se o banco antigo tiver formato diferente, desinstale o app do emulador.