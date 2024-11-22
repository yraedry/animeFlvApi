package com.apinojutsu.component.commons;

import com.microsoft.playwright.*;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class PlaywrightManagerComponent {
    private Playwright playwright;
    private BrowserContext persistentContext;
    private static final String USER_DATA_DIR = "user-data-apinojutsu"; //

    /**
     * Inicializa Playwright con un contexto persistente.
     */
    public void initializePersistentContent() {
        if (playwright == null) {
            playwright = Playwright.create();
            persistentContext = playwright.chromium().launchPersistentContext(
                    Paths.get(USER_DATA_DIR),
                    new BrowserType.LaunchPersistentContextOptions().setHeadless(true)
            );
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
        return persistentContext.newPage();
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

        // Elimina el directorio de datos persistentes
        try {
            Files.walk(Paths.get(USER_DATA_DIR))
                    .map(java.nio.file.Path::toFile)
                    .forEach(java.io.File::delete);
            Files.deleteIfExists(Paths.get(USER_DATA_DIR));
            System.out.println("Sesiones persistentes limpiadas correctamente.");
        } catch (IOException e) {
            System.err.println("Error al limpiar las sesiones persistentes: " + e.getMessage());
        }
    }
}