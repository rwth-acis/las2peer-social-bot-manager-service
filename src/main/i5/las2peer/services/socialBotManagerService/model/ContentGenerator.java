package i5.las2peer.services.socialBotManagerService.model;

public class ContentGenerator {
	private String id;
	private String name;
	private String serviceName;
	private ServiceFunctionAttribute input;
	private ServiceFunctionAttribute output;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public ServiceFunctionAttribute getInput() {
		return input;
	}

	public void setInput(ServiceFunctionAttribute input) {
		this.input = input;
	}

	public ServiceFunctionAttribute getOutput() {
		return output;
	}

	public void setOutput(ServiceFunctionAttribute output) {
		this.output = output;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
