package com.xaxin.qrcode.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do álbum do usuário")
public class AlbumResponse {
    private Long id;
    private String username;
    private boolean routeComplete;
    private int quantidadeFotos;
    private boolean podeAdicionar;
    private List<FotoDto> fotos;

    public AlbumResponse() {}

    public AlbumResponse(Long id, String username, boolean routeComplete, int quantidadeFotos, boolean podeAdicionar, List<FotoDto> fotos) {
        this.id = id;
        this.username = username;
        this.routeComplete = routeComplete;
        this.quantidadeFotos = quantidadeFotos;
        this.podeAdicionar = podeAdicionar;
        this.fotos = fotos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isRouteComplete() { return routeComplete; }
    public void setRouteComplete(boolean routeComplete) { this.routeComplete = routeComplete; }
    public int getQuantidadeFotos() { return quantidadeFotos; }
    public void setQuantidadeFotos(int quantidadeFotos) { this.quantidadeFotos = quantidadeFotos; }
    public boolean isPodeAdicionar() { return podeAdicionar; }
    public void setPodeAdicionar(boolean podeAdicionar) { this.podeAdicionar = podeAdicionar; }
    public List<FotoDto> getFotos() { return fotos; }
    public void setFotos(List<FotoDto> fotos) { this.fotos = fotos; }
}
