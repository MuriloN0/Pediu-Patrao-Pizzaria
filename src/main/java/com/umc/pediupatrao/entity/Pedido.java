package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "pedidos")
@Entity
@Getter
@Setter
public class Pedido {
    @Id
    private String id;

    private String clienteId;

    private String tipoPizza;

    private String status;

    private int quantidade;

    private String sabor;

    private List<PedidoItens> itens;

    private BigDecimal percentualDesconto = BigDecimal.ZERO;

    private BigDecimal valorTotal;

    private String responsavelRegistro;

    private Instant dataHoraEntrada;

    private Instant dataHoraSaida;

    private String responsavelSaida;

    private String justificativaCancelamento;
    
}
