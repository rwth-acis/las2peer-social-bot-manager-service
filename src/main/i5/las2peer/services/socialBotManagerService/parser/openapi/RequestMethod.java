package i5.las2peer.services.socialBotManagerService.parser.openapi;

public enum RequestMethod {
    GET, POST, PUT, DELETE;

    public static boolean validate(String value) {
	for (RequestMethod method : RequestMethod.class.getEnumConstants()) {
	    if (method.name().contentEquals(value)) {
		return true;
	    }
	}
	return false;
    }

}