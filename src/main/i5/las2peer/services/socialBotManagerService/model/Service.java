package i5.las2peer.services.socialBotManagerService.model;

/**
 * Model element of a web service
 * 
 */
public class Service {
		
	/**
	 * The service type indicates if this is the model of a las2peer service or a
	 * external service with openapi documentation
	 */
	private final ServiceType serviceType;

	/**
	 * The serviceAlias is the identification of the las2peer service. If this
	 * object is not a las2peer service, the serviceAlias is a arbitrary name for
	 * the service.
	 */
	private final String serviceAlias;

	/**
	 * Base URL of the web service (optional if this object is a las2peer service).
	 */
	private final String serviceURL;

	/**
	 * Explicit URL of the open api documentation (optional if the document is on a
	 * default location).
	 */
	private String swaggerURL;

	public Service(ServiceType serviceType, String serviceAlias, String serviceURL) {		
		this.serviceType = serviceType;
		this.serviceAlias= serviceAlias;
		this.serviceURL = serviceURL;		
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}

	public String getServiceAlias() {
		return serviceAlias;
	}

	public String getServiceURL() {
		return serviceURL;
	}

	public String getSwaggerURL() {
		return swaggerURL;
	}

	public void setSwaggerURL(String swaggerURL) {
		this.swaggerURL = swaggerURL;
	}
	
	@Override
	public String toString() {
		return "Service [serviceType=" + serviceType + ", serviceAlias=" + serviceAlias + ", serviceURL=" + serviceURL
				+ ", swaggerURL=" + swaggerURL + "]";
	}

}
