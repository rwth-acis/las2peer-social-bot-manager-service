package i5.las2peer.services.socialBotManagerService.model;

public class ServiceEvent extends TriggerFunction {

	private String name;
	private Service service;	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}
	
	
	
}
