package i5.las2peer.services.socialBotManagerService.parser.openapi;

public enum ParameterType {

    PATH, QUERY, BODY, CHILD, DISCRIMINATOR, HELPER;

    public static ParameterType fromString(String parameterType) {
	{
	    for (ParameterType type : ParameterType.values()) {
		if (type.name().equalsIgnoreCase(parameterType)) {
		    return type;
		}
	    }
	    return null;
	}

    }

}
