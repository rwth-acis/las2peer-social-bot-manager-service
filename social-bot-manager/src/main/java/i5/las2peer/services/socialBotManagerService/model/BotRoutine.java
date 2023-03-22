package i5.las2peer.services.socialBotManagerService.model;

import java.util.HashSet;

public class BotRoutine extends TriggerFunction{
	private String name;
	private String interval;
	private String time;
	private long lastUpdate;
	private HashSet<Trigger> trigger;
	private Bot bot;

	public BotRoutine() {
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

	public Bot getBot() {
		return bot;
	}

	public void setBot(Bot bot) {
		this.bot = bot;
	}
}
