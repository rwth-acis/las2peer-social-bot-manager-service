package services.socialBotManagerService.controller;

import java.util.List;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import services.socialBotManagerService.model.Training;
import services.socialBotManagerService.service.SocialBotManagerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
	public ResponseEntity<String> storeData(HttpEntity<String> body, @PathVariable("dataName") String name) throws IOException {
		String resp = null;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bOut);
        out.writeObject(body.getBody());
		byte[] modelBytes = bOut.toByteArray();
		
		try {
			if (service.getTrainingByName(name) != null) {
				Training t = service.getTrainingByName(name);
				t.setData(modelBytes);
				service.createTraining(t);
			} else {
				Training t = new Training(name, modelBytes);
				service.createTraining(t);
			}

			resp = "Training data stored.";
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
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
		String resp = null;

		try {
			Training t = service.getTrainingByName(name);
			resp = service.convertFromBytes(t.getData()).toString();

		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
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
	@GetMapping(value = "/training/", produces = "text/plain")
	public ResponseEntity<String> getDatasets() {
		String resp = null;
		JSONArray arr = new JSONArray();
		try {
			List<Training> trainings = service.getAllTrainings();
			for (Training t : trainings) {
				arr.add(t.getName());
			}

			resp = arr.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		} 

		return ResponseEntity.ok().body(resp);
	}
}
