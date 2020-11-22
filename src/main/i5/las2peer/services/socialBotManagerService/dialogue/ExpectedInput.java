package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class ExpectedInput {

	private InputType type;
	private String intend;
	private String entity;
	private List<String> enums;
	private boolean array;

	public ExpectedInput(InputType type, String intend) {
		super();
		this.type = type;
		this.intend = intend;
		this.setArray(false);
	}

	public ExpectedInput() {

	}

	public boolean validate(Intent semantic, String message) {

		// confirmation
		if (this.type == InputType.Confirmation) {

			if (semantic.getIntentType().isConfirmation())
				return true;
		}

		// enumeration
		if (this.hasEnums()) {

			for (String enu : this.enums) {

				if (enu.contentEquals(message))
					return true;

				for (Entity entity : semantic.getEntities()) {
					if (enu.contentEquals(entity.getValue()))
						return true;
				}
			}

			return false;
		}

		// other
		return this.validate(message);
	}

	public boolean validate(String input) {

		return this.type.validate(input);
	}

	public String getEntity() {
		return entity;
	}

	public String getIntend() {
		return intend;
	}

	public InputType getType() {
		return type;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void setIntend(String intend) {
		this.intend = intend;
	}

	public void setType(InputType type) {
		this.type = type;
	}

	public boolean isArray() {
		return array;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public void addEnum(String enu) {
		if (this.enums == null)
			this.enums = new ArrayList<>();

		this.enums.add(enu);
	}

	public boolean hasEnums() {
		return (this.enums != null && !this.enums.isEmpty());
	}

	public List<String> getEnums() {
		return this.enums;
	}

	public boolean invariant() {
		if (this.type != null)
			return true;

		return false;
	}

	@Override
	public String toString() {
		return "ExpectedInput [type=" + type + ", intend=" + intend + ", entity=" + entity + "]";
	}

}
