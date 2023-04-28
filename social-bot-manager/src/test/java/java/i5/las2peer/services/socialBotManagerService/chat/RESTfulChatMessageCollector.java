package i5.las2peer.services.socialBotManagerService.chat;

public class RESTfulChatMessageCollector extends ChatMessageCollector{
    public void handle(String convid, String userEmail, String message) {
		String text = replaceUmlaute(message.replaceAll("\\<.*?>", ""));
		addMessage(new ChatMessage(convid, userEmail, text));
	}

	


    public void handle(String fileBody, String fileName, String fileType,
			String channel) {
        try {
            System.out.println("Handling Attachment.");
            
            ChatMessage cm = new ChatMessage(channel, channel, "", fileName, fileType, fileBody);
            cm.setEmail(channel);
            cm.setRole(0);

            // timestamp
            //cm.setTime(message.getMsgTimestamp().toInstant().toString());

            // domain
            //cm.setDomain(this.getDomain());

            this.addMessage(cm);
            System.out.println("Message added.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}
