package i5.las2peer.services.socialBotManagerService.chat.state.personaldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import i5.las2peer.services.socialBotManagerService.chat.state.StatefulResponse;
import i5.las2peer.services.socialBotManagerService.chat.StaticResponses;

public class DataAskingAcceptedResponse extends StatefulResponse {

    private Connection con;
    private String email;

    public DataAskingAcceptedResponse(Connection con, String email) {
        this.con = con;
        this.email = email;
    }
    
    @Override
    public String getResponse() {
        return StaticResponses.dataAskingAccepptedResponse;
    }

    @Override
    public StatefulResponse getNext(String userMsg) {
        savePersonalData(userMsg);
        return new DataAskingPositiveFinish();
    }

    private void savePersonalData(String data) {
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("UPDATE users SET personal_data = ? WHERE email = ?");
            ps.setString(1, data);
            ps.setString(2, this.email);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}