package i5.las2peer.services.socialBotManagerService;

import java.util.HashSet;

public class VLE {
	private String separator;
	private String addr;
	private HashSet<String> userId;

	public VLE() {
		this.userId = new HashSet<String>();
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public HashSet<String> getUserId() {
		return userId;
	}

	public void setUserId(HashSet<String> userId) {
		this.userId = userId;
	}

	public void addUserId(String id) {
		this.userId.add(id);
	}

}
