# CareFlow RAG Evaluations

Suíte de avaliações ao vivo para medir a recuperação e a qualidade das respostas do RAG do projeto `spring-ia-practice`.

## Dependência entre os projetos

Este projeto não contém uma API RAG própria. Ele depende do projeto irmão `spring-ia-practice`, que fornece:

- o endpoint `POST /chat`;
- o RAG configurado com Ollama e PGVector;
- os documentos de procedimentos já ingeridos no vector store;
- a resposta especial de avaliação quando recebe `X-Evaluation-Code`.

```text
spring-ia-practice
  ├── PostgreSQL/PGVector com corpus ingerido
  ├── Ollama para resposta e embeddings
  └── POST /chat + X-Evaluation-Code
                 │
                 ▼
spring-ia-practice-evaluations
  ├── verifica os chunks recuperados
  ├── avalia a resposta com Ollama
  └── gera logs JUnit e dashboards HTML
```

Inicie e prepare o projeto principal antes de executar qualquer task deste repositório. A suíte não inicia a API principal nem executa a ingestão automaticamente.

## Pré-requisitos

- Java 25;
- Ollama em execução;
- projeto `spring-ia-practice` disponível e acessível pela rede;
- PostgreSQL/PGVector do projeto principal em execução;
- corpus de procedimentos ingerido pelo projeto principal.

Por padrão, a API avaliada deve estar em `http://localhost:8080` e o Ollama em `http://localhost:11434`.

## Preparar o ambiente

### 1. Iniciar a aplicação sob avaliação

No diretório `spring-ia-practice`:

```bash
docker compose up -d
./gradlew bootRun
```

Depois que a API estiver disponível, carregue o corpus no PGVector:

```bash
curl --request POST http://localhost:8080/ingestion/procedures
```

Esse passo deve ocorrer antes da primeira avaliação e sempre que o banco for recriado.

### 2. Executar as avaliações

No diretório deste projeto:

```bash
# Avaliação padrão do Spring AI.
./gradlew evaluateRag

# Avaliação com decisão e justificativa obrigatórias.
./gradlew evaluateRagDetailed
```

`./gradlew test` não chama a API principal nem o Ollama; ele exclui as duas suítes de avaliação ao vivo.

## Contrato com a API principal

Para cada caso, a suíte envia uma requisição como esta:

```http
POST /chat
Content-Type: application/json
X-Evaluation-Code: Q001
```

O projeto principal deve devolver JSON com os campos abaixo:

| Campo | Uso na avaliação |
| --- | --- |
| `evaluationCode` | Correlaciona a resposta com o caso enviado. |
| `answer` | Conteúdo avaliado quanto à relevância. |
| `retrievedDocuments` | Chunks usados para validar a recuperação e contextualizar o evaluator. |

Cada documento recuperado deve incluir `id`, `text` e `metadata`. O metadado `source` identifica o arquivo do procedimento esperado.

## Suítes disponíveis

| Task | Avaliador | Critério da resposta | Dashboard |
| --- | --- | --- | --- |
| `evaluateRag` | `RelevancyEvaluator` padrão do Spring AI | Score binário. Na versão 2.0.1, o feedback retornado pelo evaluator padrão fica vazio. | `build/reports/rag-evaluation/index.html` |
| `evaluateRagDetailed` | `DetailedRelevancyEvaluator` do projeto | Exige JSON com decisão `YES` ou `NO` e feedback não vazio. O teste falha se a resposta do evaluator estiver fora desse contrato. | `build/reports/rag-evaluation-detailed/index.html` |

As duas suítes registram score e feedback no stdout do JUnit e no dashboard correspondente. Para recriar um dashboard sem chamar a API nem o Ollama:

```bash
./gradlew generateEvaluationDashboard
./gradlew generateDetailedEvaluationDashboard
```

## Executar casos específicos

Informe IDs separados por vírgula para investigar uma falha:

```bash
./gradlew -PevaluationCaseIds=Q005 evaluateRag
./gradlew -PevaluationCaseIds=Q005,Q006 evaluateRagDetailed
```

Também é possível usar `EVALUATION_CASE_IDS`.

## Configuração

| Variável | Valor padrão | Descrição |
| --- | --- | --- |
| `EVALUATION_BASE_URL` | `http://localhost:8080` | URL do `spring-ia-practice` sob avaliação. |
| `EVALUATION_CASE_IDS` | vazio | IDs separados por vírgula para filtrar os casos. |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | URL do Ollama usado pelo evaluator. |
| `OLLAMA_EVALUATOR_MODEL` | `gemma4` | Modelo usado para julgar as respostas. |

Exemplo com endpoints e modelo alternativos:

```bash
EVALUATION_BASE_URL=http://localhost:8080 \
OLLAMA_BASE_URL=http://localhost:11434 \
OLLAMA_EVALUATOR_MODEL=gemma4 \
./gradlew evaluateRagDetailed
```

## O que é medido

- Casos `MATCH`: o arquivo do procedimento esperado deve aparecer entre os documentos recuperados.
- Casos `AMBIGUOUS`: ao menos um procedimento plausível deve aparecer entre os documentos recuperados.
- Casos `NO_MATCH`: ajudam a acompanhar abstenções e falsos positivos; exigem análise da resposta e do resultado da suíte.
- A relevância da resposta é julgada usando a pergunta, os chunks recuperados e a resposta produzida pela aplicação principal.

O julgamento por modelo pode variar. Para comparar versões do RAG, mantenha fixos o corpus, a divisão dos documentos, o modelo de embeddings, `topK`, limiar de similaridade, prompt e modelo avaliador.

## Casos de avaliação e sincronização

Os casos ficam em `src/test/resources/rag/procedure-retrieval-cases.jsonl`. Eles são uma cópia de `spring-ia-practice/src/test/resources/rag/procedure-retrieval-cases.jsonl` e não devem ser ingeridos no vector store.

Ao alterar os casos no projeto principal, copie a alteração para este projeto. Os IDs `PROC-001` a `PROC-020` devem continuar alinhados aos prefixos `01-` a `20-` dos arquivos de procedimentos.
