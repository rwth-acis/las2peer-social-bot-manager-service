package i5.las2peer.services.socialBotManagerService.dialogue.userSimulator;

import java.util.Collection;
import java.util.Random;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.manager.AbstractDialogueManager;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;

public class RandomUserSimulator {

    Collection<String> intents;
    Collection<Entity> entities;

    public RandomUserSimulator(AbstractDialogueManager manager) {

	intents = manager.getNLUIntents();
	intents.add("quarky");
	intents.add("inform_quarky");
	intents.add("request_quarky");
	intents.add("confirm_quarky");
	intents.add("deny_quarky");

    }

    public void setEntities(Collection<Entity> entities) {
	this.entities = entities;
    }

    public Intent handle(DialogueAct act) {

	String intent = getRandomIntent(intents);
	float conf = getRandomConf();

	Intent res = new Intent(intent, conf);
	if (entities != null) {
	    Entity entity = getRandomEntity(entities);
	    res.addEntity(entity.getEntityName(), entity);
	}

	res.setIntentType(res.deriveType());
	return res;
    }

    private String getRandomIntent(Collection<String> from) {
	Random rnd = new Random();
	int i = rnd.nextInt(from.size());
	return (String) from.toArray()[i];
    }

    private Entity getRandomEntity(Collection<Entity> from) {
	Random rnd = new Random();
	int i = rnd.nextInt(from.size());
	return (Entity) from.toArray()[i];
    }

    private float getRandomConf() {
	double rangeMin = 0.2f;
	double rangeMax = 1.0f;
	Random r = new Random();
	double createdRanNum = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
	return (float) (createdRanNum);
    }

}
