package i5.las2peer.services.socialBotManagerService.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class IncomingMessage implements MessengerElement {

	String intentKeyword;
	String entityKeyword;
	String NluID;

	ArrayList<ChatResponse> responses;

	// Intent keywords used as keys
	HashMap<String, IncomingMessage> followupMessages;

	String triggeredFunctionId;

	private static String[][] UMLAUT_REPLACEMENTS = { { new String("Ä"), "Ae" }, { new String("Ü"), "Ue" },
			{ new String("Ö"), "Oe" }, { new String("ä"), "ae" }, { new String("ü"), "ue" }, { new String("ö"), "oe" },
			{ new String("ß"), "ss" } };

	public static String replaceUmlaute(String orig) {
		String result = orig;

		for (int i = 0; i < UMLAUT_REPLACEMENTS.length; i++) {
			result = result.replace(UMLAUT_REPLACEMENTS[i][0], UMLAUT_REPLACEMENTS[i][1]);
		}

		return result;
	}

	public IncomingMessage(String intent, String NluID) {
		this.intentKeyword = replaceUmlaute(intent);
		this.followupMessages = new HashMap<String, IncomingMessage>();
		this.responses = new ArrayList<ChatResponse>();
		if (NluID == "") {
			this.NluID = "";
		} else
			this.NluID = NluID;
	}

	@Override
	public String getIntentKeyword() {
		return intentKeyword;
	}

	public String getEntityKeyword() {
		return entityKeyword;
	}

	public void setEntityKeyword(String entityKeyword) {
		this.entityKeyword = entityKeyword;
	}

	public String getNluID() {
		return NluID;
	}

	public HashMap<String, IncomingMessage> getFollowingMessages() {
		return followupMessages;
	}

	public void addFollowupMessage(String intentKeyword, IncomingMessage msg) {
		this.followupMessages.put(replaceUmlaute(intentKeyword), msg);
	}

	public void addResponse(ChatResponse response) {
		this.responses.add(response);
	}

	public void addResponse(String message) {
		ChatResponse response = new ChatResponse(message);
		this.responses.add(response);
	}

	public Collection<String> getResponseMessages() {
		Collection<String> res = new HashSet<>();
		for (ChatResponse response : this.responses)
			res.add(response.getResponse());
		return res;
	}

	public String getResponseMessage() {
		if (responses.isEmpty())
			return null;

		Random random = new Random();
		ChatResponse response = getResponse(random);
		return response.getResponse();
	}

	public ChatResponse getResponse(Random random) {
		if (responses.isEmpty()) {
			return null;
		} else {
			return responses.get(random.nextInt(responses.size()));
		}
	}

	public ArrayList<ChatResponse> getResponseArray() {
		
			return responses;
		
	}

	public void setTriggeredFunction(ServiceFunction triggeredFunction) {
		this.triggeredFunctionId = triggeredFunction.getId();
	}

	public String getTriggeredFunctionId() {
		return this.triggeredFunctionId;
	}

	@Override
	public Collection<String> getNLUIntents() {

		Collection<String> res = new HashSet<>();
		res.add(this.intentKeyword);
		if (this.followupMessages != null)
			for (IncomingMessage message : this.followupMessages.values())
				res.addAll(message.getNLUIntents());
		return res;
	}

	public Collection<String> getNLGIntents() {

		Collection<String> res = new HashSet<>();
		res.add(this.getIntentKeyword() + "_response");
		if (this.followupMessages != null)
			for (IncomingMessage message : this.followupMessages.values())
				res.addAll(message.getNLGIntents());
		return res;
	}

}
