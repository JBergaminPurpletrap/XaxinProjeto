package com.xaxin.qrcode.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xaxin.qrcode.model.Album;
import com.xaxin.qrcode.model.Foto;
import com.xaxin.qrcode.model.User;
import com.xaxin.qrcode.service.AlbumService;
import com.xaxin.qrcode.service.UserService;

@RestController
@RequestMapping("/api/album")
public class AlbumController {

    private final AlbumService albumService;
    private final UserService userService;

    public AlbumController(AlbumService albumService, UserService userService) {
        this.albumService = albumService;
        this.userService = userService;
    }

    private User getUserFromPrincipal(Principal principal) {
        return userService.buscarPorUsername(principal.getName()).orElse(null);
    }

    /**
     * GET /api/album - Retorna o álbum do usuário logado
     */
    @GetMapping
    public ResponseEntity<?> getAlbum(Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user == null) return ResponseEntity.status(401).body("Usuário não encontrado");

        Album album = albumService.getOrCreateAlbum(user);
        return ResponseEntity.ok(Map.of(
            "id", album.getId(),
            "username", user.getUsername(),
            "routeComplete", album.isRouteComplete(),
            "quantidadeFotos", album.getQuantidadeFotos(),
            "podeAdicionar", album.podeAdicionarFoto(),
            "fotos", album.getFotos().stream().map(f -> Map.of(
                "id", f.getId(),
                "originalName", f.getOriginalName(),
                "url", f.getUrl(),
                "uploadedAt", f.getUploadedAt().toString()
            )).toList()
        ));
    }

    /**
     * POST /api/album/upload - Upload de foto (máx 10)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFoto(Principal principal,
                                         @RequestParam("file") MultipartFile file) {
        User user = getUserFromPrincipal(principal);
        if (user == null) return ResponseEntity.status(401).body("Usuário não encontrado");

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo vazio");
        }

        try {
            Foto foto = albumService.uploadFoto(user, file);
            return ResponseEntity.ok(Map.of(
                "mensagem", "Foto enviada com sucesso",
                "id", foto.getId(),
                "fileName", foto.getFileName(),
                "url", foto.getUrl()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            String detail = e.getMessage() != null ? e.getMessage() : e.toString();
            return ResponseEntity.internalServerError().body(Map.of("error", "Erro ao enviar foto", "details", detail));
        }
    }

    /**
     * POST /api/album/moldura/{fotoId} - Aplica moldura a uma foto
     * Body: { "frame": 1 } (1, 2 ou 3)
     * Só funciona se os 4 QR codes forem lidos (routeComplete == true)
     */
    @PostMapping("/moldura/{fotoId}")
    public ResponseEntity<?> aplicarMoldura(Principal principal,
                                             @PathVariable Long fotoId,
                                             @RequestBody Map<String, Integer> body) {
        User user = getUserFromPrincipal(principal);
        if (user == null) return ResponseEntity.status(401).body("Usuário não encontrado");

        Integer frameIndex = body.get("frame");
        if (frameIndex == null || frameIndex < 1 || frameIndex > 3) {
            return ResponseEntity.badRequest().body("Campo 'frame' obrigatório (1, 2 ou 3)");
        }

        try {
            String caminho = albumService.aplicarMoldura(user, fotoId, frameIndex);
            String fileName = new java.io.File(caminho).getName();
            return ResponseEntity.ok(Map.of(
                "mensagem", "Moldura aplicada com sucesso!",
                "frame", frameIndex,
                "arquivo", fileName,
                "url", "/uploads/" + fileName
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao aplicar moldura: " + e.getMessage());
        }
    }
}