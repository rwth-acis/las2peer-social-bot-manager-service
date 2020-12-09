package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;

public abstract class LanguageUnderstander {

	String name;
	String url;

	public Intent parse(ChatMessage message) {
		return this.getIntent(message.getText());
	}

	public abstract Intent getIntent(String message);

	public abstract Collection<String> getIntents();

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public abstract void addIntents(Collection<String> intents);
	
	public void addIntent(String intent) {
		Collection<String> intents = new ArrayList<>();
		intents.add(intent);
		addIntents(intents);
	}

	@Override
	public String toString() {
		return "LanguageUnderstander [name=" + name + ", url=" + url + "]";
	}

}
