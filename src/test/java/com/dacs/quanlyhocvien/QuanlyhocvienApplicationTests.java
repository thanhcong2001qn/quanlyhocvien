package com.dacs.quanlyhocvien;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuanlyhocvienApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("=== TEST ENV ===");
		System.out.println("SPRING_DATASOURCE_URL = " + System.getenv("SPRING_DATASOURCE_URL"));
		System.out.println("SPRING_DATASOURCE_USERNAME = " + System.getenv("SPRING_DATASOURCE_USERNAME"));
		System.out.println("SPRING_DATASOURCE_PASSWORD = " + System.getenv("SPRING_DATASOURCE_PASSWORD"));
	}
}

