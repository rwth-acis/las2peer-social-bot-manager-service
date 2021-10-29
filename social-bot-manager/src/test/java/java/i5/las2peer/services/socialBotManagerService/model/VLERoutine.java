package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashSet;

public class VLERoutine extends TriggerFunction{
	private String name;
	private String interval;
	private String time;
	private long lastUpdate;
	private HashSet<Trigger> trigger;
	private VLE vle;

	public VLERoutine() {
		this.setTrigger(new HashSet<Trigger>());
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

	public HashSet<Trigger> getTrigger() {
		return trigger;
	}

	public void setTrigger(HashSet<Trigger> triggerFunctions) {
		this.trigger = triggerFunctions;
	}

	public void addTrigger(Trigger tf) {
		this.trigger.add(tf);
	}

	public VLE getVle() {
		return vle;
	}

	public void setVle(VLE vle) {
		this.vle = vle;
	}
}
