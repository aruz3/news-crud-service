package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class RefreshRequestDto {

    @NotNull
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}