package i5.las2peer.services.socialBotManagerService.chat.state.personaldata;

import i5.las2peer.services.socialBotManagerService.chat.state.StatefulResponse;
import i5.las2peer.services.socialBotManagerService.chat.StaticResponses;

public class DataAskingRejectedResponse extends StatefulResponse {
    
    @Override
    public String getResponse() {
        return StaticResponses.dataAskingRejectedResponse;
    }

    @Override
    public StatefulResponse getNext(String userMsg) {
        return null;
    }
}