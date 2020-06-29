
package i5.las2peer.services.socialBotManagerService.chat.state;


public abstract class StatefulResponse {

    public abstract String getResponse();
    public abstract StatefulResponse getNext(String userMsg);
}
