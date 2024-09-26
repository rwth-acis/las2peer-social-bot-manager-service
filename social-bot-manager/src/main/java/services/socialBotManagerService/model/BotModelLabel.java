package services.socialBotManagerService.model;

import java.io.Serializable;

import net.minidev.json.JSONObject;

public class BotModelLabel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8123828374501000173L;
	private String id;
	private String name;
	private BotModelValue value;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BotModelValue getValue() {
		return value;
	}
	public void setValue(BotModelValue value) {
		this.value = value;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("name", name);
		json.put("value", value.toJSON());
		return json;
	}
}
