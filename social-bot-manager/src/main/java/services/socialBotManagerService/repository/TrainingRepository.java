package services.socialBotManagerService.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import services.socialBotManagerService.model.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query(value = "SELECT t FROM Training t WHERE t.name = :name")
    Training findTrainingByName(String name);
    
}
