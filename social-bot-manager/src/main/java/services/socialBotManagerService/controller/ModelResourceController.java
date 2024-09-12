package services.socialBotManagerService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONArray;
import services.socialBotManagerService.model.BotModel;
import services.socialBotManagerService.service.SocialBotManagerService;

@Tag(name="SocialBotManagerService", description = "A service for managing social bots.")
@RestController
@RequestMapping("/models")
public class ModelResourceController {
	
    @Autowired
    private SocialBotManagerService service;
	/**
	 * Put Model function.
	 *
	 * @param name name of the model
	 * @param body content of the model
	 * @return Returns an HTTP response with plain text string content derived from
	 *         the path input param.
	 */
	@Operation(summary = "Save BotModel", description = "Stores the BotModel in the shared storage.")
	@PostMapping(value = "/{name}", consumes = "application/json", produces = "text/plain")
	public ResponseEntity<String> putModel(String name, BotModel body) {
		Connection con = null;
		PreparedStatement ps = null;
		String resp = null;

		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();

			// Write serialised model in Blob
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bOut);
			out.writeObject(body);
			Blob blob = con.createBlob();
			blob.setBytes(1, bOut.toByteArray());

			// Check if model with given name already exists in database. If yes, update it.
			// Else, insert it
			ps = con.prepareStatement("SELECT * FROM models WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				ps.close();
				ps = con.prepareStatement("UPDATE models SET model = ? WHERE name = ?");
				ps.setBlob(1, blob);
				ps.setString(2, name);
				ps.executeUpdate();
			} else {
				ps.close();
				ps = con.prepareStatement("INSERT INTO models(name, model) VALUES (?, ?)");
				ps.setString(1, name);
				ps.setBlob(2, blob);
				ps.executeUpdate();
			}

			resp = "Model stored.";
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

	@Operation(summary = "Retrieve BotModels", description = "Get all stored BotModels.")
	@GetMapping(value = "/getModels", produces = "application/json")
	public ResponseEntity<String> getModels() {
		Connection con = null;
		PreparedStatement ps = null;
		String resp = null;

		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();

			ps = con.prepareStatement("SELECT name FROM models");
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

	@Operation(summary = "Get BotModel by name", description = "Returns the BotModel for the given name.")
	@GetMapping(value = "/{name}", produces = "application/json")
	public ResponseEntity<String> getModelByName(String name) {
		Connection con = null;
		PreparedStatement ps = null;
		String resp = null;

		try {
			// Open database connection
			con = service.database.getDataSource().getConnection();

			// Fetch model with given name
			ps = con.prepareStatement("SELECT * FROM models WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			rs.next();

			// Write serialised model in Blob
			Blob b = rs.getBlob("model");
			InputStream stream = b.getBinaryStream();
			ObjectInputStream in = new ObjectInputStream(stream);
			BotModel model = (BotModel) in.readObject();

			resp = model.toString();
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
