package io.github.raphaelmun1z.ChatGepeteco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChatGepetecoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatGepetecoApplication.class, args);
	}

}
