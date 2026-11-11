# Notificação de Entregas

Microsserviço Spring Boot que consome eventos de notificação no RabbitMQ e envia e-mails aos destinatários usando SMTP. O serviço não possui endpoints REST de negócio.

## Requisitos

- Java 21
- Docker e Docker Compose, para executar as dependências (PostgreSQL e RabbitMQ)
- Acesso a um servidor SMTP e credenciais válidas para envio de e-mail

## Executar com o projeto completo

Na raiz do repositório, configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD` e `MAIL_FROM` no arquivo `.env`, conforme o [README principal](../README.md). Em seguida, execute:

```bash
docker compose up --build
```

O serviço se conecta ao RabbitMQ e escuta a fila `notificacao.email`. O console de administração do RabbitMQ fica em `http://localhost:15672` (usuário/senha padrão `guest`/`guest`).

## Executar localmente

Com PostgreSQL e RabbitMQ disponíveis, defina as variáveis de ambiente de conexão e SMTP:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/notificacoes_db
DB_NAME=notificacoes_db
DB_USER=postgres
DB_PASS=postgres
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=seu-email
MAIL_PASSWORD=sua-senha-de-aplicativo
MAIL_FROM=seu-email
```

Na pasta `notificaentregas`, inicie a aplicação com o Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

O Springdoc disponibiliza a documentação OpenAPI em [`http://localhost:8082/swagger-ui/index.html`](http://localhost:8082/swagger-ui/index.html) e o documento em [`http://localhost:8082/v3/api-docs`](http://localhost:8082/v3/api-docs) quando a aplicação está em execução. Como não há endpoints REST de negócio, a documentação descreve o propósito do serviço; o processamento ocorre por mensagens RabbitMQ.

## Mensagem consumida

O serviço recebe JSON com os dados do evento:

```json
{
  "encomendaId": 42,
  "emailDestinatario": "maria@example.com",
  "nomeDestinatario": "Maria Silva",
  "descricaoEncomenda": "Pacote de livros",
  "apartamento": "101",
  "bloco": "A"
}
```

O assunto do e-mail enviado é “Sua encomenda chegou”. Mensagens vazias ou com JSON inválido são rejeitadas sem reenvio automático pela fila.

## Configuração da mensageria

Por padrão, a aplicação usa a fila `notificacao.email`, a exchange `notificacao.exchange` e a routing key `notificacao.email`. O serviço de gerência publica nessa mesma configuração. Os nomes podem ser ajustados pelas propriedades `app.rabbitmq.queue`, `app.rabbitmq.exchange` e `app.rabbitmq.routing-key`.

Não versione credenciais SMTP; mantenha-as em variáveis de ambiente ou em um gerenciador de segredos.

## Autor

[Brenda Silva](https://www.linkedin.com/in/breudes/)

Este serviço de notificações faz parte do projeto substitutivo da fase 4 da pós tech Java (FIAP).