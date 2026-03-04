package com.cyberpunk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cyberpunk.domain.usuario.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByUsername(String username);

}