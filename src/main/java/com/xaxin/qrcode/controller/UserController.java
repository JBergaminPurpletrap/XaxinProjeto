package com.xaxin.qrcode.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaxin.qrcode.model.User;
import com.xaxin.qrcode.security.JwtTokenProvider;
import com.xaxin.qrcode.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * POST /api/users/cadastro
     * Body: { "username": "joao", "password": "123", "nome": "João" }
     */
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nome = body.get("nome");

        if (username == null || password == null || nome == null) {
            return ResponseEntity.badRequest().body("Campos obrigatórios: username, password, nome");
        }

        User user = userService.cadastrar(username, password, nome);
        if (user == null) {
            return ResponseEntity.badRequest().body("Username já existe: " + username);
        }

        String token = jwtTokenProvider.gerarToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
            "mensagem", "Usuário cadastrado: " + user.getUsername(),
            "username", user.getUsername(),
            "token", token
        ));
    }

    /**
     * POST /api/users/login
     * Body: { "username": "joao", "password": "123" }
     * Retorna um token JWT para usar nas demais requisições.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Campos obrigatórios: username, password");
        }

        User user = userService.login(username, password);
        if (user == null) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }

        String token = jwtTokenProvider.gerarToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
            "mensagem", "Login OK! Bem-vindo, " + user.getNome(),
            "username", user.getUsername(),
            "nome", user.getNome(),
            "token", token
        ));
    }
}