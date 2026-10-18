# CareFlow AI

Aplicação RAG de estudo com Spring AI para uma clínica fictícia. Ela recebe perguntas sobre procedimentos, recupera documentos no PGVector e usa o Ollama para responder com base nesse contexto.

## Papel no fluxo de avaliação

Este é o projeto principal. Ele fornece a API e o RAG que são avaliados pelo projeto irmão `spring-ia-practice-evaluations`.

```text
Ollama + PostgreSQL/PGVector
            │
            ▼
spring-ia-practice ── POST /chat ──► spring-ia-practice-evaluations
       aplicação sob avaliação             suíte de avaliações
```

O projeto `spring-ia-practice-evaluations` depende desta aplicação estar em execução, com o corpus de procedimentos já ingerido. Este projeto não depende do projeto de avaliações para funcionar normalmente.

## Componentes

- Java 25 com GraalVM Community Edition;
- Spring Boot 4.1 e Spring AI 2.0.1;
- Ollama para chat e embeddings locais;
- PostgreSQL 18 com PGVector para documentos e embeddings;
- Spring Data JDBC para dados relacionais e memória de conversa;
- Springdoc para OpenAPI e Swagger UI;
- Docker Compose para o PostgreSQL.

## Pré-requisitos

- Docker com Docker Compose;
- Ollama instalado e em execução;
- Java definido em `.tool-versions`.

```text
java graalvm-community-25.0.2
```

Com `asdf`, instale a versão configurada com:

```bash
asdf install
```

Baixe os modelos padrão antes de iniciar a aplicação:

```bash
ollama pull gemma4
ollama pull jeffh/intfloat-multilingual-e5-large-instruct:q8_0
```

## Início rápido

No diretório deste projeto:

```bash
# 1. Inicie o PostgreSQL com PGVector.
docker compose up -d

# 2. Inicie a API em outra sessão de terminal.
./gradlew bootRun

# 3. Depois que a API iniciar, carregue os procedimentos no vector store.
curl --request POST http://localhost:8080/ingestion/procedures
```

O endpoint de ingestão lê os arquivos em `src/main/resources/knowledge/procedures`, gera embeddings e grava os documentos no PGVector. Execute-o antes de testar o RAG ou rodar o projeto de avaliações.

O suporte do Spring Boot ao Docker Compose também pode iniciar o banco ao executar `bootRun`.

## Uso da API

### Conversa normal

```bash
curl --request POST http://localhost:8080/chat \
  --header 'Content-Type: application/json' \
  --data '{"message":"Como devo me preparar para uma endoscopia?"}'
```

Sem cabeçalho de avaliação, `POST /chat` devolve texto puro.

### Resposta para avaliação

O projeto `spring-ia-practice-evaluations` usa o cabeçalho `X-Evaluation-Code` para correlacionar cada caso. Com esse cabeçalho, a mesma API devolve JSON contendo a resposta do agente e os documentos recuperados:

```bash
curl --request POST http://localhost:8080/chat \
  --header 'Content-Type: application/json' \
  --header 'X-Evaluation-Code: Q001' \
  --data '{"message":"Como devo me preparar para uma endoscopia?"}'
```

| Campo | Descrição |
| --- | --- |
| `evaluationCode` | Código recebido no header, como `Q001`. |
| `answer` | Resposta gerada pelo agente. |
| `retrievedDocuments` | Documentos ou chunks usados pelo RAG, com `id`, `text` e `metadata`. |

Documentação da API:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

## Configuração

| Variável | Valor padrão | Descrição |
| --- | --- | --- |
| `POSTGRES_URL` | `jdbc:postgresql://localhost:5432/careflow` | URL JDBC do PostgreSQL. |
| `POSTGRES_USER` | `careflow` | Usuário do banco. |
| `POSTGRES_PASSWORD` | `careflow` | Senha do banco. |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Endereço do Ollama. |
| `OLLAMA_CHAT_MODEL` | `gemma4` | Modelo das conversas. |
| `OLLAMA_EMBEDDING_MODEL` | `jeffh/intfloat-multilingual-e5-large-instruct:q8_0` | Modelo de embeddings. |

O RAG recupera até três documentos com limiar de similaridade `0.7`. Para comparar resultados entre execuções de avaliação, mantenha fixos o corpus, o modelo de embeddings, esses parâmetros e o modelo de chat.

## Dados persistidos

O PostgreSQL armazena:

- documentos e embeddings do PGVector;
- memória das conversas do Spring AI;
- dados relacionais acessados com Spring Data JDBC.

O volume `careflow-postgres-data` preserva esses dados entre reinicializações. Para remover containers e apagar o banco local:

```bash
docker compose down --volumes
```

Na próxima inicialização, crie o banco novamente e repita a ingestão.

## Projeto de avaliações

Depois de concluir o início rápido, abra `spring-ia-practice-evaluations` em outro terminal e execute uma das suítes:

```bash
./gradlew evaluateRag
./gradlew evaluateRagDetailed
```

Consulte o README desse projeto para os critérios, configuração do modelo avaliador e dashboards HTML gerados.

## Corpus e casos de avaliação

O corpus contém 20 procedimentos em `src/main/resources/knowledge/procedures`. Os 60 casos de avaliação ficam em `src/test/resources/rag/procedure-retrieval-cases.jsonl` e são copiados para o projeto `spring-ia-practice-evaluations`.

Os casos de avaliação não devem ser ingeridos no vector store. Ao alterar o corpus ou os casos, mantenha os dois projetos sincronizados.

## Referências sobre embeddings

- [Best Embedding Models in 2026 — Cognee](https://www.cognee.ai/best-embedding-models)
- [MTEB Leaderboard](https://leaderboard.mteb.org/)

Os rankings ajudam a selecionar candidatos. A escolha do modelo deve ser validada com este corpus, perguntas reais, qualidade de recuperação, latência, memória e tamanho dos vetores.
