package com.breudes.notificaentregas.messaging;

import com.breudes.notificaentregas.service.NotificacaoEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificacaoEmailListenerTest {
    private final NotificacaoEmailService notificacaoEmailService = mock(NotificacaoEmailService.class);
    private final NotificacaoEmailListener listener = new NotificacaoEmailListener(notificacaoEmailService);

    @Test
    void deveRejeitarPayloadVazioSemRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receber(""));
        verifyNoInteractions(notificacaoEmailService);
    }

    @Test
    void deveRejeitarJsonNullSemRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receber("null"));
        verifyNoInteractions(notificacaoEmailService);
    }

    @Test
    void deveRejeitarJsonMalformadoSemRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receber("{"));
        verifyNoInteractions(notificacaoEmailService);
    }
}