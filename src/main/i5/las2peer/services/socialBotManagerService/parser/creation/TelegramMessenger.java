package i5.las2peer.services.socialBotManagerService.parser.creation;

import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(parent = Messenger.class, value = "Telegram")
public class TelegramMessenger extends Messenger {

    @ApiModelProperty(dataType = "string", value = "Please use https://telegram.me/BotFather to create a Telegram application. After this, please enter the generated token.", required = true, example = "1166163073:AAGd3zglvQ8L3EeGJwLcHV8l_m6HB3wsld0")
    private String token;

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    @Override
    public ChatService getType() {
	return ChatService.TELEGRAM;
    }

    @Override
    public String toString() {
	return "TelegramMessenger [token=" + token + ", getType()=" + getType() + "]";
    }

}
