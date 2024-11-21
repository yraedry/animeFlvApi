package com.apinojutsu.component.animeflv.scrapper;

import com.apinojutsu.component.commons.PlaywrightManagerComponent;
import com.apinojutsu.dto.InformacionAnimeDto;
import com.apinojutsu.dto.NovedadesAnimeFlvDto;
import com.apinojutsu.dto.NovedadesEpisodiosAnimeFlvDto;
import com.apinojutsu.utils.MessageUtils;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class AnimeFlvScraperComponent {

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private PlaywrightManagerComponent playwrightManager;

    @Value("${web.animeflv.login-url}")
    private String loginUrl;

    @Value("${web.animeflv.home-url}")
    private String homeUrl;

    /**
     * Realiza el login en AnimeFLV utilizando Playwright.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return Mapa con el estado del login y las cookies si es exitoso.
     */
    public Map<String, String> login(String username, String password) {
        Map<String, String> responseMap = new HashMap<>();
        playwrightManager.initializeBrowser();
        Page page = playwrightManager.getPage();

        try {
            // Navegar al login de AnimeFLV
            page.navigate(loginUrl);

            // Completar formulario de login
            page.fill("input[name='email']", username);
            page.fill("input[name='password']", password);

            // Enviar formulario
            page.click("button[type='submit']");
            String currentUrl = page.url();
            if (currentUrl.equals(homeUrl)){
                responseMap.put("status", "success");
                // Extraer cookies
                page.context().cookies().forEach(cookie -> responseMap.put(cookie.name, cookie.value));
            }
        } catch (Exception e) {
            responseMap.put("status", "error");
            responseMap.put("message", "An error occurred: " + e.getMessage());
        } finally {
            playwrightManager.closeBrowser();
        }
        return responseMap;
    }

    /**
     * Obtiene los ultimos episodios agregados.
     */
    public List<NovedadesEpisodiosAnimeFlvDto> obtenerUltimosEpisodiosNovedades(Map<String, String> cookies) throws IOException {
        if (cookies == null) {
            throw new IllegalStateException(messageUtils.getMessage("error.session.notfound"));
        }
        List<NovedadesEpisodiosAnimeFlvDto> episodios = new ArrayList<>();
        Document doc = Jsoup.connect(homeUrl).get();

        // Busca los elementos que contienen los ultimos episodios
        Elements episodioElements = doc.select(".ListEpisodios li");

        for (Element episodio : episodioElements) {
            String tituloEpisodio = episodio.select(".Title").text();
            String urlEpisodio = homeUrl + episodio.select("a").attr("href");
            episodios.add(new NovedadesEpisodiosAnimeFlvDto(tituloEpisodio, urlEpisodio));
        }

        return episodios;
    }

    /**
     * Obtiene los últimos animes agregados.
     */
    public List<NovedadesAnimeFlvDto> obtenerUltimasNovedades(Map<String, String> cookies) throws IOException {
        if (cookies == null) {
            throw new IllegalStateException(messageUtils.getMessage("error.session.notfound"));
        }
        List<NovedadesAnimeFlvDto> animes = new ArrayList<>();
        Document doc = Jsoup.connect(homeUrl + "/browse").get();

        // Busca los elementos que contienen los ultimos animes agregados
        Elements animeElements = doc.select(".ListAnimes li");
        Set<String> titulosUnicos = new HashSet<>();
        for (Element anime : animeElements) {
            String title = anime.select(".Title").first().text();
            String animeLink = homeUrl + anime.select("a").attr("href");
            if (titulosUnicos.add(title)) {
                animes.add(new NovedadesAnimeFlvDto(title, animeLink));
            }
        }
        return animes;
    }

    public InformacionAnimeDto obtenerInformacionAnime(Map<String, String> cookies, String animeUrl) throws IOException {
        if (cookies == null) {
            throw new IllegalStateException(messageUtils.getMessage("error.session.notfound"));
        }
        InformacionAnimeDto informacionAnime = new InformacionAnimeDto();
        // Levantamos un navegador headless
        playwrightManager.initializeBrowser();
        try (Page page = playwrightManager.getPage()) {
            page.navigate(animeUrl);

            String titulo = page.querySelector("h1.Title").innerText();
            String urlCaraturla = homeUrl + page.querySelector(".Image img").getAttribute("src");
            String sinopsis = page.querySelector(".Description p").innerText();
            String estado = page.querySelector(".AnmStts span").innerText();
            informacionAnime.setNombre(titulo);
            informacionAnime.setUrlCaratula(urlCaraturla);
            informacionAnime.setSinopsis(sinopsis);
            informacionAnime.setEstado(estado);
            page.waitForSelector("ul.ListCaps li");

            List<ElementHandle> episodiosElementos = page.querySelectorAll("ul.ListCaps li");
            for (ElementHandle elemento : episodiosElementos) {
                //realizamos esto para saltarnos el primer registro
                if (elemento.querySelector("p") == null) {
                    String proximoEpisodio = elemento.querySelector("span").innerText();
                    informacionAnime.setProximoEpisodio(proximoEpisodio);
                    continue;
                }
                String episodio = elemento.querySelector("p").innerText();
                String urlEpisodio = homeUrl + elemento.querySelector("a").getAttribute("href");
                informacionAnime.addEpisodio(episodio, urlEpisodio);
            }
        } finally {
            // Cierra la pagina despues de completar el scraping
            playwrightManager.closeBrowser();
        }

        return informacionAnime;
    }
}
