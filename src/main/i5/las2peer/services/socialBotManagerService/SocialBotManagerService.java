package i5.las2peer.services.socialBotManagerService;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.websocket.DeploymentException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.logging.bot.BotMessage;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.BotAgent;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMessageCollector;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.ContentGenerator;
import i5.las2peer.services.socialBotManagerService.model.IfBlock;
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.ThenBlock;
import i5.las2peer.services.socialBotManagerService.model.VLE;
import i5.las2peer.services.socialBotManagerService.model.VLERoutine;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.BotParser;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * las2peer-SocialBotManager-Service
 * 
 * This is a template for a very basic las2peer service that uses the las2peer WebConnector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in the SwaggerDefinition annotation to suit
 * your project. If you do not intend to provide a Swagger documentation of your service API, the entire Api and
 * SwaggerDefinition annotation should be removed.
 * 
 */
// TODO Adjust the following configuration
@Api
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Bot Manager Service",
				version = "1.0",
				description = "A las2peer Service for managing social bots.",
				termsOfService = "",
				contact = @Contact(
						name = "Alexander Tobias Neumann",
						url = "",
						email = "neumann@dbis.rwth-aachen.de"),
				license = @License(
						name = "",
						url = "")))
@ServicePath("/SBFManager")
@ManualDeployment
public class SocialBotManagerService extends RESTService {

	private static final String ENVELOPE_MODEL = "SBF_MODELLIST";
	private static final String ENVELOPE_USER = "SBF_USERCHANNELLIST";

	private static HashMap<String, Boolean> botIsActive = new HashMap<String, Boolean>();
	private static HashMap<String, Boolean> initialized = new HashMap<String, Boolean>();

	private static BotConfiguration config;

	private static HashMap<String, BotAgent> botAgents;
	private static final String botPass = "actingAgent";

	private static final String classifierName = "i5.las2peer.services.tensorFlowClassifier.TensorFlowClassifier";
	private static final String textToTextName = "i5.las2peer.services.tensorFlowTextToText.TensorFlowTextToText";

	private static ScheduledExecutorService rt = null;
	
	// TODO: Put these into bots properly
	private static RasaNlu rasaNlu = null;
	private static SlackChatMediator slackMediator = null;

	public SocialBotManagerService() {
		super();
		setFieldValues(); // This sets the values of the configuration file
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			;
		}
		if (config == null) {
			config = new BotConfiguration();
		}
		if (botAgents == null) {
			botAgents = new HashMap<String, BotAgent>();
		}

		if (rt == null) {
			System.out.println(1);
			rt = Executors.newSingleThreadScheduledExecutor();
			rt.scheduleAtFixedRate(new RoutineThread(), 0, 1, TimeUnit.SECONDS);
		}
		
