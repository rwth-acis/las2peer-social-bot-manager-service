package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;

public class BotConfiguration {
	private HashMap<String, VLE> vles;

	public BotConfiguration() {
		vles = new HashMap<String, VLE>();
	}

	public HashMap<String, VLE> getVLEs() {
		return this.vles;
	}

	public void setServiceConfiguration(HashMap<String, VLE> vles) {
		this.vles = vles;
	}

	public void addServiceConfiguration(String key, VLE vle) {
		this.vles.put(key, vle);
	}

	public VLE getServiceConfiguration(String key) {
		return this.vles.get(key);
	}
}
