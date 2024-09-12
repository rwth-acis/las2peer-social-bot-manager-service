package services.socialBotManagerService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONArray;
import services.socialBotManagerService.service.SocialBotManagerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// TODO change to spring postgres database

@Tag(name="SocialBotManagerService", description = "A service for managing social bots.")
@RestController
@RequestMapping("/training")
public class TrainingResourceController {
	@Autowired
    private SocialBotManagerService service;

	/**
	 * Store training data in the database.
	 *
	 * @param body training data body
	 * 
	 * @param name training data name
	 *
	 * @return Returns an HTTP response with plain text string content.
	 */
	@Operation(summary = "Store Training Data", description = "Stores the current training data.")
	@PostMapping(value = "/{dataName}", consumes = "text/plain", produces = "text/plain")
	public ResponseEntity<String> storeData(String body, String name) {
		Connection con = null;
		PreparedStatement ps = null;
		String resp = null;

		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();

			// Check if data with given name already exists in database. If yes, update it.
			// Else, insert it
			ps = con.prepareStatement("SELECT * FROM training WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ps.close();
				ps = con.prepareStatement("UPDATE training SET data = ? WHERE name = ?");
				ps.setString(1, body);
				ps.setString(2, name);
				ps.executeUpdate();
			} else {
				ps.close();
				ps = con.prepareStatement("INSERT INTO training(name, data) VALUES (?, ?)");
				ps.setString(1, name);
				ps.setString(2, body);
				ps.executeUpdate();
			}

			resp = "Training data stored.";
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			;
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
			}
			;
		}

		return ResponseEntity.ok().body(resp);
	}

	/**
	 * Retrieve training data from database.
	 * 
	 * @param name training data name
	 *
	 * @return Returns an HTTP response with plain text string content.
	 */
	@Operation(summary = "Fetch Training Data", description = "Fetches the current training data.")
	@GetMapping(value = "/{dataName}", produces = "text/plain")
	public ResponseEntity<String> getData(@PathVariable("dataName") String name) {
		Connection con = null;
		PreparedStatement ps = null;
		Response resp = null;

		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();

			// Fetch data with given name
			ps = con.prepareStatement("SELECT * FROM training WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			rs.next();

			// Write serialised model in Blob
			String s = rs.getString("data");

			resp = s;
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			;
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
			}
			;
		}
		return ResponseEntity.ok().body(resp);
	}

	/**
	 * Retrieve the names of all datasets in the database.
	 * 
	 * 
	 * @return Returns an HTTP response with plain text string content.
	 */
	@Operation(tags = "getDatasets", summary = "Retrieve datasets", description = "Get all stored datasets.")
	@GetMapping(value = "/getDatasets", produces = "text/plain")
	public ResponseEntity<String> getDatasets() {
		Connection con = null;
		PreparedStatement ps = null;
		Response resp = null;

		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();

			ps = con.prepareStatement("SELECT name FROM training");
			ResultSet rs = ps.executeQuery();

			// Fetch all model names in the database
			JSONArray models = new JSONArray();
			while (rs.next()) {
				models.add(rs.getString("name"));
			}

			resp = models.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (Exception e) {
			}
			;
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
			}
			;
		}

		return ResponseEntity.ok().body(resp);
	}
}
