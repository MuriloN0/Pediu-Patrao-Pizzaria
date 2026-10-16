package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PedidoItens {

    private String produtoId;
    private int quantidade;
    private BigDecimal precoUnitario;

}
