package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashSet;

public class VLEUser {
	private String id;
	private String name;
	private String role;
	private VLE vle;
	private HashSet<ServiceFunction> functions;
	
	public VLEUser() {
		functions = new HashSet<ServiceFunction>();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public VLE getVle() {
		return vle;
	}
	public void setVle(VLE vle) {
		this.vle = vle;
	}
	public HashSet<ServiceFunction> getFunctions() {
		return functions;
	}
	public void setFunctions(HashSet<ServiceFunction> functions) {
		this.functions = functions;
	}
	
	public void addFunction(ServiceFunction sf) {
		this.functions.add(sf);
	}
}
