package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.notification.EventToMessageTrigger;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;

public class BotConfiguration {

	private Map<String, VLE> vles;

	private Map<String, LanguageUnderstander> nlus;

	public BotConfiguration() {
		vles = new HashMap<>();
		nlus = new HashMap<>();
	}

	public Map<String, VLE> getVLEs() {
		return this.vles;
	}

	public void setServiceConfiguration(Map<String, VLE> vles) {
		this.vles = vles;
	}

	public void addServiceConfiguration(String key, VLE vle) {
		this.vles.put(key, vle);
	}

	public VLE getServiceConfiguration(String key) {
		return this.vles.get(key);
	}

	public LanguageUnderstander getNLU(String name) {

		for (LanguageUnderstander lu : this.getNlus().values()) {
			if (lu.getName().contentEquals(name))
				return lu;
		}

		return null;
	}

	public Map<String, LanguageUnderstander> getNlus() {

		for (Entry<String, VLE> vleEntry : this.getVLEs().entrySet()) {
			VLE vle = vleEntry.getValue();
			for (LanguageUnderstander nlu : vle.getNLUs()) {
				this.nlus.put(nlu.getUrl(), nlu);
			}
		}

		return nlus;
	}

	public void setNlus(Map<String, LanguageUnderstander> nlus) {
		this.nlus = nlus;
	}

	public void addNLU(LanguageUnderstander nlu) {
		this.nlus.put(nlu.getUrl(), nlu);
	}

	public Collection<Bot> getActiveBots() {

		Collection<Bot> bots = new HashSet<>();
		for (VLE vle : this.vles.values()) {
			bots.addAll(vle.getBots().values());
		}

		return bots;

	}

}
