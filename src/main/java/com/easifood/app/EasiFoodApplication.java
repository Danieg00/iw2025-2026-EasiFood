package com.easifood.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class})

public class EasiFoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasiFoodApplication.class, args);
	}

}
