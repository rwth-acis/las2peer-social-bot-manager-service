package i5.las2peer.services.socialBotManagerService.dialogue.manager;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Command;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.LanguageGenerator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;


@RunWith(MockitoJUnitRunner.class)
public class PipelineManagerTest {

	@Mock 
	Messenger messenger;	

    Map<String, LanguageUnderstander> mockedNLUs;
	Map<String, LanguageGenerator> mockedNLGs;
	
	@Mock
	LanguageUnderstander nlu;
		
	@Mock
	LanguageGenerator nlg;
	
	@Before
	public void setup() throws Exception {
		mockedNLUs = new HashMap<>();
		mockedNLGs = new HashMap<>();
		mockedNLUs.put("nlu", nlu);
		mockedNLGs.put("nlg", nlg);
	}
	
	@Test
	public void handleTest() {
	
		Mockito.when(messenger.getNLUS()).thenReturn(mockedNLUs);
		Mockito.when(messenger.getNLGS()).thenReturn(mockedNLGs);
		Mockito.when(messenger.getCommands()).thenReturn(new ArrayList<Command>());
		Mockito.when(messenger.getBot()).thenReturn(new Bot());
		
		Dialogue dialogue = new Dialogue(messenger);
		ChatMessage message = new ChatMessage("channel123", "user123", "message123");
		
		PipelineManager manager = new PipelineManager();
		ResponseMessage response = manager.handle(messenger, message, dialogue);
		
		assertNotNull(response);		
		
	}
	
}
