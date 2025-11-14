# Agent Clean Architecture

Este projeto implementa um sistema de avaliação automática de projetos utilizando Clean Architecture. Ele integra análise de código, varredura de repositório e uso de um agente baseado em IA para gerar relatórios de revisão.

O backend é desenvolvido com **Quarkus**, utilizando **Java 17**, e segue uma organização modular baseada em domínio, casos de uso e adaptadores.

---

## Tecnologias Utilizadas

### Core
- Java 17
- Quarkus Framework
- Maven

### Arquitetura
- Clean Architecture
- Domain Driven Design (conceitos essenciais)
- Separação em camadas: Domain, Application, Infra

### Infraestrutura
- RESTEasy Reactive
- OpenAPI (Swagger)
- Docker e Docker Compose
- Gateways para integração com serviços externos

### Testes
- JUnit
- Quarkus Test Framework

---

## O que o Sistema Faz

O sistema realiza a avaliação automática de um projeto de software executando estas etapas:

1. Recebe o caminho de um repositório local ou o link de um repositório no Github.
2. Varre o conteúdo do projeto utilizando o `ProjectScanner`.
3. Constrói o prompt de avaliação com o `PromptBuilder`.
4. Envia o conteúdo analisado para um agente de IA configurado (OpenAiAgent).
5. Recebe a análise bruta retornada pela IA.
6. Processa e organiza os dados através do `ReportAggregator`.
7. Retorna um **ReviewReport** completo contendo:
    - Pontuações
    - Comentários técnicos
    - Sugestões de melhorias

Toda a lógica principal é orquestrada pelo **ReviewProjectUseCase**.

---

## Estrutura do Projeto

```
src/
  main/
    java/com/poc/
      application/
        useCase/
          ReviewProjectUseCase.java
      domain/
        entity/
        gateway/
      infra/
        adapter/
          web/
            ReviewController.java
        config/
          OpenApiConfig.java
        util/
          RepoHelper.java
          PromptBuilder.java
          ReportAggregator.java
        agent/
          OpenAiAgent.java
        scanner/
          ProjectScanner.java

    resources/
      application.properties
```

---

## Como Rodar o Projeto

### 1. Executar em Modo Dev

O Quarkus possui suporte a live reload:

```
./mvnw quarkus:dev
```

Dev UI disponível em:

```
http://localhost:8080/q/dev
```

---

### 2. Rodar com Docker

#### Build da imagem

```
docker build -t agent-clean-arch .
```

#### Subir com Docker Compose

Certifique-se de configurar o arquivo `.env` com variáveis necessárias, por exemplo:

```
OPENAI_KEY=sua_chave
```

Então execute:

```
docker compose up -d
```

---

## Endpoints

### Avaliar Projeto

**POST** `/api/review`

Body:

```json
{
  "path": "/caminho/para/seu/projeto"
}
```

Exemplo de resposta:

```json
{
  "score": 85,
  "comments": [],
  "improvements": []
}
```

---

## Configurações

O arquivo `application.properties` contém configurações essenciais da aplicação:

```
quarkus.http.port=8080
agent.model=gpt-4
agent.api-key=${OPENAI_KEY}
```

---

## Como Contribuir

1. Faça um fork do repositório.
2. Crie uma branch:
   ```
   git checkout -b feature/minha-feature
   ```
3. Envie suas melhorias com testes.
4. Abra um Pull Request.

---
