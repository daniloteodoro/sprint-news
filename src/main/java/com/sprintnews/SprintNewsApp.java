package com.sprintnews;

import kong.unirest.Unirest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author daniloteodoro
 */

@SpringBootApplication
@EnableJpaRepositories("com.sprintnews.infrastructure.atlassian.repository")
public class SprintNewsApp {
	public static void main(String[] args) {
		Unirest.config().addShutdownHook(true);
		SpringApplication.run(SprintNewsApp.class, args);
	}
}
