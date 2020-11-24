package i5.las2peer.services.socialBotManagerService.model;

import java.net.URL;

/**
 * Model element of a web service
 * 
 */
public class Service {
	
	public Service() {
		
	}
	
	/**
	 * The service type indicates if this is the model of a las2peer service or a
	 * external service with openapi documentation
	 */
	private ServiceType serviceType;

	/**
	 * The serviceAlias is the identification of the las2peer service. If this
	 * object is not a las2peer service, the serviceAlias is a arbitrary name for
	 * the service.
	 */
	private String serviceAlias;

	/**
	 * Base URL of the web service (optional if this object is a las2peer service).
	 */
	private URL serviceURL;

	/**
	 * Explicit URL of the open api documentation (optional if the document is on a
	 * default location).
	 */
	private URL swaggerURL;

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceAlias() {
		return serviceAlias;
	}

	public void setServiceAlias(String serviceAlias) {
		this.serviceAlias = serviceAlias;
	}

	public URL getServiceURL() {
		return serviceURL;
	}

	public void setServiceURL(URL serviceURL) {
		this.serviceURL = serviceURL;
	}

	public URL getSwaggerURL() {
		return swaggerURL;
	}

	public void setSwaggerURL(URL swaggerURL) {
		this.swaggerURL = swaggerURL;
	}

}
