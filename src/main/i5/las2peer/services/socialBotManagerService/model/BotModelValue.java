package i5.las2peer.services.socialBotManagerService.model;

import java.io.Serializable;

public class BotModelValue implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7779402101938183839L;
	private String id;
	private String name;
	private String value;
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
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
