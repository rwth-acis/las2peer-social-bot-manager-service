package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class RootNodeTest {

	Frame frame;
	Slot slot;
	Slot slotA;
	Slot slotB;
	ArrayList<String> enums;

	@Before
	public void setUp() {

		frame = new Frame();
		slot = new Slot("slotS");
		slotA = new Slot("slotA");
		slotB = new Slot("slotB");

		slotA.setInputType(InputType.Free);
		slotB.setInputType(InputType.Free);

		frame.addSlot(slot);
		slot.setEnumList(enums);
		slot.addChild(slotA);
		slot.addChild(slotB);

		slotA.setRequired(true);
		slotB.setRequired(true);
		slotA.setParameter(new ServiceFunctionAttribute("", ""));
		slotB.setParameter(new ServiceFunctionAttribute("", ""));

	}

	@Test
	public void toJSONTest() {

		RootNode node = new RootNode(frame);
		assertNotNull(node.toJSON());

	}

}
