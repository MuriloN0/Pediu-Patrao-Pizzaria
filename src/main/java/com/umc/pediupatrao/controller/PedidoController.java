package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.CancelamentoPedidosEntrada;
import com.umc.pediupatrao.dto.CriarPedidosEntrada;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public Pedido criarPedido(@RequestBody CriarPedidosEntrada entrada, Authentication authentication) {

        return pedidoService.criarPedido(entrada, authentication.getName());

    }

    @GetMapping
    public List<Pedido> listarPedidos() {

        return pedidoService.listarPedidos();

    }

    @PutMapping("/{id}/status")

    public Pedido atualizarStatus(@PathVariable String id, @RequestParam String status, Authentication authentication) {

        return pedidoService.atualizarStatus(id, status, authentication.getName());

    }

    @PutMapping("/{id}/desconto")
    public Pedido aplicarDesconto( @PathVariable String id, @RequestParam BigDecimal percentual ) {

        return pedidoService.aplicarDesconto(id, percentual);

    }

    @PutMapping("/{id}/cancelamento")
    public Pedido cancelarPedido( @PathVariable String id, @RequestBody CancelamentoPedidosEntrada entrada ) {

        return pedidoService.cancelarPedido( id, entrada.justificativa() );

    }
    
    
}
