package services.socialBotManagerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "services.socialBotManagerService")
@EnableJpaRepositories(basePackages = "services.socialBotManagerService.repository.jpa")
@EnableMongoRepositories(basePackages = "services.socialBotManagerService.repository.mongo")
public class SocialBotManagerServiceApplication {
    public static void main(String[] args) {
		System.setProperty("server.servlet.context-path", "/SBFManager");
		SpringApplication.run(SocialBotManagerServiceApplication.class, args);
	}

}
