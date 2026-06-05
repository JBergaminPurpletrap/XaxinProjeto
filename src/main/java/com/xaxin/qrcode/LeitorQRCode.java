package com.xaxin.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

/**
 * Lê um QR de uma imagem, extrai um número inteiro EXATO (ex.: "1234")
 * e salva no PostgreSQL.
 */
public class LeitorQRCode {

// --------- Ajuste estas configurações ---------
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sua_base";
    private static final String DB_USER = "seu_usuario";
    private static final String DB_PASSWORD = "sua_senha";

    private static final String INSERT_SQL = "INSERT INTO qr_numbers (numero) VALUES (?)";

    /**
     * Le o QR de uma imagem e retorna o número inteiro.
     * O QR deve conter somente um número (sem espaços e sem texto adicional).
     */
    public static int readQrNumberFromImage(File imageFile) throws IOException {
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

        // Regra: o QR deve retornar um número exato, como "1234".
        // (permitimos sinal opcional e sem outros caracteres)
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
     * Salva o número no PostgreSQL.
     */
    public static void saveNumberToPostgres(int numero) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, numero);
            ps.executeUpdate();
        }
    }

    /**
     * Cria tabela (caso ainda não exista).
     * Opcional: execute antes do uso se você não tiver a tabela criada.
     */
    public static void ensureTableExists() throws SQLException {
        String ddl = "CREATE TABLE IF NOT EXISTS qr_numbers ("
                + " id SERIAL PRIMARY KEY,"
                + " numero INTEGER NOT NULL"
                + ")";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
        }
    }

    /**
     * Prompt/entrada principal.
     * Uso:
     *   java LeitorQRCode "c:\caminho\para\qr.png"
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java LeitorQRCode <caminho_para_imagem_de_qr>\nEx.: java LeitorQRCode C:\\qr.png");
            System.exit(1);
        }

        File imageFile = new File(args[0]);

        try {
            // Crie a tabela uma vez (opcional). Pode comentar se já existe.
            ensureTableExists();

            int numero = readQrNumberFromImage(imageFile);
            System.out.println("Número lido do QR: " + numero);

            saveNumberToPostgres(numero);
            System.out.println("Número salvo no PostgreSQL com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}

