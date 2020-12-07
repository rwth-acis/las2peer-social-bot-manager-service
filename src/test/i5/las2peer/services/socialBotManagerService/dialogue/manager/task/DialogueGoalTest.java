package i5.las2peer.services.socialBotManagerService.dialogue.manager.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.InputType;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.Fillable;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RepetitionNode;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

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
	slot2.setRequired(true);
	slot3.setRequired(true);
	slot5.setRequired(false);

    }
    
    @Test
    public void testFill() {
	
	DialogueGoal goal = new DialogueGoal(frame);
	assertEquals(6, goal.getRoot().getAll().size());
	assertEquals(2, goal.getRoot().getChildren().size());

	goal.print();
	Fillable n2 = goal.getFillable("s2");
	Fillable n3 = goal.getFillable("s3");
	Fillable n5 = goal.getFillable("s5");

	assertFalse(goal.isReady());
	assertFalse(goal.isFull());

	Fillable no = (Fillable) goal.next();
	assertEquals(no, n3);

	goal.fill(n2, "1234");
	assertTrue(goal.isFilled(n2));
	assertFalse(goal.isFilled(n3));
	assertFalse(goal.isFilled(n5));
	
	no = (Fillable) goal.next();
	assertEquals(no, n3);

	goal.fill(n3, "aaaa");
	assertTrue(goal.isFilled(n2));
	assertTrue(goal.isFilled(n3));
	assertFalse(goal.isFilled(n5));

	assertTrue(goal.isReady());
	assertFalse(goal.isFull());

	no = (Fillable) goal.next();
	assertEquals(no, n5);

	goal.fill(n5, "123");
	assertTrue(goal.isFilled(n2));
	assertTrue(goal.isFilled(n3));
	assertTrue(goal.isFilled(n5));

	assertTrue(goal.isReady());
	assertTrue(goal.isFull());
    }
    
    @Test
    public void testNextRepetitionNode() {
		
	frame = new Frame();
	slot1 = new Slot("s1");
	slot2 = new Slot("s2");
	slot3 = new Slot("s3");
	
	frame.addSlot(slot1);	
	slot1.addChild(slot2);
	slot1.addChild(slot3);
	
	slot1.setRequired(true);
	slot1.setArray(true);
	
	slot2.setInputType(InputType.Free);
	slot3.setInputType(InputType.Free);
	slot2.setRequired(true);	
	slot3.setRequired(true);

	
	DialogueGoal goal = new DialogueGoal(frame);
	assertEquals(5, goal.getRoot().getAll().size());
	assertEquals(1, goal.getRoot().getChildren().size());
	
	goal.print();
	assertTrue(goal.getNode(slot1.getName()) instanceof RepetitionNode);

	RepetitionNode n1 = (RepetitionNode) goal.getNode(slot1.getName());
	Fillable n2 = (Fillable) goal.getNode(slot2.getName());
	Fillable n3 = (Fillable) goal.getNode(slot3.getName());

	assertEquals(n2, goal.next());
	assertEquals(n2, goal.next());
	n2.fill("test");
	assertEquals(n3, goal.next());
	n3.fill("test");
	assertEquals(n1, goal.next());
	assertFalse(goal.isFull());
	assertFalse(goal.isReady());
	n1.close();
	assertTrue(goal.isReady());
	assertTrue(goal.isFull());

    }

    @Test
    public void testGetRequired() {

	DialogueGoal goal = new DialogueGoal(frame);
	slot1.setSelection(true);
	slot2.setRequired(true);
	SlotList slots = frame.getRequired();
	assertTrue(slots.contains(slot1));
	assertTrue(slots.contains(slot2));

    }

    @Test
    public void testGetRequiredSelection() {

	DialogueGoal goal = new DialogueGoal(frame);
	slot3.setRequired(true);
	slot5.setRequired(true);
	SlotList slots = frame.getRequired();
	assertEquals(3, slots.size());
	assertTrue(slots.contains(slot3));
	assertTrue(slots.contains(slot5));
    }

    @Test
    public void testCreationTest() {

	DialogueGoal goal = new DialogueGoal(frame);
	slot3.setRequired(true);
	slot5.setRequired(true);
	SlotList slots = frame.getRequired();
	assertEquals(3, slots.size());
	assertTrue(slots.contains(slot3));
	assertTrue(slots.contains(slot5));
    }

    @Test
    public void toJSONTest() {

	JSONObject name = new JSONObject();
	name.put("name", "Botter");

	JSONObject tf = new JSONObject();
	tf.put("type", "AccessService");

	JSONObject sf = new JSONObject();
	sf.put("type", "Telegram");

	JSONArray func = new JSONArray();
	func.add(tf);
	
	JSONArray mes = new JSONArray();
	mes.add(sf);

    }

}
