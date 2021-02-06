package i5.las2peer.services.socialBotManagerService.chat.xAPI;

import com.google.gson.annotations.SerializedName;

public class xAPIExtension {

	@SerializedName("https://tech4comp.de/xapi/context/extensions/messageInfo")
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
