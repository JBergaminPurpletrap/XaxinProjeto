package com.xaxin.qrcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com token JWT")
public class AuthResponse {

    @Schema(description = "Mensagem informativa")
    private String mensagem;

    @Schema(description = "Nome de usuário")
    private String username;

    @Schema(description = "Nome exibido do usuário")
    private String nome;

    @Schema(description = "Token JWT para autenticação")
    private String token;

    public AuthResponse() {}

    public AuthResponse(String mensagem, String username, String nome, String token) {
        this.mensagem = mensagem;
        this.username = username;
        this.nome = nome;
        this.token = token;
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
