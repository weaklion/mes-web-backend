package com.example.mesweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class MesWebBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MesWebBackendApplication.class, args);
	}

}
