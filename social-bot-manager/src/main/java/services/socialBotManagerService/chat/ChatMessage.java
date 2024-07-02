package services.socialBotManagerService.chat;

import org.web3j.abi.datatypes.Bool;

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
	private String messageId;
	private String actionInfo; 
	private InteractiveChatElementType followupMessageTypes;

	public ChatMessage(String channel, String user, String text) {
		this.channel = channel;
		this.user = user;
		this.text = text;
	}

	public ChatMessage(String channel, String user, String text, String time) {
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.time = time;
	}

	public ChatMessage(String channel, String user, String text, String time, String messageId) {
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.time = time;
		this.messageId = messageId;
	}

	public ChatMessage(String channel, String user, String text, String fileName, String fileType, String body) {
		this.channel = channel;
		this.user = user;
		this.text = text + fileName;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileBody = body;
	}

	public ChatMessage(String channel, String user, String text, String time, String messageId, String fileName, String fileType,
					   String body) {
		this.channel = channel;
		this.user = user;
		this.text = text + fileName;
		this.time = time;
		this.messageId = messageId;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileBody = body;
	}

	public ChatMessage(String channel, String user, String text, String time, String fileName, String fileType,
					   String body) {
		this.channel = channel;
		this.user = user;
		this.text = text + fileName;
		this.time = time;
		this.fileName = fileName;
		this.fileType = fileType;
		this.fileBody = body;
	}
	// ChatMessage for actions
	public ChatMessage(String channel, String user, String text, String time, String actionInfo, boolean action) {
		this.channel = channel;
		this.user = user;
		this.text = text;
		this.time = time;
		this.actionInfo = actionInfo;
	}

	public  String getActionInfo(){
		return this.actionInfo;
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

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
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

	public boolean hasTime(){
		if(this.getTime() != null){
			return true;
		}
		return false;
	}
	
	public InteractiveChatElementType getFollowupMessageTypes() {
		return followupMessageTypes;
	}

	public void setFollowupMessageTypes(String followupMessageTypes) {
		this.followupMessageTypes = InteractiveChatElementType.valueOf(followupMessageTypes.toUpperCase());
	}
}
