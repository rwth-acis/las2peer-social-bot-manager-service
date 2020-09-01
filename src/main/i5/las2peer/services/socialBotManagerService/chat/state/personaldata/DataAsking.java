package i5.las2peer.services.socialBotManagerService.chat.state.personaldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import i5.las2peer.services.socialBotManagerService.chat.state.StatefulResponse;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;

public class DataAsking extends StatefulResponse {

	private RasaNlu rasa;
	private String email;
	private SQLDatabase database;

	public DataAsking(RasaNlu rasa, SQLDatabase database, String email) {
		this.rasa = rasa;
		this.database = database;
		this.email = email;
	}

	@Override
	public String getResponse() {
		return StaticResponses.dataAskingQuestion;
	}

	@Override
	public StatefulResponse getNext(String userMsg) {
		if (rasa.getIntent(userMsg).getKeyword().equals("positive")) {
			updateDataProvided(true);
			return new DataAskingAcceptedResponse(this.database, this.email);
		} else {
			updateDataProvided(false);
			return new DataAskingRejectedResponse();
		}
	}

	private void updateDataProvided(Boolean isProvided) {
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = database.getDataSource().getConnection();
			stmt = conn.prepareStatement("UPDATE users SET data_provided = ? WHERE email = ?");
			stmt.setBoolean(1, isProvided);
			stmt.setString(2, this.email);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
			;
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
			}
			;
		}
	}
}