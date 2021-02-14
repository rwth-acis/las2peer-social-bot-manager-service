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
import i5.las2peer.services.socialBotManagerService.model.GeneratorFunction;
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.model.Selection;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;

public class SimpleSelectionManager extends AbstractDialogueManager {

	Selection selection;
	Map<String, AbstractDialogueManager> managers;
	AbstractDialogueManager active;
	OpenAPIAction responseAction;
	String value;

	public SimpleSelectionManager(Selection selection) {		
		super();
		assert selection != null;
		assert !selection.isDynamic();
		
		managers = new HashMap<>();
		init(selection);
	}

	private void init(Selection selection) {

		assert selection != null;
		this.selection = selection;
		DialogueManagerGenerator generator = new DialogueManagerGenerator();

		for (Entry<String, MessengerElement> entry : selection.getElements().entrySet()) {
			MessengerElement element = entry.getValue();
			String key = entry.getKey();
			
			AbstractDialogueManager manager = generator.generate(element);
			managers.put(key, manager);
		}

		if (selection.getGeneratorFunction() != null)
			responseAction = new OpenAPIAction(selection.getGeneratorFunction());

	}

	@Override
	public DialogueAct handle(Intent intent) {

		// first call
		if (intent.getKeyword().contentEquals(selection.getIntentKeyword())) {
					
			// static
			DialogueAct act = DialogueActGenerator.getAct(selection);
			act.setIntentType(DialogueActType.SELECTION);
			act.setMessage(this.selection.getResponseMessage());

			// generated
			if(this.responseAction != null) 
				act = this.responseAction.generate(selection.getActIntent(), selection.getResponseMessage());
			
			ExpectedInput expected = new ExpectedInput(InputType.Enum, selection.getActIntent());			
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
				this.value = value;
				AbstractDialogueManager manager = managers.get(value);

				// start new manager
				if (manager != null) {
					this.active = manager;
					intent = new Intent(manager.getStartIntent(), 1.0f);
					intent.setIntentType(IntentType.START);

					if (selection.fillsParameter()) {
						if (manager instanceof TaskOrientedManager)
							((TaskOrientedManager) manager).fillRecursive(selection.getParameterName(), value);
						else 
							manager.fillRecursive(selection.getParameterName(), value);
					
					}
					
					

					return manager.handle(intent);
				}
			}
		}

		// continue
		if (active != null) {
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
		
		// generated Response function
		if (this.selection.getGeneratorFunction() != null) {
			GeneratorFunction function = this.selection.getGeneratorFunction();
			ServiceFunctionAttribute attr = function.getAttribute(attrId);
			if (attr != null) {
				this.responseAction.addParameter(attr, value);				
			}
		}
				
		// sub managers
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
