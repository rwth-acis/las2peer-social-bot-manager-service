package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ParameterType;

public class MultiValueNodeTest {

	Slot slot;
	ServiceFunctionAttribute attr;

	@Before
	public void setUp() {
		
		slot = new Slot("celestial", ParameterType.BODY);
		slot.setArray(true);
	}

	@Test
	public void CreationTest() {

		MultiValueNode node = new MultiValueNode(slot);
		assertEquals(slot, node.getSlot());

	}

	@Test
	public void FillTest() {

		MultiValueNode node = new MultiValueNode(slot);
		node.fill("test");
		assertNotNull(node.getValues());
		assertEquals(1, node.getValues().size());
		node.fill("hello");
		assertNotNull(node.getValues());
		assertEquals(2, node.getValues().size());
		node.fill("world");
		assertNotNull(node.getValues());
		assertEquals(3, node.getValues().size());
		assertTrue(node.getValues().contains("test"));
		assertTrue(node.getValues().contains("hello"));
		assertTrue(node.getValues().contains("world"));

	}
	
	@Test
	public void getIntentsTest() {

		MultiValueNode node = new MultiValueNode(slot);
		assertNotNull(node.getInformIntent());
		assertTrue(node.getInformIntent().startsWith("inform"));
		assertNotNull(node.getRequestIntent());
		assertTrue(node.getRequestIntent().startsWith("request"));
	}

	@Test
	public void toJSONTest() {

		MultiValueNode node = new MultiValueNode(slot);
		node.fill("sun");
		node.fill("moon");
		node.fill("star");
		assertNotNull(node.toBodyJSON());
		assertEquals("{\"celestial\":[\"sun\",\"moon\",\"star\"]}", node.toBodyJSON().toJSONString());

	}

}
