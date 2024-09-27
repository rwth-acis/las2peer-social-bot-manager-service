package services.socialBotManagerService.controller;

import java.io.IOException;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import services.socialBotManagerService.model.BotModel;
import services.socialBotManagerService.model.Model;
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
	 * @throws IOException 
	 */
	@Operation(summary = "Save BotModel", description = "Stores the BotModel in the shared storage.")
	@PostMapping(value = "/{name}", consumes = "application/json", produces = "text/plain")
	public ResponseEntity<String> putModel(@PathVariable("name") String name, HttpEntity<BotModel> request) throws IOException {
		String resp = null;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bOut);
        out.writeObject(request.getBody());
		UUID id = UUID.randomUUID();

		byte[] modelBytes = bOut.toByteArray();
		
		try {
			if (service.getModelByName(name) == null) {
				Model m = new Model(id, name, modelBytes);
				service.createModel(m);
				resp = "Model " + m.getName() + " stored.";
			} else {
				Model m = service.getModelByName(name);
				m.setModel(modelBytes);
				service.createModel(m);
				resp = "Model " + name + " has been updated.";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		} 
		
		return ResponseEntity.ok().body(resp);
	}

	@Operation(summary = "Retrieve BotModels", description = "Get all stored BotModels.")
	@GetMapping(value = "/", produces = "application/json")
	public ResponseEntity<String> getModels() {
		String resp = null;
		JSONArray modelsArray = new JSONArray();

		try {
			List<Model> models = service.getAllModels();
			for (Model model : models) {
				modelsArray.add(model.getName());
			}
			resp = modelsArray.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		}

		return ResponseEntity.ok().body(resp);
	}

	@Operation(summary = "Get BotModel by name", description = "Returns the BotModel for the given name.")
	@GetMapping(value = "/{name}", produces = "application/json")
	public ResponseEntity<JSONObject> getModelByName(@PathVariable("name") String name) {

		JSONObject resp = null;

		try {

			Model model = service.getModelByName(name);
			byte[] modelBytes = model.getModel();
			BotModel botModel = (BotModel) service.convertFromBytes(modelBytes);
			resp = botModel.toJSON();
		} catch (Exception e) {
			e.printStackTrace();
			resp = new JSONObject();
			resp.put("error", e.getMessage());
		} 

		return ResponseEntity.ok().body(resp);
	}

	@Operation(summary = "Delete BotModel by name", description = "Deletes the BotModel for the given name.")
	@GetMapping(value = "/delete/{name}", produces = "text/plain")
	public ResponseEntity<String> deleteModelByName(@PathVariable("name") String name) {
		String resp = null;

		try {
			Model m = service.getModelByName(name);
			service.deleteModel(m.getId());
			resp = "Model " + name + " deleted.";
		} catch (Exception e) {
			e.printStackTrace();
			resp = e.getMessage();
		}

		return ResponseEntity.ok().body(resp);
	}
}
