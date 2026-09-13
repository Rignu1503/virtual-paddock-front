package com.virtual_paddock.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VirtualPaddockBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualPaddockBackApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner run(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE leagues MODIFY COLUMN logo_url VARCHAR(2048)");
				jdbcTemplate.execute("ALTER TABLE leagues MODIFY COLUMN background_url VARCHAR(2048)");
				System.out.println("====== DB SCHEMA UPDATED SUCCESS ======");
			} catch (Exception e) {
				System.out.println("====== DB SCHEMA UPDATE FAILED OR ALREADY APPLIED ======");
			}
		};
	}
}
