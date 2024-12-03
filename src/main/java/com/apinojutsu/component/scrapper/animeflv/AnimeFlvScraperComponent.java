package com.apinojutsu.component.scrapper.animeflv;

import com.apinojutsu.component.commons.PlaywrightManagerComponent;
import com.apinojutsu.dto.InformacionAnimeDto;
import com.apinojutsu.dto.InformacionEpisodioAnimeDto;
import com.apinojutsu.dto.NovedadesAnimeFlvDto;
import com.apinojutsu.dto.NovedadesEpisodiosAnimeFlvDto;
import com.apinojutsu.utils.MessageUtils;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
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

    @Value("${web.animeflv.anime-url}")
    private String animeUrl;

    @Value("${web.animeflv.episode-url}")
    private String episodeUrl;
    /**
     * Realiza el login en AnimeFLV utilizando Playwright.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return Mapa con el estado del login y las cookies si es exitoso.
     */
    public Map<String, String> login(String username, String password) {
        Map<String, String> responseMap = new HashMap<>();
        Page page = playwrightManager.getPage();
        try {
                // Navegar al login de AnimeFLV
                page.navigate(loginUrl);
                // Completar formulario de login si no estamos ya logueados
            if(page.url().equalsIgnoreCase(loginUrl)){
                page.fill("input[name='email']", username);
                page.fill("input[name='password']", password);

                // Enviar formulario
                page.click("button[type='submit']");
                if (page.url().equals(homeUrl)) {
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
            String episodeTitle = episode.select(".Title").text();
            String urlEpisode = homeUrl + episode.select("a").attr("href");
            String episodeNumber = episode.select(".Capi").text();
            String coverAnime = homeUrl + episode.select("img").attr("src");
            episodes.add(new NovedadesEpisodiosAnimeFlvDto(episodeTitle, urlEpisode, episodeNumber, coverAnime));
        }

        return episodes;
    }

    /**
     * Obtiene los últimos animes agregados.
     */
    public List<NovedadesAnimeFlvDto> obtenerUltimasNovedades() throws IOException {
        List<NovedadesAnimeFlvDto> animes = new ArrayList<>();
        Document doc = Jsoup.connect(homeUrl).get();

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

    public InformacionAnimeDto obtenerInformacionAnime(String anime) throws IOException {
        InformacionAnimeDto animeInformation = new InformacionAnimeDto();
        // Levantamos un navegador headless
        try (Page page = playwrightManager.getPage()) {
            page.navigate(animeUrl + anime);

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

    public InformacionEpisodioAnimeDto obtenerUrlsVisualizacionEpisodioAnime(String episodeAnimeName) throws IOException {
        InformacionEpisodioAnimeDto visualizationUrls = new InformacionEpisodioAnimeDto();
        // Inicializamos el navegador headless
        playwrightManager.initializePersistentContent();
        try (Page page = playwrightManager.getPage()) {
            // Navegamos a la URL
            page.navigate(episodeUrl + episodeAnimeName);
            visualizationUrls.setNombre(page.querySelector("h1.Title").innerText().replaceAll("Episodio \\d+", "").trim());
            visualizationUrls.setEpisodio(page.querySelector("h2.SubTitle").innerText());

            // Esperar hasta que el contenido principal este cargado ya que se carga via javascript
            page.waitForSelector("#video_box");
            Locator ulOpciones = page.locator("ul.CapiTnv.nav.nav-pills");
            ulOpciones.waitFor();

            // Localizar los <li> dentro del ul
            Locator opciones = ulOpciones.locator("li");

            // Iterar sobre cada opción
            for (int i = 0; i < opciones.count(); i++) {
                // Vamos cambiando entre las diferentes opciones para obtener los enlaces
                opciones.nth(i).click();

                // Obtenemos lo enlaces de visualizacion
                Locator iframeLocator = page.locator("#video_box iframe");
                if (iframeLocator.count() > 0) {
                    String dataOriginalTitle = opciones.nth(i).getAttribute("data-original-title");
                    String iframeSrc = iframeLocator.first().getAttribute("src");
                    if (iframeSrc != null && !iframeSrc.isEmpty()) {
                        visualizationUrls.addEnlaceVisualizacion(dataOriginalTitle, iframeSrc);
                    }
                }
            }
        } finally {
            // Cerramos el navegador al finalizar
            playwrightManager.closeBrowser();
        }

        return visualizationUrls;
        }

    public InformacionEpisodioAnimeDto obtenerUrlsDescargaEpisodioAnime(String episodeAnimeName) throws IOException {
        InformacionEpisodioAnimeDto visualizationUrls = new InformacionEpisodioAnimeDto();
        // Inicializamos el navegador headless
        playwrightManager.initializePersistentContent();
        try (Page page = playwrightManager.getPage()) {
            // Navegamos a la URL
            page.navigate(episodeUrl + episodeAnimeName);

            visualizationUrls.setNombre(page.querySelector("h1.Title").innerText().replaceAll("Episodio \\d+", "").trim());
            visualizationUrls.setEpisodio(page.querySelector("h2.SubTitle").innerText());

            //obtenemos los enlace de descarga estaticos
            Locator tableDownloadOptions = page.locator("table.RTbl.Dwnl tbody tr");
            for (int i = 0; i < tableDownloadOptions.count(); i++) {
                Locator fila = tableDownloadOptions.nth(i);
                String servidor = fila.locator("td:nth-child(1)").textContent();
                String enlace = fila.locator("td:nth-child(4) a").getAttribute("href");
                visualizationUrls.addEnlaceDescarga(servidor,enlace);
            }
        } finally {
            // Cerramos el navegador al finalizar
            playwrightManager.closeBrowser();
        }

        return visualizationUrls;
    }
}
