# Checkpoint 5 — Bug Hunt PetFiap

## Identificação

**Grupo:** ___ (PREENCHER)

| Integrante | RM | Turma |
|---|---|---|
| (PREENCHER) | | |
| (PREENCHER) | | |
| (PREENCHER) | | |
| (PREENCHER) | | |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | 12 / 12 |
| **Total de ajustes de Clean Code** | 6 / 6 |
| **Total de testes novos escritos** | 6 / 6 |
| **Suíte final (Run As → JUnit Test)** | 26 testes (20 entregues + 6 novos), 0 falhas |

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | `GeradorProtocoloTest`: `deveManterUmaUnicaInstancia` falhou no `assertSame` e `deveGerarProtocolosSequenciais` deu `expected: <2> but was: <1>` | `GeradorProtocolo.getInstancia()` (~linha 17) fazia `return new GeradorProtocolo()` e nunca guardava em `instancia`. Além disso `proximo()` não era `synchronized`, apesar do comentário dizer que era thread-safe | Guardar `instancia = new GeradorProtocolo()` e deixar `getInstancia()` e `proximo()` `synchronized` | Padrão Singleton (Aula 14), thread-safety |
| bug02 | `AtendimentoFactoryTest.deveCriarTosaQuandoTipoForTosa`: `expected: <Tosa> but was: <Banho>` | `AtendimentoFactory.criar` (~linha 15): `case "TOSA" -> new Banho(...)` | Trocado para `new Tosa(...)` | Padrão Factory e polimorfismo (Aula 14) |
| bug03 | `AtendimentoFactoryTest.devePreencherOsDadosDoPetNaConsulta`: `expected: <Mimi> but was: <null>` | Construtor de `ConsultaVeterinaria` (~linha 17) chamava `super()` vazio e perdia todos os dados (e o status ficava `null`) | `super(protocolo, petNome, petPorte, tutorNome, dataHora)` | Herança e construtores, `super` |
| bug04 | `AtendimentoBuilderTest.deveMontarAtendimentoCompleto`: `expected: <Rex> but was: <null>` | `AtendimentoBuilder.comPet` (~linha 20): `petNome = petNome;` atribuía o parâmetro a ele mesmo (faltava `this`) | `this.petNome = petNome;` | `this`, escopo de variável, encapsulamento |
| bug05 | `deveRecusarMontagemSemNomeDoPet` e `deveRecusarMontagemSemPorte`: `Expected IllegalArgumentException to be thrown, but nothing was thrown` | `AtendimentoBuilder.construir` não validava nada (o comentário dizia que o controller validava, mas o controller não valida) | `construir()` agora lança `IllegalArgumentException` se nome ou porte forem nulos ou vazios | Padrão Builder (objeto só nasce válido), exceções |
| bug06 | `AgendaServiceTest.deveRecusarAgendamentoComHorarioJaOcupado`: veio `NullPointerException` em vez de `HorarioOcupadoException` (o conflito passou e o `save` do mock devolveu `null`) | `AgendaService.agendar` (~linha 25) comparava `String` e `LocalDateTime` com `==` | Trocado por `.equals()` | `==` x `equals` (Aula 7) |
| bug07 | `AgendaServiceTest.deveLancarExcecaoQuandoAtendimentoNaoExiste`: `Expected AtendimentoNaoEncontradoException to be thrown, but nothing was thrown` | `AgendaService.buscarPorId` tinha `catch (Exception e) { return null; }` que engolia a exceção do `orElseThrow` | Removido o `try/catch`; a exceção chega a quem chamou (o controller já trata e devolve 404) | Exceções (Aula 11) |
| bug08 | `teste01` (novo) ficou vermelho: `expected: <60.0> but was: <100.0>` | `Banho.calcularPreco` (~linha 24): valores de PEQUENO (100) e GRANDE (60) estavam trocados | PEQUENO 60, MEDIO 80, GRANDE 100 | Polimorfismo e regras de negócio no model |
| bug09 | `teste02` (novo) ficou vermelho: `expected: <60> but was: <30>` | `Tosa.getDuracaoMinutos(String porte)` (~linha 33) criava um overload, então `getDuracaoMinutos()` continuava com o 30 da classe mãe | Método sem parâmetro e com `@Override`, retornando 60 | Sobrescrita x sobrecarga (Aula 7) |
| bug10 | `teste03` (novo) ficou vermelho: `Expected StatusInvalidoException to be thrown, but nothing was thrown` | `Atendimento.cancelar` (~linha 62) não checava o status e cancelava qualquer atendimento | Só cancela se estiver `AGENDADO`, senão lança `StatusInvalidoException` | Encapsulamento das regras de status, exceções |
| bug11 | `teste04` (novo) ficou vermelho: veio `NullPointerException` em vez de `IllegalArgumentException` (agendou no passado e o `save` do mock devolveu `null`) | `AgendaService.agendar` não validava a data/hora | Recusa data nula ou no passado com `IllegalArgumentException`, antes de consultar o banco | Validação de entrada (fail fast), exceções |
| bug12 | Achado lendo o código (code review): nenhum teste mostra, pois os testes não usam banco. Na API, o `POST /api/atendimentos` falharia ao salvar | `Atendimento.id` (~linha 26) tinha `@Id` sem `@GeneratedValue`, então o Hibernate não gerava o id | `@GeneratedValue(strategy = GenerationType.IDENTITY)` | JPA e persistência (Aulas 12/13) |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | `AtendimentoFactory.criar(int p, String t, String n, String po, String tu, LocalDateTime d)` | Nomes que revelam a intenção: parâmetros de uma letra não dizem o que são | Renomeados para `protocolo`, `tipo`, `petNome`, `petPorte`, `tutorNome`, `dataHora` |
| clean02 | `System.out.println` no `AgendaService.agendar` e no construtor do `GeradorProtocolo` | Não usar print para registrar eventos: usar logger | Print do recibo trocado por `log.info` (SLF4J, já vem no Spring Boot) e print do singleton removido |
| clean03 | `AtendimentoController`: método privado `calcularDescontoFidelidade` nunca usado e comentário de funcionalidade futura | Código morto e comentário que não explica o código (YAGNI) | Método e comentário removidos |
| clean04 | Strings repetidas `"AGENDADO"`, `"CONCLUIDO"`, `"CANCELADO"`, `"PEQUENO"`, `"MEDIO"` em várias classes | Evitar valores mágicos (e erro de digitação) | Criadas as constantes `STATUS_*` e `PORTE_*` em `Atendimento` e usadas no model e no service |
| clean05 | `AtendimentoController`: `ResponseEntity.status(201)` e `status(409)` | Evitar números mágicos: o código não diz o que 201 e 409 significam | Trocados por `HttpStatus.CREATED` e `HttpStatus.CONFLICT` |
| clean06 | Variável `a` no `for` do `AgendaService`, imports com `*` (`jakarta.persistence.*` e `web.bind.annotation.*`) e comentário errado do endpoint POST (`?tutorNome=Ana`) | Nomes claros, imports explícitos e comentários que não mentem | Variável renomeada para `existente`, imports explícitos e comentário corrigido |

