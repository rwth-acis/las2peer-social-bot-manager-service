package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServiceEvent extends TriggerFunction {

	private String name;
	private Service service;
	private List<ChatResponse> responses;

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

	public void addResponse(ChatResponse response) {
		if(this.responses  == null)
			this.responses = new ArrayList<>();
		this.responses.add(response);
	}

	public ChatResponse getResponse() {
		if (responses == null)
			return null;

		int item = new Random().nextInt(responses.size());
		return responses.get(item);

	}

	public List<ChatResponse> getResponses() {
		return this.responses;
	}

}
