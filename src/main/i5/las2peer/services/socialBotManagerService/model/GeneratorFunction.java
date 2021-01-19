package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActType;
import i5.las2peer.services.socialBotManagerService.parser.openapi.FunctionInterface;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIResponse;
import i5.las2peer.services.socialBotManagerService.parser.openapi.ResponseParseMode;

/**
 * Generate the Response dialogue act for messenger/dialogue elements Including
 * dynamic response calls
 *
 */
public class GeneratorFunction implements FunctionInterface {

	/**
	 * Indicates how to generate the response from the response
	 */
	ResponseParseMode mode;

	/**
	 * Function to call to get additional information
	 */
	ServiceFunction function;

	/**
	 * Additional information to interpret the function call response
	 */
	String key;

	/**
	 * Indicates the type of dialogue act this generator generates
	 */
	DialogueActType actType;

	/**
	 * Connects the variables of the input with the path of the JSON response
	 * (variable, path)
	 */
	Map<String, String> paths;

	/**
	 * (path, variable)
	 */
	Map<String, String> vars;

	/**
	 * Response Templates for diffrent response http codes (response http code,
	 * response template)
	 */
	// Map<Integer, String> messages;

	/**
	 * Creates a valid Generator Function
	 * 
	 * @param actType
	 * @param mode
	 * @param function
	 * @param key
	 */
	public GeneratorFunction(DialogueActType actType, ResponseParseMode mode, ServiceFunction function, String key) {
		super();
		assert function != null : "invalid generator function";
		assert mode != null : "no response parse mode";
		assert actType != null : "generator no act Type";
		this.function = function;
		this.key = key;
		this.actType = actType;
		this.vars = new HashMap<>();
		this.paths = new HashMap<>();

		if (mode == null)
			mode = ResponseParseMode.MESSAGE_TEXT;
		this.mode = mode;

	}

	/**
	 * Generate a response dialogue act
	 * 
	 * @param intent
	 * @param template
	 * @param parameters
	 * @return
	 */
	public DialogueAct generate(String intent, String template, Map<String, String> parameters) {

		assert intent != null;
		assert !intent.contentEquals("");

		if (template == null)
			template = new String();

		System.out.println(this.hasFunction());

		if (parameters == null)
			parameters = new HashMap<>();

		if (!this.hasFunction()) {
			System.out.println("return template");
			return new DialogueAct(this.actType, intent, template);

		}

		OpenAPIAction action = new OpenAPIAction(function, parameters);
		if (!action.validate())
			throw new IllegalArgumentException("Not all required function parameters are filled");
		OpenAPIResponse response = action.execute();

		// failed request
		if (response.isError()) {
			System.out.println("response is error");
			return new DialogueAct(actType, intent, template);
		}

		// success request
		String message = response.getMessage();

		// response is plain text
		if (!response.isJSON()) {

			if (!template.isEmpty())
				return new DialogueAct(actType, intent, template);

			System.out.println("template is empty " + message);
			return new DialogueAct(actType, intent, message);

		}

		JsonElement jelement = response.getAsJSON();
		// response is plain text
		if (jelement.isJsonNull() || jelement.isJsonPrimitive()) {
			System.out.println("response is plain text");
			return new DialogueAct(actType, intent, message);

		}

		// response is JSON
		if (jelement.isJsonObject()) {
			JsonObject object = jelement.getAsJsonObject();
			Map<String, String> entities = new HashMap<>();
			for (Entry<String, JsonElement> entry : object.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					JsonPrimitive jsonPrim = entry.getValue().getAsJsonPrimitive();
					String key = entry.getKey();
					String value = "";
					if (jsonPrim.isNumber())
						value = String.valueOf(jsonPrim.getAsNumber());

					if (jsonPrim.isBoolean())
						value = String.valueOf(jsonPrim.getAsBoolean());

					if (jsonPrim.isString())
						value = jsonPrim.getAsString();

					entities.put(key, value);
					System.out.println("add entry " + key + " " + value);
				}
			}

			DialogueAct res = new DialogueAct(actType, intent, parseTemplate(template, entities), entities);
			return res;

		}

		DialogueAct res = new DialogueAct(actType, intent, template);
		return res;

	}

	public static Map<String, String> getEntities(JsonElement jelement) {
		Map<String, String> entities = new HashMap<>();

		if (jelement.isJsonObject()) {
			JsonObject object = jelement.getAsJsonObject();

			for (Entry<String, JsonElement> entry : object.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					JsonPrimitive jsonPrim = entry.getValue().getAsJsonPrimitive();
					String key = entry.getKey();
					String value = "";
					if (jsonPrim.isNumber())
						value = String.valueOf(jsonPrim.getAsNumber());

					if (jsonPrim.isBoolean())
						value = String.valueOf(jsonPrim.getAsBoolean());

					if (jsonPrim.isString())
						value = jsonPrim.getAsString();

					entities.put(key, value);
					System.out.println("add entry " + key + " " + value);
				}
			}
		}

		if (jelement.isJsonArray()) {
			JsonArray object = jelement.getAsJsonArray();

			int i = 0;
			for (JsonElement entry : object) {

				if (entry.isJsonPrimitive()) {

				}

				if (entry.isJsonObject()) {

					JsonObject jsonObj = entry.getAsJsonObject();
					String context = String.valueOf(i);
					i++;

					for (Entry<String, JsonElement> sentry : jsonObj.entrySet()) {
						if (sentry.getValue().isJsonPrimitive()) {
							JsonPrimitive jsonPrim = sentry.getValue().getAsJsonPrimitive();
							String key = sentry.getKey();
							String value = "";
							if (jsonPrim.isNumber())
								value = String.valueOf(jsonPrim.getAsNumber());

							if (jsonPrim.isBoolean())
								value = String.valueOf(jsonPrim.getAsBoolean());

							if (jsonPrim.isString())
								value = jsonPrim.getAsString();

							// entities.put(i, key, value);
							System.out.println("add entry " + i + " " + key + " " + value);
						}
					}
				}
			}

		}
		
		return entities;

	}

	/**
	 * Replace template with vars
	 * 
	 * @param template
	 * @param results  <pattern, value>
	 * @return
	 */
	public String parseTemplate(String template, Map<String, String> results) {

		for (Entry<String, String> entry : results.entrySet())
			template = template.replace("#" + entry.getKey(), entry.getValue());

		return template;
	}

	public void addVarPath(String var, String path) {
		this.paths.put(var, path);
		this.vars.put(path, var);
	}

	public boolean hasFunction() {
		return this.function != null;
	}

	public ServiceFunction getFunction() {
		return this.function;
	}

	@Override
	public String getFunctionName() {
		return function.getFunctionName();
	}

	@Override
	public String getBasePath() {
		return function.getBasePath();
	}

	@Override
	public String getFunctionPath() {
		return function.getFunctionPath();
	}

	@Override
	public String getHttpMethod() {
		return function.getHttpMethod();
	}

	@Override
	public Collection<ServiceFunctionAttribute> getAllAttributes() {
		return function.getAllAttributes();
	}

	@Override
	public ServiceFunction asServiceFunction() {
		return function;
	}

}
