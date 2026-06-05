package com.xaxin.qrcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de upload de foto")
public class UploadResponse {
    private String mensagem;
    private Long id;
    private String fileName;
    private String url;

    public UploadResponse() {}

    public UploadResponse(String mensagem, Long id, String fileName, String url) {
        this.mensagem = mensagem;
        this.id = id;
        this.fileName = fileName;
        this.url = url;
    }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
