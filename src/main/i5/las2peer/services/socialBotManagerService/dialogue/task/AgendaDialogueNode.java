package i5.las2peer.services.socialBotManagerService.dialogue.task;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;

public class AgendaDialogueNode {

    /**
     * Intent that activates this node
     * e.g inform_messenger_type
     */
    private String intent;
    
    /**
     * key of entity which value is collected by this node
     * e.g. messenger_type
     */
    private String entity;
    
    /**
     * Identifies if this node have to collect information FALSE or just triggers a response message TRUE
     * e.g. messenger_type
     */
    private boolean passive;
    
    /**
     * Children nodes 
     */
    private ArrayList<AgendaDialogueNode> children;
    
    /**
     * Chat responses
     */
    private ArrayList<String> responses;
    
    /**
     * Bot actions
     */
    private ArrayList<ServiceFunction> actions;    

    public AgendaDialogueNode(String intent, ArrayList<AgendaDialogueNode> childs, ArrayList<ServiceFunction> actions) {
	this.intent = intent;
	this.children = childs;
	this.actions = actions;
	this.responses = new ArrayList<String>();
    }

    public AgendaDialogueNode() {
	
	this.children = new ArrayList<AgendaDialogueNode>();
	this.actions = new ArrayList<ServiceFunction>();
	this.responses = new ArrayList<String>();

    }

    public boolean hasIntent(String intent) {
	return (this.intent.equals(intent));
    }

    public boolean hasChildren() {
	return (!this.getChildren().isEmpty());
    }

    public void addChild(AgendaDialogueNode child) {
	this.children.add(child);
    }

    public String getIntent() {
	return intent;
    }

    public void setIntent(String intent) {
	this.intent = intent;
    }

    public ArrayList<AgendaDialogueNode> getChildren() {
	return children;
    }

    public void setChildren(ArrayList<AgendaDialogueNode> children) {
	this.children = children;
    }

    public ArrayList<ServiceFunction> getActions() {
	return actions;
    }

    public void setActions(ArrayList<ServiceFunction> actions) {
	this.actions = actions;
    }
    
    public void addResponse(String response) {
	this.responses.add(response);
    }
    
    public ArrayList<String> getResponses() {
	return this.responses;
    }
    
    public String getEntity() {
	return entity;
    }

    public void setEntity(String entity) {
	this.entity = entity;
    }
    
    public Collection<? extends AgendaDialogueNode> getDescendants() {
	Collection<AgendaDialogueNode> desc = new ArrayList<AgendaDialogueNode>();
	desc.add(this);
	if (this.hasChildren())
	    for (AgendaDialogueNode node : this.getChildren()) {
		desc.addAll(node.getDescendants());
	    }
	return desc;
    }

    public boolean isPassive() {
	return passive;
    }

    public void setPassive(boolean bool) {
	this.passive = bool;
    }



}
