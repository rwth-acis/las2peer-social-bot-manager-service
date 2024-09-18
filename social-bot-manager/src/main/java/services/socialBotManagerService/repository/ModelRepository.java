package services.socialBotManagerService.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import services.socialBotManagerService.model.Model;

public interface ModelRepository extends JpaRepository<Model, Long> {

    @Query(value = "SELECT m FROM Model m WHERE m.name = :name")
    Model findModelByName(String name);
}
