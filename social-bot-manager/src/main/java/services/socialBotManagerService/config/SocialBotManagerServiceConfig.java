package services.socialBotManagerService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class SocialBotManagerServiceConfig {
    
    @Value("${spring.data.mongodb.uri}")
    private String connString;

    /*
    * Use the standard Mongo driver API to create a com.mongodb.client.MongoClient instance.
    */
    public @Bean MongoClient mongoClient() {
        return MongoClients.create(connString);
    }
}
