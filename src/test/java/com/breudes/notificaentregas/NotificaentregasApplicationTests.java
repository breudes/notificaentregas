package com.breudes.notificaentregas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.breudes.notificaentregas.core.domain.NotificacaoEmail;
import com.breudes.notificaentregas.infrastructure.messaging.NotificacaoEmailListener;
import com.breudes.notificaentregas.infrastructure.service.NotificacaoEmailService;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Testcontainers(disabledWithoutDocker = true)
class NotificaentregasApplicationTests {
    private static final String QUEUE = "integration.notificacao.email";
    private static final String EXCHANGE = "integration.notificacao.exchange";
    private static final String ROUTING_KEY = "integration.notificacao.email";

	@Container
	private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
			.withDatabaseName("notificacoes_test")
			.withUsername("test")
			.withPassword("test");

	@Container
	private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management-alpine");

	private CachingConnectionFactory connectionFactory;
	private SimpleMessageListenerContainer listenerContainer;

	@Test
	void deveConsumirMensagemDoRabbitEEnviarEmail() throws Exception {
		connectionFactory = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
		connectionFactory.setUsername(rabbit.getAdminUsername());
		connectionFactory.setPassword(rabbit.getAdminPassword());

		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		Queue queue = new Queue(QUEUE, false, false, true);
		DirectExchange exchange = new DirectExchange(EXCHANGE);
		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY));

		JavaMailSender mailSender = mock(JavaMailSender.class);
		CountDownLatch emailEnviado = new CountDownLatch(1);
		doAnswer(invocation -> {
			emailEnviado.countDown();
			return null;
		}).when(mailSender).send(any(SimpleMailMessage.class));

		NotificacaoEmailService service = new NotificacaoEmailService(mailSender, "entregas@example.com");
		NotificacaoEmailListener listener = new NotificacaoEmailListener(service);
		listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		listenerContainer.setQueueNames(QUEUE);
		listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
		listenerContainer.setMessageListener((Message message) -> listener.receber(
				new String(message.getBody(), StandardCharsets.UTF_8)
		));
		listenerContainer.start();

		String payload = new ObjectMapper().writeValueAsString(new NotificacaoEmail(
				42L, "maria@example.com", "Maria Silva", "Pacote de livros", "101", "A"
		));
		new RabbitTemplate(connectionFactory).convertAndSend(EXCHANGE, ROUTING_KEY, payload);

		assertTrue(emailEnviado.await(10, TimeUnit.SECONDS), "O consumer não processou a mensagem a tempo.");
		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		assertEquals("maria@example.com", captor.getValue().getTo()[0]);
		assertEquals("Sua encomenda chegou", captor.getValue().getSubject());
	}

	@AfterEach
	void encerrarRecursos() {
		if (listenerContainer != null) {
			listenerContainer.stop();
		}
		if (connectionFactory != null) {
			connectionFactory.destroy();
		}
	}

}
