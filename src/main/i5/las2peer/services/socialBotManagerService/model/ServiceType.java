package i5.las2peer.services.socialBotManagerService.model;

public enum ServiceType {
	SERVICE, OPENAPI;

	public static ServiceType fromString(String value) {

		switch (value) {
		case "Service":
		case "las2peer":
		case "las2peer Service":
			return SERVICE;
		case "OpenAPI":
			return ServiceType.OPENAPI;
		}

		return null;
	}
}
