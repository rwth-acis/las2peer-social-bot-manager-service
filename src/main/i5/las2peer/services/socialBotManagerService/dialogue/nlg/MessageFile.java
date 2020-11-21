package i5.las2peer.services.socialBotManagerService.dialogue.nlg;

import java.nio.charset.StandardCharsets;

public class MessageFile {

	String name;
	String data;
	String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataString() {
		return data;
	}

	public byte[] getDataBytes() {
		return data.getBytes(StandardCharsets.UTF_8);
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
