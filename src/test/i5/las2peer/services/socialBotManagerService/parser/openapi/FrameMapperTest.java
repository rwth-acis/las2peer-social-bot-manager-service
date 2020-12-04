package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.DialogueGoal;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RepetitionNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.RootNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.SelectionNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.SequenceNode;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.task.goal.ValueNode;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import io.swagger.util.Json;

public class FrameMapperTest {

    @Test
    public void frameMapPetstoreV3Test() {

	FrameMapper mapper = new FrameMapper();
	ServiceFunction function = new ServiceFunction();
	function.setServiceName("https://petstore3.swagger.io");
	function.setFunctionName("addPet");
	Frame frame = mapper.map(function, new Frame());

	assertNotNull(frame.getDescendants());
	assertNotNull(frame.getSlots());
	assertEquals(1, frame.getSlots().size());
	assertNotNull(frame.getSlot("pet"));
	assertEquals(9, frame.getDescendants().size());
	assertNotNull(frame.getSlot("pet_name"));

	assertNotNull(frame.getCommand());
	assertEquals("Add a new pet to the store", frame.getMessage());


    }

    @Test
    public void frameMapPetstoreV2Test() {

	FrameMapper mapper = new FrameMapper();
	ServiceFunction function = new ServiceFunction();
	function.setHttpMethod("post");
	function.setServiceName("https://petstore.swagger.io/");
	function.setFunctionName("addPet");
	Frame frame = mapper.map(function, new Frame());

	assertNotNull(frame.getDescendants());
	assertNotNull(frame.getSlots());
	assertEquals(1, frame.getSlots().size());
	assertNotNull(frame.getSlot("Pet"));
	assertEquals(11, frame.getDescendants().size());
	assertNotNull(frame.getSlot("Pet_name"));

	assertNotNull(frame.getCommand());
	assertEquals("Add a new pet to the store", frame.getMessage());

    }

    // @Test
    public void frameMapBotTest() {

	FrameMapper mapper = new FrameMapper();
	ServiceFunction function = new ServiceFunction();
	function.setHttpMethod("post");
	function.setServiceName("http://localhost:8080/sbfmanager/swagger.json");
	function.setFunctionName("createBot");
	Frame frame = mapper.map(function, new Frame());

	assertNotNull(frame.getDescendants());
	assertEquals(18, frame.getDescendants().size());
	assertNotNull(frame.getSlot("Bot_name"));
	assertNotNull(frame.getSlot("Bot_function_type"));
	assertNotNull(frame.getSlot("Bot_function_type_ChitChat_messages_intent"));
	assertNotNull(frame.getSlot("Bot_function_type_ChitChat_messages_message"));
	assertNotNull(frame.getSlot("Bot_function_type_AccessService"));
	assertNotNull(frame.getSlot("Bot_function_type_AccessService_serviceURL"));

	assertNotNull(frame.getSlot("Bot_messenger_type"));
	assertNotNull(frame.getSlot("Bot_messenger_type_Slack"));
	assertNotNull(frame.getSlot("Bot_messenger_type_Telegram"));
	assertNotNull(frame.getSlot("Bot_messenger_type_Slack_token"));
	assertNotNull(frame.getSlot("Bot_messenger_type_Telegram_token"));

	assertNotNull(frame.getCommand());
	assertEquals("creates the bot.", frame.getCommand().getDescription());

	DialogueGoal goal = new DialogueGoal(frame);
	RootNode root = goal.getRoot();
	SequenceNode seq = (SequenceNode) root.getChildren().get(0);
	assertEquals(3, seq.getChildren().size());

	root.getAll().print();
	assertNotNull(goal.getNode("Bot_name"));
	assertNotNull(goal.getNode("Bot_function_type"));

	goal.fill(goal.getFillable("Bot_function_type"), "ChitChat");
	assertEquals("ChitChat", goal.getFillable("Bot_function_type").getValue());
	assertTrue(goal.getFillable("Bot_function_type").isFilled());
	assertFalse(goal.getFillable("Bot_function_type").isReady());
	SelectionNode ft = (SelectionNode) goal.getNode("Bot_function_type");

	System.out.println("ft");
	ft.getAll().print();

	// seq1
	SequenceNode se = (SequenceNode) ft.getChild("ChitChat");
	System.out.println("se");
	se.getAll().print();

	// rep
	System.out.println("rep");
	RepetitionNode rep = (RepetitionNode) se.getChildren().get(0);
	rep.getAll().print();

	// seq2
	System.out.println("se2");
	SequenceNode se2 = (SequenceNode) rep.getValueChildren().get(0);
	se2.getAll().print();

	ValueNode valno = (ValueNode) se2.getChildren().get(0);


	assertNotNull(goal.getNode("Bot_function_type_ChitChat_messages_intent"));
	assertNotNull(goal.getNode("Bot_function_type_ChitChat_messages_message"));


	System.out.println("#######################");
	Json.prettyPrint(goal.toJSON());

    }

}
