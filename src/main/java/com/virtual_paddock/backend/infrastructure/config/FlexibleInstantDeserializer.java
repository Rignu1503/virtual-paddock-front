package com.virtual_paddock.backend.infrastructure.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Deserializador flexible para Instant que soporta:
 * 1. Strings ISO con indicador de zona ("2026-03-25T20:00:00Z" o "2026-03-25T20:00:00+02:00")
 * 2. Strings LocalDateTime sin zona ("2026-03-25T20:00:00", asumiendo UTC)
 * 3. Strings de fecha legacy ("2026-03-25", asumiendo inicio del día en UTC)
 */
public class FlexibleInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        text = text.trim();

        // 1. Intentar como Instant estándar (ISO con zona/offset)
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {
        }

        // 2. Intentar como LocalDateTime (sin indicador de zona)
        try {
            LocalDateTime ldt = LocalDateTime.parse(text);
            return ldt.atZone(ZoneId.of("UTC")).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        // 3. Intentar como LocalDate (solo fecha YYYY-MM-DD)
        try {
            LocalDate ld = LocalDate.parse(text);
            return ld.atStartOfDay(ZoneId.of("UTC")).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        // Si no coincide con ningún formato conocido, delegar a excepción descriptiva
        throw new IllegalArgumentException("No se pudo interpretar el formato de fecha/hora para Instant: " + text);
    }
}
