package com.xaxin.qrcode.service;

import com.xaxin.qrcode.model.QrNumber;
import com.xaxin.qrcode.repository.QrNumberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.LuminanceSource;

@Service
public class QrCodeService {

    private final QrNumberRepository repository;

    public QrCodeService(QrNumberRepository repository) {
        this.repository = repository;
    }

    /**
     * Lê um número inteiro de uma imagem de QR Code.
     */
    public int readQrNumberFromImage(File imageFile) throws IOException {
        if (imageFile == null) throw new IllegalArgumentException("imageFile não pode ser null");
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new IOException("Arquivo de imagem não encontrado: " + imageFile.getAbsolutePath());
        }

        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Não foi possível ler a imagem (null): " + imageFile.getAbsolutePath());
        }

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        MultiFormatReader reader = new MultiFormatReader();
        Result result;
        try {
            result = reader.decode(bitmap);
        } catch (NotFoundException e) {
            throw new IllegalStateException("QR code não encontrado na imagem: " + imageFile.getAbsolutePath(), e);
        }

        String text = result.getText();
        if (text == null) {
            throw new IllegalStateException("QR code retornou texto nulo");
        }

        text = text.trim();

        if (!text.matches("-?\\d+")) {
            throw new IllegalStateException("QR code não contém um número exato. Conteúdo: '" + text + "'");
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Número no QR está fora do range de int. Conteúdo: '" + text + "'", e);
        }
    }

    /**
     * Salva o número lido no banco de dados via JPA.
     */
    @Transactional
    public QrNumber saveNumber(int numero) {
        QrNumber qrNumber = new QrNumber(numero);
        return repository.save(qrNumber);
    }
}