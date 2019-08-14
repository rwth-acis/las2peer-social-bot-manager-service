package i5.las2peer.services.socialBotManagerService.model;

public class BotModelNodeAttribute {
	private String id;
	private String name;
	private BotModelValue value;
	private String option;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BotModelValue getValue() {
		return value;
	}
	public void setValue(BotModelValue value) {
		this.value = value;
	}
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
}
