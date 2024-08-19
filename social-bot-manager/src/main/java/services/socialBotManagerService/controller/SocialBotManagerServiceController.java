package services.socialBotManagerService.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Base64;
import java.util.List;

import javax.websocket.server.PathParam;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

// import com.mongodb.client.gridfs.GridFSBucket;
// import com.mongodb.client.gridfs.GridFSBuckets;
// import com.mongodb.client.gridfs.model.GridFSFile;
// import com.mongodb.client.model.Filters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.bson.BsonObjectId;
import org.bson.types.ObjectId;

import services.socialBotManagerService.nlu.TrainingHelper;
import services.socialBotManagerService.service.SocialBotManagerService;

// import javax.ws.rs.core.MediaType;

// import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
// import org.glassfish.jersey.media.multipart.FormDataParam;


@Tag(name="SocialBotManagerService", description = "A service for managing social bots.")
@RestController
@RequestMapping("/")
public class SocialBotManagerServiceController {
    @Autowired
    private SocialBotManagerService service;

    @Operation(tags = {"trainAndLoad"}, description = "Trains and loads an NLU model on the given Rasa NLU server instance.")
    @PostMapping(value = "/trainAndLoad", consumes = "application/json", produces = "text/plain")
	// TODO: Just an adapter, since the Rasa server doesn't support
	// "Access-Control-Expose-Headers"
	// and the model file name is returned as a response header... Remove and just
	// use Rasa's
	// API directly once that's fixed. The whole `TrainingHelper` class can be
	// deleted then as well.
	public ResponseEntity<String> trainAndLoad(String body) {
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		if (service.nluTrainThread != null && service.nluTrainThread.isAlive()) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Training still in progress.");
		}
		try {
			JSONObject bodyJson = (JSONObject) p.parse(body);
			String url = (String) bodyJson.get("url");
			String config = (String) bodyJson.get("config");
			String markdownTrainingData = (String) bodyJson.get("markdownTrainingData");
			String intents = (String) bodyJson.get("intents");
			// added to have a way to access the intents of the rasa server
			service.rasaIntents.put(url.split("://")[1], intents);
			service.nluTrain = new TrainingHelper(url, config, markdownTrainingData);
			service.nluTrainThread = new Thread(service.nluTrain);
			service.nluTrainThread.start();
			// TODO: Create a member for this thread, make another REST method to check
			// whether
			// training was successful.
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Doesn't signal that training and loading was successful, but that it was
		// started.
		return ResponseEntity.ok("Training started.");
	}

	@Operation(description = "Returns information about the training process started by the last invocation of `/trainAndLoad`.", tags = "")
	@GetMapping(value = "/trainAndLoadStatus", produces = "text/plain")
	// TODO: Just an adapter, since the Rasa server doesn't support
	// "Access-Control-Expose-Headers"
	// and the model file name is returned as a response header... Remove and just
	// use Rasa's
	// API directly once that's fixed. The whole `TrainingHelper` class can be
	// deleted then as well.
	public ResponseEntity<String> trainAndLoadStatus(String body) {
		if (service.nluTrainThread == null) {
			return ResponseEntity.ok("No training process was started yet.");
		} else if (service.nluTrainThread.isAlive()) {
			return ResponseEntity.ok("Training still in progress.");
		} else if (service.nluTrain.getSuccess()) {
			return ResponseEntity.ok("Training was successful.");
		} else {
			return ResponseEntity.ok("Training failed.");
		}
	}

	@Operation(description = "Returns the intents of a current Rasa Model.", tags = "")
	@GetMapping(value = "/{rasaUrl}/intents", produces = "application/json")
	public ResponseEntity<String> getIntents(@PathVariable("rasaUrl") String url) {
		if (service.rasaIntents.get(url) == null) {
			return ResponseEntity.ok("failed.");
		} else {
			String intents = service.rasaIntents.get(url);
			JSONObject ex = new JSONObject();
			ex.put("intents", intents);
			return ResponseEntity.ok(ex.toString());
		}
	}
    
}
