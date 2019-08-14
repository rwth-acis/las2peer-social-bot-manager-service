package i5.las2peer.services.socialBotManagerService.model;

public class Trigger {
	private TriggerFunction triggerFunction;
	private ServiceFunction triggeredFunction;
	
	public Trigger(TriggerFunction t, ServiceFunction s) {
		this.triggerFunction = t;
		this.triggeredFunction = s;
	}
	
	public TriggerFunction getTriggerFunction() {
		return triggerFunction;
	}
	public void setTriggerFunction(TriggerFunction triggerFunction) {
		this.triggerFunction = triggerFunction;
	}
	public ServiceFunction getTriggeredFunction() {
		return triggeredFunction;
	}
	public void setTriggeredFunction(ServiceFunction triggeredFunction) {
		this.triggeredFunction = triggeredFunction;
	}
}
