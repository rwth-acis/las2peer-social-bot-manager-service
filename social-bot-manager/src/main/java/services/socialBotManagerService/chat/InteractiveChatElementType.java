package services.socialBotManagerService.chat;

public enum InteractiveChatElementType {
    BUTTON("button"),
    RADIO_BUTTON("radio_button"),
    CHECKBOX("checkbox"),
    DROPDOWN("dropdown"),
    TEXT("text");

    private String type;

    InteractiveChatElementType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
