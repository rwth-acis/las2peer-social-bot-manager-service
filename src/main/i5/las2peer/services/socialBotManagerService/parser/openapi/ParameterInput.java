package i5.las2peer.services.socialBotManagerService.parser.openapi;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;

public class ParameterInput {

    InputType type;

    public boolean validate(String value) {
	return type.validate(value);	
    }
    
    public InputType getType() {
        return type;
    }

    public void setType(InputType type) {
        this.type = type;
    }
    

}
