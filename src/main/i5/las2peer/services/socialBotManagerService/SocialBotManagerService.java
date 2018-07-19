package i5.las2peer.services.socialBotManagerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.persistency.Envelope;
import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
import i5.las2peer.api.persistency.EnvelopeNotFoundException;
import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.logging.bot.BotMessage;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.BotAgent;
import i5.las2peer.security.UserAgentImpl;
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

// TODO Describe your own service
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
	private static boolean initialized = false;
	private static HashMap<String, Boolean> ready = new HashMap<String, Boolean>();

	private static BotConfiguration config;

	private static BotAgent botAgent;
	private static final String botPass = "actingAgent";

	private static final String classifierName = "i5.las2peer.services.tensorFlowClassifier.TensorFlowClassifier";
	private static final String textToTextName = "i5.las2peer.services.tensorFlowTextToText.TensorFlowTextToText";

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
		// if (initialized) {
		// return Response.status(Response.Status.CONFLICT).build();
		// } else {
		String returnString = "";
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		try {
			JSONObject nodes = (JSONObject) ((JSONObject) p.parse(body)).get("nodes");
			JSONObject edges = (JSONObject) ((JSONObject) p.parse(body)).get("edges");
			HashMap<String, ServiceFunction> bsfList = new HashMap<String, ServiceFunction>(); // CHECK
			HashMap<String, ServiceFunction> usfList = new HashMap<String, ServiceFunction>(); // CHECK
			HashMap<String, ServiceFunctionAttribute> sfaList = new HashMap<String, ServiceFunctionAttribute>();// CHECK
																												// config.getServiceFunctionsAttributes();
			HashMap<String, IfBlock> ibList = new HashMap<String, IfBlock>();// config.getAttributeIfs(); CHECK
			HashMap<String, ThenBlock> tbList = new HashMap<String, ThenBlock>();// config.getAttributeThens(); CHECK
			HashMap<String, IfThenBlock> itbList = new HashMap<String, IfThenBlock>();// config.getAttributeIfThens();
																						// CHECK
			HashSet<IfThenBlock> tempitbList = new HashSet<IfThenBlock>();
			HashMap<String, ContentGenerator> gList = new HashMap<String, ContentGenerator>(); // CHECK
			HashMap<String, VLE> vles = new HashMap<String, VLE>();
			HashMap<String, Bot> bots = new HashMap<String, Bot>(); // CHECK
			HashMap<String, VLEUser> users = new HashMap<String, VLEUser>();

			for (HashMap.Entry<String, Object> entry : nodes.entrySet()) {
				JSONObject elem = (JSONObject) entry.getValue();
				String nodeType = (String) elem.get("type");
				if (nodeType.equals("VLE Instance")) {
					VLE vle = setVLEInstance(elem);
					vles.put(entry.getKey(), vle);
				} else if (nodeType.equals("Bot")) {
					Bot bot = addBot(elem);
					bots.put(entry.getKey(), bot);
				} else if (nodeType.equals("VLE User")) {
					VLEUser u = addUser(elem);
					u.setId(entry.getKey());
					users.put(entry.getKey(), u);
				} else if (nodeType.equals("Bot Action")) {
					ServiceFunction sf = addAction(entry.getKey(), elem);
					bsfList.put(entry.getKey(), sf);
					returnString += sf.getFunctionName() + "\n";
				} else if (nodeType.equals("User Action")) {
					ServiceFunction sf = addAction(entry.getKey(), elem);
					usfList.put(entry.getKey(), sf);
					returnString += sf.getFunctionName() + "\n";
				} else if (nodeType.equals("Action Parameter")) {
					ServiceFunctionAttribute sfa = addActionParameter(entry.getKey(), elem);
					sfaList.put(entry.getKey(), sfa);
					returnString += sfa.getType() + ":" + sfa.getName() + "\n";
				} else if (nodeType.equals("Action Result")) {
					ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
					sfa.setId(entry.getKey());
					sfa.setName("result");
					sfa.setType("String");
					sfaList.put(entry.getKey(), sfa);
					returnString += sfa.getType() + ":" + sfa.getName() + "\n";
				} else if (nodeType.equals("Action Body")) {
					ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
					sfa.setId(entry.getKey());
					sfa.setName("body");
					sfa.setType("JSON");
					sfaList.put(entry.getKey(), sfa);
					returnString += sfa.getType() + ":" + sfa.getName() + "\n";
				} else if (nodeType.equals("If")) {
					IfBlock ib = addIfBlock(entry.getKey(), elem);
					ibList.put(entry.getKey(), ib);
				} else if (nodeType.equals("Then")) {
					ThenBlock tb = addThenBlock(entry.getKey(), elem);
					tbList.put(entry.getKey(), tb);
				} else if (nodeType.equals("TextToText") || nodeType.equals("Classifier")) {
					ContentGenerator g = new ContentGenerator();
					g.setId(entry.getKey());
					g.setName(nodeType);
					if (nodeType.equals("TextToText")) {
						g.setServiceName(textToTextName);
					} else if (nodeType.equals("Classifier")) {
						g.setServiceName(classifierName);
					}
					gList.put(entry.getKey(), g);
				}
			}

			if (vles.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity("Missing VLE Instance!").build();
			} else if (users.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity("Missing VLE User!").build();
			} else if (bots.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity("Missing Bot!").build();
			} else if (bsfList.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity("Missing Bot Action!").build();
			} else if (usfList.size() == 0) {
				return Response.status(Status.BAD_REQUEST).entity("Missing User Action!").build();
			}

			// pass botfunctions
			for (HashMap.Entry<String, ServiceFunction> entry : bsfList.entrySet()) {
				String service = entry.getValue().getServiceName();
				BotServiceConfiguration sconfig = config.getServiceConfiguration(service);
				sconfig.addBotServiceFunction(entry.getKey(), entry.getValue());
			}

			// pass userfunctions
			for (HashMap.Entry<String, ServiceFunction> entry : usfList.entrySet()) {
				String service = entry.getValue().getServiceName();
				BotServiceConfiguration sconfig = config.getServiceConfiguration(service);
				sconfig.addBotServiceFunction(entry.getKey(), entry.getValue());
			}

			int checkGeneratorIns = 0;
			int checkGeneratorOuts = 0;

			for (HashMap.Entry<String, Object> entry : edges.entrySet()) {
				JSONObject elem = (JSONObject) entry.getValue();
				String type = (String) elem.get("type");
				String source = (String) elem.get("source");
				String target = (String) elem.get("target");
				if (type.equals("Action has Parameter")) {
					ServiceFunctionAttribute to = null;

					ServiceFunctionAttribute sfaListItem = sfaList.get(target);
					if (sfaListItem != null) {
						to = sfaListItem;
					}

					ServiceFunction bsfListItem = bsfList.get(source);
					ServiceFunction usfListItem = usfList.get(source);
					sfaListItem = sfaList.get(source);

					if (bsfListItem != null) {
						bsfListItem.addAttribute(to);
						BotServiceConfiguration sconfig = config.getServiceConfiguration(bsfListItem.getServiceName());
						sconfig.addBotServiceFunction(bsfListItem.getId(), bsfListItem);
					} else if (usfListItem != null) {
						usfListItem.addAttribute(to);
						BotServiceConfiguration sconfig = config.getServiceConfiguration(usfListItem.getServiceName());
						sconfig.addUserServiceFunction(usfListItem.getId(), usfListItem);
					} else if (sfaListItem != null) {
						sfaListItem.addChildAttribute(to);
					}
					to.setFunctionId(source);
				} else if (type.equals("User Action triggers Bot Action")) {
					BotServiceConfiguration sconfig = config
							.getServiceConfiguration(usfList.get(source).getServiceName());
					sconfig.addTrigger(usfList.get(source).getFunctionName(), target);
				} else if (type.equals("Bot performs Action")) {
					BotServiceConfiguration sconfig = config
							.getServiceConfiguration(bsfList.get(target).getServiceName());
					sconfig.addBot(source, bots.get(source));
				} else if (type.equals("Same Action Parameter as")) {
					sfaList.get(source).setSameAsTrigger(true);
					sfaList.get(source).setMappedTo(target);
					sfaList.get(target).setSameAsTrigger(true);
					sfaList.get(target).setMappedTo(source);
				} else if (type.equals("Action Parameter is Generator Input")) {
					checkGeneratorIns++;
					sfaList.get(source).setGeneratorId(target);
					ContentGenerator g = (ContentGenerator) (gList.get(target));
					g.setInput(source);
				} else if (type.equals("Generator has Ouput Parameter")) {
					checkGeneratorOuts++;
					sfaList.get(target).setGeneratorId(source);
					ContentGenerator g = (ContentGenerator) (gList.get(source));
					g.setOutput(target);
				} else if (type.equals("VLE consists of User")) {
					VLE v = vles.get(source);
					v.addUserId(target);
				} else if (type.equals("User performs Action")) {
					VLEUser u = users.get(source);
					u.setFunctionId(target);
				} else if (type.equals("is If Input")) {
					if (ibList.containsKey(target)) {
						// if chain
						if (ibList.containsKey(source)) {
							//
							ibList.get(target).setPrev(ibList.get(source));
							ibList.get(source).setNext(ibList.get(target));
						} else {
							// start of chain
							ibList.get(target).setSource(source);
						}
					} else {
						// error
						System.out.println("Endpoint is not an if...");
					}
				} else if (type.equals("Then has Output")) {
					if (tbList.containsKey(source)) {
						// if chain
						if (tbList.containsKey(target)) {
							//
							tbList.get(source).setNext(tbList.get(target));
							tbList.get(target).setPrev(tbList.get(source));
						} else {
							// start of chain
							tbList.get(source).setOutput(target);
						}
					} else {
						// error
						System.out.println("Error: Does not start with a then...");

					}
				} else if (type.equals("If Then")) {
					IfThenBlock itb = new IfThenBlock();
					itb.setIb(source);
					itb.setTb(target);
					tempitbList.add(itb);
				}
			}

			if (checkGeneratorIns != checkGeneratorOuts) {
				return Response.status(Status.BAD_REQUEST).entity("Check the Content Generator connections!").build();
			}

			// create if then structure

			for (IfThenBlock itbIterator : tempitbList) {
				IfBlock ib = ibList.get(itbIterator.getIb());
				while (ib.getPrev() != null) {
					ib = ib.getPrev();
				}

				ThenBlock tb = tbList.get(itbIterator.getTb());
				ThenBlock tbIterator = tb;
				while (tbIterator.getNext() != null) {
					tbIterator = tbIterator.getNext();
				}
				IfThenBlock itb = new IfThenBlock();
				itb.setIb(ib.getSource());
				itb.setIb(ib.getId());
				itb.setTb(tb.getId());
				itbList.put(tbIterator.getOutput(), itb);

				itb.setSource(ib.getSource());
			}

			// pass if then
			for (HashMap.Entry<String, IfThenBlock> entry : itbList.entrySet()) {
				// If Block id
				String ibs = entry.getValue().getIb();
				IfBlock ib = ibList.get(ibs);
				BotServiceConfiguration sconfig = null;

				String sfaParent = getParent(ib.getSource(), sfaList);
				System.out.println(sfaParent);
				if (bsfList.get(sfaList.get(sfaParent)) != null) {
					String sname = bsfList.get(sfaParent).getServiceName();
					sconfig = config.getServiceConfiguration(sname);
				} else if (usfList.get(sfaParent) != null) {
					String sname = usfList.get(sfaParent).getServiceName();
					sconfig = config.getServiceConfiguration(sname);
				} else {
					return Response.status(Status.BAD_REQUEST).entity("Check your If input and Then Output!").build();
				}

				sconfig.addAttributeIfThen(entry.getKey(), entry.getValue());

				do {
					sconfig.addAttributeIf(ib.getId(), ib);
					ib = ib.getNext();
				} while (ib != null);

				String tbs = entry.getValue().getTb();
				ThenBlock tb = tbList.get(tbs);
				do {
					sconfig.addAttributeThen(tb.getId(), tb);
					tb = tb.getPrev();
				} while (tb != null);

			}

			// pass attributes
			for (HashMap.Entry<String, ServiceFunctionAttribute> entry : sfaList.entrySet()) {
				String parent = getParent(entry.getKey(), sfaList);
				if (bsfList.get(parent) != null) {
					String service = bsfList.get(parent).getServiceName();
					config.getServiceConfiguration(service).addServiceFunctionsAttributes(entry.getKey(),
							entry.getValue());
				} else if (usfList.get(parent) != null) {
					String service = usfList.get(parent).getServiceName();
					config.getServiceConfiguration(service).addServiceFunctionsAttributes(entry.getKey(),
							entry.getValue());
				} else {
					return Response.status(Status.BAD_REQUEST)
							.entity("Check the ServiceFunctionAttribute to ServiceFunction connection!").build();
				}
			}

			// pass content generators
			for (HashMap.Entry<String, ContentGenerator> entry : gList.entrySet()) {
				String attributeString = entry.getValue().getInput();
				String parent = getParent(attributeString, sfaList);
				if (bsfList.get(parent) != null) {
					String service = bsfList.get(parent).getServiceName();
					config.getServiceConfiguration(service).addGenerator(entry.getKey(), entry.getValue());
				} else if (usfList.get(parent) != null) {
					String service = usfList.get(parent).getServiceName();
					config.getServiceConfiguration(service).addGenerator(entry.getKey(), entry.getValue());
				} else {
					return Response.status(Status.BAD_REQUEST).entity("Check the content generator connections!")
							.build();
				}
			}

			// pass vle content

			for (HashMap.Entry<String, VLE> entry : vles.entrySet()) {
				VLE vle = entry.getValue();
				VLEUser u = null;
				ServiceFunction sf = null;
				for (String us : vle.getUserId()) {
					u = users.get(us);
					if (u.getFunctionId() != null) {
						if (usfList.get(u.getFunctionId()) != null) {
							sf = usfList.get(u.getFunctionId());
							break;
						}
					}

				}
				if (sf != null) {
					BotServiceConfiguration sconfig = config.getServiceConfiguration(sf.getServiceName());
					sconfig.setBotServiceAddr(vle.getAddr());
					sconfig.setEnvironmentSeparator(vle.getSeparator());
				} else {
					return Response.status(Status.BAD_REQUEST)
							.entity("Check the ServiceFunctionAttribute to ServiceFunction connection!").build();
				}
			}

			JSONArray jaf = new JSONArray();
			HashMap<String, ServiceFunction> allFunctions = new HashMap<String, ServiceFunction>();
			for (BotServiceConfiguration sconfig : config.getServices().values()) {
				for (String s : sconfig.getTriggerList().keySet()) {
					jaf.add(s);
				}
				allFunctions.putAll(sconfig.getUserServiceFunctions());
				allFunctions.putAll(sconfig.getBotServiceFunctions());
				for (ServiceFunction s : allFunctions.values()) {
					// try to get swagger information

					if (sconfig.getServiceInformation().get(s.getServiceName()) == null
							&& s.getFunctionType().equals("service")) {
						try {
							JSONObject j = readJsonFromUrl(
									sconfig.getBotServiceAddr() + "/" + s.getServiceName() + "/swagger.json");
							sconfig.addServiceInformation(s.getServiceName(), j);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (sconfig.getServiceInformation().get(s.getServiceName()) != null
							&& s.getFunctionName() != null) {
						addServiceInformation(s, sconfig.getServiceInformation().get(s.getServiceName()));
					}
				}

			}

			JSONObject j = new JSONObject();
			j.put("triggerFunctions", jaf);
			j.put("botId", botAgent.getIdentifier());
			Context.get().monitorEvent(MonitoringEvent.BOT_ADD_TO_MONITORING, j.toJSONString());
		} catch (ParseException | ClassCastException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();
		}
		initialized = true;
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
			BotServiceConfiguration sconfig = config.getServiceConfiguration(service);
			if (!sconfig.getEnvironmentSeparator().equals("singleEnvironment")) {
				if (sconfig == null || sconfig.getEnvironmentSeparator() == null
						|| ((JSONObject) j.get("attributes")).get(sconfig.getEnvironmentSeparator()) == null
						|| botIsActive.get(
								((JSONObject) j.get("attributes")).get(sconfig.getEnvironmentSeparator())) != true) {
					return Response.status(Status.FORBIDDEN).entity("Bot is not active").build();
				}
			}

			String f = (String) j.get("functionName");
			String triggerUID = (String) j.get("uid");
			if (sconfig.getTriggerList().containsKey(f)
					&& !(triggerUID.toLowerCase().equals(botAgent.getIdentifier().toLowerCase()))) {

				System.out.println("Bot triggered:");

				String[] triggerFunctionKeys = ((HashSet<String>) sconfig.getTriggerList().get(f))
						.toArray((new String[sconfig.getTriggerList().get(f).size()]));

				String randomTriggerFunctionKey = triggerFunctionKeys[(int) (Math.random()
						* triggerFunctionKeys.length)];

				ServiceFunction sf = sconfig.getBotServiceFunctions().get(randomTriggerFunctionKey);

				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(sconfig.getBotServiceAddr());
				client.setLogin(botAgent.getLoginName(), botPass);
				HashMap<String, String> headers = new HashMap<String, String>();
				String adjustedPath = "";
				if (sf.getFunctionType().equals("service"))
					adjustedPath = sf.getPath();
				JSONObject b = new JSONObject();
				HashMap<String, ServiceFunctionAttribute> attlist = sconfig.getServiceFunctionsAttributes();
				for (ServiceFunctionAttribute sfa : sf.getAttributes()) {
					// Attributes of the triggered function
					if (sfa.isSameAsTrigger()) {
						// get id of the trigger function
						String mappedTo = sfa.getMappedTo();
						// service function attribtue
						ServiceFunctionAttribute sfam = attlist.get(mappedTo);
						// attributes of the function that triggered the bot
						JSONObject triggerAttributes = (JSONObject) j.get("attributes");
						JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
						if (triggerAttributes.containsKey(sfam.getName())) {
							String replaceWith = triggerAttributes.getAsString(sfam.getName());
							adjustedPath = adjustedPath.replace("{" + sfa.getName() + "}", replaceWith);
							b.put(sfa.getName(), replaceWith);
						} else if (triggerBody != null && triggerBody.containsKey(sfam.getName())) {
							String replaceWith = triggerBody.getAsString(sfam.getName());
							adjustedPath = adjustedPath.replace("{" + sfa.getName() + "}", replaceWith);
							b.put(sfa.getName(), replaceWith);
						} else {
							// TODO Error could not map attributes
						}

					} else if (sfa.getName() == "body") {
						JSONObject triggerAttributes = (JSONObject) j.get("attributes");
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
								ContentGenerator g = sconfig.getGeneratorList().get(subsfa.getGeneratorId());
								if (g != null) {
									String inputId = g.getInput();
									String inferInput = "";
									ServiceFunctionAttribute tsfa = attlist.get(inputId);
									if (attlist.get(tsfa.getFunctionId()) != null) {
										inferInput = (String) triggerBody.get(attlist.get(inputId).getName());
									} else if (sconfig.getUserServiceFunctions().containsKey(tsfa.getFunctionId())) {
										inferInput = (String) triggerAttributes.get(attlist.get(inputId).getName());
									} else {
										// TODO ERROR
									}
									Serializable rmiResult = Context.get().invoke(g.getServiceName(), "inference", "",
											inferInput);
									if (rmiResult instanceof String) {
										b.put(subsfa.getName(), (String) rmiResult);
									} else {
										throw new InternalServiceException("Unexpected result ("
												+ rmiResult.getClass().getCanonicalName() + ") of RMI call");
									}
								} else {
									if (sconfig.getAttributeIfThens().containsKey(subsfa.getId())) {
										// if then
										IfThenBlock itb = sconfig.getAttributeIfThens().get(subsfa.getId());
										String sourceAttributeName = attlist.get(itb.getSource()).getName();
										String source = "";
										if (triggerBody.containsKey(sourceAttributeName)) {
											source = triggerBody.getAsString(sourceAttributeName);
										} else if (triggerAttributes.containsKey(sourceAttributeName)) {
											source = triggerAttributes.getAsString(sourceAttributeName);
										} else {
											// TODO could not map attribtue
										}
										IfBlock ib = sconfig.getAttributeIfs().get(itb.getIb());
										do {
											if (checkIfCondition(source, ib.getConditionType(), ib.getValue())) {
												if (ib.getNext() != null)
													ib = ib.getNext();
											} else {
												return Response.status(Status.BAD_REQUEST)
														.entity(ib.getConditionType() + " not fullfiled").build();
											}
										} while (ib.getNext() != null);
										ThenBlock tb = sconfig.getAttributeThens().get(itb.getTb());
										do {
											source = manipulateString(source, tb.getManipulationType(), tb.getValue());
											if (tb.getNext() != null)
												tb = tb.getNext();
										} while (tb.getNext() != null);
										b.put(subsfa.getName(), source);
									} else {
										// Ja well... weder if noch tensorflow
									}
								}

							}

						}
					} else {
						// TODO Pathparam -> ai or if then
						JSONObject triggerAttributes = (JSONObject) j.get("attributes");
						ContentGenerator g = sconfig.getGeneratorList().get(sfa.getGeneratorId());
						if (g != null) {
							String inputId = g.getInput();
							String sourceAttributeName = attlist.get(inputId).getName();
							JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
							String inferInput = "";
							if (triggerAttributes.containsKey(sourceAttributeName)) {
								inferInput = triggerAttributes.getAsString(sourceAttributeName);
							} else if (triggerBody != null && triggerBody.containsKey(sourceAttributeName)) {
								inferInput = triggerBody.getAsString(sourceAttributeName);
							} else {
								// TODO could not map attribtue
							}
							Serializable rmiResult = Context.get().invoke(g.getServiceName(), "inference", "",
									inferInput);
							if (rmiResult instanceof String) {
								b.put(sfa.getName(), (String) rmiResult);
							} else {
								throw new InternalServiceException("Unexpected result ("
										+ rmiResult.getClass().getCanonicalName() + ") of RMI call");
							}
						} else {
							if (sconfig.getAttributeIfThens().containsKey(sfa.getId())) {
								// if then
								IfThenBlock itb = sconfig.getAttributeIfThens().get(sfa.getId());
								String sourceAttributeName = attlist.get(itb.getSource()).getName();
								String source = "";
								JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
								if (triggerAttributes.containsKey(sourceAttributeName)) {
									source = triggerAttributes.getAsString(sourceAttributeName);
								} else if (triggerBody != null && triggerBody.containsKey(sourceAttributeName)) {
									source = triggerBody.getAsString(sourceAttributeName);
								} else {
									// TODO could not map attribtue
								}

								IfBlock ib = sconfig.getAttributeIfs().get(itb.getIb());
								do {
									// TODO Helper function for check
									if (checkIfCondition(source, ib.getConditionType(), ib.getValue())) {
										if (ib.getNext() != null)
											ib = ib.getNext();
									} else {
										// TODO Discard function
										return Response.status(Status.BAD_REQUEST)
												.entity(ib.getConditionType() + " not fullfiled").build();
									}
								} while (ib.getNext() != null);
								ThenBlock tb = sconfig.getAttributeThens().get(itb.getTb());
								do {
									// TODO Manipulation
									source = manipulateString(source, tb.getManipulationType(), tb.getValue());
									if (tb.getNext() != null)
										tb = tb.getNext();
								} while (tb.getNext() != null);
								b.put(sfa.getName(), source);
							} else {
								// Ja well... weder if noch tensorflow
							}
						}
					}
				}
				if (sf.getFunctionType().equals("service")) {
					ClientResponse result = client.sendRequest(sf.getType().toUpperCase(),
							sf.getServiceName() + adjustedPath, b.toJSONString(), sf.getConsumes(), sf.getProduces(),
							headers);
					System.out.println(result.getHttpCode());
					System.out.println(result.getResponse());
					returnString = result.getResponse();

					JSONObject logData = new JSONObject();
					logData.put("service", sf.getServiceName());
					logData.put("body", b);
					logData.put("trigger", j);
					Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2, logData.toString());
				} else if (sf.getFunctionType().equals("conversation")) {
					if (sf.getConversationType().equals("Slack")) {
						if (b.get("channel") == null) {
							// TODO Anonymous agent error
							String mail = ((UserAgentImpl) Context.get().fetchAgent(triggerUID)).getEmail();
							b.put("email", mail);
						}
						b.put("token", sf.getToken());
						triggerSlack(b.toJSONString());
						JSONObject logData = new JSONObject();
						logData.put("slack", sf.getServiceName());
						logData.put("body", b);
						logData.put("trigger", j);
						Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, logData.toString());
					}
				}
			} else {
				returnString = "Not a trigger function";
			}
			// System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.ok().entity(returnString).build();
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
	@Path("/trigger/slack")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public void triggerSlack(String body) {
		HashMap<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		MiniClient client = new MiniClient();
		client.setConnectorEndpoint("https://slack.com/api/");
		client.setLogin(botAgent.getLoginName(), botPass);
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
	 * @return Returns an HTTP response with plain text string content derived from the path input param.
	 */
	@POST
	@Path("/join")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	@ApiOperation(
			value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "Example method that returns a phrase containing the received input.")
	public Response join(String body) {
		String returnString = "";
		try {
			body = body.replace("$botId", botAgent.getIdentifier());
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject j = (JSONObject) p.parse(body);
			String basePath = (String) j.get("basePath");
			String[] s = basePath.split("/");
			String service = s[s.length - 1];
			System.out.println(service);
			System.out.println(config.getServiceConfiguration(service).getEnvironmentSeparator());
			botIsActive.put(j.getAsString(config.getServiceConfiguration(service).getEnvironmentSeparator()), true);
			if (j.get("directJoin") == null) {
				String joinPath = (String) j.get("joinPath");

				joinPath.replace("$botId", botAgent.getIdentifier());

				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(basePath);
				client.setLogin(botAgent.getLoginName(), botPass);

				j.remove("joinPath");
				j.remove("basePath");
				ClientResponse result = client.sendRequest("POST", joinPath, j.toJSONString(), "application/json",
						"text/html", new HashMap<String, String>());
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
		BotServiceConfiguration sconfig = config.getServiceConfiguration(service);
		return Response.ok().entity(sconfig.getGeneratorList()).build();
	}

	// TODO your own service methods, e. g. for RMI
	private VLE setVLEInstance(JSONObject elem) {
		VLE vle = new VLE();
		// BotServiceConfiguration sconfig = config.getServiceConfiguration(service);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Addr")) {
					vle.setAddr((String) subVal.get("value"));
				} else if (subVal.get("name").equals("Environment Separator")) {
					String sep = (String) subVal.get("value");
					if (sep.equals("")) {
						// Single Environment
						vle.setSeparator("singleEnvironment");
						// sconfig.setEnvironmentSeparator("singleEnvironment");
					} else {
						// normal setup
						vle.setSeparator(sep);
						// sconfig.setEnvironmentSeparator(sep);
					}
				}
			}
		}
		return vle;
	}

	private ServiceFunction addAction(String key, JSONObject elem) {

		ServiceFunction sf = new ServiceFunction();
		sf.setId(key);
		String actionType = "";
		String conversationType = "";
		String service = "";
		String sfName = "";
		String token = "";
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Name")) {
					sfName = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Service")) {
					service = (String) subVal.get("value");
					config.addServiceConfiguration(service, new BotServiceConfiguration());
				} else if (subVal.get("name").equals("Action Type")) {
					actionType = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Conversation Type")) {
					conversationType = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Token")) {
					token = (String) subVal.get("value");
				}
			}
		}

		if (actionType.equals("conversation")) {
			sf.setFunctionType(actionType);
			sf.setConversationType(conversationType);
			sf.setServiceName(service);
			sf.setToken(token);
		} else {
			// default case
			sf.setFunctionName(sfName);
			sf.setServiceName(service);
		}
		return sf;
	}

	private ServiceFunctionAttribute addActionParameter(String key, JSONObject elem) {

		ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
		sfa.setId(key);

		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("type")) {
					sfa.setType((String) subVal.get("value"));
				} else if (subVal.get("name").equals("name")) {
					sfa.setName((String) subVal.get("value"));
				}
			}
		}
		return sfa;
	}

	private void addServiceInformation(ServiceFunction f, JSONObject elem) {
		// pfade
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("paths")).entrySet()) {
			// type
			for (HashMap.Entry<String, Object> subsubEntry : ((JSONObject) subEntry.getValue()).entrySet()) {
				JSONObject functionInfo = (JSONObject) subsubEntry.getValue();
				String opId = (String) functionInfo.get("operationId");
				JSONArray consumes = (JSONArray) functionInfo.get("consumes");
				JSONArray produces = (JSONArray) functionInfo.get("produces");
				if (opId.toLowerCase().equals(f.getFunctionName().toLowerCase())) {
					f.setPath(subEntry.getKey());
					f.setType(subsubEntry.getKey());
					if (consumes == null) {
						f.setConsumes("text/html");
					} else {
						f.setConsumes(consumes.get(0).toString());
					}
					if (produces == null) {
						f.setProduces("text/html");
					} else {
						f.setProduces(produces.get(0).toString());
					}
				}
			}
		}
	}

	private Bot addBot(JSONObject elem) {
		Bot b = new Bot();
		b.setActive(false);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Name")) {
					try {
						String botName = (String) subVal.get("value");
						botAgent = BotAgent.createBotAgent(botPass);
						botAgent.unlock(botPass);
						botAgent.setLoginName(botName);
						Context.getCurrent().storeAgent(botAgent);
						botAgent = (BotAgent) Context.getCurrent()
								.fetchAgent(Context.getCurrent().getUserAgentIdentifierByLoginName(botName));
						botAgent.unlock(botPass);
						Context.getCurrent().registerReceiver(botAgent);
						// runningAt = botAgent.getRunningAtNode();
						System.out.println(
								"Bot " + botName + " registered at: " + botAgent.getRunningAtNode().getNodeId());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					// config.addBot(botAgent.getIdentifier(), botAgent.getLoginName());
					b.setId(botAgent.getIdentifier());
					b.setName(botAgent.getLoginName());
				}
			}
		}
		return b;
	}

	private VLEUser addUser(JSONObject elem) {
		VLEUser u = new VLEUser();
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("type")) {

					String userType = (String) subVal.get("value");
					u.setName(userType);
				}
			}
		}
		return u;
	}

	private IfBlock addIfBlock(String key, JSONObject elem) {
		IfBlock ib = new IfBlock();
		ib.setId(key);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Condition Type")) {
					ib.setConditionType((String) subVal.get("value"));
				} else if (subVal.get("name").equals("value")) {
					ib.setValue((String) subVal.get("value"));
				}
			}
		}
		return ib;
	}

	private ThenBlock addThenBlock(String key, JSONObject elem) {
		ThenBlock ib = new ThenBlock();
		ib.setId(key);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Manipulation Type")) {
					ib.setManipulationType((String) subVal.get("value"));
				} else if (subVal.get("name").equals("value")) {
					ib.setValue((String) subVal.get("value"));
				}
			}
		}
		return ib;
	}

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
		}
		// TODO implement more
		return false;
	}

	private String getParent(String sfa, HashMap<String, ServiceFunctionAttribute> sfaList) {
		String parent = sfa;
		do {
			parent = sfaList.get(parent).getFunctionId();
		} while (sfaList.get(parent) != null);

		return parent;
	}

	private String manipulateString(String text, String manipulationType, String value) {
		if (manipulationType.equals("static")) {
			text = value;
		} else if (manipulationType.equals("replace")) {
			// TODO
		}
		return text;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject json = (JSONObject) p.parse(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
}
