package com.animeflv.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginAnimeFlvDto {
    private String usuario;
    private String mensaje;
    private Map<String, String> cookies;
}
