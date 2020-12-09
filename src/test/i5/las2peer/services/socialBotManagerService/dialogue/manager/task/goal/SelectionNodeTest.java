package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class SelectionNodeTest {

	Slot slotS;
	Slot slotA;
	Slot slotB;
	ArrayList<String> enums;

	@Before
	public void setUp() {

		slotS = new Slot("slotS");
		slotA = new Slot("slotA");
		slotB = new Slot("slotB");

		slotA.setInputType(InputType.Free);
		slotB.setInputType(InputType.Free);

		enums = new ArrayList<String>();
		enums.add("A");
		enums.add("B");

		slotS.setSelection(true);
		slotS.setInputType(InputType.Enum);
		slotS.setEnumList(enums);
		slotS.addChild(slotA);
		slotS.addChild(slotB);

		slotA.setEntity("A");
		slotB.setEntity("B");
		slotA.setRequired(true);
		slotB.setRequired(true);
		slotA.setParameter(new ServiceFunctionAttribute("", ""));
		slotB.setParameter(new ServiceFunctionAttribute("", ""));

	}

	@Test
	public void CreationTest() {

		SelectionNode node = new SelectionNode(slotS);

		assertEquals(slotS, node.getSlot());
		assertNotNull(node.getChildren());
		assertEquals(2, node.getChildren().size());
		assertNotNull(node.getChildren().get("A"));
		assertNotNull(node.getChildren().get("B"));

	}

	@Test
	public void isReadyTest() {

		SelectionNode node = new SelectionNode(slotS);

		assertFalse(node.isReady());
		node.fill("A");
		assertFalse(node.isReady());
		assertNotNull(node.getChild("A"));
		((ValueNode) node.getChild("A")).fill("tA");
		assertTrue(node.isReady());
		node.clear();
		assertFalse(node.isReady());
		node.fill("A");
		assertNotNull(node.getChild("B"));
		((ValueNode) node.getChild("B")).fill("tB");

	}

	@Test
	public void toJSONTest() {

		SelectionNode node = new SelectionNode(slotS);
		assertNotNull(node.toJSON());

	}

}