## Parte 3 — Testes novos (regras que estavam sem cobertura)

| # | Teste escrito (classe.método) | Regra coberta | Resultado ao escrever (vermelho/verde) |
|---|---|---|---|
| teste01 | `BanhoTest.deveCobrarPrecoPorPorteQuandoBanho` | Preço do banho por porte: R$ 60 / 80 / 100 | Vermelho: revelou o bug08 (preços de PEQUENO e GRANDE trocados) |
| teste02 | `TosaTest.deveDurar60Minutos` | Duração da tosa: 60 min | Vermelho: revelou o bug09 (overload no lugar de override) |
| teste03 | `AtendimentoStatusTest.deveRecusarCancelamentoQuandoAtendimentoJaConcluido` | `cancelar()` em CONCLUIDO recusa com `StatusInvalidoException` | Vermelho: revelou o bug10 (cancelar sem checar status) |
| teste04 | `AgendaServiceTest.deveRecusarAgendamentoQuandoDataNoPassado` | Agendar no passado recusa com `IllegalArgumentException` e o banco nem é consultado | Vermelho: revelou o bug11 (data não era validada) |
| teste05 | `ConsultaVeterinariaTest.deveCobrar150ReaisQuandoQualquerPorte` | Consulta tem preço fixo de R$ 150, o porte não muda o preço | Verde de cara: a regra já estava correta |
| teste06 | `AtendimentoStatusTest.deveRecusarConclusaoQuandoAtendimentoCancelado` | `concluir()` em CANCELADO recusa com `StatusInvalidoException` | Verde de cara: a regra já estava correta |

