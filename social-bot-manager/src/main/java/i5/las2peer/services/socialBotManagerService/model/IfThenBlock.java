package i5.las2peer.services.socialBotManagerService.model;

public class IfThenBlock {
	private ServiceFunctionAttribute sourceAttribute;
	private ServiceFunctionAttribute targetAttribute;
	private IfThenBlock prev;
	private IfThenBlock next;
	private String conditionType;
	private String statementType;
	private String conditionValueA;
	private String conditionValueB;
	private String statementValueA;
	private String statementValueB;
	public ServiceFunctionAttribute getSourceAttribute() {
		return sourceAttribute;
	}
	public void setSourceAttribute(ServiceFunctionAttribute sourceAttribute) {
		this.sourceAttribute = sourceAttribute;
	}
	public ServiceFunctionAttribute getTargetAttribute() {
		return targetAttribute;
	}
	public void setTargetAttribute(ServiceFunctionAttribute targetAttribute) {
		this.targetAttribute = targetAttribute;
	}
	public IfThenBlock getPrev() {
		return prev;
	}
	public void setPrev(IfThenBlock prev) {
		this.prev = prev;
	}
	public IfThenBlock getNext() {
		return next;
	}
	public void setNext(IfThenBlock next) {
		this.next = next;
	}
	public String getConditionType() {
		return conditionType;
	}
	public void setConditionType(String conditionType) {
		this.conditionType = conditionType;
	}
	public String getStatementType() {
		return statementType;
	}
	public void setStatementType(String statementType) {
		this.statementType = statementType;
	}
	public String getConditionValueA() {
		return conditionValueA;
	}
	public void setConditionValueA(String conditionValueA) {
		this.conditionValueA = conditionValueA;
	}
	public String getConditionValueB() {
		return conditionValueB;
	}
	public void setConditionValueB(String conditionValueB) {
		this.conditionValueB = conditionValueB;
	}
	public String getStatementValueA() {
		return statementValueA;
	}
	public void setStatementValueA(String statementValueA) {
		this.statementValueA = statementValueA;
	}
	public String getStatementValueB() {
		return statementValueB;
	}
	public void setStatementValueB(String statementValueB) {
		this.statementValueB = statementValueB;
	}
}
