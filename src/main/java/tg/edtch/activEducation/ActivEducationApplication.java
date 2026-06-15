package tg.edtch.activEducation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ActivEducationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActivEducationApplication.class, args);
	}

}
