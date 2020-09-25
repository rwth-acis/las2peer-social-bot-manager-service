package i5.las2peer.services.socialBotManagerService.parser.creation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ AccessServiceFunction.class, ChitChatFunction.class })
public class Function {
    private FunctionType type;

}
