package i5.las2peer.services.socialBotManagerService.model;

import java.net.URL;

/**
 * Model element of a web service
 * 
 */
public class Service {

	/**
	 * The service type indicates if this is the model of a las2peer service or a
	 * external service with OpenAPI documentation
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
	private final URL serviceURL;

	/**
	 * Explicit URL of the OpenAPI documentation (optional if the document is on a
	 * default location).
	 */
	private URL swaggerURL;
	
	public Service(ServiceType serviceType, String serviceAlias, URL serviceURL) {

		assert serviceURL != null : "cant create service with null url";

		if (serviceType == null)
			serviceType = ServiceType.OPENAPI;
		
		if (serviceAlias == null)
			serviceAlias = "";

		this.serviceType = serviceType;
		this.serviceAlias = serviceAlias;
		this.serviceURL = serviceURL;
	}
	
	public Service(ServiceType serviceType, String serviceAlias, URL serviceURL, URL swaggerURL) {
		this(serviceType, serviceAlias, serviceURL);
		this.swaggerURL = swaggerURL;
	}
	
	public Service(URL serviceURL) {
		this(null, null, serviceURL);
	}
	
	public ServiceType getServiceType() {
		return serviceType;
	}

	public String getServiceAlias() {
		return serviceAlias;
	}

	public String getServiceURL() {
		return serviceURL.toString();
	}

	public String getSwaggerURL() {
		if (this.swaggerURL != null)
			return swaggerURL.toString();
		return null;
	}

	public void setSwaggerURL(URL swaggerURL) {

		if (swaggerURL != null)
			this.swaggerURL = swaggerURL;
	}

	@Override
	public String toString() {
		return "Service [serviceType=" + serviceType + ", serviceAlias=" + serviceAlias + ", serviceURL=" + serviceURL
				+ ", swaggerURL=" + swaggerURL + "]";
	}

	public void invariant() {
		assert this.serviceURL != null : "service has no URL";
	}

}
