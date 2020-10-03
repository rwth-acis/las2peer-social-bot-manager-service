package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class DialogueGoalTest {
    
    Frame frame;
    Slot slot1;
    Slot slot2;
    Slot slot3;
    Slot slot4;
    Slot slot5;
    
    @Before
    public void setUp() {
	
	frame = new Frame();
	slot1 = new Slot("s1");
	slot2 = new Slot("s2");
	slot3 = new Slot("s3");
	slot4 = new Slot("s4");
	slot5 = new Slot("s5");
	frame.addSlot(slot1);
	frame.addSlot(slot2);
	slot1.addChild(slot3);
	slot1.addChild(slot4);
	slot4.addChild(slot5);

	slot2.setInputType(InputType.Number);
	slot3.setInputType(InputType.Free);
	slot5.setInputType(InputType.Number);
    }
    
    @Test
    public void testFill() {
	
	DialogueGoal goal = new DialogueGoal(frame);
	assertEquals(6, goal.getRoot().getAll().size());
	assertEquals(2, goal.getRoot().getChildren().size());

	goal.print();
	Fillable n2 = goal.getNode("s2");
	Fillable n3 = goal.getNode("s3");
	Fillable n5 = goal.getNode("s5");

	goal.fill(n2, "1234");
	assertTrue(goal.isFilled(n2));
	assertFalse(goal.isFilled(n3));
	assertFalse(goal.isFilled(n5));
	
	goal.fill(n3, "aaaa");
	assertTrue(goal.isFilled(n2));
	assertTrue(goal.isFilled(n3));
	assertFalse(goal.isFilled(n5));
    }
    
    @Test
    public void testGetRequired() {

	DialogueGoal goal = new DialogueGoal(frame);
	slot1.setSelection(true);
	slot2.setRequired(true);
	SlotSet slots = frame.getRequired();
	assertTrue(slots.contains(slot1));
	assertTrue(slots.contains(slot2));

    }

    @Test
    public void testGetRequiredSelection() {

	DialogueGoal goal = new DialogueGoal(frame);
	slot3.setRequired(true);
	slot5.setRequired(true);
	SlotSet slots = frame.getRequired();
	assertEquals(2, slots.size());
	assertTrue(slots.contains(slot3));
	assertTrue(slots.contains(slot5));
    }

}
