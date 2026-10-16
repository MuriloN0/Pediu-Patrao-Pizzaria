package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.RegistroAlteracaoCampo;
import com.umc.pediupatrao.repository.AuditoriaRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrar(String operacao, String entidade, String entidadeId, List<RegistroAlteracaoCampo> alteracoes, String justificativa) {

        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("Auditoria exige usuário autenticado");
        }

        String perfil = auth.getAuthorities().stream()
                .map(autoridade -> autoridade.getAuthority())
                .filter(valor -> valor.startsWith("ROLE_"))
                .map(valor -> valor.substring(5))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário sem perfil"));

        Auditoria evento = new Auditoria();
        evento.setUsuario(auth.getName());
        evento.setPerfil(perfil);
        evento.setDataHora(Instant.now());
        evento.setOperacao(operacao);
        evento.setEntidade(entidade);
        evento.setEntidadeId(entidadeId);
        evento.setAlteracoes(alteracoes);
        evento.setJustificativa(justificativa);

        repository.save(evento);
    }

    public List<Auditoria> listar() {

        return repository.findAll(Sort.by(Sort.Direction.DESC, "dataHora"));
    }

}
