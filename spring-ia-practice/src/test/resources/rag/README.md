# Casos de avaliação da recuperação

O arquivo `procedure-retrieval-cases.jsonl` contém consultas para avaliar a recuperação dos documentos de procedimentos.

Cada linha é um objeto JSON independente:

- `MATCH`: existe um único procedimento esperado;
- `AMBIGUOUS`: mais de um procedimento é plausível e o agente deve pedir esclarecimento;
- `NO_MATCH`: a base atual não contém um procedimento correspondente.

Os casos de avaliação não devem ser carregados no vector store. Para comparar modelos, mantenha constantes o corpus, a divisão dos documentos, a distância, os filtros e o valor de `topK`.
