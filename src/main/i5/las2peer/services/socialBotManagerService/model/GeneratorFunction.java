package i5.las2peer.services.socialBotManagerService.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueAct;
import i5.las2peer.services.socialBotManagerService.dialogue.DialogueActType;
import i5.las2peer.services.socialBotManagerService.parser.openapi.FunctionInterface;
import i5.las2peer.services.socialBotManagerService.parser.openapi.OpenAPIAction;
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
		assert template != null;
		assert !template.contentEquals("");

		if (parameters == null)
			parameters = new HashMap<>();

		if (!this.hasFunction())
			return new DialogueAct(this.actType, intent, template);

		OpenAPIAction action = new OpenAPIAction(function, parameters);
		if (!action.validate())
			throw new IllegalArgumentException("Not all required function parameters are filled");
		ClientResponse cr = action.execute();

		// failed request
		if (cr == null || cr.getHttpCode() > 299 || cr.getResponse() == null)
			return new DialogueAct(actType, intent, template);

		// success request
		String crResponse = cr.getResponse();
		try {
			JsonElement jelement = JsonParser.parseString(crResponse);

			// response is plain text
			if (jelement.isJsonNull() || jelement.isJsonPrimitive())
				return new DialogueAct(actType, intent, crResponse);

			// response is JSON
			DialogueAct res = new DialogueAct(actType, intent, template);
			return res;

		} catch (Exception e) {
			e.printStackTrace();
			if(crResponse != null && crResponse.length() > 0)
			return new DialogueAct(actType, intent, crResponse);
			return new DialogueAct(actType, intent, template);
		}

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
			template = template.replace(entry.getKey(), entry.getValue());

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
