package i5.las2peer.services.socialBotManagerService.chat;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;
import java.util.Vector;

public class RESTfulChatMediator extends ChatMediator{

    static HashMap<String,RESTfulChatResponse> chatsession = null;
	private RESTfulChatMessageCollector messageCollector = new RESTfulChatMessageCollector();

    // To we need a token? Prevent malicious attacks? 
    public RESTfulChatMediator(String authToken) {
        super(authToken);
        if(chatsession==null){
            chatsession = new HashMap<String,RESTfulChatResponse>();
            messageCollector = new RESTfulChatMessageCollector();
        }
    }

    @Override
    public void sendMessageToChannel(String channel, String text, Optional<String> id) {
        RESTfulChatResponse rcr = new RESTfulChatResponse(text);
        chatsession.put(channel, rcr);
    }

    @Override
    public void editMessage(String channel, String messageId, String message, Optional<String> id) {
        RESTfulChatResponse rcr = new RESTfulChatResponse(message);
        chatsession.put(channel, rcr);
    }

    @Override
    public void sendBlocksMessageToChannel(String channel, String blocks, String authToken, Optional<String> id) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateBlocksMessageToChannel(String channel, String blocks, String authToken, String ts,
            Optional<String> id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendFileMessageToChannel(String channel, File f, String text, Optional<String> id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Vector<ChatMessage> getMessages() {
        Vector<ChatMessage> messages = this.messageCollector.getMessages();
		return messages;
    }

    public RESTfulChatResponse getMessageForChannel(String channel) {
		return chatsession.getOrDefault(channel, new RESTfulChatResponse(""));
    }

    @Override
    public String getChannelByEmail(String email) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public ChatMessageCollector getMessageCollector(){
        return (ChatMessageCollector) messageCollector;
    }
    
}
