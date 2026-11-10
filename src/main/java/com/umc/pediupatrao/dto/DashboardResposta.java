package com.umc.pediupatrao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardResposta(LocalDate inicio, LocalDate fim, long totalPedidos, Indicadores indicadores, List<VendaDiaria> vendasPorDia, Map<String, Long> pedidosPorStatus, List<ProdutoVendido> produtosMaisVendidos) {

    public record Indicadores(long pedidosFinalizados, BigDecimal valorLiquido, BigDecimal ticketMedio) {}

    public record VendaDiaria(LocalDate data, BigDecimal valorLiquido) {}

    public record ProdutoVendido(String produtoId, String nome, long unidades) {}
}