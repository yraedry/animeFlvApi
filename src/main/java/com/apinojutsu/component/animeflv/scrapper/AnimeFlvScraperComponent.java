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
        playwrightManager.initializePersistentContent();
        Page page = playwrightManager.getPage();
        try {
            boolean activeSession = playwrightManager.isSessionActive(homeUrl, "div[class='Login Online']");
            if(!activeSession) {

                // Navegar al login de AnimeFLV
                page.navigate(loginUrl);

                // Completar formulario de login
                page.fill("input[name='email']", username);
                page.fill("input[name='password']", password);

                // Enviar formulario
                page.click("button[type='submit']");
                String currentUrl = page.url();
                if (currentUrl.equals(homeUrl)) {
                    responseMap.put("status", "success");
                }
            }else{
                responseMap.put("status", "activo");
            }
        } catch (Exception e) {
            responseMap.put("status", "error");
            responseMap.put("message", "An error occurred: " + e.getMessage());
        } finally {
            playwrightManager.closeBrowser();
        }
        return responseMap;
    }

    public boolean logout() {
        playwrightManager.initializePersistentContent();

        try (Page page = playwrightManager.getPage()) {
            // Navegar a la pagina de deslogueo
            page.navigate(homeUrl + "/auth/sign_out");

            // Verificar que se redirigio a la pagina principal
            return page.url().equals(homeUrl);

        } catch (Exception e) {
            return false; // Error durante el proceso de logout
        } finally {
            playwrightManager.closeBrowser();
        }
    }

    /**
     * Obtiene los ultimos episodios agregados.
     */
    public List<NovedadesEpisodiosAnimeFlvDto> obtenerUltimosEpisodiosNovedades() throws IOException {
        List<NovedadesEpisodiosAnimeFlvDto> episodes = new ArrayList<>();
        Document doc = Jsoup.connect(homeUrl).get();

        // Busca los elementos que contienen los ultimos episodios
        Elements episodeElements = doc.select(".ListEpisodios li");

        for (Element episode : episodeElements) {
            String tituloEpisodio = episode.select(".Title").text();
            String urlEpisodio = homeUrl + episode.select("a").attr("href");
            episodes.add(new NovedadesEpisodiosAnimeFlvDto(tituloEpisodio, urlEpisodio));
        }

        return episodes;
    }

    /**
     * Obtiene los últimos animes agregados.
     */
    public List<NovedadesAnimeFlvDto> obtenerUltimasNovedades() throws IOException {
        List<NovedadesAnimeFlvDto> animes = new ArrayList<>();
        Document doc = Jsoup.connect(homeUrl + "/browse").get();

        // Busca los elementos que contienen los ultimos animes agregados
        Elements animeElements = doc.select(".ListAnimes li");
        Set<String> uniqueTitles = new HashSet<>();
        for (Element anime : animeElements) {
            String title = anime.select(".Title").first().text();
            String animeLink = homeUrl + anime.select("a").attr("href");
            if (uniqueTitles.add(title)) {
                animes.add(new NovedadesAnimeFlvDto(title, animeLink));
            }
        }
        return animes;
    }

    public InformacionAnimeDto obtenerInformacionAnime(String animeUrl) throws IOException {
        InformacionAnimeDto animeInformation = new InformacionAnimeDto();
        // Levantamos un navegador headless
        playwrightManager.initializePersistentContent();
        try (Page page = playwrightManager.getPage()) {
            page.navigate(animeUrl);

            String title = page.querySelector("h1.Title").innerText();
            String coverUrl = homeUrl + page.querySelector(".Image img").getAttribute("src");
            String synopsis = page.querySelector(".Description p").innerText();
            String state = page.querySelector(".AnmStts span").innerText();
            animeInformation.setNombre(title);
            animeInformation.setUrlCaratula(coverUrl);
            animeInformation.setSinopsis(synopsis);
            animeInformation.setEstado(state);
            page.waitForSelector("ul.ListCaps li");

            List<ElementHandle> episodesElements = page.querySelectorAll("ul.ListCaps li");
            for (ElementHandle element : episodesElements) {
                //realizamos esto para saltarnos el primer registro
                if (element.querySelector("p") == null) {
                    String nextEpisode = element.querySelector("span").innerText();
                    animeInformation.setProximoEpisodio(nextEpisode);
                    continue;
                }
                String episode = element.querySelector("p").innerText();
                String urlEpisode = homeUrl + element.querySelector("a").getAttribute("href");
                animeInformation.addEpisodio(episode, urlEpisode);
            }
        } finally {
            // Cierra la pagina despues de completar el scraping
            playwrightManager.closeBrowser();
        }
        return animeInformation;
    }
}
