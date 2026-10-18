# AGENTS.md - Intro Agent Project Guide

## Architecture Overview
This is a Spring Boot 4.0.3 application integrating Spring AI 2.0.0-M2 with Ollama for conversational AI. The core architecture consists of:
- **IntroAgentApplication**: Standard Spring Boot main class
- **ChatController**: Single REST endpoint `/chat` handling user messages via GET requests
- **Configuration**: Ollama model settings in `application.yaml` (model: qwen3.5, num-ctx: 8000, temperature: 0.2)

Data flow: User message → ChatController → ChatClient (with SimpleLoggerAdvisor) → Ollama API → Pirate-themed response in Brazilian Portuguese.

## Key Patterns
- **Package Naming**: Use underscores instead of hyphens (e.g., `com.estudo.intro_agent` not `intro-agent`) due to Java package restrictions.
- **ChatClient Setup**: Inject `ChatClient.Builder` in constructor, configure with advisors like `new SimpleLoggerAdvisor()`.
- **System Prompts**: Define persona in `.system()` calls, e.g., "Você é um pirata do Caribe, fale como um pirata e responda as perguntas do usuário. Responda em português do Brasil."
- **Response Handling**: Use `ChatResponse` with assertions for non-null checks, log metadata and output text.
- **Configuration**: Model options (top-k, top-p, temperature) tuned in `application.yaml` under `spring.ai.ollama.chat.options`.

## Developer Workflows
- **Build**: `./gradlew build` - compiles and packages the app.
- **Run**: `./gradlew bootRun` - starts the server (requires Ollama daemon running locally).
- **Test**: `./gradlew test` - runs JUnit 5 tests, including context load verification.
- **Debug**: Enable logging in `application.yaml` (e.g., `logging.level.org.springframework.ai: DEBUG`) to trace ChatClient interactions.

## Dependencies & Integrations
- **Spring AI Ollama**: For local LLM inference; ensure Ollama is installed and `qwen3.5` model pulled (`ollama pull qwen3.5`).
- **External Requirements**: Ollama service must be running on default port; app assumes local setup.
- **Java Version**: Requires Java 21 toolchain as specified in `build.gradle`.

## Conventions
- **Logging**: Use SLF4J Logger with class name, log response metadata and text in controllers.
- **Assertions**: Employ `assert` statements for runtime checks on AI responses (e.g., `assert response != null;`).
- **Endpoint Design**: Simple GET with `@RequestParam` for messages, defaulting to "Olá, tudo bem?".
- **Test Structure**: Minimal `@SpringBootTest` for context loading, no integration tests for AI endpoints.

Reference files: `ChatController.java` (controller logic), `application.yaml` (config), `build.gradle` (deps).</content>
<parameter name="filePath">/media/junior/Backup/Projetos2/Java/IA/spring-ia/intro-agent/AGENTS.md
