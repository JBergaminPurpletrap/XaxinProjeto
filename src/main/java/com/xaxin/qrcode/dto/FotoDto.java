package com.xaxin.qrcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de foto no álbum")
public class FotoDto {
    private Long id;
    private String originalName;
    private String url;
    private String uploadedAt;

    public FotoDto() {}

    public FotoDto(Long id, String originalName, String url, String uploadedAt) {
        this.id = id;
        this.originalName = originalName;
        this.url = url;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
}
