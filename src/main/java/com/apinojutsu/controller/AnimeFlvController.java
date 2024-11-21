package com.apinojutsu.controller;

import com.apinojutsu.component.animeflv.scrapper.AnimeFlvScraperComponent;
import com.apinojutsu.component.commons.SessionManagerComponent;
import com.apinojutsu.dto.InformacionAnimeDto;
import com.apinojutsu.dto.NovedadesAnimeFlvDto;
import com.apinojutsu.dto.NovedadesEpisodiosAnimeFlvDto;
import com.apinojutsu.dto.LoginAnimeFlvDto;
import com.apinojutsu.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animeflv")
public class AnimeFlvController {

    private final AnimeFlvScraperComponent animeFlvScraper;
    private final SessionManagerComponent sessionManager;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    public AnimeFlvController(AnimeFlvScraperComponent animeFlvScraper, SessionManagerComponent sessionManager) {
        this.animeFlvScraper = animeFlvScraper;
        this.sessionManager= sessionManager;
    }

    @PostMapping("/login")
    public LoginAnimeFlvDto login(@RequestParam String username, @RequestParam String password) {
        try {
            // Realiza el login y guarda las cookies en la sesion
            Map<String, String> cookies = animeFlvScraper.login(username, password);
            if (cookies != null && !cookies.isEmpty()) {
                if (cookies.get("status").equalsIgnoreCase("success")) {
                    sessionManager.addSession(username, cookies);
                    return new LoginAnimeFlvDto(cookies.get("status"), username, messageUtils.getMessage("animeflv.login.correcto", username), cookies);
                }
            }
            return new LoginAnimeFlvDto("error", username, messageUtils.getMessage("animeflv.login.fallo", username), null);
        } catch (Exception e) {
            return new LoginAnimeFlvDto("error", username,messageUtils.getMessage("animeflv.login.fallo", username),null);
        }
    }

    @GetMapping("/novedades-episodios")
    public List<NovedadesEpisodiosAnimeFlvDto> getNovedadesEpisodios(@RequestParam String username) {
        try {
            if (!sessionManager.isSessionActive(username)) {
                return List.of(new NovedadesEpisodiosAnimeFlvDto(
                        messageUtils.getMessage("error.session.notfound"), null
                ));
            }
            // Recupera las cookies de la sesion y obtiene la lista de episodios
            Map<String, String> cookies = sessionManager.getSessionCookies(username);
            return animeFlvScraper.obtenerUltimosEpisodiosNovedades(cookies);
        } catch (IOException e) {
            e.printStackTrace();
            return List.of(new NovedadesEpisodiosAnimeFlvDto(
                    messageUtils.getMessage("animeflv.episodios.error"), null
            ));
        }
    }

    @GetMapping("/novedades-animes")
    public List<NovedadesAnimeFlvDto> obtenerNovedadesAnime(@RequestParam String username) {
        try {
            if (!sessionManager.isSessionActive(username)) {
                return List.of(new NovedadesAnimeFlvDto(
                        messageUtils.getMessage("error.session.notfound"), null
                ));
            }
            // Recupera las cookies de la sesion y obtiene la pagina de series
            Map<String, String> cookies = sessionManager.getSessionCookies(username);
            return animeFlvScraper.obtenerUltimasNovedades(cookies);
        } catch (IOException e) {
            return List.of(new NovedadesAnimeFlvDto(
                    messageUtils.getMessage("animeflv.animes.error"), null
            ));
        }
    }

    @GetMapping("/obtener-anime")
    public InformacionAnimeDto obtenerInformacionAnime(@RequestParam String username, String animeUrl) {
        try {
            if (!sessionManager.isSessionActive(username)) {
                return new InformacionAnimeDto(messageUtils.getMessage("error.session.notfound"));
            }
            // Recupera las cookies de la sesion y obtiene la pagina de series
            Map<String, String> cookies = sessionManager.getSessionCookies(username);
            return animeFlvScraper.obtenerInformacionAnime(cookies, animeUrl);
        } catch (IOException e) {
            return new InformacionAnimeDto(messageUtils.getMessage("error.session.notfound"));
        }
    }

    @PostMapping("/logout")
    public LoginAnimeFlvDto logout(@RequestParam String username) {
        if (sessionManager.isSessionActive(username)) {
            sessionManager.removeSession(username);
            return new LoginAnimeFlvDto(username,messageUtils.getMessage("animeflv.logout.correcto", username) ,null, null);
        } else {
            return new LoginAnimeFlvDto("error", username,messageUtils.getMessage("error.session.notfound"), null);
        }
    }
}
