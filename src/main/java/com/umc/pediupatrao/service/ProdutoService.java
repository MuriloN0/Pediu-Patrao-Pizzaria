package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    // Método para excluir cliente
    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }


    // Método para salvar um novo cliente ou atualizar um cliente existente
    public Produto salvar(Produto produto) {
        if (produto.getNome() == null || produto.getNome().isBlank() || produto.getTipo() == null || produto.getTipo().isBlank() || produto.getPreco() == null || !Double.isFinite(produto.getPreco()) || produto.getPreco() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe nome, tipo e preço válido.");
        }

        if (produto.getId() != null
                && !produtoRepository.existsById(produto.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produto não encontrado.");
        }

        produto.setNome(produto.getNome().trim());
        produto.setTipo(produto.getTipo().trim());

        return produtoRepository.save(produto);
    }
}
