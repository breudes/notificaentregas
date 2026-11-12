package com.breudes.notificaentregas.infrastructure.messaging;

import com.breudes.notificaentregas.core.domain.NotificacaoEmail;
import com.breudes.notificaentregas.infrastructure.service.NotificacaoEmailService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoEmailListener {
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoEmailListener.class);

    private final NotificacaoEmailService notificacaoEmailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificacaoEmailListener(NotificacaoEmailService notificacaoEmailService) {
        this.notificacaoEmailService = notificacaoEmailService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receber(String payload) {
        if (payload == null || payload.isBlank()) {
            logger.warn("Mensagem de notificação vazia rejeitada.");
            throw new AmqpRejectAndDontRequeueException("Mensagem de notificação vazia.");
        }

        try {
            NotificacaoEmail notificacao = objectMapper.readValue(payload, NotificacaoEmail.class);
            if (notificacao == null) {
                throw new AmqpRejectAndDontRequeueException("Mensagem de notificação não pode ser null.");
            }
            notificacaoEmailService.enviar(notificacao);
        } catch (JsonProcessingException exception) {
            logger.warn("Mensagem de notificação com JSON inválido rejeitada.");
            throw new AmqpRejectAndDontRequeueException("Mensagem de notificação inválida.", exception);
        }
    }
}