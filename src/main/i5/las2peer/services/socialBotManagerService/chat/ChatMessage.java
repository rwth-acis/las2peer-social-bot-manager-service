package i5.las2peer.services.socialBotManagerService.chat;

public class ChatMessage {
	private String channel;
	private String user;
	private int role;
	private String email;
	private String text;

	public ChatMessage(String channel, String user, String text) {
		this.channel = channel;
		this.user = user;
		this.text = text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getChannel() {
		return this.channel;
	}

	public String getUser() {
		return this.user;
	}

	public String getText() {
		return this.text;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}
}
