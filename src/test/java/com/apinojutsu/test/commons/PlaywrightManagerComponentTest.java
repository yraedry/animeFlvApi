package com.apinojutsu.test.commons;

import com.apinojutsu.component.commons.PlaywrightManagerComponent;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlaywrightManagerComponentTest {

    private PlaywrightManagerComponent playwrightManager;
    private Playwright mockPlaywright;
    private BrowserType mockBrowserType;
    private BrowserContext mockContext;
    private Page mockPage;

    @BeforeEach
    void setUp() {
        playwrightManager = new PlaywrightManagerComponent();

        // Crear mocks para Playwright y sus componentes
        mockPlaywright = mock(Playwright.class);
        mockBrowserType = mock(BrowserType.class);
        mockContext = mock(BrowserContext.class);
        mockPage = mock(Page.class);
    }

    @Test
    void testInitializePersistentContent() {
        try (MockedStatic<Playwright> mockedPlaywrightStatic = Mockito.mockStatic(Playwright.class)) {
            // Configurar comportamiento de Playwright.create()
            mockedPlaywrightStatic.when(Playwright::create).thenReturn(mockPlaywright);
            when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
            when(mockBrowserType.launchPersistentContext(
                    eq(Paths.get(System.getProperty("user.dir") + "/user-data-apinojutsu")),
                    any(BrowserType.LaunchPersistentContextOptions.class))
            ).thenReturn(mockContext);

            // Ejecutar el metodo a probar
            playwrightManager.initializePersistentContent();

            // Verificar interacciones
            mockedPlaywrightStatic.verify(Playwright::create, times(1));
            verify(mockPlaywright.chromium(), times(1)).launchPersistentContext(
                    eq(Paths.get(System.getProperty("user.dir") + "/user-data-apinojutsu")),
                    any(BrowserType.LaunchPersistentContextOptions.class)
            );
        }
    }

    @Test
    void testGetPage() {
        try (MockedStatic<Playwright> mockedPlaywrightStatic = Mockito.mockStatic(Playwright.class)) {
            // Configurar mocks
            mockedPlaywrightStatic.when(Playwright::create).thenReturn(mockPlaywright);
            when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
            when(mockBrowserType.launchPersistentContext(
                    eq(Paths.get(System.getProperty("user.dir") + "/user-data-apinojutsu")),
                    any(BrowserType.LaunchPersistentContextOptions.class))
            ).thenReturn(mockContext);
            when(mockContext.newPage()).thenReturn(mockPage);

            // Ejecutar el metodo a probar
            playwrightManager.initializePersistentContent();
            Page page = playwrightManager.getPage();

            // Verificar resultados
            assertNotNull(page);
            verify(mockContext, times(1)).newPage();
        }
    }

    @Test
    void testIsSessionActive() {
        try (MockedStatic<Playwright> mockedPlaywrightStatic = Mockito.mockStatic(Playwright.class)) {
            // Configurar mocks
            mockedPlaywrightStatic.when(Playwright::create).thenReturn(mockPlaywright);
            when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
            when(mockBrowserType.launchPersistentContext(
                    eq(Paths.get(System.getProperty("user.dir") + "/user-data-apinojutsu")),
                    any(BrowserType.LaunchPersistentContextOptions.class))
            ).thenReturn(mockContext);
            when(mockContext.newPage()).thenReturn(mockPage);

            Locator mockLocator = mock(Locator.class);
            when(mockPage.locator("#logout")).thenReturn(mockLocator);
            when(mockLocator.isVisible()).thenReturn(true);

            // Ejecutar el metodo a probar
            boolean isActive = playwrightManager.isSessionActive("https://mock-url.com", "#logout");

            // Verificar resultados
            assertTrue(isActive);
            verify(mockPage, times(1)).navigate("https://mock-url.com");
            verify(mockPage.locator("#logout"), times(1)).isVisible();
        }
    }

    @Test
    void testCloseBrowser() {
        try (MockedStatic<Playwright> mockedPlaywrightStatic = Mockito.mockStatic(Playwright.class)) {
            // Configurar mocks
            mockedPlaywrightStatic.when(Playwright::create).thenReturn(mockPlaywright);
            when(mockPlaywright.chromium()).thenReturn(mockBrowserType);
            when(mockBrowserType.launchPersistentContext(
                    eq(Paths.get(System.getProperty("user.dir") + "/user-data-apinojutsu")),
                    any(BrowserType.LaunchPersistentContextOptions.class))
            ).thenReturn(mockContext);

            // Ejecutar el metodo a probar
            playwrightManager.initializePersistentContent();
            playwrightManager.closeBrowser();

            // Verificar resultados
            verify(mockContext, times(1)).close();
            mockedPlaywrightStatic.verify(Playwright::create, times(1));
        }
    }
}