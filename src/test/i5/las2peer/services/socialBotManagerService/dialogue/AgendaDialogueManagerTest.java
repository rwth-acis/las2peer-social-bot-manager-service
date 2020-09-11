package i5.las2peer.services.socialBotManagerService.dialogue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.task.AgendaDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.task.AgendaDialogueNode;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;

public class AgendaDialogueManagerTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testHandle() {

    }

    @Test
    public void testReset() {

	// Intent hello = new Intent("hello", 1.0f);
	// Intent pathA = new Intent("pathA", 1.0f);
	// Intent pathB = new Intent("pathB", 1.0f);
	// Intent pathA1 = new Intent("pathA1", 1.0f);
	// Intent pathA2 = new Intent("pathA2", 1.0f);
	AgendaDialogueNode pathA2 = new AgendaDialogueNode("pathA2", new ArrayList<AgendaDialogueNode>(),
		new ArrayList<ServiceFunction>());
	AgendaDialogueNode pathA1 = new AgendaDialogueNode("pathA1", new ArrayList<AgendaDialogueNode>(),
		new ArrayList<ServiceFunction>());
	AgendaDialogueNode pathB = new AgendaDialogueNode("pathB", new ArrayList<AgendaDialogueNode>(),
		new ArrayList<ServiceFunction>());
	AgendaDialogueNode pathA = new AgendaDialogueNode("pathA", new ArrayList<AgendaDialogueNode>(),
		new ArrayList<ServiceFunction>());
	AgendaDialogueNode root = new AgendaDialogueNode("hello", new ArrayList<AgendaDialogueNode>(),
		new ArrayList<ServiceFunction>());

	root.addChild(pathA);
	root.addChild(pathB);

	AgendaDialogueManager manager = new AgendaDialogueManager();
	manager.setRoot(root);
	manager.reset();
	assertEquals(pathA, manager.stack.get(0));
	assertEquals(pathB, manager.stack.get(1));

    }

}
