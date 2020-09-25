package i5.las2peer.services.socialBotManagerService.parser.creation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import io.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "Messenger")
public class Messenger {

    @ApiModelProperty(dataType = "string", allowableValues = "Slack, Telegram", value = "The messenger application you want to use.", required = true, example = "Slack")
    private ChatService type;
    @ApiModelProperty(dataType = "string", value = "The authentication token", required = true, example = "1362637096:AAGIfsj3CfS3zJ_mj_09K5-GL7ltGq3ARcM")
    private String token;
    @ApiModelProperty(dataType = "string", value = "Application ID of your Slack application", required = false, example = "A1BES823B")
    private String appId;

    @XmlElement(name = "type", required = true)
    public ChatService getType() {
	return type;
    }

    public void setType(ChatService type) {
	this.type = type;
    }

    @XmlElement(name = "token", required = true)
    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    @XmlElement(name = "appId")
    public String getAppId() {
	return appId;
    }

    public void setAppId(String appId) {
	this.appId = appId;
    }

    @Override
    public String toString() {
	return "Messenger [type=" + type + ", token=" + token + ", appId=" + appId + "]";
    }

}
