package i5.las2peer.services.socialBotManagerService.chat.xAPI;

public class xAPIActor {
	private String name;
	private xAPIAccount account;
	private String objectType;

	public xAPIActor() {
		account = new xAPIAccount();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public xAPIAccount getAccount() {
		return account;
	}

	public void setAccount(xAPIAccount account) {
		this.account = account;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
}
