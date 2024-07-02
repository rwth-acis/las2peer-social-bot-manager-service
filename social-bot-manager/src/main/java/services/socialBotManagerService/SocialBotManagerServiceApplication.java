package services.socialBotManagerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocialBotManagerServiceApplication {
    public static void main(String[] args) {
		System.setProperty("server.servlet.context-path", "/SBFManager");
		SpringApplication.run(SocialBotManagerServiceApplication.class, args);
	}
}
