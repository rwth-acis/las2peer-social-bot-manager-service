package i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class SequenceNodeTest {

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

	slotS.setEnumList(enums);
	slotS.addChild(slotA);
	slotS.addChild(slotB);

	slotA.setRequired(true);
	slotB.setRequired(true);

    }

    @Test
    public void CreationTest() {

	SequenceNode node = new SequenceNode(slotS);

	assertNotNull(node.getChildren());
	assertEquals(2, node.getChildren().size());
	assertNotNull(node.getChildren().get(0));
	assertNotNull(node.getChildren().get(1));

    }

    @Test
    public void isReadyTest() {

	SequenceNode node = new SequenceNode(slotS);

	assertFalse(node.isReady());
	((Fillable) node.getChildren().get(0)).fill("At");
	assertFalse(node.isReady());
	((Fillable) node.getChildren().get(1)).fill("Bt");
	assertTrue(node.isReady());

	System.out.println(node.toJSON());

    }

    @Test
    public void toJSONTest() {

	SequenceNode node = new SequenceNode(slotS);
	assertNotNull(node.toJSON());
	((Fillable) node.getChildren().get(0)).fill("At");
	((Fillable) node.getChildren().get(1)).fill("Bt");
	assertEquals("{\"slotB\":\"Bt\",\"slotA\":\"At\"}", node.toJSON().toString());

    }

}
