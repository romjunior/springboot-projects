# springboot-projects

Repositório com projetos de estudo, laboratórios e exemplos usando Spring Boot, mensageria e Spring AI.

## Projetos

- [kafka-cluster](./kafka-cluster/) — aplicação Spring Boot com Kafka e PostgreSQL, acompanhada de um ambiente Docker Compose com Kafka, ZooKeeper e Control Center.
- [rabbit-cluster](./rabbit-cluster/) — demonstração de integração Spring Boot com um cluster RabbitMQ de três nós e HAProxy. Consulte o [README do projeto](./rabbit-cluster/README.md) para detalhes.
- [spring-ia-intro](./spring-ia-intro/) — exemplos de Spring AI com Ollama, incluindo chat, streaming, ferramentas, memória de conversa, RAG e cache semântico com Redis.
- [spring-ia-practice](./spring-ia-practice/) — aplicação RAG CareFlow AI para responder perguntas sobre procedimentos, usando Ollama e PostgreSQL com PGVector. Consulte o [README do projeto](./spring-ia-practice/README.md) para configuração e uso.
- [spring-ia-practice-evaluations](./spring-ia-practice-evaluations/) — suítes e dashboards para avaliar recuperação e respostas do RAG. Depende da API e do corpus ingerido em `spring-ia-practice`; consulte o [README do projeto](./spring-ia-practice-evaluations/README.md).
