package i5.las2peer.services.socialBotManagerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.ContentGenerator;
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Trigger;
import i5.las2peer.services.socialBotManagerService.model.TriggerFunction;
import i5.las2peer.services.socialBotManagerService.model.VLE;
import i5.las2peer.services.socialBotManagerService.model.VLERoutine;
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
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * las2peer-SocialBotManager-Service
 *
 * A REST service that manages social bots in a las2peer network.
 *
 */
@Api(
		value = "test")
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Bot Manager Service",
				version = "1.0.13",
				description = "A las2peer service for managing social bots.",
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

	private static HashMap<String, Boolean> botIsActive = new HashMap<String, Boolean>();

	private static BotConfiguration config;

	private static HashMap<String, BotAgent> botAgents;
	private static final String botPass = "actingAgent";

	private static ScheduledExecutorService rt = null;

	private int BOT_ROUTINE_PERIOD = 5; // 5 seconds

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
		if (getConfig() == null) {
			setConfig(new BotConfiguration());
			getConfig().setServiceConfiguration(new HashMap<String, VLE>());
		}
		if (getBotAgents() == null) {
			setBotAgents(new HashMap<String, BotAgent>());
		}

		if (rt == null) {
			rt = Executors.newSingleThreadScheduledExecutor();
			rt.scheduleAtFixedRate(new RoutineThread(), 0, BOT_ROUTINE_PERIOD, TimeUnit.SECONDS);
		}
	}

	@Override
	protected void initResources() {
		getResourceConfig().register(BotResource.class);
		getResourceConfig().register(BotModelResource.class);
		getResourceConfig().register(this);
	}

	@Api(
			value = "Bot Resource")
	@SwaggerDefinition(
			info = @Info(
					title = "las2peer Bot Manager Service",
					version = "1.0.13",
					description = "A las2peer service for managing social bots.",
					termsOfService = "",
					contact = @Contact(
							name = "Alexander Tobias Neumann",
							url = "",
							email = "neumann@dbis.rwth-aachen.de"),
					license = @License(
							name = "",
							url = "")))
	@Path("/bots")
	public static class BotResource {
		SocialBotManagerService sbfservice = (SocialBotManagerService) Context.get().getService();


		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "List of bots") })
		@ApiOperation(
				value = "Get all bots",
				notes = "Returns a list of all registered bots.")
		public Response getBots() {
			JSONObject vleList = new JSONObject();
			// Iterate through VLEs
			for (Entry<String, VLE> vleEntry : getConfig().getVLEs().entrySet()) {
			    String vleName = vleEntry.getKey();
			    VLE vle = vleEntry.getValue();
			    JSONObject botList = new JSONObject();
			    // Iterate bots
			    for (Entry<String, Bot> botEntry : vle.getBots().entrySet()) {
			    	Bot b = (Bot) botEntry.getValue();
					JSONObject jb = new JSONObject();
					JSONObject ac = new JSONObject();
					ac.putAll(b.getActive());
					jb.put("active", ac);
					jb.put("id", b.getId());
					jb.put("version", b.getVersion());
					botList.put(botEntry.getValue().getName(), jb);
			    }
			    vleList.put(vleName, botList);
			}
			return Response.ok().entity(vleList).build();
		}

		@GET
		@Path("/{botName}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Returns bot information") })
		@ApiOperation(
				value = "Retrieve bot by name",
				notes = "Returns bot information by the given name.")
		public Response getBotsForVLE(@PathParam("botName") String name) {
			VLE vle = getConfig().getVLEs().get(name);
			//Set<String> botList = new HashSet<String>();
			JSONObject j = new JSONObject();
			if (vle != null) {
				Iterator<Entry<String, Bot>> it = vle.getBots().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Bot> pair = (Map.Entry<String, Bot>) it.next();
					Bot b = pair.getValue();
					JSONObject jb = new JSONObject();
					JSONObject ac = new JSONObject();
					ac.putAll(b.getActive());
					jb.put("active", ac);
					jb.put("id", b.getId());
					jb.put("version", b.getVersion());
					j.put((String) pair.getKey(), jb);
					// it.remove(); // avoids a ConcurrentModificationException
				}
			}
			return Response.ok().entity(j).build();
		}

		/**
		 * Initialize a bot.
		 *
		 * @param botModel Model of a bot
		 *
		 * @return Returns an HTTP response with plain text string content.
		 */
		@POST
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Init successful.") })
		@ApiOperation(
				value = "Init Bot",
				notes = "Reads the configuration file.")
		public Response init(BotModel botModel) {
			BotParser bp = BotParser.getInstance();

			String returnString = "";
			LinkedHashMap<String, BotModelNode> nodes = botModel.getNodes();
			LinkedHashMap<String, BotModelEdge> edges = botModel.getEdges();
			try {
				bp.parseNodesAndEdges(SocialBotManagerService.getConfig(), SocialBotManagerService.getBotAgents(), nodes, edges);
			} catch (ParseBotException | IOException | DeploymentException e) {
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			}
			// initialized = true;
			JSONObject logData = new JSONObject();
			logData.put("status", "initialized");
			Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, logData.toString());

			return Response.ok().entity(returnString).build();
			// }
		}

		/**
		 * Join function
		 *
		 * @param body TODO
		 * @param botName TODO
		 * @return Returns an HTTP response with plain text string content derived from the path input param.
		 */
		@POST
		@Path("/{botName}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Bot activated") })
		@ApiOperation(
				value = "Activate Bot",
				notes = "Has the capability to join the digital space to get rights.")
		public Response join(String body, @PathParam("botName") String botName) {
			String returnString = "";
			try {
				BotAgent botAgent = getBotAgents().get(botName);
				if(botAgent==null) {
					return Response.status(Status.NOT_FOUND).entity("Bot not found").build();
				}
				body = body.replace("$botId", botAgent.getIdentifier());
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject j = (JSONObject) p.parse(body);
				String basePath = (String) j.get("basePath");
				String[] s = basePath.split("/");
				String service = s[s.length - 1];
				VLE vle = getConfig().getServiceConfiguration(service);

				Bot bot = null;
				for (Bot b: vle.getBots().values()) {
					if(b.getName().equals(botName)) {
						bot = b;
						break;
					}
				}
				if(bot==null) {
					return Response.status(Status.NOT_FOUND).entity("Bot not found").build();
				}
				String sepName = getConfig().getServiceConfiguration(service).getEnvironmentSeparator();
				System.out.println(sepName);
				String sepValue;
				if(sepName.equals("singleEnvironment")) {
					sepValue = sepName;
				}else {
					sepValue = j.getAsString(sepName);
				}

				botIsActive.put(sepValue, true);
				bot.setIdActive(sepValue,true);

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
					System.out.println(result.getResponse());
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Response.ok().entity(returnString).build();
		}

		@POST
		@Path("/{botName}/trigger/service")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(
				value = "Trigger bot by service function",
				notes = "Service Function triggers bot")
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Bot triggered") })
		public Response trigger(String body, @PathParam("botName") String name) {
			String returnString = "";
			try {
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject parsedBody = (JSONObject) p.parse(body);
				String service = (String) parsedBody.get("serviceAlias");
				VLE vle = getConfig().getServiceConfiguration(service);

				if (!vle.getEnvironmentSeparator().equals("singleEnvironment")) {
					if (vle == null || vle.getEnvironmentSeparator() == null
							|| ((JSONObject) parsedBody.get("attributes")).get(vle.getEnvironmentSeparator()) == null || botIsActive
									.get(((JSONObject) parsedBody.get("attributes")).get(vle.getEnvironmentSeparator())) != true) {
						return Response.status(Status.FORBIDDEN).entity("Bot is not active").build();
					}
				}

				String triggerFunctionName = parsedBody.getAsString("functionName");
				String triggerUID = parsedBody.getAsString("uid");

				for (BotAgent botAgent : getBotAgents().values()) {
					try {
						this.sbfservice.checkTriggerBot(vle, parsedBody, botAgent, triggerUID, triggerFunctionName);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Response.ok().entity(returnString).build();
		}

		@POST
		@Path("/{botName}/trigger/routine")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(
				value = "Trigger bot by routine",
				notes = "Routine triggers bot")
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Bot triggered") })
		public Response triggerRoutine(String body, @PathParam("botName") String name) {
			String returnString = "Routine is running.";
			SocialBotManagerService sbf = this.sbfservice;
			new Thread(new Runnable() {
			     @Override
			     public void run() {
					try {
						JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

						JSONObject j = (JSONObject) p.parse(body);
						String service = (String) j.get("serviceAlias");
						VLE vle = getConfig().getServiceConfiguration(service);
						JSONObject context = new JSONObject();
						context.put("addr", vle.getAddress());
						if (!vle.getEnvironmentSeparator().equals("singleEnvironment")) {
							if (vle == null || vle.getEnvironmentSeparator() == null
									|| ((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator()) == null || botIsActive
											.get(((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator())) != true) {
								return;
							}else {
								JSONObject atts = (JSONObject)j.get("attributes");
								context.put("env", atts.getAsString(vle.getEnvironmentSeparator()));
							}
						}

						String botFunctionId = j.getAsString("function");
						BotAgent botAgent = getBotAgents().get(j.getAsString("bot"));

						try {
							sbf.checkRoutineTrigger(vle, j, botAgent, botFunctionId, context);
							// checkTriggerBot(vle, j, botAgent, "", f);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try{
				            Thread.sleep(500);
				        } catch(InterruptedException e){
				            e.printStackTrace();
				        }
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("Routine finished.");
				}
			}).start();
			return Response.ok().entity(returnString).build();
		}

		@DELETE
		@Path("/{botName}/{unit}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Bot deactivated") })
		@ApiOperation(
				value = "Deactivate bot for unit",
				notes = "Deactivates a bot for a unit.")
		public Response deactivateBot(@PathParam("botName") String bot, @PathParam("unit") String unit) {
			Collection<VLE> vles = getConfig().getVLEs().values();
			for(VLE vle:vles) {
				Bot b = vle.getBots().get(bot);
				if (b != null) {
					b.setIdActive(unit, false);
					return Response.ok().entity(bot + " deactivated.").build();
				}
			}

			return Response.status(Status.NOT_FOUND).entity(bot + " not found.").build();
		}

		@GET
		@Path("/{botName}/generators")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "List of content generators") })
		@ApiOperation(
				value = "Get content generators",
				notes = "Returns a list of content generators specified for that bot.")
		public Response getContentGenerators(@PathParam("botName") String service) {
			VLE vle = null;
			if (getConfig() != null) {
				vle = getConfig().getServiceConfiguration(service);
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
	}

	public void checkRoutineTrigger(VLE vle, JSONObject j, BotAgent botAgent, String botFunctionId, JSONObject context)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException, AgentNotFoundException, AgentOperationFailedException {
		String botId = botAgent.getIdentifier();
		Bot bot = vle.getBots().get(botId);
		if (bot != null) {
			System.out.println("Bot " + botAgent.getLoginName() + " triggered:");
			ServiceFunction botFunction = bot.getBotServiceFunctions().get(botFunctionId);

			String functionPath = "";
			if (botFunction.getActionType().equals(ActionType.SERVICE))
				functionPath = botFunction.getFunctionPath();
			JSONObject body = new JSONObject();
			HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String,ServiceFunctionAttribute>();

			JSONObject triggerAttributes = (JSONObject) j.get("attributes");
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes()) {
				formAttributes(vle, sfa, bot, body, functionPath, attlist, triggerAttributes);
			}

			performTrigger(vle, botFunction, botAgent, functionPath, "", body);

		}
	}

	public void checkTriggerBot(VLE vle, JSONObject body, BotAgent botAgent, String triggerUID, String triggerFunctionName)
			throws AgentNotFoundException, AgentOperationFailedException, ServiceNotFoundException,
			ServiceNotAvailableException, InternalServiceException, ServiceMethodNotFoundException,
			ServiceInvocationFailedException, ServiceAccessDeniedException, ServiceNotAuthorizedException,
			ParseBotException {
		String botId = botAgent.getIdentifier();

		Bot bot = vle.getBots().get(botId);
		if (bot != null
				&& !(triggerUID.toLowerCase().equals(botAgent.getIdentifier().toLowerCase()))) {

			// get all triggers of the bot
			Set<Trigger> tlist = bot.getTriggerList();
			for(Trigger trigger:tlist) {
				TriggerFunction tf = trigger.getTriggerFunction();
				// in this function we only handle service functions
				if(tf instanceof ServiceFunction) {
					ServiceFunction sf = (ServiceFunction) tf;
					// check if the function name we got equals the service function name
					if(sf.getFunctionName().equals(triggerFunctionName)) {
						ServiceFunction triggeredFunction = trigger.getTriggeredFunction();

						String functionPath = "";
						// add path if the triggered function is a service function
						if (triggeredFunction.getActionType().equals(ActionType.SERVICE))
							functionPath = triggeredFunction.getFunctionPath();
						JSONObject triggeredBody = new JSONObject();
						HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String,ServiceFunctionAttribute>();
						for(ServiceFunction bsf:bot.getBotServiceFunctions().values()) {
							for(ServiceFunctionAttribute bsfa:bsf.getAttributes()) {
								attlist.put(bsfa.getId(), bsfa);
							}
						}

						JSONObject triggerAttributes = (JSONObject) body.get("attributes");
						for (ServiceFunctionAttribute triggeredFunctionAttribute : triggeredFunction.getAttributes()) {
							formAttributes(vle, triggeredFunctionAttribute, bot, triggeredBody, functionPath, attlist, triggerAttributes);
						}

						System.out.println("Performing...");
						performTrigger(vle, triggeredFunction, botAgent, functionPath, triggerUID, triggeredBody);
					}
				}
			}

		}else {
			//TODO
		}
	}

	private void formAttributes(VLE vle, ServiceFunctionAttribute triggeredFunctionAttribute, Bot bot, JSONObject triggeredBody, String functionPath,
			HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException {
		// Attributes of the triggered function
		if (triggeredFunctionAttribute.isSameAsTrigger()) {
			mapAttributes(triggeredBody, triggeredFunctionAttribute, functionPath, attlist, triggerAttributes);
		} else if (triggeredFunctionAttribute.getName() == "body") {
			JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
			for (ServiceFunctionAttribute subsfa : triggeredFunctionAttribute.getChildAttributes()) {
				if (subsfa.isSameAsTrigger()) {
					ServiceFunctionAttribute mappedTo = subsfa.getMappedTo();
					if (triggerBody.get(mappedTo.getName()) != null)
						triggeredBody.put(subsfa.getName(), triggerBody.get(mappedTo.getName()));
					else
						triggeredBody.put(subsfa.getName(), triggerAttributes.get(mappedTo.getName()));
				} else {
					// Use AI to generate body
					ContentGenerator g = subsfa.getGenerator();
					if (g != null) {
						mapWithContentGenerator(triggeredBody, g, subsfa.getName(), subsfa.getContentType(), functionPath, attlist,
								triggerAttributes, vle.getEnvironmentSeparator());
					} else {
						if (triggeredFunctionAttribute.getItb()!=null) {
							mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute, triggeredBody, attlist, triggerAttributes, functionPath);
						} else {
							if (subsfa.hasStaticContent()) {
								mapWithStaticContent(subsfa, triggeredBody);
							} else {
								// TODO no match!
							}
						}
					}

				}

			}
		} else {
			ContentGenerator g = triggeredFunctionAttribute.getGenerator();
			if (g != null) {
				mapWithContentGenerator(triggeredBody, g, triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContentType(), functionPath, attlist, triggerAttributes,
						vle.getEnvironmentSeparator());
			} else {

				System.out.println(triggeredFunctionAttribute.getName());
				if (triggeredFunctionAttribute.getItb()!=null) {
					System.out.println("4");
					mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute, triggeredBody, attlist, triggerAttributes, functionPath);
				} else {
					System.out.println("5");
					if (triggeredFunctionAttribute.hasStaticContent()) {
						mapWithStaticContent(triggeredFunctionAttribute, triggeredBody);
					} else {
						// TODO no match!
					}
				}
			}
		}
	}

	private void mapWithIfThen(IfThenBlock itb, ServiceFunctionAttribute triggeredFunctionAttribute,
			JSONObject triggeredBody, HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes,
			String functionPath) {
		IfThenBlock ifThenIterator = itb;
		while(ifThenIterator.getPrev()!=null) {
			ifThenIterator = ifThenIterator.getPrev();
		}
		System.out.println(triggerAttributes.toJSONString());
		ServiceFunctionAttribute triggerAttribute = ifThenIterator.getSourceAttribute();
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		String source ="";
		if (triggerBody != null && triggerBody.containsKey(triggerAttribute.getName())) {
			source = triggerBody.getAsString(triggerAttribute.getName());
		} else if (triggerAttributes.containsKey(triggerAttribute.getName())) {
			source = triggerAttributes.getAsString(triggerAttribute.getName());
		}

		do {
			if(checkIfCondition(ifThenIterator,source)) {
				source = manipulateString(ifThenIterator,source);
			}
			ifThenIterator = ifThenIterator.getNext();
		} while (ifThenIterator != null);
		triggeredBody.put(triggeredFunctionAttribute.getName(), source);
	}

	private void mapWithStaticContent(ServiceFunctionAttribute triggeredFunctionAttribute, JSONObject triggeredBody) {
		if (triggeredFunctionAttribute.getContent().length() > 0) {
			triggeredBody.put(triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContent());
		}
		if (triggeredFunctionAttribute.getContentURL().length() > 0) {
			URL url;
			String body = "";
			try {
				url = new URL(triggeredFunctionAttribute.getContentURL());
				HttpURLConnection  con = (HttpURLConnection) url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);

				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(
			            new InputStreamReader(con.getInputStream(), "utf-8"));
			    String line = null;
			    while ((line = br.readLine()) != null) {
			        sb.append(line + "\n");
			    }
			    br.close();

				body = sb.toString();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			triggeredBody.put(triggeredFunctionAttribute.getName(), body);
		}
	}



	private void mapWithContentGenerator(JSONObject b, ContentGenerator g, String sfaName, String sfaType,
			String functionPath, HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes,
			String envSep) throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException {

		ServiceFunctionAttribute input = g.getInput();
		String sourceAttributeName = input.getName();
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
			if (functionPath.contains("{" + sfaName + "}")) {
				functionPath = functionPath.replace("{" + sfaName + "}", (String) rmiResult);
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

	private void mapAttributes(JSONObject b, ServiceFunctionAttribute sfa, String functionPath,
			HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes) {
		// get id of the trigger function
		ServiceFunctionAttribute mappedTo = sfa.getMappedTo();
		// attributes of the function that triggered the bot
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		if (triggerAttributes.containsKey(mappedTo.getName())) {
			String replaceWith = triggerAttributes.getAsString(mappedTo.getName());
			if (functionPath.contains("{" + sfa.getName() + "}")) {
				functionPath = functionPath.replace("{" + sfa.getName() + "}", replaceWith);
			} else {
				b.put(sfa.getName(), replaceWith);
			}
		} else if (triggerBody != null && triggerBody.containsKey(mappedTo.getName())) {
			String replaceWith = triggerBody.getAsString(mappedTo.getName());
			if (functionPath.contains("{" + sfa.getName() + "}")) {
				functionPath = functionPath.replace("{" + sfa.getName() + "}", replaceWith);
			} else {
				b.put(sfa.getName(), replaceWith);
			}
		} else {
			// TODO Error could not map attributes
		}
	}

	private void performTrigger(VLE vle, ServiceFunction sf, BotAgent botAgent, String functionPath, String triggerUID,
			JSONObject triggeredBody) throws AgentNotFoundException, AgentOperationFailedException {
		if (sf.getActionType().equals(ActionType.SERVICE)) {
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(vle.getAddress());
			client.setLogin(botAgent.getLoginName(), botPass);

			HashMap<String, String> headers = new HashMap<String, String>();
			ClientResponse r = client.sendRequest(sf.getHttpMethod().toUpperCase(), sf.getServiceName() + functionPath,
					triggeredBody.toJSONString(), sf.getConsumes(), sf.getProduces(), headers);
			System.out.println(r.getResponse());
		} else if (sf.getActionType().equals(ActionType.SENDMESSAGE)) {
			if (triggeredBody.get("channel") == null && triggeredBody.get("email")==null) {
				// TODO Anonymous agent error
				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(vle.getAddress());
				HashMap<String, String> headers = new HashMap<String, String>();
				ClientResponse result = client.sendRequest("GET", "SBFManager/email/"+triggerUID,
						"", MediaType.TEXT_HTML, MediaType.TEXT_HTML, headers);
				String mail = result.getResponse().trim();
				triggeredBody.put("email", mail);
			}
			String messengerID = sf.getMessengerName();
			ChatMediator chat = vle.getMessenger(messengerID).getChatMediator();
			triggerChat(chat, botAgent, triggeredBody.toJSONString());
		}
	}


	public void triggerChat(ChatMediator chat, BotAgent bot, String body) {
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject b;
		try {
			b = (JSONObject) p.parse(body);
			String text = b.getAsString("text");
			if(chat instanceof SlackChatMediator) {
				SlackChatMediator slack = (SlackChatMediator)chat;
				String channel;
				if (b.containsKey("email")) {
					String email = b.getAsString("email");
					channel = slack.getChannelByEmail(email);
				} else {
					channel = b.getAsString("channel");
				}
				slack.sendMessageToChannel(channel, text);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Api(
			value = "Model Resource")
	@SwaggerDefinition(
			info = @Info(
					title = "las2peer Bot Manager Service",
					version = "1.0.13",
					description = "A las2peer service for managing social bots.",
					termsOfService = "",
					contact = @Contact(
							name = "Alexander Tobias Neumann",
							url = "",
							email = "neumann@dbis.rwth-aachen.de"),
					license = @License(
							name = "",
							url = "")))
	@Path("/models")
	public static class BotModelResource {
		SocialBotManagerService service = (SocialBotManagerService) Context.get().getService();

		/**
		 * Put Model function.
		 *
		 * @param name name of the model
		 * @param body content of the model
		 * @return Returns an HTTP response with plain text string content derived from the path input param.
		 */
		@POST
		@Path("/{name}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Model stored") })
		@ApiOperation(
				value = "Save BotModel",
				notes = "Stores the BotModel in the shared storage.")
		public Response putModel(@PathParam("name") String name, BotModel body) {
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
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "List of BotModels") })
		@ApiOperation(
				value = "Retrieve BotModels",
				notes = "Get all stored BotModels.")
		public Response getModels() {
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
			return Response.ok().entity(models.keySet()).build();
		}

		@GET
		@Path("/{name}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = { @ApiResponse(
						code = HttpURLConnection.HTTP_OK,
						message = "Return BotModel") })
		@ApiOperation(
				value = "Get BotModel by name",
				notes = "Returns the BotModel for the given name.")
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
	}


	public boolean getMessages(ArrayList<BotMessage> messages) {
		System.out.println("Bot: Got " + messages.size() + " bot messages!");
		for (BotMessage m : messages) {
			BotResource br = new BotResource();
			br.trigger(m.getRemarks(),"");
		}
		return true;
	}

	private boolean checkIfCondition(IfThenBlock itb, String text) {
		String conditionType = itb.getConditionType();
		if (conditionType.equals("Contains")) {
			return text.contains(itb.getConditionValueA());
		} else if (conditionType.equals("Equals")) {
			return text.equals(itb.getConditionValueA());
		} else if (conditionType.equals("True")) {
			return true;
		} else if (conditionType.equals("Less Than")) {
			return text.length() < Integer.parseInt(itb.getConditionValueA());
		} else if (conditionType.equals("Greater Than")) {
			return text.length() > Integer.parseInt(itb.getConditionValueA());
		}
		// TODO implement more
		return false;
	}

	private String manipulateString(IfThenBlock itb, String text) {
		String manipulationType = itb.getStatementType();
		if (manipulationType.equals("Return")) {
			text = itb.getStatementValueA();
		} else if (manipulationType.equals("Replace")) {
			text = text.replace(itb.getStatementValueA(), itb.getStatementValueB());
		} else if (manipulationType.equals("Append")) {
			text = text + itb.getStatementValueA();
		} else if (manipulationType.equals("Prepend")) {
			text = itb.getStatementValueA() + text;
		}
		return text;
	}

	public static BotConfiguration getConfig() {
		return config;
	}

	public static void setConfig(BotConfiguration config) {
		SocialBotManagerService.config = config;
	}

	public static HashMap<String, BotAgent> getBotAgents() {
		return botAgents;
	}

	public static void setBotAgents(HashMap<String, BotAgent> botAgents) {
		SocialBotManagerService.botAgents = botAgents;
	}

	private class RoutineThread implements Runnable {

		public void run() {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
			for (VLE vle : getConfig().getVLEs().values()) {
				vle.handleMessages();

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
					if (r.getInterval().equals("Minute")) {

						if (min >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}
					if (r.getInterval().equals("Hour")) {
						long hour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (hour >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}
					if (r.getInterval().equals("Day")) {
						long day = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (day >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}
					if (r.getInterval().equals("Month")) {
						long day = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						// TODO
						day = day / 28;
						if (day >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					} else if (r.getInterval().equals("Working days") && dayOfWeek != Calendar.SATURDAY
							&& dayOfWeek != Calendar.SUNDAY) {
						if (min >= 1 && df2.format(d1).equals(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					} else if (r.getInterval().equals("Weekend")
							&& (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)) {
						if (min >= 1 && df2.format(d1).equals(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					} else if (r.getInterval().equals("Every day")) {
						if (min >= 1 && df2.format(d1).equals(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}
					}

					if (trigger) {
						System.out.println("Trigger active");
						for (Bot b : vle.getBots().values()) {
							HashMap<String,Boolean> activeBots = b.getActive();
							HashSet<Trigger> tList = r.getTrigger();
							for (Trigger t : tList) {
								for (Entry<String, Boolean> entry : activeBots.entrySet()) {
									// If bot is active
									if (entry.getValue()) {
										System.out.println(df.format(d1) + ": " + b.getName());
										MiniClient client = new MiniClient();
										client.setConnectorEndpoint(vle.getAddress());

										JSONObject body = new JSONObject();
										body.put("serviceAlias", vle.getName());

										JSONObject atts = new JSONObject();

										body.put("function", t.getTriggeredFunction().getId());
										body.put("bot", b.getName());
										atts.put(vle.getEnvironmentSeparator(), entry.getKey());
										body.put("attributes", atts);

										HashMap<String, String> headers = new HashMap<String, String>();
										ClientResponse result = client.sendRequest("POST", "SBFManager/"+b.getName()+"/trigger/routine",
												body.toJSONString(), MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
										System.out.println(result.getResponse());
									}
								}
								//jar.add(t.getTriggeredFunction().getId());
							}

						}
					}
				}
			}
		}
	}

}
