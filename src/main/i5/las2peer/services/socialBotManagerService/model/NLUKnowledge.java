package i5.las2peer.services.socialBotManagerService.model;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class NLUKnowledge {

	@ApiModelProperty(dataType = "string", value = "The name of the nlu server", required = true, example = "MyRasaServer")
	String name;
	
	@ApiModelProperty(value = "The url of the nlu server", required = true, example = "http://localhost:5005")
	URL url;
	
	@ApiModelProperty(hidden = true)
	String id;
	
	@ApiModelProperty(hidden = true)
	String type;

	public NLUKnowledge() {

	}

	public NLUKnowledge(String name, String id, String url, String type) {
		this(name, id ,url);
		if(type == null)
			type = "Understanding";
		this.type = type;

	}
	
	public NLUKnowledge(String name, String id, String url) {
		this.name = name;
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.fillInStackTrace();
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