		try {
			rasaNlu = new RasaNlu(new URL(System.getenv("RASA_NLU_URL")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			slackMediator = new SlackChatMediator(System.getenv("SLACK_BOT_USER_TOKEN"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Template of a get function.
	 * 
	 * @param body Todo
	 * 
	 * @return Returns an HTTP response with plain text string content.
	 */
	@POST
	@Path("/init")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Init successful.") })
	@ApiOperation(
			value = "init",
			notes = "Reads the configuration file.")
	public Response init(String body) {
		BotParser bp = BotParser.getInstance();

		String returnString = "";
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONObject nodes = (JSONObject) ((JSONObject) p.parse(body)).get("nodes");
			JSONObject edges = (JSONObject) ((JSONObject) p.parse(body)).get("edges");
			try {
				bp.parseNodesAndEdges(config, botAgents, nodes, edges);
			} catch (ParseBotException e) {
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			}

		} catch (ParseException | ClassCastException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}
		// initialized = true;
		JSONObject logData = new JSONObject();
		logData.put("status", "initialized");
		Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, logData.toString());
		return Response.ok().entity(returnString).build();
		// }
	}

	@POST
	@Path("/trigger")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response trigger(String body) {
		String returnString = "";
		try {
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

			JSONObject j = (JSONObject) p.parse(body);
			String service = (String) j.get("serviceAlias");
			System.out.println(service);
			VLE vle = config.getServiceConfiguration(service);
			if (!vle.getEnvironmentSeparator().equals("singleEnvironment")) {
				if (vle == null || vle.getEnvironmentSeparator() == null
						|| ((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator()) == null || botIsActive
								.get(((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator())) != true) {
					return Response.status(Status.FORBIDDEN).entity("Bot is not active").build();
				}
			}

			String f = (String) j.get("functionName");
			String triggerUID = (String) j.get("uid");

			for (BotAgent botAgent : botAgents.values()) {
				try {
					checkTriggerBot(vle, j, botAgent, triggerUID, f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.ok().entity(returnString).build();
	}

	@POST
	@Path("/trigger/routine")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response triggerRoutine(String body) {
		String returnString = "";
		try {
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

			JSONObject j = (JSONObject) p.parse(body);
			String service = (String) j.get("serviceAlias");
			VLE vle = config.getServiceConfiguration(service);
			if (!vle.getEnvironmentSeparator().equals("singleEnvironment")) {
				if (vle == null || vle.getEnvironmentSeparator() == null
						|| ((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator()) == null || botIsActive
								.get(((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator())) != true) {
					return Response.status(Status.FORBIDDEN).entity("Bot is not active").build();
				}
			}

			JSONArray jar = (JSONArray) j.get("functions");

			for (BotAgent botAgent : botAgents.values()) {
				try {
					for (int i = 0; i < jar.size(); ++i) {
						String sfId = (String) jar.get(i);
						checkRoutineTrigger(vle, j, botAgent, sfId);
					}
					// checkTriggerBot(vle, j, botAgent, "", f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.ok().entity(returnString).build();
	}

	private void checkRoutineTrigger(VLE vle, JSONObject j, BotAgent botAgent, String sfId)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException, AgentNotFoundException, AgentOperationFailedException {
		// TODO Auto-generated method stub
		String botName = botAgent.getLoginName();
		Bot bot = vle.getBots().get(botName);
		if (bot != null) {

			System.out.println("Bot " + botAgent.getLoginName() + " triggered:");
			ServiceFunction sf = bot.getBotServiceFunctions().get(sfId);

			String adjustedPath = "";
			if (sf.getFunctionType().equals("service"))
				adjustedPath = sf.getPath();
			JSONObject body = new JSONObject();
			HashMap<String, ServiceFunctionAttribute> attlist = bot.getServiceFunctionsAttributes();

			JSONObject triggerAttributes = (JSONObject) j.get("attributes");
			for (ServiceFunctionAttribute sfa : sf.getAttributes()) {
				formAttributes(vle, sfa, bot, body, adjustedPath, attlist, triggerAttributes);
			}

			performTrigger(vle, sf, botAgent, adjustedPath, "", body, j);

		}
	}

	private void checkTriggerBot(VLE vle, JSONObject j, BotAgent botAgent, String triggerUID, String f)
			throws AgentNotFoundException, AgentOperationFailedException, ServiceNotFoundException,
			ServiceNotAvailableException, InternalServiceException, ServiceMethodNotFoundException,
			ServiceInvocationFailedException, ServiceAccessDeniedException, ServiceNotAuthorizedException,
			ParseBotException {
		String botName = botAgent.getLoginName();
		Bot bot = vle.getBots().get(botName);
		if (bot != null && bot.getTriggerList().containsKey(f)
				&& !(triggerUID.toLowerCase().equals(botAgent.getIdentifier().toLowerCase()))) {

			System.out.println("Bot " + botAgent.getLoginName() + " triggered:");
			for (String sfName : bot.getTriggerList().get(f)) {

				ServiceFunction sf = bot.getBotServiceFunctions().get(sfName);

				System.out.println("Adjusting paramters");
				String adjustedPath = "";
				if (sf.getFunctionType().equals("service"))
					adjustedPath = sf.getPath();
				JSONObject b = new JSONObject();
				HashMap<String, ServiceFunctionAttribute> attlist = bot.getServiceFunctionsAttributes();

				JSONObject triggerAttributes = (JSONObject) j.get("attributes");
				for (ServiceFunctionAttribute sfa : sf.getAttributes()) {
					formAttributes(vle, sfa, bot, b, adjustedPath, attlist, triggerAttributes);
				}

				System.out.println("Performing...");
				performTrigger(vle, sf, botAgent, adjustedPath, triggerUID, b, j);
			}
		}
	}

	private void formAttributes(VLE vle, ServiceFunctionAttribute sfa, Bot bot, JSONObject b, String adjustedPath,
			HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException {
		// Attributes of the triggered function
		if (sfa.isSameAsTrigger()) {
			mapAttributes(b, sfa, adjustedPath, attlist, triggerAttributes);
		} else if (sfa.getName() == "body") {
			JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
			for (ServiceFunctionAttribute subsfa : sfa.getChildAttributes()) {
				if (subsfa.isSameAsTrigger()) {
					String mappedTo = subsfa.getMappedTo();
					ServiceFunctionAttribute sfam = attlist.get(mappedTo);
					if (triggerBody.get(sfam.getName()) != null)
						b.put(subsfa.getName(), triggerBody.get(sfam.getName()));
					else
						b.put(subsfa.getName(), triggerAttributes.get(sfam.getName()));
				} else {
					// Use AI to generate body
					ContentGenerator g = bot.getGeneratorList().get(subsfa.getGeneratorId());
					if (g != null) {
						mapWithContentGenerator(b, g, subsfa.getName(), subsfa.getType(), adjustedPath, attlist,
								triggerAttributes, vle.getEnvironmentSeparator());
					} else {
						if (bot.getAttributeIfThens().containsKey(subsfa.getId())) {
							mapWithIfThen(bot, subsfa, b, attlist, triggerAttributes, adjustedPath);
						} else {
							if (subsfa.hasStaticContent()) {
								mapWithStaticContent(subsfa, b);
							} else {
								System.out.println("nö");
							}
						}
					}

				}

			}
		} else {
			// TODO Pathparam -> ai or if then
			ContentGenerator g = bot.getGeneratorList().get(sfa.getGeneratorId());
			if (g != null) {
				mapWithContentGenerator(b, g, sfa.getName(), sfa.getType(), adjustedPath, attlist, triggerAttributes,
						vle.getEnvironmentSeparator());
			} else {
				if (bot.getAttributeIfThens().containsKey(sfa.getId())) {
					mapWithIfThen(bot, sfa, b, attlist, triggerAttributes, adjustedPath);
				} else {
					if (sfa.hasStaticContent()) {
						mapWithStaticContent(sfa, b);
					} else {
						System.out.println("nö");
					}
				}
			}
		}
	}

	private void mapWithStaticContent(ServiceFunctionAttribute sfa, JSONObject b) {
		if (sfa.getContent().length() > 0) {
			b.put(sfa.getName(), sfa.getContent());
		} else if (sfa.getContentURL().length() > 0) {
			URL url;
			String body = "";
			try {
				url = new URL(sfa.getContentURL());
				URLConnection con = url.openConnection();
				InputStream in = con.getInputStream();
				String encoding = con.getContentEncoding();
				encoding = encoding == null ? "UTF-8" : encoding;
				body = IOUtils.toString(in, encoding);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			b.put(sfa.getName(), body);
		}
	}

	private String mapWithIfThen(Bot bot, ServiceFunctionAttribute sfa, JSONObject b,
			HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes, String adjustedPath)
			throws ParseBotException {
		// if then
		IfThenBlock itb = bot.getAttributeIfThens().get(sfa.getId());
		String sourceAttributeName = attlist.get(itb.getSource()).getName();
		String source = "";
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		if (triggerBody != null && triggerBody.containsKey(sourceAttributeName)) {
			source = triggerBody.getAsString(sourceAttributeName);
		} else if (triggerAttributes.containsKey(sourceAttributeName)) {
			source = triggerAttributes.getAsString(sourceAttributeName);
		} else {
			// Could not map attributes
			throw new ParseBotException("Could not map attributes");
		}
		IfBlock ib = bot.getAttributeIfs().get(itb.getIb());
		do {
			if (checkIfCondition(source, ib.getConditionType(), ib.getValue())) {
				if (ib.getNext() != null)
					ib = ib.getNext();
			} else {
				// Condition not fulfilled
				// throw new ParseBotException("Condition not fulfilled");
			}
		} while (ib.getNext() != null);
		ThenBlock tb = bot.getAttributeThens().get(itb.getTb());
		do {
			source = manipulateString(source, tb.getManipulationType(), tb.getValue(), tb.getValue2());
			if (tb.getNext() != null)
				tb = tb.getNext();
		} while (tb.getNext() != null);
		if (adjustedPath.contains("{" + sfa.getName() + "}")) {
			adjustedPath = adjustedPath.replace("{" + sfa.getName() + "}", source);
		} else {
			b.put(sfa.getName(), source);
		}
		return adjustedPath;
	}

	private void mapWithContentGenerator(JSONObject b, ContentGenerator g, String sfaName, String sfaType,
			String adjustedPath, HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes,
			String envSep) throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException {

		String inputId = g.getInput();
		ServiceFunctionAttribute tsfa = attlist.get(inputId);
		String sourceAttributeName = tsfa.getName();
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		String inferInput = "";
		// try to get provided input
		if (triggerAttributes.containsKey(sourceAttributeName)) {
			inferInput = triggerAttributes.getAsString(sourceAttributeName);
		} else if (triggerBody != null && triggerBody.containsKey(sourceAttributeName)) {
			inferInput = triggerBody.getAsString(sourceAttributeName);
		} else {
			// TODO could not map attribtue
		}
		String unit = "";
		if (triggerAttributes.getAsString(envSep) != null) {
			unit = triggerAttributes.getAsString(envSep);
		}
		Serializable rmiResult = Context.get().invoke(g.getServiceName(), "inference", unit, inferInput);
		if (rmiResult instanceof String) {
			if (adjustedPath.contains("{" + sfaName + "}")) {
				adjustedPath = adjustedPath.replace("{" + sfaName + "}", (String) rmiResult);
			} else {
				if (sfaType.equals("int")) {
					b.put(sfaName, Integer.parseInt((String) rmiResult));
				} else {
					b.put(sfaName, (String) rmiResult);
				}
			}
		} else {
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
		}

	}

	private void mapAttributes(JSONObject b, ServiceFunctionAttribute sfa, String adjustedPath,
			HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes) {
		// get id of the trigger function
		String mappedTo = sfa.getMappedTo();
		// service function attribtue
		ServiceFunctionAttribute sfam = attlist.get(mappedTo);
		// attributes of the function that triggered the bot
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		if (triggerAttributes.containsKey(sfam.getName())) {
			String replaceWith = triggerAttributes.getAsString(sfam.getName());
			if (adjustedPath.contains("{" + sfa.getName() + "}")) {
				adjustedPath = adjustedPath.replace("{" + sfa.getName() + "}", replaceWith);
			} else {
				b.put(sfa.getName(), replaceWith);
			}
		} else if (triggerBody != null && triggerBody.containsKey(sfam.getName())) {
			String replaceWith = triggerBody.getAsString(sfam.getName());
			if (adjustedPath.contains("{" + sfa.getName() + "}")) {
				adjustedPath = adjustedPath.replace("{" + sfa.getName() + "}", replaceWith);
			} else {
				b.put(sfa.getName(), replaceWith);
			}
		} else {
			// TODO Error could not map attributes
		}
	}

	private void performTrigger(VLE vle, ServiceFunction sf, BotAgent botAgent, String adjustedPath, String triggerUID,
			JSONObject b, JSONObject j) throws AgentNotFoundException, AgentOperationFailedException {
		if (sf.getFunctionType().equals("service")) {
			System.out.println("Service function");
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(vle.getAddr());
			client.setLogin(botAgent.getLoginName(), botPass);

			HashMap<String, String> headers = new HashMap<String, String>();
			ClientResponse result = client.sendRequest(sf.getType().toUpperCase(), sf.getServiceName() + adjustedPath,
					b.toJSONString(), sf.getConsumes(), sf.getProduces(), headers);

			JSONObject logData = new JSONObject();
			logData.put("service", sf.getServiceName());
			logData.put("body", b);
			logData.put("trigger", j);
			Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2, logData.toString());
		} else if (sf.getFunctionType().equals("conversation")) {
			if (sf.getConversationType().equals("Slack")) {
				System.out.println("Slack msg");
				if (b.get("channel") == null) {
					// TODO Anonymous agent error
					String mail = ((UserAgentImpl) Context.get().fetchAgent(triggerUID)).getEmail();
					b.put("email", mail);
				}
				b.put("token", sf.getToken());
				triggerSlack(botAgent.getLoginName(), b.toJSONString());
				JSONObject logData = new JSONObject();
				logData.put("slack", sf.getServiceName());
				logData.put("body", b);
				logData.put("trigger", j);
				Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, logData.toString());
			}
		}
	}

	private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return result.toString();
	}

	@POST
	@Path("/trigger/slack/{botName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public void triggerSlack(@PathParam("botName") String botName, String body) {
		HashMap<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint("https://slack.com/api/");
		client.setLogin(botName, botPass);
		String channel = "";
		ClientResponse r = null;
		Envelope userEnv = null;
		String triggerMessage = "";
		boolean found = false;
		try {
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject b = (JSONObject) p.parse(body);
			if (!b.containsKey("channel")) {
				try {
					userEnv = Context.get().requestEnvelope(ENVELOPE_USER + "_" + "agendId");
					Serializable s = userEnv.getContent();
					if (s instanceof String && s != null) {
						channel = (String) s;
					} else {
						System.out.println("Wrong content");
						if (s == null)
							found = true;
						throw new EnvelopeOperationFailedException("");
					}
				} catch (EnvelopeNotFoundException | EnvelopeOperationFailedException e) {
					String token = b.getAsString("token");
					params.put("token", token);
					r = client.sendRequest("POST", "users.list", getDataString(params),
							"application/x-www-form-urlencoded", "application/x-www-form-urlencoded", header);
					JSONObject j = (JSONObject) p.parse(r.getResponse());
					JSONArray memberList = (JSONArray) j.get("members");
					JSONObject foundUser = new JSONObject();

					String triggerMail = b.getAsString("email");
					for (Object o : memberList) {
						if (o instanceof JSONObject) {
							JSONObject jo = (JSONObject) o;
							JSONObject profile = (JSONObject) jo.get("profile");
							if (profile.get("email") != null) {
								String pmail = (String) profile.get("email");
								if (pmail.equals(triggerMail)) {
									foundUser = jo;
									break;
								}
							}
						}
					}
					String uId = (String) foundUser.get("id");
					r = client.sendRequest("POST", "im.list", getDataString(params),
							"application/x-www-form-urlencoded", "application/x-www-form-urlencoded", header);

					j = (JSONObject) p.parse(r.getResponse());
					JSONArray imList = (JSONArray) j.get("ims");
					JSONObject foundChannel = new JSONObject();
					for (Object o : imList) {
						if (o instanceof JSONObject) {
							JSONObject channels = (JSONObject) o;
							if (channels.get("user") != null) {
								String u = (String) channels.get("user");
								if (u.equals(uId)) {
									foundChannel = channels;
									break;
								}
							}
						}
					}
					channel = foundChannel.getAsString("id");
					if (found) {
						try {
							userEnv = Context.get().requestEnvelope(ENVELOPE_USER + "_" + "agendId");
						} catch (EnvelopeNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						userEnv = Context.get().createEnvelope(ENVELOPE_USER + "_" + "agendId",
								Context.get().getServiceAgent());
						// update envelope content
						userEnv.setPublic();
					}
					userEnv.setContent(channel);
					// store envelope with file content
					Context.get().storeEnvelope(userEnv, Context.get().getServiceAgent());
				}
			} else {
				channel = b.getAsString("channel");
			}
			triggerMessage = b.getAsString("text");
			String token = b.getAsString("token");
			params.put("channel", channel);
			params.put("token", token);
			params.put("text", triggerMessage);
			params.put("username", botName);
			r = client.sendRequest("POST", "chat.postMessage", getDataString(params),
					"application/x-www-form-urlencoded", "application/x-www-form-urlencoded", header);

		} catch (UnsupportedEncodingException | ParseException | EnvelopeAccessDeniedException
				| EnvelopeOperationFailedException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Join function
	 * 
	 * @param body TODO
	 * @param botName TODO
	 * @return Returns an HTTP response with plain text string content derived from the path input param.
	 */
	@POST
	@Path("/join/{botName}")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response join(String body, @PathParam("botName") String botName) {
		String returnString = "";
		try {
			BotAgent botAgent = botAgents.get(botName);
			body = body.replace("$botId", botAgent.getIdentifier());
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject j = (JSONObject) p.parse(body);
			String basePath = (String) j.get("basePath");
			String[] s = basePath.split("/");
			String service = s[s.length - 1];
			VLE vle = config.getServiceConfiguration(service);
			Bot bot = vle.getBots().get(botName);
			if (bot != null) {
				bot.setActive(true);
			}
			botIsActive.put(j.getAsString(config.getServiceConfiguration(service).getEnvironmentSeparator()), true);

			if (j.get("directJoin") == null) {
				String joinPath = (String) j.get("joinPath");

				joinPath.replace("$botId", botAgent.getIdentifier());

				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(basePath);
				client.setLogin(botAgent.getLoginName(), botPass);

				j.remove("joinPath");
				j.remove("basePath");
				j.remove("uid");
				ClientResponse result = client.sendRequest("POST", joinPath, j.toJSONString(), "application/json",
						"text/html", new HashMap<String, String>());
				System.out.println(result.getHttpCode());
				System.out.println(result.getResponse());
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.ok().entity(returnString).build();
	}

	/**
	 * Put Model function.
	 * 
	 * @param name TODO
	 * @param body TODO
	 * @return Returns an HTTP response with plain text string content derived from the path input param.
	 */
	@POST
	@Path("/models/{name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response putModel(@PathParam("name") String name, String body) {
		// fetch or create envelope by file identifier
		Envelope fileEnv = null;
		JSONObject models = new JSONObject();
		try {
			try {
				// load existing models
				fileEnv = Context.get().requestEnvelope(ENVELOPE_MODEL);
				Serializable s = fileEnv.getContent();
				if (s instanceof JSONObject) {
					models = (JSONObject) s;
				} else {
					// return wrong content exception error
					System.out.println("Wrong content");
				}
			} catch (EnvelopeNotFoundException e) {
				// Create new model list
				fileEnv = Context.get().createEnvelope(ENVELOPE_MODEL, Context.get().getServiceAgent());
			}
			// Update envelope content
			fileEnv.setPublic();
			models.put(name, body);
			fileEnv.setContent(models);
			// store envelope with file content
			Context.get().storeEnvelope(fileEnv, Context.get().getServiceAgent());
		} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e) {
			// return Envelope exception error
			e.printStackTrace();
		}
		return Response.ok().entity("Model stored.").build();
	}

	@GET
	@Path("/models")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response getModels() {
		// fetch or create envelope by file identifier

		boolean created = false;
		Envelope fileEnv = null;
		JSONObject models = new JSONObject();
		try {
			try {
				fileEnv = Context.get().requestEnvelope(ENVELOPE_MODEL);
				Serializable s = fileEnv.getContent();
				if (s instanceof JSONObject) {
					models = (JSONObject) s;
				} else {
					System.out.println("Wrong content");
				}
			} catch (EnvelopeNotFoundException e) {
				// logger.info("File (" + ENVELOPE_MODEL + ") not found. Creating new one. " + e.toString());
				fileEnv = Context.get().createEnvelope(ENVELOPE_MODEL, Context.get().getServiceAgent());
				created = true;
				// update envelope content
				fileEnv.setPublic();
				fileEnv.setContent(models);
				// store envelope with file content
				Context.get().storeEnvelope(fileEnv, Context.get().getServiceAgent());
			}
		} catch (EnvelopeAccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EnvelopeOperationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.ok().entity(models.keySet()).build();
	}

	@GET
	@Path("/models/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response getModelByName(@PathParam("name") String name) {
		// fetch or create envelope by file identifier
		Envelope fileEnv = null;
		JSONObject models = new JSONObject();
		try {
			try {
				fileEnv = Context.get().requestEnvelope(ENVELOPE_MODEL);
				Serializable s = fileEnv.getContent();
				if (s instanceof JSONObject) {
					models = (JSONObject) s;
				} else {
					System.out.println("Wrong content");
				}
			} catch (EnvelopeNotFoundException e) {
				// logger.info("File (" + ENVELOPE_MODEL + ") not found. Creating new one. " + e.toString());
				fileEnv = Context.get().createEnvelope(ENVELOPE_MODEL, Context.get().getServiceAgent());
				// update envelope content
				fileEnv.setPublic();
				fileEnv.setContent(models);
				// store envelope with file content
				Context.get().storeEnvelope(fileEnv, Context.get().getServiceAgent());
			}
		} catch (EnvelopeAccessDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EnvelopeOperationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.ok().entity(models.get(name)).build();
	}

	@GET
	@Path("/bots/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response getBotsForVLE(@PathParam("name") String name) {
		VLE vle = config.getVLEs().get(name);
		Set<String> botList = new HashSet<String>();
		JSONObject j = new JSONObject();
		if (vle != null) {
			Iterator it = vle.getBots().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				Bot b = (Bot) pair.getValue();
				JSONObject jb = new JSONObject();
				jb.put("active", b.isActive());
				j.put((String) pair.getKey(), jb);
				// it.remove(); // avoids a ConcurrentModificationException
			}
		}
		return Response.ok().entity(j).build();
	}

	@DELETE
	@Path("/bots/{name}/{bot}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response setBot(@PathParam("name") String name, @PathParam("bot") String bot) {
		VLE vle = config.getVLEs().get(name);
		if (vle != null) {
			Bot b = vle.getBots().get(bot);
			if (b != null) {
				b.setActive(false);
				return Response.ok().entity(bot + " deactivated.").build();
			}
		}
		return Response.status(Status.NOT_FOUND).entity(bot + " not found.").build();
	}

	@GET
	@Path("/contentGenerators/{service}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response getContentGenerators(@PathParam("service") String service) {
		VLE vle = null;
		if (config != null) {
			vle = config.getServiceConfiguration(service);
		}
		if (vle != null) {
			HashMap<String, ContentGenerator> cgl = new HashMap<String, ContentGenerator>();
			for (Bot b : vle.getBots().values()) {
				cgl.putAll(b.getGeneratorList());
			}
			return Response.ok().entity(cgl).build();
		} else {
			return Response.ok().entity(new HashMap<String, ContentGenerator>()).build();
		}
	}

	// TODO your own service methods, e. g. for RMI

	public boolean getMessages(ArrayList<BotMessage> messages) {
		System.out.println("Bot: Got " + messages.size() + " bot messages!");
		for (BotMessage m : messages) {
			trigger(m.getRemarks());
		}
		return true;
	}

	private boolean checkIfCondition(String text, String conditionType, String value) {
		if (conditionType.equals("contains")) {
			return text.contains(value);
		} else if (conditionType.equals("equals")) {
			return text.equals(value);
		} else if (conditionType.equals("true")) {
			return true;
		} else if (conditionType.equals("lengthSmallerThan")) {
			return text.length() < Integer.parseInt(value);
		} else if (conditionType.equals("lengthGreaterThan")) {
			return text.length() > Integer.parseInt(value);
		} else if (conditionType.equals("lengthEquals")) {
			return text.length() == Integer.parseInt(value);
		}
		// TODO implement more
		return false;
	}

	private String manipulateString(String text, String manipulationType, String value, String value2) {
		if (manipulationType.equals("static")) {
			text = value;
		} else if (manipulationType.equals("replace")) {
			text = text.replace(value2, value);
		} else if (manipulationType.equals("append")) {
			text = text + value;
		} else if (manipulationType.equals("beginning")) {
			text = value + text;
		}
		return text;
	}
	
	// TODO: Move into bots
	private void replyWithIntent(ChatMessage msg) {
		String channel = "";
		if (msg.getChannel() != null) {
			channel = msg.getChannel();
		}
		
		String text = msg.getText();
		Intent intent;
		try {
			intent = rasaNlu.getIntent(text);
			String message = "Intent: " + intent.getIntentName() + ", Confidence: " + intent.getConfidence();
			slackMediator.sendMessageToChannel(channel, message, 0);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// TODO: Move into bots
	private void handleMessages() {
		ArrayList<ChatMessage> messages = slackMediator.getMessages();
		for (ChatMessage msg: messages) {
			replyWithIntent(msg);
		}
	}

	private class RoutineThread implements Runnable {

		public void run() {
			// TODO: Move into bots
			if (slackMediator != null) {
				handleMessages();
			}

			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
			for (VLE vle : config.getVLEs().values()) {
				for (VLERoutine r : vle.getRoutines().values()) {
					// current time
					Calendar c = Calendar.getInstance();
					long d1 = c.getTime().getTime();
					// last time updated
					long d2 = r.getLastUpdate();

					long diffInMillies = d1 - d2;

					int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

					boolean trigger = false;
					long min = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
					if (r.getInterval().equals("minute")) {

						if (min >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}
					if (r.getInterval().equals("hour")) {
						long hour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (hour >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}
					if (r.getInterval().equals("day")) {
						long day = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (day >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}
					if (r.getInterval().equals("month")) {
						long day = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						// TODO
						day = day / 28;
						if (day >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					} else if (r.getInterval().equals("working days") && dayOfWeek != Calendar.SATURDAY
							&& dayOfWeek != Calendar.SUNDAY) {
						if (min >= 1 && df2.format(d1).equals(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					} else if (r.getInterval().equals("weekend")
							&& (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)) {
						if (min >= 1 && df2.format(d1).equals(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}

					if (trigger) {
						for (Bot b : vle.getBots().values()) {
							if (b.isActive()) {
								System.out.println(df.format(d1) + ": " + b.getName());
								MiniClient client = new MiniClient();
								client.setConnectorEndpoint(vle.getAddr());
								// client.setLogin(b.getName(), botPass);

								JSONObject body = new JSONObject();
								body.put("serviceAlias", vle.getName());
								HashSet<ServiceFunction> sfList = r.getTriggerFunctions();
								JSONArray jar = new JSONArray();
								for (ServiceFunction sf : sfList) {
									jar.add(sf.getId());
								}
								body.put("functions", jar);
								JSONObject atts = new JSONObject();
								// TODO Fill in attributes
								body.put("attributes", atts);
								HashMap<String, String> headers = new HashMap<String, String>();
								ClientResponse result = client.sendRequest("POST", "SBFManager/trigger/routine",
										body.toJSONString(), MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
								System.out.println(result.getResponse());
							}
						}
					}
				}
			}
		}
	}

}
