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
	private String time;
	private String domain;
	private String previousMessage;
	private String currMessage;
	private String ts;

	public ChatMessage(String channel, String user, String text) {
		this.channel = channel;
		this.user = user;
		this.text = text;
	}

	public ChatMessage(String channel, String user, String text, String ts) {
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.ts = ts;
	}

	public ChatMessage(String channel, String user, String text, String fileName, String fileType, String body) {
		this.channel = channel;
		this.user = user;
		this.text = text + fileName;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileBody = body;
	}

	public ChatMessage(String channel, String user, String text, String ts, String currMessage, String previousMessage, String extra) {
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.ts = ts;
		this.previousMessage = previousMessage;
		this.currMessage = currMessage;
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPreviousMessage() {
		return previousMessage;
	}

	public void setPreviousMessage(String previousMessage) {
		this.previousMessage = previousMessage;
	}

	public String getCurrMessage() {
		return currMessage;
	}

	public void setCurrMessage(String currMessage) {
		this.currMessage = currMessage;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}
}
