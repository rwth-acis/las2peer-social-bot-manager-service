package i5.las2peer.services.socialBotManagerService.chat;

import java.util.Vector;

public class ChatMessageCollector {
	Vector<ChatMessage> messages;
	private boolean connected;
	private String domain;	

	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
			{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" }, { new String("ö"), "oe" },
			{ new String("ß"), "ss" } };

	public static String replaceUmlaute(String orig) {
		String result = orig;

		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}

		return result;
	}

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

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
