package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.TableLanguageGenerator;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.FallbackNlu;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;

public class PipelineManagerTest {

	Messenger messenger;	

    Map<String, LanguageUnderstander> nlus;
	Map<String, LanguageGenerator> nlgs;
	
	LanguageUnderstander nlu;

	LanguageGenerator nlg;
	
	@Before
	public void setup() throws Exception {
		
		messenger = new Messenger();
		messenger.addMessage(new IncomingMessage("greet", "hello"));
		Bot bot = new Bot();
		nlus = new HashMap<>();
		nlgs = new HashMap<>();
		LanguageUnderstander nlu = new FallbackNlu();
		LanguageGenerator nlg = new TableLanguageGenerator();
		nlus.put("nlu", nlu);
		nlgs.put("nlg", nlg);
		bot.setNLUs(nlus);
		bot.setNLGs(nlgs);
		messenger.setBot(bot);
	}
	
	@Test
	public void handleTest() {
					
		Dialogue dialogue = new Dialogue(messenger);
		ChatMessage message = new ChatMessage("channel123", "user123", "message123");
		
		PipelineManager manager = new PipelineManager(messenger);
		ResponseMessage response = manager.handle(messenger, message, dialogue);
		
		assertNotNull(response);		
		
	}
	
}
