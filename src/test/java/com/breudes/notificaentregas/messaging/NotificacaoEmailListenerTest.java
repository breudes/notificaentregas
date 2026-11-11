package com.breudes.notificaentregas.messaging;

import com.breudes.notificaentregas.event.NotificacaoEmail;
import com.breudes.notificaentregas.service.NotificacaoEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificacaoEmailListenerTest {
    private final NotificacaoEmailService notificacaoEmailService = mock(NotificacaoEmailService.class);
    private final NotificacaoEmailListener listener = new NotificacaoEmailListener(notificacaoEmailService);

        @Test
        void deveDesserializarMensagemEEncaminharNotificacao() {
        String payload = """
            {"encomendaId":42,"emailDestinatario":"maria@example.com","nomeDestinatario":"Maria Silva","descricaoEncomenda":"Pacote de livros","apartamento":"101","bloco":"A"}
            """;

        listener.receber(payload);

        ArgumentCaptor<NotificacaoEmail> captor = ArgumentCaptor.forClass(NotificacaoEmail.class);
        verify(notificacaoEmailService).enviar(captor.capture());
        assertEquals(new NotificacaoEmail(
            42L, "maria@example.com", "Maria Silva", "Pacote de livros", "101", "A"
        ), captor.getValue());
        }

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