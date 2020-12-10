package i5.las2peer.services.socialBotManagerService.chat;

public class MoodleForumMessageCollector extends ChatMessageCollector {
	public void handle(String discussionid, String userEmail, String message) {
		String text = replaceUmlaute(message.replaceAll("\\<.*?>", ""));
		addMessage(new ChatMessage(discussionid, userEmail, text));
		//System.out.println("Debug --- Collector: " + messages.toString());
	}
}
