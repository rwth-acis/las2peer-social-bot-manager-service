package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.Slot;

public class FrameMapperTest {

    @Test
    public void frameMapPetstoreTest() {

	FrameMapper mapper = new FrameMapper();
	ServiceFunction function = new ServiceFunction();
	function.setActionType(ActionType.REST);
	function.setHttpMethod("post");
	function.setServiceName("https://petstore3.swagger.io");
	function.setFunctionName("addPet");
	Frame frame = mapper.create(function, new Frame());

	assertNotNull(frame.getDescendants());
	assertNotNull(frame.getSlots());
	assertEquals(1, frame.getSlots().size());
	assertNotNull(frame.getSlot("pet"));
	assertEquals(9, frame.getDescendants().size());
	assertNotNull(frame.getSlot("pet_name"));

    }

    @Test
    public void frameMapMessengerTest() {

	FrameMapper mapper = new FrameMapper();
	ServiceFunction function = new ServiceFunction();
	function.setActionType(ActionType.REST);
	function.setHttpMethod("post");
	function.setServiceName("http://localhost:8080/sbfmanager/swagger.json");
	function.setFunctionName("createBot");
	Frame frame = mapper.create(function, new Frame());

	assertNotNull(frame.getDescendants());
	assertEquals(7, frame.getDescendants().size());
	assertNotNull(frame.getSlot("Messenger_type"));
	assertNotNull(frame.getSlot("Messenger_type_Slack"));

	assertNotNull(frame.getSlots());
	assertEquals(1, frame.getSlots().size());
	assertNotNull(frame.getSlot("Messenger"));
	assertEquals(1, frame.getSlot("Messenger").getChildren().size());
	assertNotNull(frame.getSlot("Messenger").getChild("Messenger_type"));
	Slot selection = frame.getSlot("Messenger").getChild("Messenger_type");
	assertEquals(2, selection.getChildren().size());
	assertTrue(selection.isSelection());
	assertNotNull(selection.getChild("Messenger_type_Slack"));
	assertNotNull(selection.getChild("Messenger_type_Telegram"));

	Slot slack = selection.getChild("Messenger_type_Slack");
	assertEquals("Slack", slack.getEntity());
	assertNotNull(slack.getChild("Messenger_type_Slack_appId"));

	Map<Slot, String> map = new HashMap<Slot, String>();
	map.put(frame.getSlot("Messenger_type"), "Slack");
	assertEquals(4, frame.getSlot("Messenger_type").getDescendants(map).size());
	assertFalse(frame.getSlot("Messenger_type").getDescendants(map)
		.contains(frame.getSlot("Messenger_type_Telegram_token")));

	assertFalse(frame.getSlot("Messenger").getDescendants(map)
		.contains(frame.getSlot("Messenger_type_Telegram_token")));
	assertFalse(frame.getDescendants(map).contains(frame.getSlot("Messenger_type_Telegram_token")));
	assertEquals(5, frame.getDescendants(map).size());


    }

}
