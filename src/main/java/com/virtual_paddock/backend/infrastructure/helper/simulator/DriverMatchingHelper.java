package com.virtual_paddock.backend.infrastructure.helper.simulator;

import com.virtual_paddock.backend.domain.entities.Driver;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DriverMatchingHelper {

    private DriverMatchingHelper() {}

    /**
     * Empareja un piloto del log con la base de datos por Gamertag, Nombre o Dorsal con normalización.
     */
    public static Driver matchDriver(String name, String gamertag, String carNumber, List<Driver> allDrivers) {
        if (allDrivers == null || allDrivers.isEmpty()) return null;

        // 1. Coincidencia por Gamertag explícito
        if (gamertag != null && !gamertag.trim().isEmpty()) {
            String normTag = normalizeString(gamertag);
            Optional<Driver> byTag = allDrivers.stream()
                    .filter(d -> d.getGamertag() != null && normalizeString(d.getGamertag()).equals(normTag))
                    .findFirst();
            if (byTag.isPresent()) return byTag.get();
        }

        // 2. Coincidencia por Nombre/Nick del log del simulador contra el GAMERTAG registrado en la BD
        if (name != null && !name.trim().isEmpty()) {
            String normName = normalizeString(name);
            Optional<Driver> byGamertag = allDrivers.stream()
                    .filter(d -> {
                        if (d.getGamertag() == null || d.getGamertag().trim().isEmpty()) return false;
                        String dbTag = normalizeString(d.getGamertag());
                        return dbTag.equals(normName) || dbTag.contains(normName) || normName.contains(dbTag);
                    })
                    .findFirst();
            if (byGamertag.isPresent()) return byGamertag.get();
        }

        // 3. Coincidencia por Nombre registrado en la BD (normalizado, sin acentos ni guiones bajos)
        if (name != null && !name.trim().isEmpty()) {
            String normName = normalizeString(name);
            Optional<Driver> byName = allDrivers.stream()
                    .filter(d -> {
                        if (d.getName() == null) return false;
                        String dbNorm = normalizeString(d.getName());
                        return dbNorm.equals(normName)
                                || dbNorm.contains(normName)
                                || normName.contains(dbNorm);
                    })
                    .findFirst();
            if (byName.isPresent()) return byName.get();
        }

        // 4. Coincidencia por Dorsal (#)
        if (carNumber != null && !carNumber.trim().isEmpty()) {
            String cleanNum = carNumber.replaceAll("[^0-9]", "");
            if (!cleanNum.isEmpty()) {
                Optional<Driver> byNum = allDrivers.stream()
                        .filter(d -> {
                            if (d.getCarNumber() == null) return false;
                            String dbClean = d.getCarNumber().replaceAll("[^0-9]", "");
                            return !dbClean.isEmpty() && (cleanNum.equals(dbClean) || (tryParseInt(cleanNum) != null && Objects.equals(tryParseInt(cleanNum), tryParseInt(dbClean))));
                        })
                        .findFirst();
                if (byNum.isPresent()) return byNum.get();
            }
        }

        return null;
    }

    /**
     * Normaliza un string removiendo tildes, caracteres especiales, guiones bajos y convirtiendo a minúsculas.
     */
    public static String normalizeString(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", ""); // Remueve marcas diacríticas (acentos, tildes)
        normalized = normalized.replace("_", " ").replace("-", " ");
        return normalized.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private static Integer tryParseInt(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }
}
