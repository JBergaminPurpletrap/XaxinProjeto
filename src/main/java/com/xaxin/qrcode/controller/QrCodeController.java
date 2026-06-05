package com.xaxin.qrcode.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaxin.qrcode.model.Checkpoint;
import com.xaxin.qrcode.service.QrCodeService;
import com.xaxin.qrcode.service.TrajetoService;
import com.xaxin.qrcode.dto.ApiResponse;

@RestController
@RequestMapping("/api/qrcode")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final TrajetoService trajetoService;

    public QrCodeController(QrCodeService qrCodeService, TrajetoService trajetoService) {
        this.qrCodeService = qrCodeService;
        this.trajetoService = trajetoService;
    }

    /**
     * POST /api/qrcode/ler
     * Lê um QR Code de uma imagem e valida se é um dos 4 checkpoints válidos.
     * Body: { "caminho": "C:\\qr.png" }
     */
    @PostMapping("/ler")
    public ResponseEntity<?> lerQrCode(java.security.Principal principal, @RequestBody Map<String, String> body) {
        String caminho = body.get("caminho");
        if (caminho == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Campo obrigatório: caminho"));
        }

        try {
            File imagem = new File(caminho);
            int numero = qrCodeService.readQrNumberFromImage(imagem);
            String codigoLido = String.valueOf(numero);

            String username = principal == null ? null : principal.getName();
            Checkpoint checkpoint = trajetoService.validarELancarQrCode(codigoLido, username);
            if (checkpoint == null) {
                return ResponseEntity.ok(ApiResponse.message("QR lido: " + numero + " (NÃO é um checkpoint válido)"));
            }

            return ResponseEntity.ok(ApiResponse.message("QR lido: " + numero + " | Checkpoint: " + checkpoint.getTitulo() + " ✓"));
        } catch (IllegalStateException | IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Erro ao ler QR: " + e.getMessage()));
        }
    }

    /**
     * POST /api/qrcode/validar-texto
     * Valida um código QR informado manualmente (texto).
     * Body: { "codigo": "1001" }
     */
    @PostMapping("/validar-texto")
    public ResponseEntity<?> validarTexto(java.security.Principal principal, @RequestBody Map<String, String> body) {
        String codigo = body.get("codigo");
        if (codigo == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Campo obrigatório: codigo"));
        }

        String username = principal == null ? null : principal.getName();
        Checkpoint checkpoint = trajetoService.validarELancarQrCode(codigo, username);
        if (checkpoint == null) {
            return ResponseEntity.ok(ApiResponse.message("Código \"" + codigo + "\" NÃO é um checkpoint válido"));
        }

        return ResponseEntity.ok(ApiResponse.message("Checkpoint válido: " + checkpoint.getTitulo() + " ✓"));
    }

    /**
     * GET /api/qrcode/progresso
     * Retorna o progresso atual dos 4 checkpoints.
     */
    @GetMapping("/progresso")
    public ResponseEntity<?> progresso(java.security.Principal principal) {
        String username = principal == null ? null : principal.getName();
        return ResponseEntity.ok(ApiResponse.ok(trajetoService.getProgresso(username)));
    }
}