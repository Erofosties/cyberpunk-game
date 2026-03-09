package com.cyberpunk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CyberpunkServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CyberpunkServerApplication.class, args);
	}

}
