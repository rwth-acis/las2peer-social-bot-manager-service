package i5.las2peer.services.socialBotManagerService.dialogue.notification;

import java.util.UUID;

import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.dialogue.nlg.ResponseMessage;
import i5.las2peer.services.socialBotManagerService.model.ServiceEvent;
import i5.las2peer.services.socialBotManagerService.model.Trigger;

public class EventToMessageTrigger {

	/**
	 * Identifier of this trigger
	 */
	String id;

	/**
	 * Event that activates the trigger
	 */
	ServiceEvent event;

	/**
	 * Message that is send after trigger is activated
	 */
	ResponseMessage message;

	/**
	 * Mediator that sends this message
	 */
	ChatMediator mediator;

	public EventToMessageTrigger(ServiceEvent event, ResponseMessage message, ChatMediator mediator) {
		this.id = UUID.randomUUID().toString();
		this.event = event;
		this.message = message;
		this.mediator = mediator;
	}

	public String getId() {
		return this.id;
	}

	public ServiceEvent getEvent() {
		return event;
	}

	public ResponseMessage getMessage() {
		return message;
	}

	public ChatMediator getMediator() {
		return this.mediator;
	}

	public boolean invariant() {
		if (id == null | event == null | message == null | mediator == null)
			return false;
		return true;
	}

	public void perform() {
		assert invariant() : "EventToMessageTrigger in invalid state";
		this.mediator.sendMessageToChannel(message);
	}
}
