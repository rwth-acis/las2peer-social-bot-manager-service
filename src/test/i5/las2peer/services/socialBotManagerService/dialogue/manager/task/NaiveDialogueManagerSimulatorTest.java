package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.DefaultDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.userSimulator.RandomUserSimulator;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.IntentType;
import i5.las2peer.services.socialBotManagerService.parser.openapi.FrameMapper;

public class NaiveDialogueManagerSimulatorTest {

    @SuppressWarnings("deprecation")
	@Test
    public void RandomTest() {

	ServiceFunction action = new ServiceFunction();
	action.setFunctionName("addPet");
	action.setServiceName("https://petstore3.swagger.io");
	Frame frame = new Frame();
	frame.setName("testFrame");
	frame.setIntent("testFrameIntent");
	frame = new FrameMapper().create(action, frame);

	DialogueGoal goal = new DialogueGoal(frame);
	DefaultDialogueManager manager = new DefaultDialogueManager(goal);
	manager.setStartIntent("start_intent");

	RandomUserSimulator simulator = new RandomUserSimulator(manager);
	System.out.println(manager.getNLUIntents());
	int maxSteps = 200;
	Intent intent = new Intent("greet", 1.0f);
	intent.setIntentType(IntentType.START);
	for (int i = 0; i <= maxSteps; i++) {
	    DialogueAct act = manager.handle(intent);
	    assertNotNull(act);
	    // assertTrue(act.hasIntent() || act.hasMessage());
	    intent = simulator.handle(act);
	    assertNotNull(intent);
	}

    }

}
