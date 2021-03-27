package i5.las2peer.services.socialBotManagerService.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.nlu.ConfirmationNLU;
import i5.las2peer.services.socialBotManagerService.parser.creation.CreatorBot;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.ChatFunction;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.Messenger;
import i5.las2peer.services.socialBotManagerService.parser.creation.messenger.TelegramMessenger;

public class BotModelParserTest {

	@Test
	public void parseBotTest() {
		
		CreatorBot bot = new CreatorBot();
		bot.setName("TestBot");
		bot.setDescription("test description");
		ChatFunction function = new ChatFunction("greet", "hello", "huhu", "hi");
		bot.addFunction(function);
		
		BotConfiguration config = new BotConfiguration();
		config.addNLU(new ConfirmationNLU());
		BotModelParser parser = new BotModelParser(config);
		
		BotModel model = parser.parse(bot);
		assertNotNull(model);
		assertTrue(model.containsNode("Bot"));
		assertTrue(model.containsNode("Incoming Message"));
		
	}
	
	
	@Test
	public void parseChatFunctionTest() {
		
		CreatorBot bot = new CreatorBot();
		bot.setName("TestBot");
		bot.setDescription("test description");
		ChatFunction function = new ChatFunction("greet", "hello", "huhu", "hi");
		bot.addFunction(function);
		
		TelegramMessenger messenger = new TelegramMessenger();
		messenger.setToken("asdb");
		ArrayList<Messenger> list = new ArrayList<>();
		list.add(messenger);
		bot.setMessenger(list);
		
		BotConfiguration config = new BotConfiguration();
		config.addNLU(new ConfirmationNLU());
		BotModelParser parser = new BotModelParser(config);
		BotModel model = parser.parse(bot);
		parser.read(model);
		parser.parse(function);
		
		BotModel res = parser.generate();
		assertNotNull(res);
		assertTrue(res.containsNode("Bot"));
		assertTrue(res.containsNode("Incoming Message"));
		
	}
	
}
