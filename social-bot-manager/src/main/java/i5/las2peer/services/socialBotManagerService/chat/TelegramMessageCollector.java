package i5.las2peer.services.socialBotManagerService.chat;

public class TelegramMessageCollector extends ChatMessageCollector {

    public void handle(String convid, String userEmail, String message) {
        String text = replaceUmlaute(message.replaceAll("\\<.*?>", ""));
        addMessage(new ChatMessage(convid, userEmail, text));
        //System.out.println("Debug --- Collector: " + messages.toString());
    }
}
