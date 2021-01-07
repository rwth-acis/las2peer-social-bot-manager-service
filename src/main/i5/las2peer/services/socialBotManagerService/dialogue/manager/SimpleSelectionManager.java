package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActType;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.TaskOrientedManager;
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.model.Selection;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;

public class SimpleSelectionManager extends AbstractDialogueManager {

	Selection selection;
	Map<String, AbstractDialogueManager> managers;
	AbstractDialogueManager active;
	OpenAPIAction responseAction;
	String value;

	public SimpleSelectionManager(Selection selection) {		
		super();
		assert selection != null;
		
		managers = new HashMap<>();
		init(selection);
	}

	private void init(Selection selection) {

		System.out.println("INIT SELECTION MANAGER " + selection.getElements().size());
		this.selection = selection;
		DialogueManagerGenerator generator = new DialogueManagerGenerator();

		for (Entry<String, MessengerElement> entry : selection.getElements().entrySet()) {
			MessengerElement element = entry.getValue();
			String key = entry.getKey();
			System.out.println("SELECTION ELEMENT CLASS " + element);

			AbstractDialogueManager manager = generator.generate(element);
			managers.put(key, manager);
		}

		if (selection.isDynamic())
			responseAction = new OpenAPIAction(selection.getResponseFunction());

	}

	@Override
	public DialogueAct handle(Intent intent) {

		// first call
		if (intent.getKeyword().contentEquals(selection.getIntentKeyword())) {
			System.out.println("SELECTION First call: " + intent.getKeyword());
			
			DialogueAct act = DialogueActGenerator.getAct(selection);
			act.setIntentType(DialogueActType.SELECTION);
			act.setMessage(this.selection.getResponseMessage());

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
			
			if(selection.isDynamic()) {
				this.responseAction.setResponseParseMode(ResponseParseMode.JSON_TO_MARKDOWN);
				act.setAction(this.responseAction);				
			}
			
			return act;
		}

		// selection
		if (intent.getEntity("selection") != null) {
			String value = intent.getEntity("selection").getValue();
			System.out.println("SELECTION second call selection: " + value);
			Collection<String> options = selection.getElements().keySet();
			if (options.contains(value)) {
				this.value = value;
				AbstractDialogueManager manager = managers.get(value);

				// start new manager
				if (manager != null) {
					System.out.println("selection intent: " + intent.getKeyword() + " on manager " + manager.getClass() + " intent: " + manager.getStartIntent());;
					this.active = manager;
					intent = new Intent(manager.getStartIntent(), 1.0f);
					intent.setIntentType(IntentType.START);

					if (selection.fillsParameter()) {
						if (manager instanceof TaskOrientedManager)
							((TaskOrientedManager) manager).getDialogueGoal().fill(selection.getParameterName(), value);
					}

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
	public void fillRecursive(String attrId, String value) {

		System.out.println("simple selection " + this.getStartIntent() + " try to fill " + attrId + " with " + value);
		if (this.selection.isDynamic()) {
			ServiceFunction function = this.selection.getResponseFunction();
			ServiceFunctionAttribute attr = function.getAttribute(attrId);
			if (attr != null) {
				this.responseAction.addParameter(attr, value);
				System.out.println(
						"fill recursive " + attr.getName() + " filled in simple selection " + this.getStartIntent());
				return;
			}
		}

		for (AbstractDialogueManager manager : this.managers.values())
			manager.fillRecursive(attrId, value);
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
		res.add(selection.getIntentKeyword());
		res.add(selection.getActIntent());
		for (AbstractDialogueManager manager : this.managers.values()) {
			if (manager.getNLUIntents() != null)
				res.addAll(manager.getNLUIntents());
		}
		return res;
	}

	@Override
	public String getStartIntent() {
		return selection.getIntentKeyword();
	}

}
