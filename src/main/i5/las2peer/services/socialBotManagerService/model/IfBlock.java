package i5.las2peer.services.socialBotManagerService.model;

public class IfBlock {
	private String id;
	private String conditionType;
	private String value;
	private IfBlock next;
	private IfBlock prev;
	private String source;

	public String getConditionType() {
		return conditionType;
	}

	public void setConditionType(String conditionType) {
		this.conditionType = conditionType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public IfBlock getNext() {
		return next;
	}

	public void setNext(IfBlock next) {
		this.next = next;
	}

	public boolean hasNext() {
		return next != null;
	}

	public IfBlock getPrev() {
		return prev;
	}

	public void setPrev(IfBlock prev) {
		this.prev = prev;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
