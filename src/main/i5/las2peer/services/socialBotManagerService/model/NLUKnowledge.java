package i5.las2peer.services.socialBotManagerService.model;

import java.net.MalformedURLException;
import java.net.URL;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class NLUKnowledge {

    @ApiModelProperty(dataType = "string", value = "The name of the nlu server", required = true, example = "MyRasaServer")
    String name;
    @ApiModelProperty
    String id;
    @ApiModelProperty(value = "The url of the nlu server", required = true, example = "http://localhost:5005")
    URL url;

    public NLUKnowledge() {

    }

    public NLUKnowledge(String name, String id, String url) {
	this.name = name;
	try {
	    this.url = new URL(url);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	}
	if (id != "") {
	    this.id = id;
	} else
	    this.id = "0";

    }

    public String getName() {
	return this.name;
    }

    public String getId() {
	return this.id;
    }

    public URL getUrl() {
	return this.url;
    }

}
