package com.dacs.quanlyhocvien;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class QuanlyhocvienApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuanlyhocvienApplication.class, args);
	}
	@Bean
	public CommandLineRunner showEndpoints(RequestMappingHandlerMapping mapping) {
		return args -> mapping.getHandlerMethods().forEach((k, v) -> {
			System.out.println(k + " -> " + v.getMethod().getName());
		});
	}
}
