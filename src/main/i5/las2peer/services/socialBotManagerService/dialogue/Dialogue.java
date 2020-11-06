package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

public class Dialogue {

    DialogueAct lastAct;
    List<MessageInfo> inputs;

    AbstractDialogueManager activeManager;
    List<AbstractDialogueManager> managers;

    public Dialogue(Messenger messenger) {
	this.inputs = new ArrayList<>();
    }

    public void cancel() {

	System.out.println("cancel: input acts size: " + this.inputs.size());
	this.lastAct = null;
	this.inputs.clear();
	DialogueActGenerator gen = new DialogueActGenerator();

    }

    public void revert() {

	this.inputs.remove(this.inputs.size() - 1);
	this.lastAct = null;

	for (MessageInfo msg : this.inputs) {
	    System.out.println("redo " + msg.getIntent().getKeyword());
	    (msg.getIntent(), msg.getMessage().getText());
	}

	semantic = this.inputs.get(this.inputs.size() - 1).getIntent();
	this.lastAct = null;

    }

    public boolean isEmpty() {
	return this.inputs.isEmpty();
    }

    public void add(MessageInfo message) {
	this.inputs.add(message);
    }

    public MessageInfo getLastInput() {
	return this.inputs.get(this.inputs.size() - 1);
    }

    public DialogueAct getLastOutput() {
	return this.lastAct;
    }

    public boolean hasExpected() {
	if (this.lastAct == null)
	    return false;

	if (this.lastAct.getExpected() == null)
	    return false;

	return true;
    }

    public ExpectedInput getExpected() {

	if (this.lastAct == null)
	    return null;

	if (this.lastAct.getExpected() == null)
	    return null;

	return this.lastAct.getExpected();
    }

    public DialogueAct getLastAct() {
	return lastAct;
    }

    public void setLastAct(DialogueAct lastAct) {
	this.lastAct = lastAct;
    }

    public List<MessageInfo> getInputs() {
	return inputs;
    }

    public void setInputs(List<MessageInfo> inputs) {
	this.inputs = inputs;
    }

    public AbstractDialogueManager getActiveManager() {
	return activeManager;
    }

    public void setActiveManager(AbstractDialogueManager activeManager) {
	this.activeManager = activeManager;
    }

    public List<AbstractDialogueManager> getManagers() {
	return managers;
    }

    public void setManagers(List<AbstractDialogueManager> managers) {
	this.managers = managers;
    }

}
