package services.socialBotManagerService.chat.xAPI;

import com.google.gson.annotations.SerializedName;

public class xAPIDisplay {
	@SerializedName("en-EN")
	private String enEN;
	@SerializedName("de-DE")
	private String deDE;

	public String getEnEN() {
		return enEN;
	}

	public void setEnEN(String enEN) {
		this.enEN = enEN;
	}

	public String getDeDE() {
		return deDE;
	}

	public void setDeDE(String deDE) {
		this.deDE = deDE;
	}
}
