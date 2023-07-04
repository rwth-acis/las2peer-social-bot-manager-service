package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;

public class BotConfiguration {
	private HashMap<String, Bot> bots;

	public BotConfiguration() {
		bots = new HashMap<String, Bot>();
	}

	public HashMap<String, Bot> getBots() {
		return this.bots;
	}

	public void setBotConfiguration(HashMap<String, Bot> bots) {
		this.bots = bots;
	}

	public void addBot(String key, Bot bot) {
		this.bots.put(key, bot);
	}

	public void removeBot(String key) {
		this.bots.remove(key);
	}

	public Bot getBot(String key) {
		return this.bots.get(key);
	}
}
