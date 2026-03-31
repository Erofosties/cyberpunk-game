package com.cyberpunk.dto;

public class AuthResponse {

    private final String token;
    private final Long usuarioId;
    private final String username;

    public AuthResponse(String token, Long usuarioId, String username) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.username = username;
    }

    public String getToken() { return token; }
    public Long getUsuarioId() { return usuarioId; }
    public String getUsername() { return username; }
}
