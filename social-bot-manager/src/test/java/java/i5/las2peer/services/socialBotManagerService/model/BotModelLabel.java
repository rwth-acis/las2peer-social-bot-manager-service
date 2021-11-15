package i5.las2peer.services.socialBotManagerService.model;

import java.io.Serializable;

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
}
