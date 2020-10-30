package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import i5.las2peer.services.socialBotManagerService.chat.TelegramChatMediator;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import net.minidev.json.JSONObject;

public class VLE {
	private String name;
	private String address;

	private HashMap<String, VLEUser> users;
	private HashMap<String, Bot> bots;
	private HashMap<String, Bot> slackBots;
	
	private String environmentSeparator = "";

	private HashMap<String, JSONObject> serviceInformation;

	private HashMap<Trigger, HashSet<String>> triggerList;
	private HashMap<String, VLERoutine> routines;

	public VLE() {
		setBots(new HashMap<String, Bot>());
		setSlackBots(new HashMap<String, Bot>());
		setUsers(new HashMap<String, VLEUser>());
		setServiceInformation(new HashMap<String, JSONObject>());
		setTriggerList(new HashMap<Trigger, HashSet<String>>());
		setRoutines(new HashMap<String, VLERoutine>());
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public HashMap<String, VLEUser> getUsers() {
		return this.users;
	}

	public void setUsers(HashMap<String, VLEUser> users) {
		this.users = users;
	}

	public void addUser(String name, VLEUser user) {
		this.users.put(name, user);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEnvironmentSeparator() {
		return environmentSeparator;
	}

	public void setEnvironmentSeparator(String environmentSeparator) {
		this.environmentSeparator = environmentSeparator;
	}

	public HashMap<String, JSONObject> getServiceInformation() {
		return serviceInformation;
	}

	public void setServiceInformation(HashMap<String, JSONObject> serviceInformation) {
		this.serviceInformation = serviceInformation;
	}

	public void addServiceInformation(String name, JSONObject info) {
		this.serviceInformation.put(name, info);
	}

	public HashMap<String, Bot> getBots() {
		return bots;
	}

	public void setBots(HashMap<String, Bot> bots) {
		this.bots = bots;
	}

	public void addBot(String id, Bot bot) {
		this.bots.put(id, bot);
	}

	public HashMap<Trigger, HashSet<String>> getTriggerList() {
		return triggerList;
	}

	public void setTriggerList(HashMap<Trigger, HashSet<String>> triggerList) {
		this.triggerList = triggerList;
	}

	public void addTrigger(Trigger t, String f) {
		HashSet<String> l = this.triggerList.get(t);
		if (l == null) {
			l = new HashSet<String>();
			l.add(f);
			this.triggerList.put(t, l);
		} else {
			l.add(f);
		}
	}

	public HashMap<String, VLERoutine> getRoutines() {
		return routines;
	}

	public void setRoutines(HashMap<String, VLERoutine> routines) {
		this.routines = routines;
	}

	public void addRoutine(String name, VLERoutine routine) {
		this.routines.put(name, routine);
	}
	
	public void addBotbySlackID(String appID, String teamID, Bot bot) {
		this.slackBots.put(teamID.concat(appID), bot);
	}
	
	public Bot getBotbySlackID(String appID, String teamID) {
		return this.slackBots.get(teamID.concat(appID));		
	}
	
	public HashMap<String, Bot> getSlackBotMap() {
		return this.slackBots;
	}
	
	private void setSlackBots(HashMap<String, Bot> hashMap) {
		this.slackBots = hashMap;		
	}
	
	public Bot getBotbyTelegramToken(String token) {
		for(Bot bot: bots.values()) {
			TelegramChatMediator mediator = (TelegramChatMediator) bot.getMessenger(ChatService.TELEGRAM).getChatMediator();
			if(mediator.hasToken(token))
				return bot;
		}
		return null;
	}

    public Collection<LanguageUnderstander> getNLUs() {

	Collection<LanguageUnderstander> res = new HashSet<>();
	for (Entry<String, Bot> entry : this.bots.entrySet()) {
	    Bot bot = entry.getValue();
	    for (Entry<String, LanguageUnderstander> nlu : bot.getNLUs().entrySet()) {
		res.add(nlu.getValue());
	    }
	}

	return res;

    }
}
