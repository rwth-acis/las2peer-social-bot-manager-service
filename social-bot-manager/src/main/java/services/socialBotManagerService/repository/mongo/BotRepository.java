package services.socialBotManagerService.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import services.socialBotManagerService.model.Bot;

@Repository
public interface BotRepository extends MongoRepository<Bot,String> {
    
    // @Query("{ 'name' : ?0 }")
    Bot findBotByName(String name);

    Bot findBotById(String id);

    Bot deleteBotById(String id);
}