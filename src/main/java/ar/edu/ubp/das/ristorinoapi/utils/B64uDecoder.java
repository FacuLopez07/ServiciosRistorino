package ar.edu.ubp.das.ristorinoapi.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad para decodificar valores codificados en Base64 URL-safe sin padding ("b64u")
 * tal como los envía el frontend:
 *  btoa(str).replace('+','-').replace('/','_').replace(/=+$/,'')
 *
 * Métodos de conveniencia para convertir a Integer y Boolean con manejo de errores.
 */
public final class B64uDecoder {

    private B64uDecoder() {}

    /**
     * Restaura caracteres y padding eliminados para que el decoder estándar pueda procesar la cadena.
     */
    private static String normalize(String s) {
        String restored = s.replace('-', '+').replace('_', '/');
        int mod = restored.length() % 4;
        if (mod != 0) {
            restored = restored + "=".repeat(4 - mod);
        }
        return restored;
    }

    /**
     * Decodifica una cadena b64u y devuelve el texto original.
     * @param encoded valor codificado (no debe ser null).
     * @return texto decodificado.
     * @throws IllegalArgumentException si el formato es inválido o null.
     */
    public static String decode(String encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("Valor codificado nulo");
        }
        try {
            String normalized = normalize(encoded);
            byte[] decoded = Base64.getDecoder().decode(normalized);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Formato b64u inválido: " + encoded, ex);
        }
    }

    /**
     * Decodifica y parsea a Integer.
     * @param encoded valor codificado.
     * @return Integer resultante.
     */
    public static Integer decodeToInt(String encoded) {
        String raw = decode(encoded);
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("No es un número válido: " + raw, ex);
        }
    }

    /**
     * Decodifica y parsea a Boolean (case-insensitive de "true"/"false").
     * @param encoded valor codificado.
     * @return Boolean.
     */
    public static Boolean decodeToBoolean(String encoded) {
        String raw = decode(encoded);
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return Boolean.valueOf(raw);
        }
        throw new IllegalArgumentException("No es un booleano válido: " + raw);
    }
}
