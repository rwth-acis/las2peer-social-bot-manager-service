package services.socialBotManagerService.model;

public class NLUKnowledge {
	String name;
	String id;
	String url;

	public NLUKnowledge(String name, String id, String url) {
		this.name = name;
		this.url = url;
		if (id != "") {
			this.id = id;
		} else
			this.id = "0";

	}

	public String getName() {
		return this.name;
	}

	public String getId() {
		return this.id;
	}

	public String getUrl() {
		return this.url;
	}

}
