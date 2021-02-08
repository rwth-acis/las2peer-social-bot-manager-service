package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.parser.BotModelInfo;

public class BotConfiguration {

	private Map<String, VLE> vles;

	private Map<String, LanguageUnderstander> nlus;

	private BotModelInfo botModelInfo;

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
		assert name != null;

		for (LanguageUnderstander lu : this.getNLUs().values()) {
			if (lu.getName().contentEquals(name))
				return lu;
		}

		return null;
	}

	public Map<String, LanguageUnderstander> getNLUs() {

		Map<String, LanguageUnderstander> res = new HashMap<>();
		res.putAll(this.nlus);
		for (Entry<String, VLE> vleEntry : this.getVLEs().entrySet()) {
			VLE vle = vleEntry.getValue();
			for (LanguageUnderstander nlu : vle.getNLUs()) {
				res.put(nlu.getUrl(), nlu);
			}
		}

		return res;
	}

	public void removeNLU(String name) {
		
		LanguageUnderstander nlu = this.getNLU(name);
		if(nlu != null && this.nlus != null)
			this.nlus.remove(nlu.getUrl());
		
	}

	public void setNLUs(Map<String, LanguageUnderstander> nlus) {
		this.nlus = nlus;
	}

	public void addNLU(LanguageUnderstander nlu) {
		this.nlus.put(nlu.getUrl(), nlu);
	}

	public BotModelInfo getBotModelInfo() {
		return botModelInfo;
	}

	public void setBotModelInfo(BotModelInfo botModelInfo) {
		this.botModelInfo = botModelInfo;
	}

	public Collection<Bot> getActiveBots() {

		Collection<Bot> bots = new HashSet<>();
		for (VLE vle : this.vles.values()) {
			bots.addAll(vle.getBots().values());
		}

		return bots;
	}

	public VLE getVLEofBot(String botName) {

		if (this.vles == null)
			return null;

		for (VLE vle : this.vles.values()) {
			Bot bot = vle.getBotByName(botName);
			if (bot != null)
				return vle;
		}
		System.out.println("no vle for " + botName + " found");
		return null;

	}

}
