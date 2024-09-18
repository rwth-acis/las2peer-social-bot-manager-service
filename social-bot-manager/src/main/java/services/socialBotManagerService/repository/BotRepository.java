package services.socialBotManagerService.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import services.socialBotManagerService.model.Bot;;

public interface BotRepository extends MongoRepository<Bot, String> {

    @Query(value = "SELECT b FROM Bot b WHERE b.name = :name")
    Bot findBotByName(String name);
}
