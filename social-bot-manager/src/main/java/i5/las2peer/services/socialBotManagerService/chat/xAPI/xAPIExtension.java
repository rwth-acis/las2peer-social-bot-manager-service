package i5.las2peer.services.socialBotManagerService.chat.xAPI;

import com.google.gson.annotations.SerializedName;

public class xAPIExtension {

	@SerializedName("https://xapi.tech4comp.dbis.rwth-aachen.de/definitions/chat/activities/message")
	private xAPIMessageInfo messageInfo;

	public xAPIExtension() {
		messageInfo = new xAPIMessageInfo();
	}

	public xAPIMessageInfo getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(xAPIMessageInfo messageInfo) {
		this.messageInfo = messageInfo;
	}
}
