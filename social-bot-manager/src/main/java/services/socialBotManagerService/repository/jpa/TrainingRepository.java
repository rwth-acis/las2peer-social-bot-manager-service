package services.socialBotManagerService.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import services.socialBotManagerService.model.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query(value = "SELECT t FROM Training t WHERE t.name = :name")
    Training findTrainingByName(@Param("name") String name);

}