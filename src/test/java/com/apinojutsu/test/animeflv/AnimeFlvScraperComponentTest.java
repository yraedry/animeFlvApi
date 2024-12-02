package com.apinojutsu.test.animeflv;

import com.apinojutsu.component.scrapper.animeflv.AnimeFlvScraperComponent;
import com.apinojutsu.component.commons.PlaywrightManagerComponent;
import com.apinojutsu.dto.InformacionAnimeDto;
import com.apinojutsu.dto.NovedadesAnimeFlvDto;
import com.apinojutsu.dto.NovedadesEpisodiosAnimeFlvDto;
import com.microsoft.playwright.*;
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

    @Test
    public void testObtenerUltimosAnimesNovedades() throws IOException {
        // Simular el documento HTML de Jsoup
        Document mockDocument = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        Element mockElement = mock(Element.class);
        Element mockTitleElement = mock(Element.class);

        // Configurar mocks para los elementos del DOM
        when(mockElement.select(".Title")).thenReturn(mockElements);
        when(mockElements.first()).thenReturn(mockTitleElement); // Simular el primer elemento
        when(mockTitleElement.text()).thenReturn("Anime 1"); // Simular el texto del título
        when(mockElement.select("a")).thenReturn(mockElements);
        when(mockElements.attr("href")).thenReturn("/anime-1");

        // Configurar la iteración de los elementos
        when(mockElements.iterator()).thenReturn(List.of(mockElement).iterator());
        when(mockDocument.select(".ListAnimes li")).thenReturn(mockElements);

        // Mockear Connection y Jsoup.connect()
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(mockDocument);

            // Ejecutar el metodo con cookies válidas
            List<NovedadesAnimeFlvDto> novedadesAnime = scraperComponent.obtenerUltimasNovedades();

            // Verificar los resultados
            assertNotNull(novedadesAnime);
            assertEquals(1, novedadesAnime.size());
            assertEquals("Anime 1", novedadesAnime.get(0).getTitulo());
            assertEquals(HOME_URL_TEST + "/anime-1", novedadesAnime.get(0).getUrl());
        }
    }

    @Test
    void testObtenerInformacionAnime() throws IOException {
        // Crear mocks para Playwright y sus componentes
        Page mockPage = mock(Page.class);
        ElementHandle mockTitleElement = mock(ElementHandle.class);
        ElementHandle mockCoverElement = mock(ElementHandle.class);
        ElementHandle mockSynopsisElement = mock(ElementHandle.class);
        ElementHandle mockStateElement = mock(ElementHandle.class);
        ElementHandle mockEpisodeElement = mock(ElementHandle.class);
        ElementHandle mockNextEpisodeElement = mock(ElementHandle.class);

        when(playwrightManager.getPage()).thenReturn(mockPage); // Retorna un mock de Page

        // Mockear comportamiento de elementos de la pagina
        when(mockPage.querySelector("h1.Title")).thenReturn(mockTitleElement);
        when(mockTitleElement.innerText()).thenReturn("Titulo Anime");

        when(mockPage.querySelector(".Image img")).thenReturn(mockCoverElement);
        when(mockCoverElement.getAttribute("src")).thenReturn("/images/anime-cover.jpg");

        when(mockPage.querySelector(".Description p")).thenReturn(mockSynopsisElement);
        when(mockSynopsisElement.innerText()).thenReturn("Esto es una descripcion del anime.");

        when(mockPage.querySelector(".AnmStts span")).thenReturn(mockStateElement);
        when(mockStateElement.innerText()).thenReturn("Terminado");

        when(mockPage.querySelectorAll("ul.ListCaps li")).thenReturn(List.of(mockEpisodeElement, mockNextEpisodeElement));

        // Configurar episodios
        when(mockEpisodeElement.querySelector("p")).thenReturn(mockEpisodeElement);
        when(mockEpisodeElement.innerText()).thenReturn("episodio 1");
        when(mockEpisodeElement.querySelector("a")).thenReturn(mockEpisodeElement);
        when(mockEpisodeElement.getAttribute("href")).thenReturn("/episodio/1");

        // Configurar proximo episodio
        when(mockNextEpisodeElement.querySelector("p")).thenReturn(null);
        when(mockNextEpisodeElement.querySelector("span")).thenReturn(mockNextEpisodeElement);
        when(mockNextEpisodeElement.innerText()).thenReturn("proximo episodio");

        // Ejecutar el metodo a probar
        scraperComponent = new AnimeFlvScraperComponent();
        ReflectionTestUtils.setField(scraperComponent, "playwrightManager", playwrightManager);
        ReflectionTestUtils.setField(scraperComponent, "homeUrl", "https://mock-home-url.com");

        InformacionAnimeDto animeInfo = scraperComponent.obtenerInformacionAnime("https://mock-anime-url.com");

        // Verificar resultados
        assertNotNull(animeInfo);
        assertEquals("Titulo Anime", animeInfo.getNombre());
        assertEquals("https://mock-home-url.com/images/anime-cover.jpg", animeInfo.getUrlCaratula());
        assertEquals("Esto es una descripcion del anime.", animeInfo.getSinopsis());
        assertEquals("Terminado", animeInfo.getEstado());
        assertEquals("proximo episodio", animeInfo.getProximoEpisodio());
        assertEquals(1, animeInfo.getMetadata().size());
        assertEquals("episodio 1", animeInfo.getMetadata().get(0).getEpisodio());
        assertEquals("https://mock-home-url.com/episodio/1", animeInfo.getMetadata().get(0).getUrl());

        // Verificar interaccion con Playwright
        verify(mockPage, times(1)).navigate("https://mock-anime-url.com");
        verify(mockPage, times(1)).close();
    }
}