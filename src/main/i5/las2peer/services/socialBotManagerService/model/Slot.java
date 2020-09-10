package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.List;

public class Slot {

	/**
	 * name of this slot
	 */
	String name;

	/**
	 * intent that corresponds to the intent recognition of the NLU module
	 */
	String nluIntent;

	/**
	 * intent that corresponding to the intent recognition of the NLG module
	 */
	String nlgIntent;

	/**
	 * entity identification corresponding to the entity recognition of the NLU
	 * module
	 */
	String entity;

	/**
	 * Identifies if this slot has to be filled during a conversation or if it is
	 * optional
	 */
	boolean required;

	/**
	 * Identifies if this slot is filled by entity extraction or by free direct user
	 * input
	 */
	boolean entity_extraction;

	/**
	 * Identifies the priority with which this slot must be filled. A low number
	 * implies a high priority.
	 */
	int priority;

	/**
	 * The message that the bot can send to the user to ask for the information
	 * about this slot
	 */
	String message;

	/**
	 * The service parameter that the value of this slot should fill
	 */
	ServiceFunctionAttribute parameter;

	List<Slot> children;

	public Slot() {
		super();
		this.children = new ArrayList<Slot>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ServiceFunctionAttribute getParameter() {
		return parameter;
	}

	public void setParameter(ServiceFunctionAttribute parameter) {
		this.parameter = parameter;
	}

	protected boolean isEntity_extraction() {
		return entity_extraction;
	}

	protected void setEntity_extraction(boolean entity_extraction) {
		this.entity_extraction = entity_extraction;
	}

	public String getNlu_intent() {
		return nluIntent;
	}

	public void setNlu_intent(String nlu_intent) {
		this.nluIntent = nlu_intent;
	}

	public String getNlg_intent() {
		return nlgIntent;
	}

	public void setNlg_intent(String nlg_intent) {
		this.nlgIntent = nlg_intent;
	}

	protected List<Slot> getChildren() {
		return children;
	}

	protected void setChildren(List<Slot> children) {
		this.children = children;
	}

	public void addChildren(Slot slot) {
		this.children.add(slot);
	}

	public String toString() {
		String res = "Slot ".concat(this.getName()).concat(" nluIntent: ").concat(this.getNlu_intent())
				.concat(" nlgIntent: ").concat(this.getNlg_intent()).concat(" message: ").concat(this.getMessage());
		if(this.isEntity_extraction()) {
			res = res.concat(" entity: ").concat(this.getEntity());
		}
		
		res = res.concat("children: ");
		for (Slot slot : this.children) {
			res = res.concat(slot.toString());
		}
		return res;
	}

}
