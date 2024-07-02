package services.socialBotManagerService.chat.xAPI;

public class xAPIContext {
	private xAPIExtension extension;

	public xAPIContext() {
		extension = new xAPIExtension();
	}

	public xAPIExtension getExtension() {
		return extension;
	}

	public void setExtension(xAPIExtension extension) {
		this.extension = extension;
	}
}
