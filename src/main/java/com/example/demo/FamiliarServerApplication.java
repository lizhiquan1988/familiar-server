package com.example.demo;

import com.example.demo.utils.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(JwtUtils.class)
public class FamiliarServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FamiliarServerApplication.class, args);
	}

}
