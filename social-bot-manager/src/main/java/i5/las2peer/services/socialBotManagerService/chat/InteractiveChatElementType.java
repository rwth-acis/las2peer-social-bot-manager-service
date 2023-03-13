package i5.las2peer.services.socialBotManagerService.chat;

public enum InteractiveChatElementType {
    BUTTON("button"),
    RADIO_BUTTON("radio_button"),
    CHECKBOX("checkbox");

    private String type;

    InteractiveChatElementType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
