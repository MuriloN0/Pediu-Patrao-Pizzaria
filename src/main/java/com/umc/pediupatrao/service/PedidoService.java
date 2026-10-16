package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.CriarPedidosEntrada;
import com.umc.pediupatrao.dto.PedidosEntrada;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.PedidoItens;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.RegistroAlteracaoCampo;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Pedido criarPedido(CriarPedidosEntrada entrada, String responsavel) {

        if (entrada == null || entrada.clienteId() == null || entrada.clienteId().isBlank() || !clienteRepository.existsById(entrada.clienteId())) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inválido");

        }

        if (entrada.itens() == null || entrada.itens().isEmpty()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe pelo menos um item");

        }

        List<PedidoItens> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PedidosEntrada itemEntrada : entrada.itens()) {

            if (itemEntrada == null || itemEntrada.produtoId() == null || itemEntrada.produtoId().isBlank() || itemEntrada.quantidade() <= 0) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item inválido");

            }

            Produto produto = produtoRepository
                    .findById(itemEntrada.produtoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto não encontrado"));

            Double precoCadastrado = produto.getPreco();

            if (precoCadastrado == null || !Double.isFinite(precoCadastrado) || precoCadastrado < 0) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto com preço inválido");

            }

            BigDecimal preco = BigDecimal.valueOf(precoCadastrado)
                    .setScale(2, RoundingMode.HALF_UP);

            PedidoItens item = new PedidoItens();
            item.setProdutoId(produto.getId());
            item.setQuantidade(itemEntrada.quantidade());
            item.setPrecoUnitario(preco);
            itens.add(item);

            total = total.add(preco.multiply(BigDecimal.valueOf(itemEntrada.quantidade())));
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(entrada.clienteId());
        pedido.setItens(itens);
        pedido.setPercentualDesconto(BigDecimal.ZERO);
        pedido.setValorTotal(total);
        pedido.setResponsavelRegistro(responsavel);
        pedido.setDataHoraEntrada(Instant.now());
        pedido.setStatus("RECEBIDO");

        Pedido salvo = pedidoRepository.save(pedido);

        List<RegistroAlteracaoCampo> alteracoes = new ArrayList<>();

        alteracoes.add(new RegistroAlteracaoCampo("clienteId", null, salvo.getClienteId()));
        alteracoes.add(new RegistroAlteracaoCampo("status", null, salvo.getStatus()));
        alteracoes.add(new RegistroAlteracaoCampo("valorTotal", null, salvo.getValorTotal()));
        alteracoes.add(new RegistroAlteracaoCampo("percentualDesconto", null, salvo.getPercentualDesconto()));
        alteracoes.add(new RegistroAlteracaoCampo("responsavelRegistro", null, salvo.getResponsavelRegistro()));
        alteracoes.add(new RegistroAlteracaoCampo("dataHoraEntrada", null, salvo.getDataHoraEntrada()));

        for (int i = 0; i < salvo.getItens().size(); i++) {
            PedidoItens item = salvo.getItens().get(i);
            String campo = "itens[" + i + "].";

            alteracoes.add(new RegistroAlteracaoCampo(campo + "produtoId", null, item.getProdutoId()));
            alteracoes.add(new RegistroAlteracaoCampo(campo + "quantidade", null, item.getQuantidade()));
            alteracoes.add(new RegistroAlteracaoCampo(campo + "precoUnitario", null, item.getPrecoUnitario()));
        }

        auditoriaService.registrar("INCLUSAO_PEDIDO", "Pedido", salvo.getId(), alteracoes, null);

        return salvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido atualizarStatus(String id, String novoStatus, String responsavel) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        String atual = pedido.getStatus();

        if (atual == null || novoStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido");
        }

        boolean permitido = switch (atual) {

            case "RECEBIDO" -> "EM_PREPARACAO".equals(novoStatus);

            case "EM_PREPARACAO" -> "PRONTO".equals(novoStatus);

            case "PRONTO" -> "SAIU_PARA_ENTREGA".equals(novoStatus) || "RETIRADO".equals(novoStatus);

            case "SAIU_PARA_ENTREGA", "RETIRADO" -> "FINALIZADO".equals(novoStatus);

            default -> false;
        };

        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transição não permitida: " + atual + " → " + novoStatus);
        }

        // Captura os valores antes de modificar o pedido.
        Instant saidaAnterior = pedido.getDataHoraSaida();
        String responsavelAnterior = pedido.getResponsavelSaida();

        boolean registrandoSaida = "SAIU_PARA_ENTREGA".equals(novoStatus) || "RETIRADO".equals(novoStatus);

        if (registrandoSaida) {
            pedido.setDataHoraSaida(Instant.now());
            pedido.setResponsavelSaida(responsavel);
        }

        pedido.setStatus(novoStatus);

        Pedido salvo = pedidoRepository.save(pedido);

        List<RegistroAlteracaoCampo> alteracoes = new ArrayList<>();

        alteracoes.add(new RegistroAlteracaoCampo("status", atual, novoStatus));

        if (registrandoSaida) {
            alteracoes.add(new RegistroAlteracaoCampo("dataHoraSaida", saidaAnterior, salvo.getDataHoraSaida()));

            alteracoes.add(new RegistroAlteracaoCampo("responsavelSaida", responsavelAnterior, salvo.getResponsavelSaida()));
        }

        auditoriaService.registrar("ALTERACAO_STATUS", "Pedido", salvo.getId(), alteracoes, null);

        return salvo;
    }

    private void validarAntesDaSaida(Pedido pedido) {
        String status = pedido.getStatus();

        boolean permitido = "RECEBIDO".equals(status) || "EM_PREPARACAO".equals(status) || "PRONTO".equals(status);

        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operação permitida somente antes da saída " + "e em pedidos não cancelados.");
        }
    }

    public Pedido aplicarDesconto(String id, BigDecimal percentual) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        validarAntesDaSaida(pedido);

        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) < 0 || percentual.compareTo(new BigDecimal("20")) > 0) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O desconto deve estar entre 0 e 20%.");
        }

        BigDecimal percentualAnterior = pedido.getPercentualDesconto();
        BigDecimal totalAnterior = pedido.getValorTotal();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PedidoItens item : pedido.getItens()) {
            subtotal = subtotal.add( item.getPrecoUnitario().multiply( BigDecimal.valueOf(item.getQuantidade())));
        }

        BigDecimal desconto = subtotal
                .multiply(percentual)
                .divide(new BigDecimal("100"));

        BigDecimal total = subtotal.subtract(desconto)
                .setScale(2, RoundingMode.HALF_UP);

        pedido.setPercentualDesconto(percentual);
        pedido.setValorTotal(total);

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar("APLICACAO_DESCONTO", "Pedido", salvo.getId(),
                List.of(
                        new RegistroAlteracaoCampo("percentualDesconto", percentualAnterior, salvo.getPercentualDesconto()),
                        new RegistroAlteracaoCampo("valorTotal", totalAnterior, salvo.getValorTotal())
                ),
                null
        );

        return salvo;
    }

    public Pedido cancelarPedido(String id, String justificativa) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        validarAntesDaSaida(pedido);

        if (justificativa == null || justificativa.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a justificativa do cancelamento.");
        }

        String statusAnterior = pedido.getStatus();
        String justificativaAnterior = pedido.getJustificativaCancelamento();

        pedido.setJustificativaCancelamento(justificativa.trim());
        pedido.setStatus("CANCELADO");

        Pedido salvo = pedidoRepository.save(pedido);

        auditoriaService.registrar("CANCELAMENTO_PEDIDO", "Pedido", salvo.getId(),

                List.of(
                        new RegistroAlteracaoCampo("status", statusAnterior, salvo.getStatus()),
                        new RegistroAlteracaoCampo("justificativaCancelamento", justificativaAnterior, salvo.getJustificativaCancelamento())
                ),
                salvo.getJustificativaCancelamento()
        );

        return salvo;
    }
}