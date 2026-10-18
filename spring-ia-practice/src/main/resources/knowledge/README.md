# Base de conhecimento sintética da CareFlow

Esta pasta contém documentos fictícios criados exclusivamente para desenvolvimento e avaliação do fluxo RAG. Os textos não devem ser usados como orientação médica real.

## Organização

- `procedures/`: um documento Markdown por procedimento;
- cada documento possui metadados em YAML front matter;
- procedimentos parecidos foram incluídos de propósito para avaliar a precisão da recuperação.

Os casos de avaliação ficam em `src/test/resources/rag` e não devem ser ingeridos no vector store.

## Estratégia inicial de ingestão

Cada arquivo foi mantido curto para poder ser indexado como um único documento. Ao carregar os arquivos, preserve pelo menos os metadados `serviceId`, `serviceName`, `procedureGroup`, `version`, `effectiveDate` e `source`.

Ao trocar o modelo de embeddings, recrie a tabela vetorial e gere novamente todos os vetores.
