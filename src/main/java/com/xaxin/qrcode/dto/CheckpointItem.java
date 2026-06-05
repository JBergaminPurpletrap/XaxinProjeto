package com.xaxin.qrcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item de checkpoint com status lido")
public class CheckpointItem {
    private int ordem;
    private String codigo;
    private String titulo;
    private boolean lido;

    public CheckpointItem() {}

    public CheckpointItem(int ordem, String codigo, String titulo, boolean lido) {
        this.ordem = ordem;
        this.codigo = codigo;
        this.titulo = titulo;
        this.lido = lido;
    }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public boolean isLido() { return lido; }
    public void setLido(boolean lido) { this.lido = lido; }
}
