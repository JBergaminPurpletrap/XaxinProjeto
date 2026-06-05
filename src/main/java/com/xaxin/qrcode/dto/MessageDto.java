package com.xaxin.qrcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem simples como payload")
public class MessageDto {
    private String text;

    public MessageDto() {}
    public MessageDto(String text) { this.text = text; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
