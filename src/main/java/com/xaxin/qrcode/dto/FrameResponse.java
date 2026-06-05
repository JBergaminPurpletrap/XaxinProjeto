package com.xaxin.qrcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta ao aplicar moldura")
public class FrameResponse {
    private String mensagem;
    private int frame;
    private String arquivo;
    private String url;

    public FrameResponse() {}

    public FrameResponse(String mensagem, int frame, String arquivo, String url) {
        this.mensagem = mensagem;
        this.frame = frame;
        this.arquivo = arquivo;
        this.url = url;
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public int getFrame() { return frame; }
    public void setFrame(int frame) { this.frame = frame; }
    public String getArquivo() { return arquivo; }
    public void setArquivo(String arquivo) { this.arquivo = arquivo; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
