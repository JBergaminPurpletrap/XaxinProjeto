package com.xaxin.qrcode.frame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class FrameService {

    private static final String FRAMES_CLASSPATH = "static/frames/";

    /**
     * Aplica uma moldura sobre a foto e salva a imagem resultante.
     *
     * @param fotoPath    Caminho absoluto da foto original
     * @param frameIndex  Índice da moldura (1, 2 ou 3)
     * @return            Caminho da imagem com moldura gerada
     */
    public String aplicarMoldura(String fotoPath, int frameIndex) throws IOException {
        if (frameIndex < 1 || frameIndex > 3) {
            throw new IllegalArgumentException("frameIndex deve ser 1, 2 ou 3. Recebido: " + frameIndex);
        }

        File fotoFile = new File(fotoPath);
        if (!fotoFile.exists()) {
            throw new IOException("Foto não encontrada: " + fotoPath);
        }

        BufferedImage foto = ImageIO.read(fotoFile);
        if (foto == null) {
            throw new IOException("Não foi possível ler a imagem: " + fotoPath);
        }

        // Carrega a moldura do classpath (funciona dentro e fora do JAR)
        String frameFileName = "MolduraCaracol" + (frameIndex == 1 ? "" : String.valueOf(frameIndex)) + ".png";
        BufferedImage moldura = carregarMoldura(frameFileName);
        if (moldura == null) {
            throw new IOException("Moldura não encontrada ou inválida: " + frameFileName);
        }

        // Redimensiona a moldura para o tamanho da foto
        int fotoW = foto.getWidth();
        int fotoH = foto.getHeight();
        BufferedImage molduraRedimensionada = new BufferedImage(fotoW, fotoH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gMoldura = molduraRedimensionada.createGraphics();
        gMoldura.drawImage(moldura, 0, 0, fotoW, fotoH, null);
        gMoldura.dispose();

        // Sobrepoe moldura na foto
        BufferedImage resultado = new BufferedImage(fotoW, fotoH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resultado.createGraphics();
        g.drawImage(foto, 0, 0, null);
        g.drawImage(molduraRedimensionada, 0, 0, null);
        g.dispose();

        // Gera arquivo de saída
        String nomeOriginal = fotoFile.getName();
        String nomeSemExt = nomeOriginal.contains(".")
                ? nomeOriginal.substring(0, nomeOriginal.lastIndexOf('.'))
                : nomeOriginal;
        String extensao = nomeOriginal.contains(".")
                ? nomeOriginal.substring(nomeOriginal.lastIndexOf('.'))
                : ".png";

        String outputFileName = nomeSemExt + "_frame" + frameIndex + extensao;
        File outputFile = new File(fotoFile.getParent(), outputFileName);
        ImageIO.write(resultado, "PNG", outputFile);

        return outputFile.getAbsolutePath();
    }

    private BufferedImage carregarMoldura(String fileName) {
        // Tenta carregar do classpath (dentro do JAR)
        try {
            ClassPathResource resource = new ClassPathResource(FRAMES_CLASSPATH + fileName);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return ImageIO.read(is);
                }
            }
        } catch (IOException e) {
            // Fallback para sistema de arquivos
        }

        // Fallback: tenta diretório src/main/resources/static/frames/ (dev)
        try {
            File file = new File("src/main/resources/static/frames/" + fileName);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}