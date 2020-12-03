package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.HashMap;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;

public class DialogueAct {

	String intent;
	DialogueActType intentType;
	Map<String, String> entities;
	String message;

	ExpectedInput expected;
	OpenAPIAction action;
	String file;

	boolean full;

	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public DialogueAct() {

	}

	public DialogueAct(String message) {
		this.message = message;
	}

	public DialogueAct(String message, ExpectedInput expected) {
		super();
		this.message = message;
		this.expected = expected;
	}

	public DialogueAct(String intent, String message, ExpectedInput expected) {
		super();
		this.intent = intent;
		this.message = message;
		this.expected = expected;
	}

	public void addEntity(String name, String value) {
		if (!this.hasEntities())
			this.entities = new HashMap<String, String>();
		this.entities.put(name, value);
	}

	public Map<String, String> getEntities() {
		return this.entities;
	}

	public boolean hasEntities() {
		if (this.entities != null && !this.entities.isEmpty())
			return true;
		return false;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ExpectedInput getExpected() {
		return expected;
	}

	public void setExpected(ExpectedInput expected) {
		this.expected = expected;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public boolean hasIntent() {
		return (this.intent != null);
	}

	public boolean hasMessage() {
		return this.message != null;
	}

	public boolean hasExpected() {
		return this.expected != null;
	}

	public DialogueAct concat(DialogueAct act) {

		if (act.hasIntent())
			this.intent = act.intent;
		if (act.hasMessage())
			this.message = this.message.concat(act.getMessage());
		if (act.hasExpected())
			this.expected = act.getExpected();
		return this;
	}
	
	public OpenAPIAction getAction() {
		return action;
	}

	public void setAction(OpenAPIAction action) {
		this.action = action;
	}

	public void setEntities(Map<String, String> entities) {
		this.entities = entities;
	}

	public void addEntity(Entity entity) {
		if (!this.hasEntities())
			this.entities = new HashMap<String, String>();
		this.entities.put(entity.getEntityName(), entity.getValue());
	}

	public boolean hasAction() {
		return this.action != null;
	}

	public boolean hasEnums() {
		if (!this.hasExpected())
			return false;
		return this.getExpected().hasEnums();
	}

	public DialogueActType getIntentType() {
		return intentType;
	}

	public void setIntentType(DialogueActType intentType) {
		this.intentType = intentType;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	@Override
	public String toString() {
		return "DialogueAct [intentType=" + this.intentType + " intent=" + intent + ", message=" + message + ", expected=" + expected + "]";
	}
	
	
}
