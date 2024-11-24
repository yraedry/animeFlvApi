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
public class InformacionEpisodioAnimeDto {
    private String nombre;
    private String episodio;
    private List<InformacionEpisodioAnimeDto.EnlaceEpisodiosDto> enlacesVisualizacion = new ArrayList<>();
    private List<InformacionEpisodioAnimeDto.EnlaceEpisodiosDto> enlacesDescarga = new ArrayList<>();


    public InformacionEpisodioAnimeDto(String message) {
    }

    // Métodos para agregar episodios a la lista
    public void addEnlaceVisualizacion(String servidor, String url) {
        this.enlacesVisualizacion.add(new EnlaceEpisodiosDto(servidor, url));
    }

    public void addEnlaceDescarga(String servidor, String url) {
        this.enlacesDescarga.add(new EnlaceEpisodiosDto(servidor, url));
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnlaceEpisodiosDto{
        private String servidor;
        private String url;
    }
}
