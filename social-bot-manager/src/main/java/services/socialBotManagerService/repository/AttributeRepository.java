package services.socialBotManagerService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import services.socialBotManagerService.model.Attributes;

public interface AttributeRepository extends JpaRepository<Attributes, Long> {
    
}
