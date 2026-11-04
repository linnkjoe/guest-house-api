package com.mabjaiainn.proj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProjApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjApplication.class, args);
                System.out.println("Conectado");
        }

}
