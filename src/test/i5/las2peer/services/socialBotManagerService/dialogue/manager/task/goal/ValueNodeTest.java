package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

		// slot
		slot = new Slot("slotName");
		slot.setInputType(InputType.Free);
		
		//attribute
		attr = new ServiceFunctionAttribute("attrId", "attrName");
		slot.setParameter(attr);

		// optional enum list
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
	public void isFilledTest() {

		ValueNode node = new ValueNode(slot);
		node.fill("test");
		assertTrue(node.isFilled());

	}
	
	@Test
	public void FillTest() {

		ValueNode node = new ValueNode(slot);

		node.fill("test");
		assertEquals("test", node.getValue());

	}

	@Test
	public void getIntentsTest() {

		ValueNode node = new ValueNode(slot);
		assertNotNull(node.getInformIntent());
		assertTrue(node.getInformIntent().startsWith("inform"));
		assertNotNull(node.getRequestIntent());
		assertTrue(node.getRequestIntent().startsWith("request"));
	}
	
	@Test
	public void toJSONTest() {

		attr.setParameterType(ParameterType.CHILD);
		ValueNode node = new ValueNode(slot);
		node.fill("hello");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"attrName\":\"hello\"}", node.toBodyJSON().toJSONString());
		
	}

}
