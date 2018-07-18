package i5.las2peer.services.socialBotManagerService;

import java.util.HashMap;

public class BotConfiguration {
	private HashMap<String, BotServiceConfiguration> services;

	public BotConfiguration() {
		services = new HashMap<String, BotServiceConfiguration>();
	}

	public HashMap<String, BotServiceConfiguration> getServices() {
		return services;
	}

	public void setServiceConfiguration(HashMap<String, BotServiceConfiguration> services) {
		this.services = services;
	}

	public void addServiceConfiguration(String key, BotServiceConfiguration config) {
		this.services.put(key, config);
	}

	public BotServiceConfiguration getServiceConfiguration(String key) {
		return this.services.get(key);
	}
}
