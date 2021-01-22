package com.piaand.co2emissions;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Co2EmissionsApplication {

	private final LogConfigurations configurations;

	public Co2EmissionsApplication(LogConfigurations configurations) {
		this.configurations = configurations;
	}

	public static void main(String[] args) {
		SpringApplication.run(Co2EmissionsApplication.class, args);
	}

	@Bean
	CommandLineRunner runner() {
		return args -> {
			configurations.setConfig();
			System.out.println("Hello World!");
		};
	}

}
