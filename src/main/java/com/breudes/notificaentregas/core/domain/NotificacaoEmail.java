package com.breudes.notificaentregas.core.domain;

public record NotificacaoEmail(
        Long encomendaId,
        String emailDestinatario,
        String nomeDestinatario,
        String descricaoEncomenda,
        String apartamento,
        String bloco
) {
}