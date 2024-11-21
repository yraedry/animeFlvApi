package com.apinojutsu.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtils {

    private final MessageSource messageSource;

    @Autowired
    public MessageUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Obtiene un mensaje usando solo la clave y los parámetros opcionales.
     *
     * @param key  Clave del mensaje en el archivo de propiedades.
     * @return Mensaje resuelto con los parámetros.
     */
    public String getMessage(String key) {
        return getMessage(key, new Object[]{});
    }

    /**
     * Obtiene un mensaje usando solo la clave y los parámetros opcionales.
     *
     * @param key    Clave del mensaje en el archivo de propiedades.
     * @param params Parámetros opcionales para el mensaje.
     * @return Mensaje resuelto con los parámetros.
     */
    public String getMessage(String key, Object... params) {
        return messageSource.getMessage(key, params, Locale.getDefault());
    }
}