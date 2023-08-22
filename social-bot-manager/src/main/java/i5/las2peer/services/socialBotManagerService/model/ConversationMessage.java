package i5.las2peer.services.socialBotManagerService.model;

public class ConversationMessage {
    String conversationId;
    String role;
    String content;
    
    public ConversationMessage(String conversationId, String role, String content) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "ConversationMessage [role=" + role + ", content=" + content + "]";
    }

    public String getConversationId() {
        return conversationId;
    }
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
}
