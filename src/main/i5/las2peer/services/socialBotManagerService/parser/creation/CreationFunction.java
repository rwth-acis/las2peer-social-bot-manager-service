package i5.las2peer.services.socialBotManagerService.parser.creation;

public class CreationFunction {

	String name;
	String type;
	String des;
	
	public CreationFunction(String name, String type) {
		assert name != null;
		assert type != null;
		
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}
	
	
	
	
	
}
