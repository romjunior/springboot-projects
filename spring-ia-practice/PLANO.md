# CareFlow AI — Assistente de Consultas e Procedimentos

## 1. Visão do projeto

O CareFlow AI será um agente conversacional para uma clínica fictícia. Ele acompanhará a jornada administrativa do paciente, desde a busca por uma consulta, exame ou procedimento até a confirmação do agendamento.

O agente usará a base de conhecimento oficial da clínica para explicar preparos e políticas. Para consultar ou alterar dados da agenda, usará ferramentas Java disponibilizadas com Spring AI.

Este projeto servirá como aplicação prática dos seguintes conceitos:

- modelos de chat, roles e prompt templates;
- `ChatClient` e respostas com streaming;
- structured output;
- advisors;
- chat memory;
- embeddings e RAG;
- semantic cache;
- tool calling;
- MCP em uma etapa posterior.

## 2. Objetivo

Permitir que um paciente converse em linguagem natural para:

- conhecer consultas, exames e procedimentos oferecidos;
- receber orientações oficiais de preparo;
- consultar unidades, profissionais e horários disponíveis;
- agendar uma consulta ou procedimento;
- consultar, remarcar ou cancelar um agendamento;
- receber um protocolo e um checklist ao final do atendimento.

## 3. Limites de atuação

O agente atenderá questões administrativas e fornecerá somente orientações presentes nos documentos aprovados da clínica.

Solicitações que envolvam diagnóstico, interpretação de exames, prescrição, alteração de medicamentos, indicação de tratamento ou avaliação de urgência deverão ser encaminhadas para um profissional de saúde ou para o canal de emergência apropriado.

O agente não deverá inventar informações ausentes da base de conhecimento. Quando não encontrar uma resposta confiável, deverá informar a limitação e oferecer encaminhamento para atendimento humano.

## 4. Principais jornadas

### 4.1 Conhecer um procedimento

Exemplo:

> Como funciona a endoscopia e qual é o preparo?

Fluxo:

1. Identificar o procedimento mencionado.
2. Recuperar os documentos relevantes por meio de RAG.
3. Responder usando somente o conteúdo recuperado.
4. Informar documentos, preparo, duração e orientações da clínica.
5. Oferecer a busca por horários disponíveis.

### 4.2 Encontrar um horário

Exemplo:

> Quero uma consulta com dermatologista na próxima semana, à tarde.

Fluxo:

1. Extrair especialidade, intervalo de datas e período preferido.
2. Perguntar somente os dados obrigatórios que estiverem ausentes.
3. Chamar a ferramenta de consulta de horários.
4. Apresentar algumas opções válidas.
5. Aguardar a escolha do paciente.

### 4.3 Realizar um agendamento

Fluxo:

1. Receber a escolha de horário.
2. Coletar os dados obrigatórios do paciente.
3. Apresentar um resumo do agendamento.
4. Solicitar confirmação explícita.
5. Chamar a ferramenta de agendamento.
6. Retornar protocolo, data, local e checklist.

### 4.4 Consultar, remarcar ou cancelar

Fluxo:

1. Localizar os agendamentos do paciente.
2. Identificar qual agendamento será alterado.
3. Consultar novos horários quando necessário.
4. Apresentar o resumo da alteração.
5. Solicitar confirmação explícita.
6. Executar a operação escolhida.
7. Retornar o novo estado do agendamento.

## 5. Escopo do MVP

O primeiro MVP terá:

- clínica, unidades e dados inteiramente fictícios;
- cinco procedimentos ou especialidades cadastrados;
- documentos Markdown usados como base RAG;
- chat com memória separada por conversa;
- consulta de horários disponíveis;
- criação de agendamento após confirmação;
- persistência de pacientes, horários e agendamentos;
- protocolo e checklist retornados como objetos estruturados;
- logs das tools chamadas e do resultado de cada operação.

Sugestão de catálogo inicial:

- consulta com clínico geral;
- consulta com dermatologista;
- endoscopia;
- ultrassom abdominal;
- ressonância magnética.

## 6. Funcionalidades posteriores

