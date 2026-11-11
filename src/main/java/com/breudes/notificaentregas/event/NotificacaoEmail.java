package com.breudes.notificaentregas.event;

public record NotificacaoEmail(
        Long encomendaId,
        String emailDestinatario,
        String nomeDestinatario,
        String descricaoEncomenda,
        String apartamento,
        String bloco
) {
}