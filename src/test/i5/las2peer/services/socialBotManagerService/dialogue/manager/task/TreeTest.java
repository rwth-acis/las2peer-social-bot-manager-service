package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RootNode;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class TreeTest {

	Slot[] slots;
	Frame frame;

	@Before
	public void setUp() {

		frame = new Frame();
		slots = new Slot[10];
		for (int i = 0; i < 10; i++)
			slots[i] = new Slot("s_" + i);

		frame.addSlot(slots[0]);
		frame.addSlot(slots[1]);
		slots[1].addChild(slots[2]);
		slots[1].addChild(slots[3]);
		slots[3].setSelection(true);
		slots[3].addChild(slots[4]);
		slots[3].addChild(slots[7]);
		slots[4].addChild(slots[5]);
		slots[4].addChild(slots[6]);
		slots[7].addChild(slots[8]);
		slots[7].addChild(slots[9]);

	}

	@Test
	public void BuildTreeTest() {

		RootNode tree = new RootNode(frame);
		assertNotNull(tree.getChildren());
		assertEquals(2, tree.getChildren().size());
		assertEquals(2, slots[1].getChildren().size());
		assertFalse(slots[2].hasChildren());
		assertEquals(2, slots[3].getChildren().size());
		assertTrue(slots[3].isSelection());
		assertEquals(2, slots[4].getChildren().size());
		assertEquals(2, slots[7].getChildren().size());
		assertFalse(slots[5].hasChildren());
		assertFalse(slots[6].hasChildren());
		assertFalse(slots[8].hasChildren());
		assertFalse(slots[9].hasChildren());

	}

	@Test
	public void IsReadyTest() {

		slots[5].setRequired(true);
		slots[8].setRequired(true);
		slots[9].setRequired(true);
		slots[1].setRequired(true);

		RootNode tree = new RootNode(frame);
		assertFalse(tree.isReady());

	}

}
