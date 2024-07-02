package services.socialBotManagerService.chat.xAPI;

public class xAPIVerb {
	private xAPIDisplay display;
	private String id;

	public xAPIVerb() {
		display = new xAPIDisplay();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public xAPIDisplay getDisplay() {
		return display;
	}

	public void setDisplay(xAPIDisplay display) {
		this.display = display;
	}
}
