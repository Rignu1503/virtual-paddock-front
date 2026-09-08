package com.virtual_paddock.backend.infrastructure.config;

import com.virtual_paddock.backend.domain.entities.User;
import com.virtual_paddock.backend.domain.repositories.UserRepository;
import com.virtual_paddock.backend.utils.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${application.security.superadmin.email}")
    private String superadminEmail;

    @Value("${application.security.superadmin.password}")
    private String superadminPassword;

    @Override
    public void run(String... args) {
        // Migraciones idempotentes de columnas en race_events
        modifyColumn("race_events", "date", "DATETIME(6)");
        addColumnIfNotExists("race_events", "race_duration", "VARCHAR(50) DEFAULT '45 Min'");
        addColumnIfNotExists("race_events", "qualy_duration", "VARCHAR(50) DEFAULT '15 Min'");
        addColumnIfNotExists("race_events", "qualy_date", "DATETIME(6)");

        // Índices estratégicos para optimización de consultas recurrentes
        addIndexIfNotExists("race_events", "idx_race_events_season", "season_id");
        addIndexIfNotExists("race_events", "idx_race_events_status", "status");
        addIndexIfNotExists("race_results", "idx_race_results_event", "race_event_id");
        addIndexIfNotExists("race_results", "idx_race_results_driver", "driver_id");
        addIndexIfNotExists("sanctions", "idx_sanctions_event", "race_event_id");
        addIndexIfNotExists("seasons", "idx_seasons_championship", "championship_id");
        addIndexIfNotExists("championships", "idx_championships_league", "league_id");
        addIndexIfNotExists("leagues", "idx_leagues_slug", "slug_url");
        addIndexIfNotExists("drivers", "idx_drivers_league", "league_id");
        addIndexIfNotExists("teams", "idx_teams_league", "league_id");

        if (!userRepository.existsByEmail(superadminEmail)) {
            log.info("Inicializando cuenta de SUPERADMIN por defecto: {}", superadminEmail);
            User superadmin = User.builder()
                    .email(superadminEmail)
                    .password(passwordEncoder.encode(superadminPassword))
                    .role(Role.SUPERADMIN)
                    .build();
            userRepository.save(superadmin);
            log.info("SUPERADMIN inicializado exitosamente.");
        } else {
            log.info("La cuenta de SUPERADMIN ({}) ya existe.", superadminEmail);
        }
    }

    private void addColumnIfNotExists(String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class,
                    table,
                    column
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute(String.format("ALTER TABLE %s ADD COLUMN %s %s", table, column, definition));
                log.info("Columna añadida exitosamente: {}.{}", table, column);
            }
        } catch (Exception e) {
            log.debug("Aviso verificación columna {}.{}: {}", table, column, e.getMessage());
        }
    }

    private void modifyColumn(String table, String column, String newDefinition) {
        try {
            jdbcTemplate.execute(String.format("ALTER TABLE %s MODIFY COLUMN %s %s", table, column, newDefinition));
            log.debug("Esquema columna {}.{} verificado como {}", table, column, newDefinition);
        } catch (Exception e) {
            log.debug("Aviso modificación {}.{}: {}", table, column, e.getMessage());
        }
    }

    private void addIndexIfNotExists(String table, String indexName, String columns) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                    Integer.class,
                    table,
                    indexName
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute(String.format("CREATE INDEX %s ON %s (%s)", indexName, table, columns));
                log.info("Índice de base de datos creado exitosamente: {} en {}({})", indexName, table, columns);
            }
        } catch (Exception e) {
            log.debug("Aviso verificación índice {}.{}: {}", table, indexName, e.getMessage());
        }
    }
}
