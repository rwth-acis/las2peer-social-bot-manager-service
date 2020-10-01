package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.Messenger;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Bot", description = "Bot model")
@XmlRootElement(name = "Bot")
public class Bot {

	@ApiModelProperty(dataType = "string", value = "The name of your bot", required = true, example = "Botter")
	private String name;


	private List<Function> function = new ArrayList<Function>();


	private List<Messenger> messenger = new ArrayList<Messenger>();

	@XmlElement(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Function> getFunction() {
		return function;
	}

	public void setFunction(List<Function> function) {
		this.function = function;
	}

	@XmlElementWrapper(name = "messengers")
	@XmlElement(name = "messenger")
	public List<Messenger> getMessenger() {
		return messenger;
	}

	public void setMessenger(List<Messenger> messenger) {
		this.messenger = messenger;
	}

	@Override
	public String toString() {
		return "Bot [name=" + name + ", function=" + function + ", messenger=" + messenger + "]";
	}

}
