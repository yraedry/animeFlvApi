package com.apinojutsu.component.commons;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SessionManagerComponent {
    private final Map<String, SessionData> sessions = new HashMap<>();
    private static final long SESSION_TIMEOUT = 3600000; // 1 hora

    public void addSession(String username, Map<String, String> cookies) {
        sessions.put(username, new SessionData(cookies, System.currentTimeMillis()));
    }

    public Map<String, String> getSessionCookies(String username) {
        SessionData session = sessions.get(username);
        if (session != null && (System.currentTimeMillis() - session.lastAccess) < SESSION_TIMEOUT) {
            session.lastAccess = System.currentTimeMillis(); // Renueva el tiempo de acceso
            return session.cookies;
        }
        removeSession(username);
        return null; // Sesión caducada
    }

    public void removeSession(String username) {
        sessions.remove(username);
    }

    public boolean isSessionActive(String username) {
        return sessions.containsKey(username);
    }

    protected static class SessionData {
        Map<String, String> cookies;
        long lastAccess;

        SessionData(Map<String, String> cookies, long lastAccess) {
            this.cookies = cookies;
            this.lastAccess = lastAccess;
        }
    }
}
