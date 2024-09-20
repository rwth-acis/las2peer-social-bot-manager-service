package services.socialBotManagerService.model;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.websocket.DeploymentException;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import net.minidev.json.JSONObject;
import services.socialBotManagerService.chat.AuthTokenException;
import services.socialBotManagerService.chat.ChatService;
import services.socialBotManagerService.nlu.RasaNlu;
import services.socialBotManagerService.parser.ParseBotException;

@Entity
@Table(name = "bot")
public class Bot {
	private String name;
	// create uuid for bot id
	@Id
	private String id;
	private String version = "1.0.0";
	private String service;
	private Date created_at;
	private Date updated_at;

	@Field("active")
	private HashMap<String, Boolean> active;

	private HashMap<String, ServiceFunction> botServiceFunctions;
	private HashSet<Trigger> triggerList;

	private HashMap<String, Messenger> messengers;

	// private String botAgent;

	private HashMap<String, RasaNlu> rasaServers;

	private HashMap<String, JSONObject> serviceInformation = new HashMap<String, JSONObject>();
	private String address;
	private HashMap<String, BotRoutine> routines;

	public Bot() {
		botServiceFunctions = new HashMap<String, ServiceFunction>();
		triggerList = new HashSet<Trigger>();
		active = new HashMap<String, Boolean>();
		this.messengers = new HashMap<String, Messenger>();
		this.rasaServers = new HashMap<String, RasaNlu>();
		setRoutines(new HashMap<String, BotRoutine>());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public HashMap<String, ServiceFunction> getBotServiceFunctions() {
		return botServiceFunctions;
	}

	public void setServiceFunctions(HashMap<String, ServiceFunction> serviceFunctions) {
		this.botServiceFunctions = serviceFunctions;
	}

	public void addBotServiceFunction(String name, ServiceFunction serviceFunction) {
		this.botServiceFunctions.put(name, serviceFunction);
	}

	public RasaNlu getRasaServer(String id) {
		return this.rasaServers.get(id);
	}

	public HashMap<String, RasaNlu> getRasaServers() {
		return this.rasaServers;
	}

	public RasaNlu getFirstRasaServer() {
		return (RasaNlu) this.rasaServers.values().toArray()[0];
	}

	public void addRasaServer(String id, String url) {
		this.rasaServers.put(id, new RasaNlu(url));
	}

	public HashSet<Trigger> getTriggerList() {
		return triggerList;
	}

	public void setTriggerList(HashSet<Trigger> triggerList) {
		this.triggerList = triggerList;
	}

	public void addTrigger(Trigger t) {
		this.triggerList.add(t);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public HashMap<String, Boolean> getActive() {
		return active;
	}

	public void setActive(HashMap<String, Boolean> active) {
		this.active = active;
	}

	public void setIdActive(String id, boolean active) {
		this.active.put(id, active);
	}

	public Messenger getMessenger(String name) {
		// TODO: I'm not too sure about thread safety when calling
		// something on this. Might need to make ChatMediator
		// methods synchronized?
		return this.messengers.get(name);
	}

	public Messenger getMessenger(ChatService chatservice) {
		for (Messenger messenger : this.messengers.values()) {
			if (messenger.getChatService() == chatservice)
				return messenger;
		}
		return null;
	}

	public HashMap<String, Messenger> getMessengers() {
		return this.messengers;
	}

	public void addMessenger(Messenger messenger, String url) throws IOException, DeploymentException, ParseBotException, AuthTokenException {
		messenger.setUrl(url);
		this.messengers.put(messenger.getName(), messenger);
	}

	public void deactivateAll() {
		for (Messenger m : this.messengers.values()) {
			m.close();
		}
		for (String k : this.active.keySet()) {
			this.active.put(k, false);
		}
	}

	public boolean deactivateAllWithCheck(ArrayList messengerNames) {
		int correctEntries = 0;
		for (Object object : messengerNames) {
			HashMap<String, String> list = (HashMap<String, String>) object;
			for (Messenger m : this.messengers.values()) {
				if (list.get("name").toLowerCase().equals(m.getName().toLowerCase())) {
					if (m.getChatMediator().checkToken(list.get("authToken"))) {
						correctEntries++;
					}
				}
			}
		}
		if (correctEntries < this.messengers.size()) {
			return false;
		}
		for (Messenger m : this.messengers.values()) {
			m.close();
		}
		for (String k : this.active.keySet()) {
			this.active.put(k, false);
		}
		return true;
	}

	public int countActive() {
		int trueCount = 0;
		for (boolean b : active.values()) {
			if (b)
				trueCount++;
		}
		return trueCount;
	}

	public void handleMessages(ArrayList<MessageInfo> messageInfos) {
		for (Messenger m : this.messengers.values()) {
			m.handleMessages(messageInfos, this);
		}
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

    public String getAddress() {
        return address;
    }
	public void setAddress(String address) {
        this.address =  address;
    }

	public HashMap<String, BotRoutine> getRoutines() {
		return routines;
	}

	public void setRoutines(HashMap<String, BotRoutine> routines) {
		this.routines = routines;
	}

	public void addRoutine(String name, BotRoutine routine) {
		this.routines.put(name, routine);
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}

	public Date getUpdated_at() {
		return updated_at;
	}
}
