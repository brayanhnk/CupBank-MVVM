# CupBank — Como o Room funciona e o que foi adicionado (Trabalho 3)

Este arquivo explica só a parte nova: o banco de dados local. O README.md principal continua sendo o mapa do app.

## 1. A ideia em uma frase

No Trabalho 2 as listas viviam em `mutableStateListOf` (memória) e sumiam ao fechar o app.
Agora **o banco SQLite (via Room) é a fonte da verdade**: a tela só mostra uma cópia do que está no banco.

## 2. O caminho de um dado

```
Tela (Compose)  ──clique──►  State (InvestimentosState / CartaoState)
                                   │  CoroutineScope(Dispatchers.IO).launch { ... }
                                   ▼
                               DAO (suspend fun)  ──►  AppDatabase (Room/SQLite, arquivo no aparelho)
                                   │
                                   ▼
                       carregar() lê tudo de novo  ──►  State atualiza a lista  ──►  Tela redesenha
```

1. A tela chama um método do State (ex.: `adicionarAtivo`).
2. O State grava no banco numa coroutine `Dispatchers.IO` (nunca na thread principal, senão a tela trava).
3. Quando termina, o State chama `carregar()`, que faz `dao.buscarTodos()` e troca a lista.
4. Como a lista é um `mutableStateOf`, o Compose redesenha sozinho.
5. Ao abrir uma tela de lista, um `LaunchedEffect(Unit)` chama `carregar()` para ler o que já estava salvo.

## 3. O que foi criado

### Arquivos novos
| Arquivo | Conteúdo |
|---|---|
| `data/AppDatabase.kt` | Classe do banco (Singleton) + carga inicial dos dados de exemplo |
| `data/Daos.kt` | `AtivoDao`, `OrdemDao`, `TransacaoCartaoDao`, `MovimentoDao` |

### Arquivos alterados
| Arquivo | O que mudou |
|---|---|
| `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts` | Plugin KSP + dependências do Room |
| `investimentos/Modelos.kt` | `Ativo` e `Ordem` viraram `@Entity` (ganharam `id` autoincrement) |
| `TelaCartao.kt` | `TransacaoCartao` virou `@Entity`; `CartaoState` usa o DAO; botão de editar; Detalhe busca por id |
| `investimentos/InvestimentosState.kt` | Reescrito para usar `AtivoDao` e `OrdemDao` |
| `investimentos/InvestirScreen.kt` | `LaunchedEffect` para buscar; menu Editar/Remover ao segurar um ativo |
| `investimentos/DetalheAtivoScreen.kt` | Garante que os dados foram carregados antes de mostrar |
| `CupBankApp.kt` | Abre o banco e entrega os DAOs aos States |

### Entities (tabelas)
- `ativos`: id, ticker (único), nome, preco, variacao, tipo
- `ordens`: id, ticker, quantidade, precoUnitario
- `transacoes_cartao`: id, descricao, valor, data, categoria, pago
- `movimentos` (Extrato): id, titulo, detalhe, valor, data

### DAOs
Cada um tem `@Insert`, `@Query("SELECT * ...")`, `@Update`, `@Delete`, todos `suspend`. Extras:
`buscarPorId` e `buscarPorTicker` (ativos), `deletarPorTicker` (ordens), `buscarPorId` (transações).

### AppDatabase
`@Database(entities = [...], version = 2)`, Singleton (`companion object` + `@Volatile` + `synchronized`).
Um `Callback.onCreate` insere os dados de exemplo **uma única vez**, quando o arquivo do banco é criado.

## 4. O que cada ação faz no banco

| Ação na tela | Operação |
|---|---|
| Abrir Investir / Cartão | `buscarTodos()` |
| "+ Investir" | `inserir(Ativo)` |
| Segurar ativo → Editar | `atualizar(Ativo)` (o código não muda) |
| Segurar ativo → Remover | `deletarPorTicker` nas ordens + `deletar(Ativo)` |
| Confirmar compra | `inserir(Ordem)` |
| Cancelar ordem | `deletar(Ordem)` |
| "+" do cartão | `inserir(TransacaoCartao)` |
| Lápis da transação | `atualizar(TransacaoCartao)` |
| Lixeira da transação | `deletar(TransacaoCartao)` |
| Pagar fatura | `atualizar` de cada transação pendente com `pago = true` + `inserir(Movimento)` no Extrato |
| Receber PIX / Enviar PIX | `inserir(Movimento)`; o saldo é recalculado |
| Abrir o app | `buscarTodos()` dos movimentos (saldo e Extrato) |
| Abrir detalhe da transação | `buscarPorId(id)` (a rota só leva o id) |

## 5. Mudanças de comportamento

- **Fatura do cartão** = soma das transações não pagas (antes era um número fixo de R$ 900). Abre com R$ 478,40.
- **Patrimônio** = R$ 8.640,75 + soma das ordens salvas.
- **Saldo** = R$ 376,80 (base) + soma dos movimentos do Extrato. Com os 3 movimentos de exemplo dá os R$ 3.284,50 de antes. Receber/enviar PIX e pagar fatura agora são salvos.
- **Banco versão 2:** a tabela `movimentos` entrou por uma `Migration(1, 2)`, então quem já tinha o app instalado não perde os ativos e transações.
- Erros de banco são capturados com `try/catch` e aparecem no Logcat com a tag `InvestimentosState` ou `CartaoState`.

## 6. Teste de aceitação

Adicionar um item → fechar o app **por completo** (tirar dos recentes) → abrir de novo → o item continua lá.
Para voltar ao estado inicial: Android Studio → Device Explorer, ou "Clear storage" nas configurações do app.

## 7. Se o projeto não roda

Não consegui compilar o projeto no ambiente onde escrevi o código, então estes são os suspeitos, do mais provável ao menos:

1. **Gradle Sync falhou ao baixar plugin/biblioteca.** Precisa de internet. Veja a aba *Build* → mensagem em vermelho. Tente File → Sync Project with Gradle Files.
2. **Erro mencionando `kotlin.sourceSets` / "built-in Kotlin"** (KSP com AGP 9). Opção 1: confira `ksp = "2.3.6"` em `libs.versions.toml`. Opção 2 (contorno conhecido): adicione `android.disallowKotlinSourceSets=false` ao `gradle.properties`.
3. **Erro de versão do KSP/Kotlin** (ex.: "requires Kotlin ..."). O projeto usa Kotlin 2.2.10; se o erro apontar incompatibilidade, suba `kotlin` para 2.3.10 em `libs.versions.toml`, ou teste outra versão de `ksp`.
4. **Erro vermelho em algum `.kt`** (Build → mensagem com arquivo e linha). Mande a linha que eu corrijo.
5. **O app abre e fecha na hora (crash).** Veja o Logcat filtrando por `AndroidRuntime` ou `Room`. Se vier de um banco antigo com formato diferente, desinstale o app do emulador e rode de novo.
6. **Ambiente:** JDK/Gradle desatualizado (o projeto usa Gradle 9.5.0, que o Android Studio atual baixa sozinho), ou pasta aberta errada (abrir a que tem o `settings.gradle.kts`).
