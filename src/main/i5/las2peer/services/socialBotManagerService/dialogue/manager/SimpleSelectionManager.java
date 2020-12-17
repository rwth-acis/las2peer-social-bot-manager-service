package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActType;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.model.Selection;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class SimpleSelectionManager extends AbstractDialogueManager {

	Selection selection;
	Map<String, AbstractDialogueManager> managers;
	AbstractDialogueManager active;

	public SimpleSelectionManager(Messenger messenger, Selection selection) {
		super();
		managers = new HashMap<>();
		init(messenger, selection);
	}

	public void init(Messenger messenger, Selection selection) {

		System.out.println("INIT SELECTION MANAGER " + selection.getElements().size());
		this.selection = selection;
		DialogueManagerGenerator generator = new DialogueManagerGenerator();
		// Collection<IncomingMessage> messages = new ArrayList<>();
		for (Entry<String, MessengerElement> element : selection.getElements().entrySet()) {
			System.out.println("SELECTION ELEMENT CLASS " + element.getValue());
			if (element.getValue() instanceof Frame) {
				AbstractDialogueManager manager = generator.generate(DialogueManagerType.TASK_ORIENTED_RULE, messenger,
						(Frame) element, null);
				managers.put(element.getKey(), manager);
			}

			if (element.getValue() instanceof IncomingMessage) {
				// messages.add((IncomingMessage) element);
				ArrayList<IncomingMessage> messages = new ArrayList<>();
				messages.add((IncomingMessage) element.getValue());
				AbstractDialogueManager manager = generator.generateSimpleMessages(messages);
				managers.put(element.getKey(), manager);
			}
		}

	}

	@Override
	public DialogueAct handle(Intent intent) {

		// first call
		if (intent.getKeyword().contentEquals(selection.getIntent())) {
			System.out.println("SELECTION First call: "+intent.getKeyword());
			DialogueAct act = new DialogueAct();
			act.setIntentType(DialogueActType.SELECTION);
			act.setIntent(selection.getActIntent());

			ExpectedInput expected = new ExpectedInput();
			expected.setType(InputType.Enum);
			expected.setIntend(selection.getActIntent());
			for (Entry<String, MessengerElement> enu : selection.getElements().entrySet()) {
				String key = enu.getKey();
				MessengerElement value = enu.getValue();
				expected.addEnum(key);
				expected.setEntity("selection");
			}
			act.setExpected(expected);
			return act;
		}

		// selection
		String value = intent.getEntity("selection").getValue();
		System.out.println("SELECTION selection: " + value);	
		Collection<String> options = selection.getElements().keySet();
		System.out.println("SELECTION options: " + options.toArray()[0] + " " + options.toArray()[1]);
		System.out.println("SELECTION managers: " + managers.keySet().toArray()[0] + " " + managers.keySet().toArray()[1]);
		if (options.contains(value)) {			
			AbstractDialogueManager manager = managers.get(value);
			if (manager != null) {
				System.out.println("SELECTION etwa selection: " + intent.getKeyword());
				this.active = manager;
				if(this.active instanceof SimpleDialogueManager) {
					String message = ((SimpleDialogueManager) this.active).getMessage();
					DialogueAct act = new DialogueAct();
					act.setMessage(message);
					act.setFull(true);
					return act;
				}
					
				return manager.handle(intent);
			}
		}

		// continue
		if(active != null) {
			System.out.println("SELECTION Selection active: " + active.getStartIntent());
			return active.handle(intent);
		}
		
		return null;

	}

	@Override
	public void reset() {
		for (AbstractDialogueManager manager : this.managers.values())
			manager.reset();
	}

	@Override
	public Collection<String> getNLGIntents() {
		Collection<String> res = new ArrayList<>();
		for (AbstractDialogueManager manager : this.managers.values()) {
			if (manager.getNLGIntents() != null)
				res.addAll(manager.getNLGIntents());
		}
		return res;
	}

	@Override
	public Collection<String> getNLUIntents() {
		Collection<String> res = new ArrayList<>();
		res.add(selection.getIntent());
		res.add(selection.getActIntent());
		for (AbstractDialogueManager manager : this.managers.values()) {
			if (manager.getNLUIntents() != null)
				res.addAll(manager.getNLUIntents());
		}
		return res;
	}

}
