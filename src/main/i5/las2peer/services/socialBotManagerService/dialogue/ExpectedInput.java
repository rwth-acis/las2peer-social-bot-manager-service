package i5.las2peer.services.socialBotManagerService.dialogue;

public class ExpectedInput {

	private InputType type;
	private String intend;
	private String entity;
	private boolean array;

	public ExpectedInput(InputType type, String intend) {
		super();
		this.type = type;
		this.intend = intend;
		this.array = false;
	}

	public ExpectedInput() {

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
