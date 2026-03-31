package com.cyberpunk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberpunk.domain.usuario.Usuario;
import com.cyberpunk.dto.AuthResponse;
import com.cyberpunk.dto.CrearUsuarioRequest;
import com.cyberpunk.dto.LoginRequest;
import com.cyberpunk.security.JwtUtil;
import com.cyberpunk.security.UserDetailsServiceImpl;
import com.cyberpunk.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthController(
            UsuarioService usuarioService,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsServiceImpl userDetailsService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CrearUsuarioRequest request) {
        Usuario usuario = usuarioService.crearUsuario(request.getUsername(), request.getPassword());
        String token = jwtUtil.generateToken(usuario.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, usuario.getId(), usuario.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails.getUsername());
        Usuario usuario = usuarioService.buscarPorUsername(request.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, usuario.getId(), usuario.getUsername()));
    }
}
