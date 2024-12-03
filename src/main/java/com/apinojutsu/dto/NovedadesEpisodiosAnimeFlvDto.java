package com.apinojutsu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NovedadesEpisodiosAnimeFlvDto {
    private String titulo;
    private String url;
    private String capitulo;
    private String caratula;
}
