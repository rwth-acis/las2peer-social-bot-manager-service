package i5.las2peer.services.socialBotManagerService.nlu;

import java.util.Collection;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;

/**
 * Language Understanding Module
 */
public abstract class LanguageUnderstander {

	/**
	 * Requests the intent detection of this nlu module.
	 * 
	 * @param message input
	 * @return detected Intent
	 */
	public Intent parse(ChatMessage message) {
		return this.parse(message.getText());
	}

	/**
	 * Requests the intent detection of this nlu module.
	 * 
	 * @param message input
	 * @return detected Intent
	 */
	public abstract Intent parse(String message);

	/**
	 * Returns all intents that are registered for this nlu module
	 * 
	 * @return known intents
	 */
	public abstract Collection<String> getIntents();
		
	public void addTrainingData(TrainingData data) {		
		assert data != null;
		
		for(TrainingDataEntry entry :data.getEntries()) {
			if(entry.validate())
				addTrainingData(entry.getIntent(), entry.getExamples());
		}		
	}
	
	public void addTrainingData(String intentKeyword, List<String> responseMessages) {
		assert intentKeyword != null;
		assert responseMessages != null;
		
		TrainingDataEntry data = new TrainingDataEntry(intentKeyword, responseMessages);
		this.addTrainingData(data);		
	}
	
	public abstract void addTrainingData(TrainingDataEntry data);
	
	public abstract TrainingData getTrainingData();

	protected abstract void setTrainingData(TrainingData data);
	
	public abstract String getName();
	
	public abstract String getUrl();

	@Override
	public String toString() {
		return "LanguageUnderstander [name=" + this.getName() + ", url=" + this.getUrl() + "]";
	}

}
