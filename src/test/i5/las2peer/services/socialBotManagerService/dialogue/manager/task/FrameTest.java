package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class FrameTest {

    Slot slot1;
    Slot slot2;
    Slot slot3;
    Slot slot4;
    Slot slot5;

    @Before
    public void setUp() {

	slot1 = new Slot("s1");
	slot2 = new Slot("s2");
	slot3 = new Slot("s3");
	slot4 = new Slot("s4");
	slot5 = new Slot("s5");

    }

    @Test
    public void getDescendants() {
	Frame frame = new Frame();
	frame.addSlot(slot1);
	frame.addSlot(slot2);

	slot2.addChild(slot3);
	slot2.addChild(slot4);
	slot3.addChild(slot5);

	Collection<Slot> slots = frame.getDescendants();
	assertTrue(slots.contains(slot1));
	assertTrue(slots.contains(slot2));
	assertTrue(slots.contains(slot3));
	assertTrue(slots.contains(slot4));
	assertTrue(slots.contains(slot5));
	assertEquals(5, slots.size());
    }

    @Test
    public void testGetRequired() {
	Frame frame = new Frame();

	slot1.setRequired(false);
	slot2.setRequired(false);
	slot3.setRequired(true);
	slot4.setRequired(true);

	frame.addSlot(slot1);
	frame.addSlot(slot2);
	slot1.addChild(slot3);
	frame.addSlot(slot4);

	Collection<Slot> slots = frame.getRequired();
	assertTrue(slots.contains(slot3));
	assertTrue(slots.contains(slot4));
	assertFalse(slots.contains(slot1));
	assertFalse(slots.contains(slot2));
	assertEquals(2, slots.size());
    }

}
