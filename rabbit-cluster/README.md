# RabbitMQ Cluster com Spring Boot - Alta Disponibilidade

Este projeto demonstra a implementação de alta disponibilidade usando **Spring Boot** com **RabbitMQ em cluster**, garantindo que a aplicação continue funcionando mesmo se um dos nós do RabbitMQ falhar.

## 🎯 Objetivo

Testar e demonstrar a **alta disponibilidade** de uma aplicação Spring Boot integrada com RabbitMQ em cluster, onde:
- Se um nó do RabbitMQ falhar, a aplicação continua funcionando
- As mensagens são replicadas entre todos os nós (HA Mirroring)
- A aplicação se reconecta automaticamente aos nós disponíveis

## 🛠️ Stack Tecnológica

- **Java 21** com Virtual Threads
- **Spring Boot 3.5.3**
- **Spring AMQP** para integração com RabbitMQ
- **RabbitMQ 3** com Management Plugin
- **Docker & Docker Compose** para orquestração do cluster
- **Micrometer Tracing** para observabilidade

## 🐰 Configuração do RabbitMQ Cluster

### Docker Compose

O cluster RabbitMQ é configurado com **3 nós** usando Docker Compose:

```yaml
# 3 nós RabbitMQ em cluster
- rabbitmq1: Porta 5672 (AMQP) e 15672 (Management)
- rabbitmq2: Porta 5673 (AMQP) 
- rabbitmq3: Porta 5674 (AMQP)
```

### Script de Inicialização do Cluster

O arquivo `cluster-entrypoint.sh` configura automaticamente:

1. **Permissões do Erlang Cookie** - Sincronização entre nós
2. **Join do Cluster** - Nós 2 e 3 se conectam ao nó 1
3. **HA Mirroring Policy** - Replicação automática de filas

```bash
# Política de HA configurada automaticamente
rabbitmqctl set_policy ha-all ".*" '{"ha-mode":"all","ha-sync-mode":"automatic"}'
```

### HA Mirroring - Por que é Essencial

O **HA Mirroring** é **obrigatório** para alta disponibilidade porque:

- **Replica filas** em todos os nós do cluster
- **Sincronização automática** de mensagens
- **Failover transparente** - se um nó falha, outro assume
- **Sem perda de mensagens** durante falhas

## ⚙️ Configuração do Spring Boot

### Conexão Multi-Nó

```yaml
spring:
  rabbitmq:
    addresses: localhost:5673,localhost:5674  # Conecta nos nós 2 e 3
    connection-timeout: 30000
    requested-heartbeat: 30
    publisher-confirms: true
    publisher-returns: true
```

### Por que essa Configuração?

1. **Múltiplos Endereços**: Se um nó falhar, Spring Boot tenta o próximo
2. **Connection Timeout**: Detecta falhas rapidamente (30s)
3. **Heartbeat**: Monitora conexões ativas a cada 30s
4. **Publisher Confirms**: Garante entrega das mensagens
5. **Publisher Returns**: Detecta mensagens não roteadas

### Configuração de Resilência

```java
@Bean
public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    final var rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMandatory(true);  // Falha se mensagem não for roteada
    rabbitTemplate.setObservationEnabled(true);  // Observabilidade
    return rabbitTemplate;
}
```

## 🚀 Como Executar

### 1. Subir o Cluster RabbitMQ

```bash
cd rabbitmq-cluster-docker
docker-compose up -d
```

### 2. Verificar o Cluster

Acesse o Management UI:
- **Nó 1**: http://localhost:15672

Credenciais padrão: `guest/guest`

### 3. Executar a Aplicação Spring Boot

```bash
./gradlew bootRun
```

### 4. Testar Alta Disponibilidade

1. **Enviar mensagens**: `GET /cluster?message={text}`
2. **Parar um nó**: `docker-compose stop rabbitmq2`
3. **Verificar**: Aplicação continua funcionando
4. **Restartar nó**: `docker-compose start rabbitmq2`

## 📊 Endpoints da Aplicação

- `GET /cluster?message={text}` - Enviar evento para a fila
- `GET /actuator/health` - Status da aplicação
- `GET /actuator/metrics` - Métricas da aplicação

## 🔍 Testando Falhas

### Cenário 1: Falha de um Nó
```bash
# Parar nó 2
docker-compose stop rabbitmq2

# Aplicação continua funcionando via nó 3
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"message": "Teste de alta disponibilidade"}'
```

### Cenário 2: Recuperação do Nó
```bash
# Restartar nó 2
docker-compose start rabbitmq2

# Nó se reintegra automaticamente ao cluster
```

## 📈 Monitoramento

A aplicação inclui:
- **Spring Boot Actuator** para health checks
- **Micrometer Tracing** para rastreamento distribuído
- **Métricas Prometheus** para monitoramento

## 🔧 Variáveis de Ambiente

```bash
# Configurações RabbitMQ (opcionais)
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
RABBITMQ_DEFAULT_VHOST=/
```

## ✅ Benefícios da Arquitetura

1. **Zero Downtime** - Aplicação não para durante falhas
2. **Recuperação Automática** - Reconexão transparente
3. **Consistência de Dados** - HA Mirroring garante integridade
4. **Escalabilidade** - Fácil adicionar novos nós
5. **Observabilidade** - Monitoramento completo da saúde do sistema

---

**Nota**: O HA Mirroring é essencial para garantir que as mensagens não sejam perdidas durante falhas de nós, proporcionando verdadeira alta disponibilidade para aplicações críticas.

## Links

[Link do repo do rabbiMQ do Docker](https://github.com/serkodev/rabbitmq-cluster-docker)