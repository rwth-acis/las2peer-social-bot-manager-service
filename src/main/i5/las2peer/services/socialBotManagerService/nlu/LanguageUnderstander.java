package i5.las2peer.services.socialBotManagerService.nlu;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;

public abstract class LanguageUnderstander {
	
	public Intent parse(ChatMessage message) {		
		return this.getIntent(message.getText());		
	}
	
	public abstract Intent getIntent(String message);
	
}
