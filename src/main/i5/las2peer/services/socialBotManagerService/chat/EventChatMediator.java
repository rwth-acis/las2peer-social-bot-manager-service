package i5.las2peer.services.socialBotManagerService.chat;

import java.util.Vector;

import net.minidev.json.JSONObject;

public abstract class EventChatMediator extends ChatMediator {

    /**
     * Stores received messages
     */
    private Vector<ChatMessage> messages = new Vector<ChatMessage>();

    public EventChatMediator(String authToken) {
	super(authToken);

    }

    public abstract ChatMessage handleEvent(JSONObject parsedEvent);

    public Vector<ChatMessage> getMessages() {
	Vector<ChatMessage> messages = this.messages;
	this.messages = new Vector<ChatMessage>();
	return messages;
    }

    public void addMessage(ChatMessage message) {
	this.messages.add(message);
	System.out.println("message stored: " + this.messages.size());
    }

    public void setMessages(Vector<ChatMessage> messages) {
	this.messages = messages;
    }

}
