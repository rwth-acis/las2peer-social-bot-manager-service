package services.socialBotManagerService.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

import services.socialBotManagerService.model.Model;

public interface ModelRepository extends JpaRepository<Model, UUID> {

    @Query(value = "SELECT m FROM Model m WHERE m.name = :name")
    Model findModelByName(@Param("name") String name);
}
