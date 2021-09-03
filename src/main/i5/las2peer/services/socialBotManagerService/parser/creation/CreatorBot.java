package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import i5.las2peer.services.socialBotManagerService.parser.creation.function.CreatorFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.Messenger;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Bot", description = "Bot model")
@XmlRootElement(name = "Bot")
public class CreatorBot {

	@ApiModelProperty(dataType = "string", value = "A name gives your bot personality. It should make him recognizable as a bot.", required = true, example = "Botter")
	private String name;

	@ApiModelProperty(dataType = "string", value = "This is how your bot introduces itself", required = true, example = "Hi, I am a bot :)")
	private String description;

	@ApiModelProperty(dataType = "string", value = "The NLU module allows us bots to understand your human language. Please choose one or create a new /createnlu", required = true)
	private String nluModule;

	@ApiModelProperty(required = true)
	private List<CreatorFunction> function = new ArrayList<CreatorFunction>();

	@ApiModelProperty(required = true)
	private List<Messenger> messenger = new ArrayList<Messenger>();

	@XmlElement(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CreatorFunction> getFunction() {
		return function;
	}

	public void setFunction(List<CreatorFunction> function) {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Bot [name=" + name + ", function=" + function + ", messenger=" + messenger + "]";
	}

	public String getNluModule() {
		return nluModule;
	}

	public void setNluModule(String nluModule) {
		this.nluModule = nluModule;
	}

	public void addFunction(CreatorFunction function) {
		if (this.function == null)
			this.function = new ArrayList<>();
		this.function.add(function);
	}

}
