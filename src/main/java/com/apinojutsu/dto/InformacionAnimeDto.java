package com.apinojutsu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InformacionAnimeDto {
    private String nombre;
    private String urlCaratula;
    private String sinopsis;
    private String estado;
    private String proximoEpisodio;
    private List<EpisodiosMetadataDto> metadata = new ArrayList<>();

    public InformacionAnimeDto(String message) {
    }

    // Métodos para agregar episodios a la lista
    public void addEpisodio(String episodio, String url) {
        this.metadata.add(new EpisodiosMetadataDto(episodio, url));
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodiosMetadataDto{
        private String episodio;
        private String url;
    }
}
