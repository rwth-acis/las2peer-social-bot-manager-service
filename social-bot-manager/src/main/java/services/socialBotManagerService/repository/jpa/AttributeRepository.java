package services.socialBotManagerService.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import services.socialBotManagerService.model.Attributes;

public interface AttributeRepository extends JpaRepository<Attributes, Long> {
    @Query(value = "SELECT id FROM Attributes at WHERE at.bot = :bot AND at.channel= :channel AND at.username = :username AND at.key = :key")
    Attributes findIdByBot(@Param("bot") String bot, @Param("channel") String channel, @Param("username")String username, @Param("key") String key);   

    @Query(value = "SELECT value FROM Attributes at WHERE at.channel= :channel AND at.key = :key")
    String findValueByBot(@Param("channel") String channel, @Param("key") String key);
}
