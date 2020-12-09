package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class RepetitionNodeTest {

	Slot slotRep;
	Slot slotVaA;
	Slot slotVaB;

	@Before
	public void setUp() {

		slotRep = new Slot("slotRep");
		slotVaA = new Slot("slotVaA");
		slotVaB = new Slot("slotVaB");

		slotVaA.setInputType(InputType.Free);
		slotVaB.setInputType(InputType.Free);

		slotRep.setArray(true);
		slotRep.addChild(slotVaA);
		slotRep.addChild(slotVaB);

		slotVaA.setRequired(true);
		slotVaB.setRequired(true);

		slotVaA.setParameter(new ServiceFunctionAttribute("", ""));
		slotVaB.setParameter(new ServiceFunctionAttribute("", ""));

	}

	@Test
	public void CreationTest() {

		RepetitionNode node = new RepetitionNode(slotRep);
		assertNotNull(node.getValueChildren());
		assertEquals(1, node.getValueChildren().size());
		SequenceNode sn = (SequenceNode) node.getValueChildren().get(0);
		assertNotNull(sn.getChildren());
		assertEquals(2, sn.getChildren().size());

	}

	@Test
	public void ExtendTest() {

		RepetitionNode node = new RepetitionNode(slotRep);

		node.extend();
		assertNotNull(node.getValueChildren());
		assertEquals(2, node.getValueChildren().size());
		SequenceNode sn1 = (SequenceNode) node.getValueChildren().get(0);
		SequenceNode sn2 = (SequenceNode) node.getValueChildren().get(1);
		assertEquals(2, sn1.getChildren().size());
		assertEquals(2, sn2.getChildren().size());

	}

	@Test
	public void isReadyTest() {

		RepetitionNode node = new RepetitionNode(slotRep);
		assertFalse(node.isReady());

		SequenceNode sn1 = (SequenceNode) node.getValueChildren().get(0);
		ValueNode va1 = ((ValueNode) sn1.getChildren().get(0));
		va1.fill("vA");
		assertFalse(node.isReady());

		ValueNode va2 = ((ValueNode) sn1.getChildren().get(1));
		va2.fill("vB");
		assertFalse(node.isReady());

		node.close();
		assertTrue(node.isReady());

	}

	@Test
	public void toJSONTest() {

		RepetitionNode node = new RepetitionNode(slotRep);
		assertNotNull(node.toJSON());

	}
}
