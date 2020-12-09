package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class ValueNodeTest {

	Slot slot;
	ServiceFunctionAttribute attr;
	ArrayList<String> enums;

	@Before
	public void setUp() {

		slot = new Slot("name");
		slot.setInputType(InputType.Free);
		
		attr = new ServiceFunctionAttribute("", "");
		slot.setParameter(attr);

		enums = new ArrayList<String>();
		enums.add("A");
		enums.add("B");

	}

	@Test
	public void CreationTest() {

		ValueNode node = new ValueNode(slot);
		assertEquals(slot, node.getSlot());

	}

	@Test
	public void FillTest() {

		ValueNode node = new ValueNode(slot);

		node.fill("test");
		assertEquals("test", node.getValue());

	}

	@Test
	public void toJSONTest() {

		attr.setParameterType(ParameterType.CHILD);
		ValueNode node = new ValueNode(slot);
		assertNotNull(node.toJSON());

	}

}
