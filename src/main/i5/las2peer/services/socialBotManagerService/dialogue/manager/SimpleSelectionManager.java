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
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.model.Selection;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;

public class SimpleSelectionManager extends AbstractDialogueManager {

	Selection selection;
	Map<String, AbstractDialogueManager> managers;
	AbstractDialogueManager active;

	public SimpleSelectionManager(Selection selection) {
		super();
		managers = new HashMap<>();
		init(selection);
	}

	public void init(Selection selection) {

		System.out.println("INIT SELECTION MANAGER " + selection.getElements().size());
		this.selection = selection;
		this.setStartIntent(selection.getIntent());
		DialogueManagerGenerator generator = new DialogueManagerGenerator();
		
		for (Entry<String, MessengerElement> entry : selection.getElements().entrySet()) {			
			MessengerElement element = entry.getValue();
			String key = entry.getKey();
			System.out.println("SELECTION ELEMENT CLASS " + element);
			
			AbstractDialogueManager manager = generator.generate(element);			
			managers.put(key, manager);
		}

	}

	@Override
	public DialogueAct handle(Intent intent) {

		// first call
		if (intent.getKeyword().contentEquals(selection.getIntent())) {
			System.out.println("SELECTION First call: " + intent.getKeyword());
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
		if (intent.getEntity("selection") != null) {
			String value = intent.getEntity("selection").getValue();
			System.out.println("SELECTION second call selection: " + value);
			Collection<String> options = selection.getElements().keySet();			
			if (options.contains(value)) {
				AbstractDialogueManager manager = managers.get(value);
				if (manager != null) {
					System.out.println("SELECTION etwa selection: " + intent.getKeyword());
					this.active = manager;

					// message
					if (this.active instanceof MultiMessageDialogueManager) {
						String message = ((MultiMessageDialogueManager) this.active).getMessage();
						DialogueAct act = new DialogueAct();
						act.setMessage(message);
						act.setFull(true);
						return act;
					}

					// frame
					intent = new Intent(manager.getStartIntent(), 1.0f);
					intent.setIntentType(IntentType.START);
					return manager.handle(intent);
					
				}
			}
		}

		// continue
		if (active != null) {
			System.out.println("SELECTION third call Selection active: " + active.getStartIntent());
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
