package com.apinojutsu.test.animeflv;

import com.apinojutsu.components.animeflv.scrapper.AnimeFlvScraperComponent;
import com.apinojutsu.controllers.AnimeFlvController;
import com.apinojutsu.dto.LoginAnimeFlvDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Map;


@WebMvcTest(AnimeFlvController.class)
public class AnimeFlvControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnimeFlvScraperComponent animeFlvScraperComponent;

    @Test
    public void testLogin() throws Exception {
        LoginAnimeFlvDto mockResponse = new LoginAnimeFlvDto("usuario", "Login exitoso", null);
        Mockito.when(animeFlvScraperComponent.login(Mockito.anyString(), Mockito.anyString())).thenReturn((Map<String, String>) mockResponse);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"usuario\",\"password\":\"contraseña\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("usuario"))
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"));
    }
}
