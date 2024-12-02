package com.apinojutsu.component.commons;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PlaywrightManagerComponent {
    @Value("${bloqueadores.path.easylist}")
    private String easyListPath;

    private Playwright playwright;
    private BrowserContext persistentContext;
    private static final String USER_DATA_DIR = System.getProperty("user.dir") + "/user-data-apinojutsu"; //// Directorio de listas de bloqueo
    private final Set<String> dominiosBloqueados = new HashSet<>();
    /**
     * Inicializa Playwright con un contexto persistente.
     */


    public void initializePersistentContent() {
        if (playwright == null) {
            playwright = Playwright.create();
            if (dominiosBloqueados.isEmpty()) {
                loadBlocklist();
            }
            persistentContext = playwright.chromium().launchPersistentContext(
                    Paths.get(USER_DATA_DIR),
                    new BrowserType.LaunchPersistentContextOptions()
                            .setHeadless(true)
                            .setArgs(List.of(
                                    "--disable-popup-blocking",
                                    "--disable-dev-shm-usage",
                                    "--no-sandbox",
                                    "--disable-extensions",
                                    "--disable-sync",
                                    "--disable-background-networking"
                            ))
            );
            // Configurar bloqueo de publicidad
            persistentContext.route("**/*", route -> {
                String url = route.request().url();
                if (isBlocked(url)) {
                    route.abort(); // Bloquea la solicitud
                } else {
                    route.resume(); // Permite la solicitud
                }
            });
        }
    }

    /**
     * Obtiene una nueva página del contexto persistente.
     *
     * @return Page
     */
    public Page getPage() {
        if (persistentContext == null) {
            initializePersistentContent();
        }
        // Reutiliza la pestaña abierta por defecto si existe
        if (!persistentContext.pages().isEmpty()) {
            return persistentContext.pages().get(0);
        }
        return persistentContext.newPage();
    }

    public BrowserContext getContext() {
        return persistentContext;
    }

    public void addCookiesContext(List<Cookie> cookies) {
        persistentContext.addCookies(cookies);
    }

    public boolean isSessionActive(String validationUrl, String loginSelector) {
        try {
            initializePersistentContent();

            // Abrir una página y navegar a la URL de validación
            Page page = getPage();
            page.navigate(validationUrl);

            // Comprobar si el selector de logout (o similar) está presente
            boolean isLogged = page.locator(loginSelector).isVisible();
            page.close();

            return isLogged;
        } catch (Exception e) {
            System.err.println("Error al verificar la sesión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cierra el navegador y libera los recursos.
     */
    public void closeBrowser() {
        if (persistentContext != null) {
            persistentContext.close();
            persistentContext = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    /**
     * Metodo llamado antes de que el bean sea destruido, limpia las sesiones persistentes.
     */
    @PreDestroy
    public void cleanUpPersistentData() {
        closeBrowser(); // Cierra el navegador si está abierto

        Path userDataPath = Paths.get(USER_DATA_DIR);
        // Verificar si el directorio existe
        if (!Files.exists(userDataPath)) {
            System.out.println("El directorio de datos persistentes no existe. No hay nada que limpiar.");
            return;
        }

        // Eliminar archivos y directorios
        try {
            Files.walk(userDataPath)
                    .sorted(Comparator.reverseOrder()) // Asegura que los archivos se eliminen antes que los directorios
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("No se pudo eliminar el archivo o directorio: " + file.getAbsolutePath());
                        }
                    });
            Files.deleteIfExists(userDataPath);
            System.out.println("Sesiones persistentes limpiadas correctamente.");
        } catch (IOException e) {
            System.err.println("Error al limpiar las sesiones persistentes: " + e.getMessage());
        }
    }

    /**
     * Carga la lista de bloqueo desde un archivo y la almacena en un HashSet.
     */
    private void loadBlocklist() {
        Resource resource = new ClassPathResource(easyListPath); // Ruta al archivo
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            System.out.println("Cargando EasyList en HashSet...");
            reader.lines()
                    .filter(line -> !line.startsWith("#") && !line.trim().isEmpty()) // Excluye comentarios y líneas vacías
                    .forEach(dominiosBloqueados::add); // Añade cada dominio al HashSet
            System.out.println("EasyList cargada con " + dominiosBloqueados.size() + " dominios.");
        } catch (IOException e) {
            System.err.println("Error al cargar la lista de bloqueo: " + e.getMessage());
        }
    }

    /**
     * Verifica si un dominio está bloqueado utilizando el HashSet.
     *
     * @param dominio URL o dominio a verificar.
     * @return true si está bloqueado, false de lo contrario.
     */
    private boolean isBlocked(String dominio) {
        return dominiosBloqueados.stream().anyMatch(dominio::contains);
    }

}