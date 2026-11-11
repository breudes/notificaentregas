package com.breudes.notificaentregas.service;

import com.breudes.notificaentregas.event.NotificacaoEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoEmailService {
    private final JavaMailSender mailSender;
    private final String remetente;

    public NotificacaoEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String remetente
    ) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviar(NotificacaoEmail notificacao) {
        if (notificacao.emailDestinatario() == null || notificacao.emailDestinatario().isBlank()) {
            throw new IllegalArgumentException("O e-mail do destinatário é obrigatório.");
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        if (!remetente.isBlank()) {
            mensagem.setFrom(remetente);
        }
        mensagem.setTo(notificacao.emailDestinatario());
        mensagem.setSubject("Sua encomenda chegou");
        mensagem.setText(String.format(
                "Olá, %s. Uma encomenda (%s) chegou para você. Localização: apartamento %s, bloco %s.",
                valorOuPadrao(notificacao.nomeDestinatario(), "morador"),
                valorOuPadrao(notificacao.descricaoEncomenda(), "sem descrição"),
                valorOuPadrao(notificacao.apartamento(), "não informado"),
                valorOuPadrao(notificacao.bloco(), "não informado")
        ));

        mailSender.send(mensagem);
    }

    private String valorOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}