- remarcação e cancelamento;
- streaming das respostas;
- autenticação e identificação do paciente;
- integração simulada com convênios;
- lembretes de consulta;
- semantic cache para perguntas frequentes;
- painel administrativo;
- servidor ou cliente MCP para integração com outros sistemas;
- observabilidade de tokens, latência e chamadas de ferramentas.

## 7. Arquitetura inicial

```text
Cliente HTTP
    |
    v
ChatController
    |
    v
PatientAssistantService
    |
    v
ChatClient
    |-- System Prompt
    |-- Chat Memory Advisor
    |-- RAG Advisor
    |-- Logging/Audit Advisor
    |
    +--> LLM
    |
    +--> AppointmentTools
              |
              v
       Serviços de domínio
              |
              v
          PostgreSQL

Documentos Markdown
    |
    v
ETL / Chunking / Embeddings
    |
    v
Vector Store
```

### Pacotes sugeridos

```text
com.estudo.spring_ia_practice
├── config
├── controller
├── agent
├── advisor
├── tool
├── rag
├── domain
│   ├── patient
│   ├── procedure
│   └── appointment
├── repository
├── service
└── exception
```

## 8. Modelo de domínio

### Patient

- `id`
- `name`
- `document`
- `birthDate`
- `phone`
- `email`

### ClinicUnit

- `id`
- `name`
- `address`
- `active`

### MedicalService

Representa uma consulta, exame ou procedimento.

- `id`
- `name`
- `type`
- `specialty`
- `durationMinutes`
- `description`
- `active`

### Professional

- `id`
- `name`
- `specialty`
- `registrationNumber`
- `active`

### AvailableSlot

- `id`
- `medicalServiceId`
- `professionalId`
- `clinicUnitId`
- `startAt`
- `endAt`
- `status`

### Appointment

- `id`
- `protocol`
- `patientId`
- `availableSlotId`
- `status`
- `createdAt`
- `updatedAt`

Status iniciais:

- `SCHEDULED`
- `RESCHEDULED`
- `CANCELLED`
- `COMPLETED`

## 9. Tools do agente

As descrições de `@Tool` e `@ToolParam` deverão informar claramente quando cada operação deve ser usada.

```java
@Tool(description = "Pesquisa consultas, exames e procedimentos oferecidos pela clínica")
List<MedicalServiceSummary> searchMedicalServices(
        @ToolParam(description = "Descrição ou nome informado pelo paciente")
        String query);

@Tool(description = "Consulta horários disponíveis para um serviço em uma unidade e intervalo de datas")
List<AvailableSlotResponse> findAvailableSlots(
        FindAvailableSlotsRequest request);

@Tool(description = "Cria um agendamento depois que o paciente confirmou todos os dados")
AppointmentConfirmation scheduleAppointment(
        ScheduleAppointmentRequest request);

@Tool(description = "Localiza os próximos agendamentos de um paciente")
List<AppointmentSummary> findPatientAppointments(
        String patientId);

@Tool(description = "Remarca um agendamento após confirmação explícita do paciente")
AppointmentConfirmation rescheduleAppointment(
        RescheduleAppointmentRequest request);

@Tool(description = "Cancela um agendamento após confirmação explícita do paciente")
CancellationResult cancelAppointment(
        CancelAppointmentRequest request);
```

Regras para operações que alteram dados:

- reunir todos os campos obrigatórios antes da chamada;
- apresentar um resumo da ação;
- solicitar confirmação explícita;
- executar a tool somente depois da confirmação;
- devolver protocolo e estado final;
- garantir idempotência quando possível.

## 10. Structured output

Objetos iniciais:

```java
public record ProcedureChecklist(
        String procedureName,
        List<String> preparationSteps,
        List<String> requiredDocuments,
        boolean companionRequired,
        List<String> clinicWarnings
) {}
```

```java
public record AppointmentConfirmation(
        String protocol,
        String service,
        String professional,
        LocalDateTime dateTime,
        String clinicUnit,
        String address,
        ProcedureChecklist checklist
) {}
```

