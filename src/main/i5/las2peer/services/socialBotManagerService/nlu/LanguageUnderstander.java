package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.parser.training.DataGroup;
import i5.las2peer.services.socialBotManagerService.parser.training.TrainingData;

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

	public abstract void addTrainingData(TrainingData data); 
	
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

	public void addTrainingData(String intentKeyword, List<String> responseMessages) {
		assert intentKeyword != null;
		assert responseMessages != null;
		System.out.println("number of responses " + responseMessages.size());
		DataGroup data = new DataGroup(intentKeyword, responseMessages);
		TrainingData res = new TrainingData();
		res.addDataGroup(data);
		this.addTrainingData(res);
	}

	public abstract TrainingData getTrainingData();

}
