package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashSet;

public class VLERoutine {
	private String name;
	private String interval;
	private String time;
	private long lastUpdate;
	private HashSet<ServiceFunction> triggerFunctions;

	public VLERoutine() {
		this.setTriggerFunctions(new HashSet<ServiceFunction>());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public HashSet<ServiceFunction> getTriggerFunctions() {
		return triggerFunctions;
	}

	public void setTriggerFunctions(HashSet<ServiceFunction> triggerFunctions) {
		this.triggerFunctions = triggerFunctions;
	}

	public void addTriggerFunction(ServiceFunction tf) {
		this.triggerFunctions.add(tf);
	}
}
