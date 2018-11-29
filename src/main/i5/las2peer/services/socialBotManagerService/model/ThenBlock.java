package i5.las2peer.services.socialBotManagerService.model;

public class ThenBlock {
	private String id;
	private String manipulationType;
	private String value;
	private String value2;
	private ThenBlock next;
	private ThenBlock prev;
	private String output;

	public String getManipulationType() {
		return manipulationType;
	}

	public void setManipulationType(String manipulationType) {
		this.manipulationType = manipulationType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ThenBlock getNext() {
		return next;
	}

	public void setNext(ThenBlock next) {
		this.next = next;
	}

	public boolean hasNext() {
		return next != null;
	}

	public ThenBlock getPrev() {
		return prev;
	}

	public void setPrev(ThenBlock prev) {
		this.prev = prev;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}
}
