package i5.las2peer.services.socialBotManagerService.dialogue.notification;

import java.util.Collection;

import i5.las2peer.services.socialBotManagerService.model.Bot;

public class TriggerHandler {

	public void trigger(EventMessage event, Collection<Bot> bots) {

		assert event != null;
		assert event.invariant();
		assert bots != null;

		if (event.getEventId() == null)
			return;

		System.out.println("search for event trigger " + event.getEventName() + " with id " + event.getEventId());

		String eventId = event.getEventId();
		String eventName = event.getEventName();
		for (Bot bot : bots) {
			if (bot.hasActiveTrigger(eventId)) {
				EventToMessageTrigger trigger = bot.getActiveTrigger(eventId, eventName);
				System.out.println("found on bot " + bot.getName());
				trigger.perform();
				bot.removeActiveTrigger(eventId);
			}
		}
	}

}
