package i5.las2peer.services.socialBotManagerService.chat.state.personaldata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import i5.las2peer.services.socialBotManagerService.chat.state.StatefulResponse;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;

public class DataAskingAcceptedResponse extends StatefulResponse {

	private String email;
	private SQLDatabase database;

	public DataAskingAcceptedResponse(SQLDatabase database, String email) {
		this.database = database;
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
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = database.getDataSource().getConnection();
			stmt = conn.prepareStatement("UPDATE users SET personal_data = ? WHERE email = ?");
			stmt.setString(1, data);
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