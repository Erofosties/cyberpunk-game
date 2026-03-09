package com.cyberpunk.service; 

import java.util.List; 
import org.springframework.stereotype.Service; 
import com.cyberpunk.domain.colonia.Colonia;
import com.cyberpunk.domain.usuario.Usuario; 
import com.cyberpunk.repository.ColoniaRepository; 
import com.cyberpunk.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ColoniaRepository coloniaRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ColoniaRepository coloniaRepository) {

        this.usuarioRepository = usuarioRepository;
        this.coloniaRepository = coloniaRepository;
    }

    public Usuario crearUsuario(String username, String password) {

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("El usuario ya existe");
        }

        Usuario usuario = new Usuario(username, password);

        usuarioRepository.save(usuario);

        Colonia colonia = new Colonia("Colonia Inicial");

        colonia.setUsuario(usuario);           

        coloniaRepository.save(colonia);

        usuario.setColonia(colonia);

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorUsername(String username) {

        return usuarioRepository.findByUsername(username)
                .orElse(null);
    }

    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}