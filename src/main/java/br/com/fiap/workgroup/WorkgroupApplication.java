package br.com.fiap.workgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WorkgroupApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkgroupApplication.class, args);
	}

}
