package i5.las2peer.services.socialBotManagerService.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.nlu.ConfirmationNLU;
import i5.las2peer.services.socialBotManagerService.parser.creation.CreatorBot;
import i5.las2peer.services.socialBotManagerService.parser.creation.function.ChatFunction;

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
	
}
