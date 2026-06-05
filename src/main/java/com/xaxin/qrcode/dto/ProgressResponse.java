package com.xaxin.qrcode.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Progresso dos checkpoints")
public class ProgressResponse {
    private int total;
    private long lidos;
    private boolean completo;
    private List<CheckpointItem> checkpoints;

    public ProgressResponse() {}

    public ProgressResponse(int total, long lidos, boolean completo, List<CheckpointItem> checkpoints) {
        this.total = total;
        this.lidos = lidos;
        this.completo = completo;
        this.checkpoints = checkpoints;
    }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public long getLidos() { return lidos; }
    public void setLidos(long lidos) { this.lidos = lidos; }
    public boolean isCompleto() { return completo; }
    public void setCompleto(boolean completo) { this.completo = completo; }
    public List<CheckpointItem> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<CheckpointItem> checkpoints) { this.checkpoints = checkpoints; }
}
