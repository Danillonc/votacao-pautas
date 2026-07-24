# API de Votação de Pautas Cooperativas

Esta é uma API RESTful projetada para gerenciar pautas, abrir sessões de votação, receber votos de associados e contabilizar resultados. Construída com Spring Boot 4.x/Java 25, possui arquitetura baseada em camadas (Layered Architecture), mensageria (Kafka) e tolerância a falhas (Resilience4j).

## Arquitetura e Decisões de Design

A aplicação segue a Arquitetura em Camadas (Layered Architecture), garantindo que as responsabilidades estejam bem separadas:
- **Controller**: Expõe a API REST e lida apenas com validações de entrada e roteamento.
- **Service**: Concentra todas as regras de negócio complexas.
- **Repository**: Gerencia a persistência usando Spring Data JPA.
- **Messaging**: Abstrai a comunicação assíncrona orientada a eventos.
- **Integration**: Isola chamadas HTTP externas via REST Client.

### Por que Layered Architecture?
Conforme alinhado nos requisitos iniciais, embora Arquitetura Hexagonal seja ideal para domínios extremamente complexos, a complexidade desta aplicação foi avaliada como moderada-baixa (CRUDs simples e contagem atômica). A Arquitetura em Camadas (rigorosamente implementada, sem "bypass" direto do Controller para o Repository) ofereceu o melhor trade-off entre velocidade de desenvolvimento e isolamento, evitando over-engineering sem perder a testabilidade.

### Soluções Técnicas e Boas Práticas

- **Alta Disponibilidade (Mensageria)**: Os votos recebidos via API POST são processados assincronamente. O `VotoController` valida o payload inicial e publica um evento no tópico do **Kafka**. O `VotoConsumer` realiza a inserção efetiva. Isso evita gargalos de I/O em banco de dados e permite que a API receba alta volumetria de votos simultâneos (throughput elevado).
- **Integração e Tolerância a Falhas**: A integração para validação do CPF do associado utiliza `RestTemplate` e está protegida pelo **Resilience4j**. Configuramos um `CircuitBreaker` e `Retry`. Caso o serviço externo fique indisponível (404/503) ou muito lento, a aplicação implementa graceful degradation (fallback), permitindo que o voto prossiga para não bloquear a operação da cooperativa, simulando "permissão condicional".
- **URLs de Callback Customizáveis**: A URL do sistema externo de validação de CPF é injetada via properties e pode ser substituída sem recompilação usando a variável de ambiente `APP_INTEGRATION_USER_INFO_URL`. Facilitando assim testes com emuladores ou dispositivos físicos na mesma rede.
- **Solid e Clean Code**: Código conciso, sem injeção de dependência por campo (`@Autowired`), e sim por construtores garantindo imutabilidade. Lógica validada unitariamente com mais de 30 testes e altíssima cobertura. Tratamento de exceções centralizado no `GlobalExceptionHandler` formatando respostas RFC 7807 (Problem Detail).

---

## Como Rodar Localmente (Docker)

O projeto possui um `docker-compose.yml` raiz que provisiona todas as dependências da infraestrutura (MySQL, Kafka, Zookeeper) e a própria aplicação.

Pré-requisitos: `Docker` e `Docker Compose` instalados.

1. Faça o build da imagem da aplicação:
   ```bash
   mvn clean package -DskipTests
   ```
2. Suba o ambiente:
   ```bash
   docker-compose up -d --build
   ```
3. A API estará acessível em: `http://localhost:8080`

Para derrubar os containers:
```bash
docker-compose down -v
```

---

## Documentação e Testes

- **Swagger / OpenAPI**: Com a aplicação rodando, acesse `http://localhost:8080/swagger-ui.html` para testar os endpoints através da interface interativa e validar o schema das requisições e respostas.
- **Testes Automatizados**: A suíte roda via Maven e conta com testes unitários focados (`@WebMvcTest` + Mockito para isolar banco de dados na camada web, e mocks limpos nos services).
  Para executar os testes e gerar relatório de cobertura (JaCoCo):
  ```bash
  mvn clean test jacoco:report
  ```
  O relatório ficará em: `target/site/jacoco/index.html`

---

## Mensagens de Commit

Ao contribuir para este projeto, deve-se adotar o padrão Conventional Commits para manter clareza e permitir rastreabilidade automatizada.

**Formato exigido**: `<tipo>[escopo opcional]: <descrição>`

- **feat**: Uma nova funcionalidade ou endpoint. (ex: `feat: adiciona consumer kafka para votos`)
- **fix**: Correção de bug. (ex: `fix: corrige timeout no circuit breaker de cpf`)
- **refactor**: Refatoração de código sem impacto de funcionalidade externa.
- **test**: Adição ou correção de testes. (ex: `test: adiciona coverage jacoco`)
- **docs**: Alteração exclusiva de documentação.
