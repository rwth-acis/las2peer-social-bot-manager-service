package i5.las2peer.services.socialBotManagerService.parser.creation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Bot", description = "Bot model")
@XmlRootElement(name = "Bot")
public class Bot {

    @ApiModelProperty(dataType = "string", value = "A name gives your bot personality. It should make him recognizable as a bot.", required = true, example = "Botter")
    private String name;

    @ApiModelProperty(dataType = "string", value = "The NLU module allows us bots to understand your human language. Please choose one or create a new /createnlu", required = true)
    private String nluModule;

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

    public String getNluModule() {
	return nluModule;
    }

    public void setNluModule(String nluModule) {
	this.nluModule = nluModule;
    }

}
