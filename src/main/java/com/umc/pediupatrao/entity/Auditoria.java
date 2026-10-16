package com.umc.pediupatrao.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "auditoria")
@Getter
@Setter
public class Auditoria {

    @Id
    private String id;

    private String usuario;
    private String perfil;
    private Instant dataHora;
    private String operacao;
    private String entidade;
    private String entidadeId;
    private List<RegistroAlteracaoCampo> alteracoes;
    private String justificativa;

}
