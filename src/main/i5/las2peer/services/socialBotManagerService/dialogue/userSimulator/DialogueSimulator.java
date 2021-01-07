package i5.las2peer.services.socialBotManagerService.dialogue.userSimulator;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.Dialogue;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.MetaDialogueManager;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

public class DialogueSimulator {

	public MetaDialogueManager bot;
	public UtteranceUserSimulator user;
	
	DialogueSimulator(MetaDialogueManager bot, UtteranceUserSimulator user) {
		this.bot = bot;
		this.user = user;		
	}
	
	public Dialogue simulate() {
		
		ChatMessage input = new ChatMessage("user", "123456", "start");		
		Dialogue dialogue = new Dialogue(new Messenger());
		ResponseMessage output = null;
		
		int maxSteps = 1000;
		int step = 0;
		while(output == null && step <= maxSteps) {
			step++;
			output = bot.handle(null, input, dialogue);
			System.out.println("bot: " + output.getMessage());
			
			input = new ChatMessage("user", "123456", user.handle(output.getMessage()));
			System.out.println("user: " + output.getMessage());
		}
		
		return dialogue;		
	}
	
	
}
