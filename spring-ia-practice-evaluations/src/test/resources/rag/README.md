# Casos de avaliação da recuperação

O arquivo `procedure-retrieval-cases.jsonl` contém consultas para avaliar a recuperação dos documentos de procedimentos.

Cada linha é um objeto JSON independente:

- `MATCH`: existe um único procedimento esperado;
- `AMBIGUOUS`: mais de um procedimento é plausível e o agente deve pedir esclarecimento;
- `NO_MATCH`: a base atual não contém um procedimento correspondente.

Os casos de avaliação não devem ser carregados no vector store. Para comparar modelos, mantenha constantes o corpus, a divisão dos documentos, a distância, os filtros e o valor de `topK`.

Na suíte do projeto evaluations, `PROC-001` a `PROC-020` correspondem aos prefixos `01-` a `20-` dos nomes dos arquivos em `spring-ia-practice/src/main/resources/knowledge/procedures`.