```java
public record FindAvailableSlotsRequest(
        String medicalServiceId,
        String clinicUnitId,
        LocalDate startDate,
        LocalDate endDate,
        PeriodPreference period
) {}
```

## 11. Base RAG

Estrutura inicial de documentos:

```text
src/main/resources/knowledge/
├── procedures/
│   ├── endoscopia.md
│   ├── ressonancia-magnetica.md
│   └── ultrassom-abdominal.md
├── specialties/
│   ├── clinica-geral.md
│   └── dermatologia.md
└── policies/
    ├── documentos-necessarios.md
    ├── agendamento.md
    ├── cancelamento.md
    └── atendimento-humano.md
```

Metadados recomendados para cada fragmento:

- `documentType`;
- `serviceId`;
- `serviceName`;
- `clinicUnitId`, quando aplicável;
- `version`;
- `effectiveDate`;
- `source`.

O processo de ingestão deverá:

1. carregar os documentos;
2. separar o conteúdo em fragmentos adequados;
3. adicionar metadados;
4. gerar embeddings;
5. armazenar os fragmentos no vector store;
6. permitir a reindexação dos documentos.

## 12. Memória de conversa

Cada conversa terá um `conversationId` próprio. A memória será usada para manter informações como:

- procedimento procurado;
- unidade escolhida;
- intervalo de datas;
- preferência de período;
- horário selecionado;
- etapa atual do fluxo.

Dados permanentes do paciente e agendamentos continuarão no banco de dados. A memória conversacional servirá apenas como contexto de interação.

## 13. Prompt de sistema

O prompt deverá estabelecer que o agente:

- representa uma clínica fictícia;
- usa os documentos recuperados para orientações sobre procedimentos;
- usa tools para consultar ou modificar dados operacionais;
- faz perguntas curtas para obter campos obrigatórios ausentes;
- nunca inventa horários, protocolos, profissionais ou unidades;
- solicita confirmação antes de agendar, remarcar ou cancelar;
- diferencia orientação administrativa de avaliação clínica;
- encaminha questões clínicas para um profissional;
- comunica falhas das tools de forma clara;
- não expõe stack traces ou detalhes internos.

## 14. Endpoints iniciais

### Conversar com o agente

```http
POST /api/v1/conversations/{conversationId}/messages
Content-Type: application/json

{
  "message": "Quero uma consulta com dermatologista na próxima semana"
}
```

### Criar uma conversa

```http
POST /api/v1/conversations
```

Resposta:

```json
{
  "conversationId": "uuid"
}
```

### Limpar a memória

```http
DELETE /api/v1/conversations/{conversationId}/memory
```

## 15. Persistência e infraestrutura

Sugestão inicial:

- PostgreSQL para dados relacionais;
- PGvector para documentos e embeddings;
- Docker Compose para o ambiente local;
- Flyway para versionamento do schema;
- JDBC ou Spring Data JPA para persistência;
- dados fictícios carregados durante o desenvolvimento.

Redis poderá ser adicionado posteriormente para semantic cache.

## 16. Semantic cache

O cache semântico será aplicado apenas a respostas gerais e relativamente estáveis, como:

- documentos necessários;
- política de cancelamento;
- descrição de um procedimento;
- endereço e horário de uma unidade.

Respostas personalizadas, horários disponíveis, protocolos e estados de agendamento não deverão compartilhar cache semântico entre pacientes.

O threshold, TTL e escopo das chaves deverão ser configuráveis e avaliados com um conjunto de perguntas semelhantes e diferentes para detectar falsos positivos.

## 17. Tratamento de erros

Casos esperados:

- serviço ou especialidade inexistente;
- período sem horários disponíveis;
- slot escolhido por outro paciente;
- paciente não localizado;
- agendamento já cancelado;
- falha temporária do modelo;
- falha do banco ou vector store;
- resposta ausente na base de conhecimento.

As tools deverão retornar erros de domínio compreensíveis. O agente poderá transformar esses resultados em mensagens amigáveis e sugerir uma ação válida.

## 18. Segurança e privacidade

