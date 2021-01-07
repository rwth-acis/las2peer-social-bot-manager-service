package i5.las2peer.services.socialBotManagerService.dialogue.userSimulator;

import java.util.Collection;
import java.util.Random;

import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;

public class DefaultUserSimulator extends UtteranceUserSimulator {

	Collection<String> uters;
	
	public DefaultUserSimulator(Bot bot) {
		
		super();
		uters = bot.getNLUIntents();
		uters.add("quarky");
		uters.add("hello");
		uters.add("yes");
		uters.add("no");
		uters.add("bye");
		uters.add("12345");
		uters.add("http://asdghadshgadg.de");

	}
	

	@Override
	public String handle(String input) {

		String res = getUter(uters);
		float conf = getRandomConf();
		
		return res;
	}

	private String getUter(Collection<String> from) {
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
