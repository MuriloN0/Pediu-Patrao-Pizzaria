package com.umc.pediupatrao.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistroAlteracaoCampo {

    private String campo;
    private String valorAnterior;
    private String valorNovo;

    public RegistroAlteracaoCampo(String campo, Object anterior, Object novo) {

        this.campo = campo;
        this.valorAnterior = anterior == null ? null : anterior.toString();
        this.valorNovo = novo == null ? null : novo.toString();

    }

}
