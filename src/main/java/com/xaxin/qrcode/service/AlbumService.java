package com.xaxin.qrcode.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xaxin.qrcode.frame.FrameService;
import com.xaxin.qrcode.model.Album;
import com.xaxin.qrcode.model.Foto;
import com.xaxin.qrcode.model.User;
import com.xaxin.qrcode.repository.AlbumRepository;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final FrameService frameService;
    private final String uploadDir;

    public AlbumService(AlbumRepository albumRepository, FrameService frameService,
                        @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.albumRepository = albumRepository;
        this.frameService = frameService;
        // Use a directory relative to project root; garante criação
        String path = uploadDir;
        this.uploadDir = path.endsWith(System.getProperty("file.separator")) ? path : path + System.getProperty("file.separator");
        java.io.File dir = new java.io.File(this.uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Retorna ou cria o álbum de um usuário.
     */
    @Transactional
    public Album getOrCreateAlbum(User user) {
        return albumRepository.findByUser(user)
                .orElseGet(() -> {
                    Album novo = new Album(user);
                    return albumRepository.save(novo);
                });
    }

    /**
     * Retorna o álbum de um usuário pelo seu ID.
     */
    public Album getAlbumByUserId(Long userId) {
        return albumRepository.findByUserId(userId)
                .orElse(null);
    }

    /**
     * Upload de foto para o álbum do usuário (máx 10).
     */
    @Transactional
    public Foto uploadFoto(User user, MultipartFile file) throws IOException {
        Album album = getOrCreateAlbum(user);

        if (!album.podeAdicionarFoto()) {
            throw new IllegalStateException("Álbum já possui 10 fotos. Máximo permitido.");
        }

        // Garante que o diretório de upload existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Validação básica de tipo e tamanho
        long maxSize = 5 * 1024 * 1024; // 5MB
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase("image/webp") )) {
            throw new IllegalStateException("Tipo de arquivo inválido. Aceito: image/jpeg, image/png, image/webp");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalStateException("Arquivo muito grande. Máx: 5MB");
        }

        // Gera nome único
        String originalName = file.getOriginalFilename();
        String extensao = "";
        if (originalName != null && originalName.contains(".")) {
            extensao = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extensao;

        // Salva o arquivo
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Cria registro no banco
        String url = "/uploads/" + fileName;
        Foto foto = new Foto(originalName, fileName, url);
        album.addFoto(foto);
        albumRepository.save(album);

        return foto;
    }

    /**
     * Retorna todas as fotos do álbum do usuário.
     */
    public List<Foto> listarFotos(User user) {
        Album album = getOrCreateAlbum(user);
        return album.getFotos();
    }

    /**
     * Aplica moldura a uma foto do álbum do usuário.
     *
     * @param user       Usuário dono do álbum
     * @param fotoId     ID da foto
     * @param frameIndex 1, 2 ou 3
     * @return Caminho da imagem gerada com moldura
     */
    @Transactional
    public String aplicarMoldura(User user, Long fotoId, int frameIndex) throws IOException {
        Album album = getOrCreateAlbum(user);

        // Verifica se a rota foi completada
        if (!album.isRouteComplete()) {
            throw new IllegalStateException("Complete todos os 4 checkpoints primeiro!");
        }

        Foto foto = album.getFotos().stream()
                .filter(f -> f.getId().equals(fotoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Foto não encontrada no álbum do usuário"));

        String fotoPath = uploadDir + foto.getFileName();
        return frameService.aplicarMoldura(new File(fotoPath).getAbsolutePath(), frameIndex);
    }

    /**
     * Marca a rota como completa (quando todos os 4 QR codes forem lidos).
     */
    @Transactional
    public void marcarRotaCompleta(User user) {
        Album album = getOrCreateAlbum(user);
        album.setRouteComplete(true);
        albumRepository.save(album);
    }

    /**
     * Verifica se a rota do usuário está completa.
     */
    public boolean isRotaCompleta(User user) {
        Album album = getOrCreateAlbum(user);
        return album.isRouteComplete();
    }
}