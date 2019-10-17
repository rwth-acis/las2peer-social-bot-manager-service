package i5.las2peer.services.socialBotManagerService.chat;

import java.util.Vector;

public class ChatMessageCollector {
	Vector<ChatMessage> messages;
	private boolean connected;

	public ChatMessageCollector() {
		this.messages = new Vector<ChatMessage>();
		setConnected(true);
	}

	public void addMessage(ChatMessage message) {
		System.out.println("Message added: Channel: " + message.getChannel() + ", User: " + message.getUser());
		this.messages.add(message);
	}

	// Copies messages in a thread-safe manner and returns the copy.
	public Vector<ChatMessage> getMessages() {
		Vector<ChatMessage> messages;
		synchronized (this.messages) {
			messages = new Vector<ChatMessage>(this.messages);
			this.messages.clear();
		}
		return messages;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
