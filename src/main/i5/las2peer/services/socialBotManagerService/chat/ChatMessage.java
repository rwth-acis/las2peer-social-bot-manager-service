package i5.las2peer.services.socialBotManagerService.chat;

public class ChatMessage {
	private String channel;
	private String user;
	private int role;
	private String email;
	private String text;
	private String fileName;
	private String fileType;
	private String fileBody;

	public ChatMessage(String channel, String user, String text) {
		this.channel = channel;
		this.user = user;
		this.text = text;
	}
	
	public ChatMessage(String channel, String user, String text, String fileName, String fileType, String body) {
		this.channel = channel;
		this.user = user;
		this.text = text + fileName;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileBody = body;
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
	
	public String getFileName() {
		return fileName;
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public String getFileBody() {
		return fileBody;
	}
}
