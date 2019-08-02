package i5.las2peer.services.socialBotManagerService.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatMessageCollector {
	List<ChatMessage> messages;
	
	public ChatMessageCollector() {
		this.messages = Collections.synchronizedList(new ArrayList<ChatMessage>());
	}
	
	public void addMessage(ChatMessage message) {
		System.out.println("Message added: Channel: " + message.getChannel() + ", User: " + message.getUser());
		this.messages.add(message);
	}
	
	// Copies messages in a thread-safe manner and returns the copy.
	public ArrayList<ChatMessage> getMessages() {
		ArrayList<ChatMessage> messages;
		synchronized (this.messages) {
			messages = new ArrayList<ChatMessage>(this.messages);
			this.messages.clear();
		}
		return messages;
	}
}
