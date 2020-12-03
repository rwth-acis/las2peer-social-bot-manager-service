package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

public enum Language {
	ENGLISH,
	GERMAN;

	public static Language fromString(String value) {
		for(Language la : values()) {
			if(la.toString().equalsIgnoreCase(value))
				return la;
		}
		return null;
	}
}
