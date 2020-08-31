package i5.las2peer.services.socialBotManagerService.model;

public class Slot {

    /**
     * name of this slot
     */
    String name;

    /**
     * entity identification corresponding to the entity recognition of the NLU
     * module
     */
    String entity;

    /**
     * Identifies if this slot has to be filled during a conversation or if it is
     * optional
     */
    boolean required;

    /**
     * Identifies the priority with which this slot must be filled. A low number
     * implies a high priority.
     */
    int priority;

    /**
     * The message that the bot can send to the user to ask for the information
     * about this slot
     */
    String message;

    /**
     * The service parameter that the value of this slot should fill
     */
    ServiceFunctionAttribute parameter;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getEntity() {
	return entity;
    }

    public void setEntity(String entity) {
	this.entity = entity;
    }

    public boolean isRequired() {
	return required;
    }

    public void setRequired(boolean required) {
	this.required = required;
    }

    public int getPriority() {
	return priority;
    }

    public void setPriority(int priority) {
	this.priority = priority;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public ServiceFunctionAttribute getParameter() {
	return parameter;
    }

    public void setParameter(ServiceFunctionAttribute parameter) {
	this.parameter = parameter;
    }

}
