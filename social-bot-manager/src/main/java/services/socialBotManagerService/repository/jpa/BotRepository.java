package services.socialBotManagerService.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import services.socialBotManagerService.model.Bot;

@Repository
public interface BotRepository extends JpaRepository<Bot,String> {
    Bot findBotByName(String name);

    Bot findBotById(String id);

    Bot deleteBotById(String id);
}