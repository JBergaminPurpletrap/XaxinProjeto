package com.xaxin.qrcode.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xaxin.qrcode.model.User;
import com.xaxin.qrcode.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra um novo usuário com senha criptografada.
     * Retorna null se o username já existir.
     */
    @Transactional
    public User cadastrar(String username, String password, String nome) {
        if (userRepository.findByUsername(username).isPresent()) {
            return null;
        }
        String senhaCriptografada = passwordEncoder.encode(password);
        User user = new User(username, senhaCriptografada, nome);
        return userRepository.save(user);
    }

    /**
     * Busca usuário por username.
     */
    public Optional<User> buscarPorUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Valida login: username + password (com criptografia).
     * Retorna o usuário se credenciais OK, ou null se inválido.
     */
    public User login(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isPresent()) {
            User user = opt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }
}