- usar somente pacientes e informações fictícias durante o desenvolvimento;
- evitar registrar documentos, contatos e conteúdo sensível em logs;
- validar identidade antes de consultar ou alterar agendamentos em uma evolução do projeto;
- limitar os dados pessoais enviados ao modelo;
- manter trilha de auditoria das operações de escrita;
- proteger endpoints administrativos;
- configurar segredos por variáveis de ambiente.

## 19. Fases de implementação

### Fase 1 — Fundação

- configurar Spring AI e o provedor de modelo;
- configurar PostgreSQL e migrations;
- criar entidades e repositórios;
- cadastrar unidades, serviços, profissionais e slots fictícios;
- criar operações tradicionais de agenda sem IA.

### Fase 2 — Agente e tools

- configurar o `ChatClient`;
- criar o prompt de sistema;
- implementar busca de serviços e horários;
- implementar agendamento;
- expor os serviços como tools;
- criar o endpoint de conversa;
- confirmar que o modelo escolhe tools e argumentos corretamente.

### Fase 3 — Memória e structured output

- adicionar memória por `conversationId`;
- manter contexto durante a escolha de serviço e horário;
- retornar confirmação e checklist estruturados;
- adicionar tratamento dos erros de domínio.

### Fase 4 — RAG

- escrever os documentos fictícios da clínica;
- configurar embeddings e vector store;
- implementar ingestão e reindexação;
- configurar o advisor de recuperação;
- garantir respostas fundamentadas nos documentos.

### Fase 5 — Evoluções

- implementar remarcação e cancelamento;
- adicionar streaming;
- adicionar semantic cache;
- adicionar métricas e observabilidade;
- explorar integração via MCP.

## 20. Cenários de demonstração

### Cenário A — Pergunta sobre procedimento

```text
Paciente: Como devo me preparar para uma endoscopia?
```

Resultado esperado:

- recuperar o documento correto;
- apresentar checklist baseado na fonte;
- oferecer busca por horários.

### Cenário B — Agendamento completo

```text
Paciente: Quero marcar dermatologista na próxima semana à tarde.
```

Resultado esperado:

- localizar o serviço;
- consultar slots;
- apresentar opções;
- receber uma escolha;
- confirmar os dados;
- criar o agendamento;
- retornar protocolo.

### Cenário C — Informação ausente

```text
Paciente: Marque uma ressonância para mim.
```

Resultado esperado:

- identificar os campos que faltam;
- perguntar unidade, data ou período;
- continuar o mesmo fluxo usando a memória.

### Cenário D — Limite clínico

```text
Paciente: Esse sintoma significa que eu preciso de uma ressonância?
```

Resultado esperado:

- explicar que a indicação exige avaliação profissional;
- oferecer o agendamento de uma consulta adequada ou atendimento humano;
- não diagnosticar nem recomendar o procedimento.

## 21. Critérios de conclusão do MVP

O MVP estará concluído quando:

- o usuário conseguir iniciar uma conversa;
- o agente responder dúvidas usando a base RAG;
- o contexto for preservado entre mensagens da mesma conversa;
- o agente consultar horários exclusivamente por tool;
- o agente solicitar os dados obrigatórios ausentes;
- o agente exigir confirmação antes do agendamento;
- o agendamento for persistido de forma consistente;
- horários ocupados deixarem de aparecer como disponíveis;
- o paciente receber protocolo e checklist;
- questões clínicas forem encaminhadas de acordo com os limites definidos;
- logs permitirem identificar a tool chamada sem expor dados sensíveis.

## 22. Primeira entrega recomendada

A primeira entrega vertical deve implementar apenas este caminho:

```text
Pedido em linguagem natural
    -> identificar dermatologia
    -> consultar slots fictícios no PostgreSQL
    -> apresentar opções
    -> receber a escolha
    -> solicitar confirmação
    -> persistir o agendamento
    -> retornar protocolo
```

Depois que esse fluxo estiver funcionando, RAG, novos procedimentos e demais operações poderão ser adicionados sem alterar o objetivo central do projeto.
