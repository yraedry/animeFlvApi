package com.apinojutsu.test.animeflv;

import com.apinojutsu.component.animeflv.scrapper.AnimeFlvScraperComponent;
import com.apinojutsu.component.commons.PlaywrightManagerComponent;
import com.apinojutsu.dto.NovedadesEpisodiosAnimeFlvDto;
import com.microsoft.playwright.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class AnimeFlvScraperComponentTest {

    private final String LOGIN_URL_TEST = "https://mock-login-url";
    private final String HOME_URL_TEST = "https://mock-home-url";

    @Mock
    private PlaywrightManagerComponent playwrightManager;

    @InjectMocks
    private AnimeFlvScraperComponent scraperComponent;


    @BeforeEach
    public void setUp() {
        // Asignar valores a las variables privadas con ReflectionTestUtils
        ReflectionTestUtils.setField(scraperComponent, "loginUrl", LOGIN_URL_TEST);
        ReflectionTestUtils.setField(scraperComponent, "homeUrl", HOME_URL_TEST);
    }


    @Test
    public void testLoginSuccess() {
        // Mockear la pagina
        Page mockPage = mock(Page.class);

        // Configurar mocks
        when(playwrightManager.getPage()).thenReturn(mockPage); // Retorna un mock de Page

        // Configurar retorno de URL para simular redireccion
        when(mockPage.url()).thenReturn(HOME_URL_TEST);

        // Ejecutar el metodo
        Map<String, String> response = scraperComponent.login("user", "password");
        verify(mockPage).navigate(anyString());
        verify(mockPage).fill(eq("input[name='email']"), eq("user"));
        verify(mockPage).fill(eq("input[name='password']"), eq("password"));
        verify(mockPage).click(anyString());

        // Verificar la respuesta
        assertNotNull(response);
        assertEquals("success", response.get("status"));
    }

    @Test
    public void testLoginFailure() {
        // Mockear la página y el contexto
        Page mockPage = mock(Page.class);

        // Configurar mocks
        when(playwrightManager.getPage()).thenReturn(mockPage);
        when(mockPage.url()).thenReturn(LOGIN_URL_TEST); // Simular que no redirige al home

        // Ejecutar el metodo
        Map<String, String> response = scraperComponent.login("user", "wrong-password");

        // Verificar que los métodos `void` fueron llamados
        verify(mockPage).navigate(anyString());
        verify(mockPage).fill(eq("input[name='email']"), eq("user"));
        verify(mockPage).fill(eq("input[name='password']"), eq("wrong-password"));
        verify(mockPage).click(anyString());

        // Verificar la respuesta
        assertNotNull(response);
        assertTrue(response.isEmpty());
    }


    @Test
    public void testObtenerUltimosEpisodiosNovedades() throws IOException {
        // Simular el documento HTML de Jsoup
        Document mockDocument = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        Element mockElement = mock(Element.class);
        Elements titleElements = mock(Elements.class);
        Elements linkElements = mock(Elements.class);

        // Configurar mocks para los elementos del DOM
        when(mockElement.select(".Title")).thenReturn(titleElements);
        when(titleElements.text()).thenReturn("Episode 1");
        when(mockElement.select("a")).thenReturn(linkElements);
        when(linkElements.attr("href")).thenReturn("/episode-1");

        // Configurar la iteracion de los elementos
        when(mockElements.iterator()).thenReturn(List.of(mockElement).iterator());
        when(mockDocument.select(".ListEpisodios li")).thenReturn(mockElements);

        // Mockear Connection y Jsoup.connect()
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);

            // Ejecutar el metodo con cookies validas
            List<NovedadesEpisodiosAnimeFlvDto> episodios = scraperComponent.obtenerUltimosEpisodiosNovedades();

            // Verificar los resultados
            assertNotNull(episodios);
            assertEquals(1, episodios.size());
            assertEquals("Episode 1", episodios.get(0).getTitulo());
            assertEquals(HOME_URL_TEST + "/episode-1", episodios.get(0).getUrl());
        }
    }
}