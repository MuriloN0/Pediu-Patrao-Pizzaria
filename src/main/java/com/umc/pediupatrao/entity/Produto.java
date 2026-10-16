package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "produtos")
@Entity
@Getter
@Setter
public class Produto {
    
    @Id
    private String id;

    private String nome;

    private String tipo; // Pizza, Esfiha, Bebida, Lanche

    private Double preco;

    private Boolean ativo;

}
