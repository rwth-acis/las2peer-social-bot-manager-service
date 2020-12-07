package i5.las2peer.services.socialBotManagerService.dialogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.DialogueManagerGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.DialogueManagerType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.TaskOrientedManager;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;

/**
 * A dialogue contains the dialogue state of a conversation
 *
 */
public class Dialogue {

	/**
	 * The last dialogue act that was performed by the bot within this dialogue
	 */
	DialogueAct lastAct;
		
	/**
	 * The dialogue manager that was last used for this dialogue
	 */
	AbstractDialogueManager activeManager;
	
	/**
	 * All dialogue managers that are available for this dialogue
	 */
	List<AbstractDialogueManager> managers;
	
	/**
	 * Record of all user performed acts in this dialogue 
	 */
	Map<AbstractDialogueManager, ArrayList<MessageInfo>> inputs;

	/**
	 * Constructor of a dialogue
	 *  
	 * @param messenger that realize this dialogue
	 */
	public Dialogue(Messenger messenger) {
		assert messenger != null : "messenger is null";
		
		this.inputs = new HashMap<>();
		this.managers = new ArrayList<>();
		init(messenger);
	}

	public DialogueAct handle(AbstractDialogueManager manager, MessageInfo info) {

		assert manager != null : "manager is null";
		assert this.managers.contains(manager) : "dialogue do not know manager";
		assert info != null : "message info is null";
		assert info.getIntent() != null : "intent is null";
		System.out.println("handle: " + info.toString());

		// record input
		if (!this.inputs.containsKey(manager))
			this.inputs.put(manager, new ArrayList<>());
		if (info.getIntent().getIntentType() != IntentType.REVERT)
			this.inputs.get(manager).add(info);

		// add dynamic entities
		Intent intent = info.getIntent();
		if(activeManager instanceof TaskOrientedManager)
			intent = addDynamicEntites(manager, info);
				
		// further handling		
		DialogueAct res = manager.handle(intent);
		this.lastAct = res;
		this.activeManager = manager;		
		
		return res;
	}
		
	public Intent addDynamicEntites(AbstractDialogueManager manager, MessageInfo info) {
		
		assert info != null;
		assert info.getIntent() != null;
		assert manager != null;
		Intent intent = info.getIntent();
		
		if(info.getMessage() == null || info.getMessage().getText() == null)
			return intent;
		
		if(!(manager instanceof TaskOrientedManager))
			return intent;
		
		TaskOrientedManager taskManager = (TaskOrientedManager) manager;
		String utterance = info.getMessage().getText();
		Collection<Entity> entities = taskManager.getDialogueGoal().getEnums(utterance);			
		intent.addEntities(entities);
		
		return intent;		
	}
	

	public void init(Messenger messenger) {
		
		DialogueManagerGenerator generator = new DialogueManagerGenerator();
		if (messenger.getIncomingMessages() != null && !messenger.getIncomingMessages().isEmpty())
			managers.add(generator.generate(DialogueManagerType.SIMPLE, messenger));
		if (messenger.getFrames() != null && !messenger.getFrames().isEmpty()) {
			for (Frame frame : messenger.getFrames()) {
				managers.add(generator.generate(DialogueManagerType.NAIVE, messenger, frame));
			}
		}
	}

	public void cancel() {

		this.lastAct = null;
		if (activeManager == null)
			return;

		System.out.println("cancel: " + this.activeManager);
		if (this.inputs.get(activeManager) != null)
			this.inputs.get(activeManager).clear();
		this.activeManager.reset();

	}

	public DialogueAct revert() {

		assert this.activeManager != null : "active manager is null";

		if (this.activeManager != null) {
			List<MessageInfo> infos = this.inputs.get(activeManager);
			if (this.activeManager.getStartIntent() != null)
				if (!infos.isEmpty())
					infos.remove(infos.size() - 1);
			replay(activeManager);

		}
		return this.lastAct;

	}

	public void replay(AbstractDialogueManager manager) {

		assert manager != null : "manager is null";
		assert this.inputs.containsKey(manager) : "key manager not contained";

		List<MessageInfo> list = new ArrayList<>(this.inputs.get(manager));
		this.inputs.get(manager).clear();
		manager.reset();
		for (MessageInfo info : list) {
			this.lastAct = this.handle(manager, info);
		}

	}

	public boolean isEmpty() {
		return this.inputs.isEmpty();
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

	public AbstractDialogueManager getActiveManager() {
		return activeManager;
	}
	
	public boolean hasActiveFrame() {
		if(this.activeManager == null)
			return false;
		if(this.activeManager instanceof TaskOrientedManager) 
			return true;
		return false;
			
	}
	
	public Frame getActiveFrame() {		
		if(!hasActiveFrame())
			return null;		
		TaskOrientedManager manager = (TaskOrientedManager) this.activeManager;
		return manager.getFrame();		
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
