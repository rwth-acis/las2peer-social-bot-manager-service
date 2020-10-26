package i5.las2peer.services.socialBotManagerService.parser.creation;

import io.swagger.annotations.ApiModelProperty;

public class LanguageUnderstanding {

    @ApiModelProperty(dataType = "string", value = "The bot needs to use a language understandig module. Please enter the url.", required = true, example = "http://localhost:5005")
    String url;

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

}
