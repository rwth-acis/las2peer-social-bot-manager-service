package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActType;
import i5.las2peer.services.socialBotManagerService.dialogue.ExpectedInput;
import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.MessengerElement;
import i5.las2peer.services.socialBotManagerService.model.Selection;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;

public class DynamicSelectionManager extends AbstractDialogueManager {

	Selection selection;
	AbstractDialogueManager manager;
	String value;

	public DynamicSelectionManager(Selection selection) {
		super();
		init(selection);
	}

	private void init(Selection selection) {

		this.selection = selection;
		DialogueManagerGenerator generator = new DialogueManagerGenerator();

		for (Entry<String, MessengerElement> entry : selection.getElements().entrySet()) {
			MessengerElement element = entry.getValue();
			String key = entry.getKey();
			AbstractDialogueManager manager = generator.generate(element);
			this.manager = manager;
		}
	}

	@Override
	public DialogueAct handle(Intent intent) {

		// first call
		if (intent.getKeyword().contentEquals(selection.getIntentKeyword())) {
			assert this.selection != null;
			assert this.selection.getDynamicEntity() != null;
			this.selection.getDynamicEntity().invariantDynamic();
			
			DialogueAct act = DialogueActGenerator.getAct(this.selection);
			act.setIntentType(DialogueActType.SELECTION);
			act.setMessage(this.selection.getResponseMessage());

			ExpectedInput expected = new ExpectedInput();
			expected.setType(InputType.Enum);
			expected.setIntend(selection.getActIntent());
			for (String enu : selection.getEnums()) {			
				expected.addEnum(enu);
				expected.setEntity("selection");
			}
			act.setExpected(expected);
			return act;
		}

		// selection
		if (intent.getEntity("selection") != null) {

			String value = intent.getEntity("selection").getValue();
			Collection<String> options = selection.getEnums();
			if (options.contains(value)) {
				this.value = value;

				if (manager != null) {
					intent = new Intent(manager.getStartIntent(), 1.0f);
					intent.setIntentType(IntentType.START);

					if (selection.fillsParameter())
						manager.fillRecursive(selection.getParameterName(), value);

					return manager.handle(intent);
				}
			}
		}

		return manager.handle(intent);

	}

	@Override
	public void reset() {
		manager.reset();
	}

	@Override
	public void fillRecursive(String attrId, String value) {
		manager.fillRecursive(attrId, value);
	}

	@Override
	public Collection<String> getNLGIntents() {
		Collection<String> res = new ArrayList<>();
		if (manager.getNLGIntents() != null)
			res.addAll(manager.getNLGIntents());

		return res;
	}

	@Override
	public Collection<String> getNLUIntents() {
		Collection<String> res = new ArrayList<>();
		res.add(selection.getIntentKeyword());
		res.add(selection.getActIntent());
		if (manager.getNLUIntents() != null)
			res.addAll(manager.getNLUIntents());

		return res;
	}

	@Override
	public String getStartIntent() {
		return selection.getIntentKeyword();
	}

}
