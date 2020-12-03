package i5.las2peer.services.socialBotManagerService.dialogue;

public enum DialogueActType {
		
	// system
	SYSTEM_HOME("system_home"),
	TALK(""),
		
	// frame
	REQCONF_FRAME("reqconf_frame"),
	REQCONF_FRAME_OPTIONAL("reqconf_frame_optional"),
	
	//slots
    REQUEST_SLOT("request_slot"),
    REQCONF_SLOT_PROCEED("reqconf_slot_proceed"),

    INFORM_SLOT(""),
    REQCONF_SLOT(""),
	    
	// error messages
    ERROR_INVALID_INPUT("error_invalid_input"),
    ERROR_COMMAND_UNKNOWN("error_command_unkown"),
    
    ERROR_SYSTEM("error_system"),	
	ERROR_NLU("error_nlu");

	String string;
	
	DialogueActType(String string) {
		this.string = string;
	}
	
	public String getString() {
		return this.string;
	}
	

}
