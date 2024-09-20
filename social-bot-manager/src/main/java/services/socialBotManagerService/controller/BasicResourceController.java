package services.socialBotManagerService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;


import services.socialBotManagerService.chat.ChatMediator;
// import services.socialBotManagerService.chat.RocketChatMediator;
// import services.socialBotManagerService.chat.SlackChatMediator;
import services.socialBotManagerService.nlu.TrainingHelper;
import services.socialBotManagerService.service.SocialBotManagerService;


@Tag(name="SocialBotManagerService", description = "A service for managing social bots.")
@RestController
@RequestMapping("/")
public class BasicResourceController {
    @Autowired
    private SocialBotManagerService service;

	@GetMapping("/swagger.json")
	public ResponseEntity<JSONObject> getSwagger() {
		JSONObject swaggerJson = service.getSwagger();
		return ResponseEntity.ok(swaggerJson);
	}
	
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
			SocialBotManagerService.rasaIntents.put(url.split("://")[1], intents);
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

	// @Operation(summary = "Trigger slack chat message to slack user with given email", description = "Trigger slack chat message to slack user with given email")
	// @PostMapping(value = "/sendMessageToSlack/{token}/{email}", consumes = "application/json", produces = "text/plain")
	// public ResponseEntity<String> sendMessageToSlack(@PathVariable("token") String token, @PathVariable("email") String email, String input) {
	// 	// This function is a proof of concept. It is not the best in terms of run time,
	// 	// but optimization would require bigger changes
	// 	// in the code structure. To make it faster, the channel could be saved in a db
	// 	// once at first access, so the expensive API do not have to be called
	// 	// everytime.
	// 	try {
	// 		SlackChatMediator chatMediator = new SlackChatMediator(token);
	// 		System.out.println("slack mediator initialized");

	// 		// get user id from slack
	// 		try {
	// 			// slack api call to get email for user id
	// 			JSONParser p = new JSONParser();
	// 			JSONObject bodyInput = (JSONObject) p.parse(input);
	// 			String msgtext = (String) bodyInput.get("msg");
	// 			System.out.println("Using token " + token);
	// 			System.out.println("Using email " + email);

	// 			String channel = chatMediator.getChannelByEmail(email);
	// 			chatMediator.sendMessageToChannel(channel, msgtext, "text");

	// 		} catch (Exception e) {
	// 			e.printStackTrace();
	// 			return ResponseEntity.ok("Sending message failed.");
	// 		}

	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}

	// 	return ResponseEntity.ok("");
	// }
	
	// @Operation(summary = "Trigger rocket chat message to given rocket chat channel", description = "Trigger rocket chat message to given rocket chat channel")
	// @PostMapping(value = "/sendMessageToRocketChat/{token}/{email}/{channel}", consumes = "application/json", produces = "text/plain")
	// public ResponseEntity<String> sendMessageToRocketChat(@PathVariable("token") String token, @PathVariable("email") String email, @PathVariable("channel") String channel, String input) {
	// 	try {
	// 		RocketChatMediator chatMediator = new RocketChatMediator(token, service.database);
	// 		System.out.println("rocket chat mediator initialized");

	// 		try {
	// 			JSONParser p = new JSONParser(0);
	// 			JSONObject bodyInput = (JSONObject) p.parse(input);
	// 			String msgtext = (String) bodyInput.get("msg");
	// 			chatMediator.sendMessageToChannel(channel, msgtext, "text");
	// 		} catch (Exception e) {
	// 			e.printStackTrace();
	// 			return ResponseEntity.ok("Sending message failed.");
	// 		}

	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}

	// 	return ResponseEntity.ok("");
	// }

	// @Operation(summary = "edit chat message", description = "edit chat message")
	// @PostMapping(value = "/editMessage/{token}/{email}", consumes = "application/json", produces = "text/plain")
	// public ResponseEntity<String> editMessage(@PathVariable("token") String token, @PathVariable("email") String email, String input) {
	// 	System.out.println("received api call to edit message");
	// 	try {

	// 		ChatMediator chatMediator = null;
	// 		String channel = "";
	// 		if (token.startsWith("xoxb")) {
	// 			chatMediator = (SlackChatMediator) new SlackChatMediator(token);
	// 			channel = chatMediator.getChannelByEmail(email);
	// 		} else {
	// 			chatMediator = (RocketChatMediator) new RocketChatMediator(token, service.database);
	// 			channel = email;
	// 		}

	// 		try {
	// 			JSONParser p = new JSONParser(0);
	// 			JSONObject bodyInput = (JSONObject) p.parse(input);
	// 			String ts = (String) bodyInput.get("ts");
	// 			String blocks = (String) bodyInput.get("blocks");
	// 			System.out.println("Using token " + token + " ts " + ts + " blocks " + blocks);

	// 			chatMediator.editMessage(channel, ts, blocks, null);

	// 		} catch (Exception e) {
	// 			e.printStackTrace();
	// 			return ResponseEntity.ok("Editing chat failed.");
	// 		}

	// 	} catch (Exception ex) {
	// 		ex.printStackTrace();
	// 	}

	// 	return ResponseEntity.ok("");
	// }

}
