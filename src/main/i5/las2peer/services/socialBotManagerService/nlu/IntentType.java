package i5.las2peer.services.socialBotManagerService.nlu;

public enum IntentType {
	INFORM, REQUEST, DENY, CONFIRM, REQCONF, REVERT, CANCEL, HELP, START, COMMAND, UNKOWN, TALK;

	public boolean isConfirmation() {
		if ((this == CONFIRM || this == DENY))
			return true;
		return false;

	}

}
