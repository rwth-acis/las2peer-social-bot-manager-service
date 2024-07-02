package services.socialBotManagerService.model;

public class IntentEntity {
	String entityKeyword;

	public IntentEntity(String entity) {
		this.entityKeyword = entity;
	}

	public String getEntityKeyword() {
		return this.entityKeyword;
	}
}
