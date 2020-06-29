package i5.las2peer.services.socialBotManagerService.chat.state.personaldata;

import i5.las2peer.services.socialBotManagerService.chat.state.StatefulResponse;
import i5.las2peer.services.socialBotManagerService.chat.StaticResponses;

public class DataAskingPositiveFinish extends StatefulResponse {

    @Override
    public String getResponse() {
        return StaticResponses.dataAskingPositiveFinish;
    }

    @Override
    public StatefulResponse getNext(String userMsg) {
        return null;
    }
}