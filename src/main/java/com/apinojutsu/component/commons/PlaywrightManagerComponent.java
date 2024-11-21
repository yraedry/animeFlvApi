package com.apinojutsu.component.commons;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Component;

@Component
public class PlaywrightManagerComponent {
    private Playwright playwright;
    private Browser browser;

    /**
     * Inicializa Playwright y el navegador en modo headless.
     */
    public void initializeBrowser() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        }
    }

    /**
     * Retorna una nueva pagina del navegador.
     *
     * @return Page (pagina Playwright)
     */
    public Page getPage() {
        if (browser == null) {
            throw new IllegalStateException("El navegador no esta inicializado. Llama a initializeBrowser() primero.");
        }
        return browser.newPage();
    }

    /**
     * Cierra el navegador y libera los recursos.
     */
    public void closeBrowser() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }
}
