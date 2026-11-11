package com.breudes.notificaentregas.service;

import com.breudes.notificaentregas.event.NotificacaoEmail;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificacaoEmailServiceTest {
    private final JavaMailSender mailSender = mock(JavaMailSender.class);

    @Test
    void deveEnviarEmailComDadosDaNotificacao() {
        NotificacaoEmailService service = new NotificacaoEmailService(mailSender, "entregas@example.com");
        NotificacaoEmail notificacao = new NotificacaoEmail(
                42L, "maria@example.com", "Maria Silva", "Pacote de livros", "101", "A"
        );

        service.enviar(notificacao);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage mensagem = captor.getValue();
        assertEquals("entregas@example.com", mensagem.getFrom());
        assertEquals("maria@example.com", mensagem.getTo()[0]);
        assertEquals("Sua encomenda chegou", mensagem.getSubject());
        assertEquals(
                "Olá, Maria Silva. Uma encomenda (Pacote de livros) chegou para você. Localização: apartamento 101, bloco A.",
                mensagem.getText()
        );
    }

    @Test
    void deveUsarValoresPadraoQuandoDadosOpcionaisForemNulos() {
        NotificacaoEmailService service = new NotificacaoEmailService(mailSender, "");
        service.enviar(new NotificacaoEmail(42L, "maria@example.com", null, null, null, null));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertEquals(null, captor.getValue().getFrom());
        assertEquals(
                "Olá, morador. Uma encomenda (sem descrição) chegou para você. Localização: apartamento não informado, bloco não informado.",
                captor.getValue().getText()
        );
    }

    @Test
    void deveRejeitarEmailDoDestinatarioNuloOuEmBranco() {
        NotificacaoEmailService service = new NotificacaoEmailService(mailSender, "");

        assertThrows(IllegalArgumentException.class,
                () -> service.enviar(new NotificacaoEmail(42L, null, "Maria", "Pacote", "101", "A")));
        assertThrows(IllegalArgumentException.class,
                () -> service.enviar(new NotificacaoEmail(42L, "  ", "Maria", "Pacote", "101", "A")));
        verifyNoInteractions(mailSender);
    }
}