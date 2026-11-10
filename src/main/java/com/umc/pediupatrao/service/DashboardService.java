package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardResposta;
import com.umc.pediupatrao.dto.DashboardResposta.Indicadores;
import com.umc.pediupatrao.dto.DashboardResposta.ProdutoVendido;
import com.umc.pediupatrao.dto.DashboardResposta.VendaDiaria;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.PedidoItens;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal ZERO_MONETARIO = new BigDecimal("0.00");
    private static final int LIMITE_DIAS = 366;
    private static final int LIMITE_PRODUTOS = 5;

    private static final List<String> STATUS = List.of("RECEBIDO", "EM_PREPARACAO", "PRONTO", "SAIU_PARA_ENTREGA", "RETIRADO", "FINALIZADO", "CANCELADO", "OUTROS");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public DashboardService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    public DashboardResposta consultar(LocalDate inicio, LocalDate fim) {
        LocalDate dataFim = fim == null ? LocalDate.now(FUSO) : fim;
        LocalDate dataInicio = inicio == null ? dataFim.minusDays(29) : inicio;

        long intervalo = ChronoUnit.DAYS.between(dataInicio, dataFim);
        if (intervalo < 0 || intervalo >= LIMITE_DIAS) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um período válido de até 366 dias.");
        }

        LocalDate diaAposFim = dataFim.plusDays(1);
        List<Pedido> pedidos = pedidoRepository.buscarPorPeriodo(dataInicio.atStartOfDay(FUSO).toInstant(), diaAposFim.atStartOfDay(FUSO).toInstant());

        Map<String, Long> porStatus = new LinkedHashMap<>();
        STATUS.forEach(status -> porStatus.put(status, 0L));

        Map<LocalDate, BigDecimal> porDia = new LinkedHashMap<>();
        dataInicio.datesUntil(diaAposFim).forEach(dia -> porDia.put(dia, ZERO_MONETARIO));

        Map<String, Long> unidadesPorProduto = new HashMap<>();
        BigDecimal valorLiquido = ZERO_MONETARIO;
        long finalizadosComValor = 0;

        for (Pedido pedido : pedidos) {

            String status = pedido.getStatus();
            String categoria = porStatus.containsKey(status) ? status : "OUTROS";
            porStatus.merge(categoria, 1L, Long::sum);

            if (!"FINALIZADO".equals(status)) {

                continue;
            }

            BigDecimal valor = pedido.getValorTotal();
            if (valor != null) {

                LocalDate dia = pedido.getDataHoraEntrada().atZone(FUSO).toLocalDate();
                porDia.merge(dia, valor, BigDecimal::add);
                valorLiquido = valorLiquido.add(valor);
                finalizadosComValor++;
            }

            somarUnidades(pedido.getItens(), unidadesPorProduto);
        }

        BigDecimal ticketMedio = finalizadosComValor == 0 ? ZERO_MONETARIO : valorLiquido.divide(BigDecimal.valueOf(finalizadosComValor), 2, RoundingMode.HALF_UP);

        List<VendaDiaria> vendasPorDia = porDia.entrySet().stream()
                .map(venda -> new VendaDiaria(venda.getKey(), venda.getValue()))
                .toList();

        return new DashboardResposta(dataInicio, dataFim, pedidos.size(), new Indicadores(porStatus.get("FINALIZADO"), valorLiquido, ticketMedio), vendasPorDia, porStatus, montarRanking(unidadesPorProduto));
    }

    private void somarUnidades(List<PedidoItens> itens, Map<String, Long> unidades) {

        if (itens == null) {

            return;
        }

        for (PedidoItens item : itens) {

            if (item != null && item.getProdutoId() != null && item.getQuantidade() > 0) {
                unidades.merge(item.getProdutoId(), (long) item.getQuantidade(), Long::sum);
            }
        }
    }

    private List<ProdutoVendido> montarRanking(Map<String, Long> unidades) {

        List<Map.Entry<String, Long>> maisVendidos = unidades.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(LIMITE_PRODUTOS)
                .toList();

        List<String> ids = maisVendidos.stream().map(Map.Entry::getKey).toList();
        Map<String, String> nomes = new HashMap<>();

        for (Produto produto : produtoRepository.findAllById(ids)) {

            nomes.put(produto.getId(), produto.getNome());
        }

        return maisVendidos.stream()
                .map(item -> new ProdutoVendido(item.getKey(), nomes.getOrDefault(item.getKey(), "Produto removido"), item.getValue()
                )).toList();
    }
}