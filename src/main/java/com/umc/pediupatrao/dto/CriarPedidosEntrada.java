package com.umc.pediupatrao.dto;

import java.util.List;

public record CriarPedidosEntrada(String clienteId, List<PedidosEntrada> itens) {
}
