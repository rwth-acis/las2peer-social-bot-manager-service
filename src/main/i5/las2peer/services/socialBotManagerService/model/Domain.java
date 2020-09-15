package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashMap;
import java.util.Map;

public class Domain {

	/**
	 * name and intent of this domain
	 */
	String name;

	/**
	 * frames of this domain
	 */
	Map<String, Frame> frames;

	/**
	 * incoming messages of this domain
	 */
	Map<String, IncomingMessage> messages;

	public Domain() {
		super();
		this.frames = new HashMap<String, Frame>();
		this.messages = new HashMap<String, IncomingMessage>();
	}

	public Domain(String name, Map<String, Frame> frames, Map<String, IncomingMessage> messages) {
		super();
		this.name = name;
		this.frames = frames;
		this.messages = messages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Frame> getFrames() {
		return frames;
	}

	public void setFrames(Map<String, Frame> frames) {
		this.frames = frames;
	}
	
	public void addFrame(Frame frame) {
		this.frames.put(frame.getName(), frame);
	}

	public Map<String, IncomingMessage> getMessages() {
		return messages;
	}
	
	public void addMessage(IncomingMessage message) {
		this.messages.put(message.getEntityKeyword(), message);
	}

	public void setMessages(Map<String, IncomingMessage> messages) {
		this.messages = messages;
	}

	public boolean validate() {		
		if (this.name == null)
			return false;
		if (this.frames == null)
			return false;
		if (this.messages == null)
			return false;
		return true;		
	}
	
	@Override
	public String toString() {
		return "Domain [name=" + name + ", frames=" + frames + ", messages=" + messages + "]";
	}
	

}
