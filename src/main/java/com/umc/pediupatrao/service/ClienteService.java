package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.RegistroAlteracaoCampo;
import com.umc.pediupatrao.repository.ClienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public Cliente novoCliente(Cliente cliente) {

        return salvar(cliente);
    }

    public List<Cliente> listarClientes() {

        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(String id) {

        return clienteRepository.findById(id);
    }

    public void excluir(String id) {

        clienteRepository.deleteById(id);
    }

    public Cliente salvar(Cliente cliente) {

        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe os dados do cliente");
        }

        boolean novo = cliente.getId() == null || cliente.getId().isBlank();

        Cliente anterior;

        if (novo) {
            cliente.setId(null);
            anterior = new Cliente();
        } else {
            anterior = clienteRepository.findById(cliente.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        }

        // Compara antes de salvar ou modificar os dados anteriores.
        List<RegistroAlteracaoCampo> alteracoes = new ArrayList<>();

        registrarCampo(alteracoes, "nome", anterior.getNome(), cliente.getNome());

        registrarCampo(alteracoes, "telefone", anterior.getTelefone(), cliente.getTelefone());

        registrarCampo(alteracoes, "cep", anterior.getCep(), cliente.getCep());

        registrarCampo(alteracoes, "logradouro", anterior.getLogradouro(), cliente.getLogradouro());

        registrarCampo(alteracoes, "numero", anterior.getNumero(), cliente.getNumero());

        registrarCampo(alteracoes, "complemento", anterior.getComplemento(), cliente.getComplemento());

        registrarCampo(alteracoes, "bairro", anterior.getBairro(), cliente.getBairro());

        registrarCampo(alteracoes, "cidade", anterior.getCidade(), cliente.getCidade());

        registrarCampo(alteracoes, "estado", anterior.getEstado(), cliente.getEstado());

        registrarCampo(alteracoes, "endereco", anterior.getEndereco(), cliente.getEndereco());

        Cliente salvo = clienteRepository.save(cliente);

        if (novo || !alteracoes.isEmpty()) {
            auditoriaService.registrar(novo ? "INCLUSAO_CLIENTE" : "ALTERACAO_CLIENTE", "Cliente", salvo.getId(), alteracoes, null
            );
        }

        return salvo;
    }

    private void registrarCampo(List<RegistroAlteracaoCampo> alteracoes, String campo, String anterior, String novo) {

        if (!Objects.equals(anterior, novo)) {
            alteracoes.add(new RegistroAlteracaoCampo(
                    campo, anterior, novo));
        }
    }
}