---

## Parte 4 — Perguntas de reflexão

### 1. A suíte como contrato (Aula 15)
Rodei a suíte e li a mensagem de cada um dos 9 testes vermelhos. Mensagens como `expected: <Rex> but was: <null>` mostram o valor errado e o nome do teste diz onde olhar: esse me levou direto ao `comPet` do Builder, que tinha `petNome = petNome` sem o `this`. Corrigi um bug de cada vez e rodei tudo de novo para ver se algo quebrava. Testar na mão com curl é lento, precisa do Oracle no ar e é fácil esquecer um caso. A suíte roda em segundos, sem banco, sempre do mesmo jeito, e avisa na hora se uma correção estragou outra regra.

### 2. Mock e injeção de dependência (Aulas 13 a 15)
Em produção, o Spring cria o `AtendimentoRepository` e injeta no `AgendaService` por causa do `@Autowired`. No teste não sobe Spring, então quem faz esse trabalho é o Mockito: o `@Mock` cria um repository falso e o `@InjectMocks` coloca ele no campo `repository` do service. O falso só devolve o que o `when(...)` mandou, então nenhuma conexão com o Oracle é feita e o teste fica rápido e isolado. Isso só funciona porque o service recebe o repository de fora e não cria ele sozinho, que é a ideia da injeção de dependência.

### 3. `==` vs `.equals()` (Aula 7)
O `==` compara se os dois são o mesmo objeto na memória e o `.equals()` compara o conteúdo. No `agendar`, o nome "Rex" parecia funcionar porque literais iguais usam o mesmo objeto do pool de Strings, mas isso é sorte: um nome vindo de uma requisição é outro objeto. Já o `LocalDateTime` nunca é o mesmo objeto, então `a.getDataHora() == novo.getDataHora()` dava sempre `false` e o horário duplicado passava. Troquei os dois por `.equals()` e o teste do horário ocupado ficou verde.

### 4. Sobrescrita vs sobrecarga (Aula 7)
Sobrescrita (override) é reescrever o mesmo método da classe mãe, com o mesmo nome e os mesmos parâmetros. Sobrecarga (overload) é criar outro método com o mesmo nome e parâmetros diferentes. A `Tosa` declarou `getDuracaoMinutos(String porte)`, que é um método novo, então `getDuracaoMinutos()` continuou devolvendo os 30 da classe `Atendimento`. Compilava sem erro porque isso é permitido. Com `@Override` o compilador reclamaria que o método não sobrescreve nada. Corrigi tirando o parâmetro e colocando o `@Override`.

### 5. Singleton manual vs bean do Spring (Aula 14)
O Singleton garante uma única instância na aplicação toda, assim existe só um contador de protocolos. O bug era que o `getInstancia()` fazia `return new GeradorProtocolo()` e nunca guardava em `instancia`, então cada chamada criava um gerador novo e o protocolo era sempre 1. Corrigi guardando a instância e deixei os métodos `synchronized`, já que o comentário dizia que a classe era thread-safe. O `AgendaService` com `@Service` não corre esse risco porque o Spring cria o bean uma vez só e entrega o mesmo objeto para todo mundo (escopo singleton), sem a gente escrever um `getInstancia()`.

### 6. Cobertura de testes: onde parar? (Aula 15)
Dos 6 testes novos, 4 ficaram vermelhos (preço do banho, duração da tosa, cancelar concluído e data no passado) e 2 ficaram verdes (preço fixo da consulta e concluir cancelado). Vale manter os verdes, porque eles protegem regras do contrato que alguém pode quebrar depois, num refactor por exemplo. Num projeto real com prazo, eu priorizaria o caminho feliz e os caminhos de erro das regras de negócio, que foi onde os bugs estavam, antes de correr atrás de 100% de cobertura. Cobertura alta com teste fraco não prova nada.

---

## Parte 5 — Espaço livre (opcional)

```
Os bugs foram numerados na ordem em que foram encontrados: bug01 a bug07 vieram dos
testes vermelhos da suíte entregue; bug08 a bug11 apareceram ao escrever os testes
que faltavam (teste01 a teste04); bug12 foi achado lendo o código. Nenhum dos 20
testes entregues foi alterado (só foram adicionados testes novos).
```
