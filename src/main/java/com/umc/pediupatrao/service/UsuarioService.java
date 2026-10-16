package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Busca um usuário por ID
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    // Busca um usuário por username
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Salva um novo usuário (criptografando a senha)
    public void salvarUsuario(Usuario usuario) {

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome de usuário é obrigatório.");
        }

        if (usuario.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O perfil do usuário é obrigatório.");
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é obrigatória.");
        }

        String username = usuario.getUsername().trim();

        usuarioRepository.findByUsername(username)
                .ifPresent(existente -> {
                    if (!existente.getId().equals(usuario.getId())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com esse nome.");
                    }
                });

        usuario.setUsername(username);
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        usuarioRepository.save(usuario);
    }

    // Deleta um usuário pelo ID
    public void deletarUsuario(String id) {
        usuarioRepository.deleteById(id);
    }

    // Lista todos os usuários
    public Iterable<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Atualiza o usuário usuários
    public void atualizarUsuario(String id, Usuario usuarioAtualizado) {

        if (usuarioAtualizado.getRole() == null) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O perfil é obrigatório.");
        }

        usuarioRepository.findById(id).ifPresent(usuarioExistente -> {
            usuarioExistente.setUsername(usuarioAtualizado.getUsername());
            usuarioExistente.setRole(usuarioAtualizado.getRole());

            // Só atualiza a senha se uma nova foi informada
            if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
            }

            usuarioRepository.save(usuarioExistente);
        });
    }
}
