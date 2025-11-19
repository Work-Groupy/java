package br.com.fiap.workgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport
@SpringBootApplication
@EnableCaching
public class WorkgroupApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkgroupApplication.class, args);
	}

}
