package i5.las2peer.services.socialBotManagerService.parser.creation;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Messenger.class, value = "Slack")
public class SlackMessenger extends Messenger {

    @ApiModelProperty(dataType = "string", value = "Please create a Slack application https://api.slack.com/apps. ", required = true, example = "xoxb-1292941988052-1287087666866-ZNsps1FNd28VeIj3TEQ2fFzL")
    private String token;

    @ApiModelProperty(dataType = "string", value = "Application ID of your Slack application", required = true, example = "A1BES823B")
    private String appId;

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    public String getAppId() {
	return appId;
    }

    public void setAppId(String appId) {
	this.appId = appId;
    }

    @Override
    public ChatService getType() {
	return ChatService.SLACK;
    }

    @Override
    public String toString() {
	return "SlackMessenger [token=" + token + ", appId=" + appId + ", getType()=" + getType() + "]";
    }

}
