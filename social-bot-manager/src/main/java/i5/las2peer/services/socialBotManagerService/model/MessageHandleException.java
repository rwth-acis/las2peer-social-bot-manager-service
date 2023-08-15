package i5.las2peer.services.socialBotManagerService.model;

public class MessageHandleException extends Exception {

    private static final long serialVersionUID = 1L;

    public MessageHandleException(String message) {
        super(message);
    }

    public MessageHandleException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageHandleException(Throwable cause) {
        super(cause);
    }
}
