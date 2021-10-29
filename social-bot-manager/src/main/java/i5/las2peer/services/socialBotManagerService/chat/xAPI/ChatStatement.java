package i5.las2peer.services.socialBotManagerService.chat.xAPI;

public class ChatStatement {
	private xAPIActor actor;
	private xAPIVerb verb;
	private xAPIObject object;
	private xAPIContext context;

	public xAPIActor getActor() {
		return actor;
	}

	public void setActor(xAPIActor actor) {
		this.actor = actor;
	}

	public xAPIVerb getVerb() {
		return verb;
	}

	public void setVerb(xAPIVerb verb) {
		this.verb = verb;
	}

	public xAPIObject getObject() {
		return object;
	}

	public void setObject(xAPIObject object) {
		this.object = object;
	}

	public xAPIContext getContext() {
		return context;
	}

	public void setContext(xAPIContext context) {
		this.context = context;
	}

	public ChatStatement() {
		actor = new xAPIActor();
		verb = new xAPIVerb();
		object = new xAPIObject();
		context = new xAPIContext();
	}

	public static ChatStatement generate(String from, String to, String text, String time, String platform) {
		ChatStatement chatStatement = new ChatStatement();
		// actor
		chatStatement.getActor().setName(from);
		chatStatement.getActor().getAccount().setName(from);
		chatStatement.getActor().getAccount().setHomePage(platform);
		chatStatement.getActor().setObjectType("Agent");
		// verb
		chatStatement.getVerb().getDisplay().setEnEN("messaged");
		chatStatement.getVerb().setId("https://tech4comp.de/xapi/verb/messaged");
		// object
		chatStatement.getObject().setName(to);
		chatStatement.getObject().getAccount().setName(to);
		chatStatement.getObject().getAccount().setHomePage(platform);
		chatStatement.getObject().setObjectType("Agent");
		// context
		chatStatement.getContext().getExtension().getMessageInfo().setContent(text);
		chatStatement.getContext().getExtension().getMessageInfo().setTimeSent(time);
		// maybe add sth. like recognized intents, entities?
		return chatStatement;
	}
}
