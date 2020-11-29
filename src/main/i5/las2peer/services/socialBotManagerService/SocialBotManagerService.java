package i5.las2peer.services.socialBotManagerService;

import java.io.BufferedReader;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.logging.Level;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.grizzly.http.util.URLDecoder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.bot.BotMessage;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.BotAgent;
import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.ChatService;
import i5.las2peer.services.socialBotManagerService.chat.EventChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabaseType;
import i5.las2peer.services.socialBotManagerService.dialogue.notification.EventMessage;
import i5.las2peer.services.socialBotManagerService.dialogue.notification.TriggerHandler;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.ContentGenerator;
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.NLUKnowledge;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.ServiceType;
import i5.las2peer.services.socialBotManagerService.model.Trigger;
import i5.las2peer.services.socialBotManagerService.model.TriggerFunction;
import i5.las2peer.services.socialBotManagerService.model.VLE;
import i5.las2peer.services.socialBotManagerService.model.VLERoutine;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.LanguageUnderstander;
import i5.las2peer.services.socialBotManagerService.nlu.NLUGenerator;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.nlu.TrainingHelper;
import i5.las2peer.services.socialBotManagerService.parser.BotModelParser;
import i5.las2peer.services.socialBotManagerService.parser.BotParser;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import i5.las2peer.services.socialBotManagerService.parser.training.TrainingData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
 * A REST service that manages social bots in a las2peer network.
 *
 */
@Api(value = "test")
@SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.0.19", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "", url = "")))
@ServicePath("/SBFManager")
@ManualDeployment
public class SocialBotManagerService extends RESTService {

	private String databaseName;
	private int databaseTypeInt = 1; // See SQLDatabaseType for more information
	private SQLDatabaseType databaseType;
	private String databaseHost;
	private int databasePort;
	private String databaseUser;
	private String databasePassword;
	private SQLDatabase database; // The database instance to write to.

	private static final String ENVELOPE_MODEL = "SBF_MODELLIST";

	private static HashMap<String, Boolean> botIsActive = new HashMap<String, Boolean>();
	private static HashMap<String, String> rasaIntents = new HashMap<String, String>();

	private static BotConfiguration config;

	private static HashMap<String, BotAgent> botAgents;
	private static final String botPass = "actingAgent";

	private static ScheduledExecutorService rt = null;

	private int BOT_ROUTINE_PERIOD = 5; // 1 second

	private TrainingHelper nluTrain = null;
	private Thread nluTrainThread = null;
	private static final L2pLogger logger = L2pLogger.getInstance(SocialBotManagerService.class.getName());

	public SocialBotManagerService() {
		super();
		setFieldValues(); // This sets the values of the configuration file
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
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
		if (getBotAgents() == null)
			setBotAgents(new HashMap<String, BotAgent>());

		this.databaseType = SQLDatabaseType.getSQLDatabaseType(databaseTypeInt);
		System.out.println(this.databaseType + " " + this.databaseUser + " " + this.databasePassword + " "
				+ this.databaseName + " " + this.databaseHost + " " + this.databasePort);
		this.database = new SQLDatabase(this.databaseType, this.databaseUser, this.databasePassword, this.databaseName,
				this.databaseHost, this.databasePort);
		try {
			Connection con = database.getDataSource().getConnection();
			con.close();
		} catch (SQLException e) {
			System.out.println("Failed to Connect: " + e.getMessage());
		}

		if (rt == null) {
			rt = Executors.newSingleThreadScheduledExecutor();
			rt.scheduleAtFixedRate(new RoutineThread(), 0, BOT_ROUTINE_PERIOD, TimeUnit.SECONDS);
		}
		L2pLogger.setGlobalConsoleLevel(Level.WARNING);
	}

	@Override
	protected void initResources() {
		getResourceConfig().register(BotResource.class);
		getResourceConfig().register(BotModelResource.class);
		getResourceConfig().register(TrainingResource.class);
		getResourceConfig().register(this);
	}

	@POST
	@Path("/trainAndLoad")
	@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@ApiOperation(value = "Trains and loads an NLU model on the given Rasa NLU server instance.", notes = "")
	// TODO: Just an adapter, since the Rasa server doesn't support
	// "Access-Control-Expose-Headers"
	// and the model file name is returned as a response header... Remove and just
	// use Rasa's
	// API directly once that's fixed. The whole `TrainingHelper` class can be
	// deleted then as well.
	public Response trainAndLoad(String body) {
		System.out.println("train and load");
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

		if (this.nluTrainThread != null && this.nluTrainThread.isAlive())
			return Response.status(Status.SERVICE_UNAVAILABLE).entity("Training still in progress.").build();

		try {
			System.out.println(body);
			JSONObject bodyJson = (JSONObject) p.parse(body);
			String url = bodyJson.getAsString("url");
			String config = bodyJson.getAsString("config");
			String markdownTrainingData = bodyJson.getAsString("markdownTrainingData");

			// added to have a way to access the intents of the rasa server
			// this.rasaIntents.put(url.split("://")[1], intents);
			this.nluTrain = new TrainingHelper(url, config, markdownTrainingData);
			this.nluTrainThread = new Thread(this.nluTrain);
			this.nluTrainThread.start();
			// TODO: Create a member for this thread, make another REST method to check
			// whether
			// training was successful.

			TrainingData td = new TrainingData();
			td.fromMarkdown(markdownTrainingData);
			System.out.println("NLU module " + url + " add intents: " + td.intents().size());

			LanguageUnderstander lu = getConfig().getNlus().get(url);

			if (lu != null)
				lu.addIntents(td.intents());
			else {
				LanguageUnderstander nlu = new RasaNlu(url);
				nlu.addIntents(td.intents());
				getConfig().addNLU(nlu);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Doesn't signal that training and loading was successful, but that it was
		// started.
		return Response.ok("Training started.").build();
	}

	@GET
	@Path("/trainAndLoadStatus")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Returns information about the training process started by the last invocation of `/trainAndLoad`.", notes = "")
	// TODO: Just an adapter, since the Rasa server doesn't support
	// "Access-Control-Expose-Headers"
	// and the model file name is returned as a response header... Remove and just
	// use Rasa's
	// API directly once that's fixed. The whole `TrainingHelper` class can be
	// deleted then as well.
	public Response trainAndLoadStatus(String body) {
		if (this.nluTrainThread == null)
			return Response.ok("No training process was started yet.").build();
		if (this.nluTrainThread.isAlive())
			return Response.ok("Training still in progress.").build();
		if (this.nluTrain.getSuccess())
			return Response.ok("Training was successful.").build();
		return Response.ok("Training failed.").build();
	}

	@GET
	@Path("/{rasaUrl}/intents")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Returns the intents of a current Rasa Model.", notes = "")
	public Response getIntents(@PathParam("rasaUrl") String url) {
		if (this.rasaIntents.get(url) == null)
			return Response.ok("failed.").build();
		else {
			String intents = this.rasaIntents.get(url);
			JSONObject ex = new JSONObject();
			ex.put("intents", intents);
			return Response.ok().entity(ex).build();

		}
	}

	@Api(value = "Bot Resource")
	@SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.0.13", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "", url = "")))
	@Path("/bots")
	public static class BotResource {
		SocialBotManagerService sbfservice = (SocialBotManagerService) Context.get().getService();

		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of bots") })
		@ApiOperation(value = "Get all bots", notes = "Returns a list of all registered bots.")
		public Response getBots() {

			JSONObject vleList = new JSONObject();
			// Iterate through VLEs
			for (Entry<String, VLE> vleEntry : getConfig().getVLEs().entrySet()) {
				String vleName = vleEntry.getKey();
				VLE vle = vleEntry.getValue();
				JSONObject botList = new JSONObject();
				// Iterate bots
				for (Entry<String, Bot> botEntry : vle.getBots().entrySet()) {
					Bot b = botEntry.getValue();
					JSONObject jb = new JSONObject();
					JSONObject ac = new JSONObject();
					ac.putAll(b.getActive());
					jb.put("active", ac);
					jb.put("id", b.getId());
					jb.put("name", b.getName());
					jb.put("version", b.getVersion());

					JSONArray messengerList = new JSONArray();
					for (Entry<String, Messenger> messengerEntry : b.getMessengers().entrySet()) {
						JSONObject jm = new JSONObject();
						jm.put("type", messengerEntry.getValue().getChatService().toString());
						jm.put("name", messengerEntry.getValue().getName());
						messengerList.add(jm);
					}
					jb.put("messengers", messengerList);
					botList.put(botEntry.getValue().getName(), jb);
				}
				vleList.put(vleName, botList);
			}
			return Response.ok().entity(vleList).build();
		}

		@GET
		@Path("/{vleName}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns bot information") })
		@ApiOperation(value = "Retrieve bot by name", notes = "Returns bot information by the given VLE name.")
		public Response getBotsForVLE(@PathParam("vleName") String name) {
			VLE vle = getConfig().getVLEs().get(name);
			// Set<String> botList = new HashSet<String>();
			JSONObject j = new JSONObject();
			if (vle != null) {
				Iterator<Entry<String, Bot>> it = vle.getBots().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Bot> pair = it.next();
					Bot b = pair.getValue();
					JSONObject jb = new JSONObject();
					JSONObject ac = new JSONObject();
					ac.putAll(b.getActive());
					jb.put("active", ac);
					jb.put("id", b.getId());
					jb.put("name", b.getName());
					jb.put("version", b.getVersion());
					j.put(pair.getKey(), jb);
					// it.remove(); // avoids a ConcurrentModificationException
				}
				j.put("slack", vle.getSlackBotMap().toString());
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
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Init successful.") })
		@ApiOperation(value = "Init Bot", notes = "Reads the configuration file.")
		public Response init(BotModel botModel) {
			BotParser bp = BotParser.getInstance();

			String returnString = "";
			LinkedHashMap<String, BotModelNode> nodes = botModel.getNodes();
			LinkedHashMap<String, BotModelEdge> edges = botModel.getEdges();
			try {
				bp.parseNodesAndEdges(SocialBotManagerService.getConfig(), SocialBotManagerService.getBotAgents(),
						nodes, edges, sbfservice.database);
			} catch (ParseBotException | IOException | DeploymentException e) {
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (Exception e) {
				e.printStackTrace();
				return Response.serverError().build();
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
		 * @param body    TODO
		 * @param botName TODO
		 * @return Returns an HTTP response with plain text string content derived from
		 *         the path input param.
		 */
		@POST
		@Path("/{botName}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Bot activated") })
		@ApiOperation(value = "Activate Bot", notes = "Has the capability to join the digital space to get rights.")
		public Response join(String body, @PathParam("botName") String botName) {
			String returnString = "";
			try {
				BotAgent botAgent = getBotAgents().get(botName);
				if (botAgent == null)
					return Response.status(Status.NOT_FOUND).entity("Bot " + botName + " not found1").build();
				body = body.replace("$botId", botAgent.getIdentifier());
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject j = (JSONObject) p.parse(body);
				String basePath = (String) j.get("basePath");
				String[] s = basePath.split("/");
				String service = s[s.length - 1];
				VLE vle = getConfig().getServiceConfiguration(service);

				Bot bot = null;
				for (Bot b : vle.getBots().values())
					if (b.getName().equals(botName)) {
						bot = b;
						break;
					}
				if (bot == null)
					return Response.status(Status.NOT_FOUND).entity("Bot " + botName + " not found2").build();
				String sepName = getConfig().getServiceConfiguration(service).getEnvironmentSeparator();

				String sepValue;
				if (sepName.equals("singleEnvironment"))
					sepValue = sepName;
				else
					sepValue = j.getAsString(sepName);

				botIsActive.put(sepValue, true);
				bot.setIdActive(sepValue, true);

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
		@ApiOperation(value = "Trigger bot by service function", notes = "Service Function triggers bot")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Bot triggered") })
		public Response trigger(String body, @PathParam("botName") String name) {
			String returnString = "";
			try {
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject parsedBody = (JSONObject) p.parse(body);
				String service = (String) parsedBody.get("serviceAlias");
				VLE vle = getConfig().getServiceConfiguration(service);

				if (vle == null)
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity("VLE does not exist").build();

				String seperator = vle.getEnvironmentSeparator();
				JSONObject attributes = ((JSONObject) parsedBody.get("attributes"));

				if (!seperator.equals("singleEnvironment"))
					if (seperator == null || attributes == null || attributes.get(seperator) == null
							|| botIsActive.get(attributes.get(seperator)) == false)
						return Response.status(Status.FORBIDDEN).entity("Bot is not active").build();

				String triggerFunctionName = parsedBody.getAsString("functionName");
				String triggerUID = parsedBody.getAsString("uid");

				for (BotAgent botAgent : getBotAgents().values())
					try {
						this.sbfservice.checkTriggerBot(vle, parsedBody, botAgent, triggerUID, triggerFunctionName);
					} catch (Exception e) {
						e.printStackTrace();
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
		@ApiOperation(value = "Trigger bot by routine", notes = "Routine triggers bot")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Bot triggered") })
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
						if (!vle.getEnvironmentSeparator().equals("singleEnvironment"))
							if (vle == null || vle.getEnvironmentSeparator() == null
									|| ((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator()) == null
									|| botIsActive.get(((JSONObject) j.get("attributes"))
											.get(vle.getEnvironmentSeparator())) != true)
								return;
							else {
								JSONObject atts = (JSONObject) j.get("attributes");
								context.put("env", atts.getAsString(vle.getEnvironmentSeparator()));
							}

						String botFunctionId = j.getAsString("function");
						BotAgent botAgent = getBotAgents().get(j.getAsString("bot"));

						try {
							sbf.checkRoutineTrigger(vle, j, botAgent, botFunctionId, context);
							// checkTriggerBot(vle, j, botAgent, "", f);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
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

		@POST
		@Path("/{botName}/trigger/intent")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Log message to MobSOS and trigger bot by intent if necessary")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "") })
		public Response triggerIntent(String body, @PathParam("botName") String name) {
			Gson gson = new Gson();
			MessageInfo m = gson.fromJson(body, MessageInfo.class);

			System.out.println("Got info: " + m.getMessage().getText() + " " + m.getTriggeredFunctionId());
			Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, body);
			// If no action should be triggered, just return
			if (m.getTriggeredFunctionId() == null)
				return Response.ok().build();

			SocialBotManagerService sbf = this.sbfservice;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						BotAgent botAgent = getBotAgents().get(m.getBotName());
						String service = m.getServiceAlias();
						VLE vle = getConfig().getServiceConfiguration(service);

						try {
							sbf.performIntentTrigger(vle, botAgent, m);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("Intent processing finished.");
				}
			}).start();
			return Response.ok().build();
		}

		@POST
		@Path("/events/slack")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Receive an Slack event")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "") })
		public Response slackAction(String body) {

			JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject parsedBody;

			try {
				parsedBody = (JSONObject) jsonParser.parse(body);
				String type = (String) parsedBody.get("type"); // event type (e.g event_callback, url_verification)
				String teamID = (String) parsedBody.get("team_id"); // workspace/team where this event occurred
				String appID = (String) parsedBody.get("api_app_id"); // application this event is intended for

				System.out.println("slack event: " + parsedBody);

				// TODO Verifying requests from Slack
				// https://api.slack.com/authentication/verifying-requests-from-slack
				// Verification tokens are deprecated. Use signed secrets

				// Verification when subscribe to be notified of events in Slack
				if (type.contentEquals("url_verification")) {
					String challenge = (String) parsedBody.get("challenge");
					return Response.ok().entity(challenge).build();
				}

				// Handle the received event
				if (type.contentEquals("event_callback")) {
					new Thread(new Runnable() {
						@Override
						public void run() {

							// Identify bot
							Collection<VLE> vles = getConfig().getVLEs().values();
							Bot bot = null;
							for (VLE vle : vles) {
								bot = vle.getBotbySlackID(appID, teamID);
								System.out.println("appId:" + appID + " teamID:" + teamID);
							}
							if (bot == null)
								System.out.println("cannot relate event to a bot");
							else {
								System.out.println("slack event: bot identified: " + bot.getName());

								// Handle event
								JSONObject parsedEventBody = (JSONObject) parsedBody.get("event");
								Messenger messenger = bot.getMessenger(appID);
								EventChatMediator mediator = (EventChatMediator) messenger.getChatMediator();
								ChatMessage message = mediator.handleEvent(parsedEventBody);
								System.out.println(message.getText());

								if (message != null)
									messenger.handleMessage(message);

							}
						}
					}).start();
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			return Response.status(200).build();
		}

		@POST
		@Path("/events/slack/interactive")
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(value = "Receive an Slack event by interactive elements")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "") })
		public Response slackActionInteractive(String body) {

			// Handle the received event
			new Thread(new Runnable() {
				@Override
				public void run() {

					String decoded = null;
					try {
						decoded = URLDecoder.decode(body);
					} catch (CharConversionException e) {
						e.printStackTrace();
					}
					
					decoded = decoded.substring("payload=".length());
					System.out.println("slack event: " + decoded);
					JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
					JSONObject parsedBody = null;

					try {
						parsedBody = (JSONObject) jsonParser.parse(decoded);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				
					String teamID = ((JSONObject) parsedBody.get("team")).getAsString("id");
					String appID = (String) parsedBody.get("api_app_id");
					String type = (String) parsedBody.get("type");
					String channel = ((JSONObject) parsedBody.get("channel")).getAsString("id");
					if(!type.contentEquals("block_actions"))
						return;

					// Identify bot
					Collection<VLE> vles = getConfig().getVLEs().values();
					Bot bot = null;
					for (VLE vle : vles) {
						bot = vle.getBotbySlackID(appID, teamID);
						System.out.println("appId:" + appID + " teamID:" + teamID);
					}

					if (bot == null)
						System.out.println("cannot relate event to a bot");
					else {
						System.out.println("slack event: bot identified: " + bot.getName());

						// Handle event
						JSONArray parsedActions = (JSONArray) parsedBody.get("actions");
						Messenger messenger = bot.getMessenger(appID);
						SlackChatMediator mediator = (SlackChatMediator) messenger.getChatMediator();
						ChatMessage message = mediator.handleAction(parsedActions, channel);

						if (message != null)
							messenger.handleMessage(message);
					}
				}
			}).start();

			return Response.status(200).build();
		}

		@POST
		@Path("/events/telegram/{token}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Receive an Telegram event")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "") })
		public Response telegramEvent(String body, @PathParam("token") String token) {

			new Thread(new Runnable() {
				@Override
				public void run() {

					// Identify bot
					Collection<VLE> vles = getConfig().getVLEs().values();
					Bot bot = null;

					for (VLE vle : vles) {
						Bot teleBot = vle.getBotbyTelegramToken(token);
						if (teleBot != null)
							bot = teleBot;
					}
					if (bot == null)
						System.out.println("cannot relate telegram event to a bot with token: " + token);
					System.out.println("telegram event: bot identified: " + bot.getName());

					// Handle event
					Messenger messenger = bot.getMessenger(ChatService.TELEGRAM);
					EventChatMediator mediator = (EventChatMediator) messenger.getChatMediator();
					JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
					JSONObject parsedBody;
					try {
						parsedBody = (JSONObject) jsonParser.parse(body);
						ChatMessage message = mediator.handleEvent(parsedBody);
						if (message != null)
							messenger.handleMessage(message);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}).start();

			return Response.status(200).build();
		}

		@DELETE
		@Path("/{botName}/{unit}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Bot deactivated") })
		@ApiOperation(value = "Deactivate bot for unit", notes = "Deactivates a bot for a unit.")
		public Response deactivateBot(@PathParam("botName") String bot, @PathParam("unit") String unit) {
			Collection<VLE> vles = getConfig().getVLEs().values();
			for (VLE vle : vles) {
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
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of content generators") })
		@ApiOperation(value = "Get content generators", notes = "Returns a list of content generators specified for that bot.")
		public Response getContentGenerators(@PathParam("botName") String service) {
			VLE vle = null;
			if (getConfig() != null)
				vle = getConfig().getServiceConfiguration(service);
			if (vle != null) {
				HashMap<String, ContentGenerator> cgl = new HashMap<String, ContentGenerator>();
				for (Bot b : vle.getBots().values())
					cgl.putAll(b.getGeneratorList());
				return Response.ok().entity(cgl).build();
			} else
				return Response.ok().entity(new HashMap<String, ContentGenerator>()).build();
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
			if (botFunction.getActionType().equals(ServiceType.SERVICE))
				functionPath = botFunction.getFunctionPath();
			JSONObject body = new JSONObject();
			HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();

			JSONObject triggerAttributes = (JSONObject) j.get("attributes");
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes())
				formAttributes(vle, sfa, bot, body, functionPath, attlist, triggerAttributes);
			performTrigger(vle, botFunction, botAgent, functionPath, "", body);
		}
	}

	// TODO: Use entity value, handle environment separator, handle other things
	// than static content
	public void performIntentTrigger(VLE vle, BotAgent botAgent, MessageInfo messageInfo)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException, AgentNotFoundException, AgentOperationFailedException {
		String botId = botAgent.getIdentifier();
		Bot bot = vle.getBots().get(botId);
		if (bot != null) {
			System.out.println("Bot " + botAgent.getLoginName() + " triggered:");
			ServiceFunction botFunction = bot.getBotServiceFunctions().get(messageInfo.getTriggeredFunctionId());
			String functionPath = "";
			System.out.println("bot function: " + botFunction.toString());
			if (botFunction.getActionType().equals(ServiceType.SERVICE))
				functionPath = botFunction.getFunctionPath();
			JSONObject body = new JSONObject();
			HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();
			JSONObject triggerAttributes = new JSONObject();
			System.out.println(botFunction.getAttributes());
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes())
				formAttributes(vle, sfa, bot, body, functionPath, attlist, triggerAttributes);
			// Patch attributes so that if a chat message is sent, it is sent
			// to the same channel the action was triggered from.
			// TODO: Handle multiple messengers
			// why the remove email?
			// body.remove("email");
			System.out.println(messageInfo.getMessage().getEmail());
			body.put("email", messageInfo.getMessage().getEmail());
			body.put("channel", messageInfo.getMessage().getChannel());
			body.put("intent", messageInfo.getIntent().getKeyword());
			for (Entity entity : messageInfo.getIntent().getEntities())
				body.put(entity.getEntityName(), entity.getValue());
			body.put("msg", messageInfo.getMessage().getText());
			body.put("contextOn", messageInfo.contextActive());
			performTrigger(vle, botFunction, botAgent, functionPath, "", body);
		}
	}

	public void checkTriggerBot(VLE vle, JSONObject body, BotAgent botAgent, String triggerUID,
			String triggerFunctionName) throws AgentNotFoundException, AgentOperationFailedException,
			ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException {
		String botId = botAgent.getIdentifier();

		Bot bot = vle.getBots().get(botId);
		if (bot != null && !(triggerUID.toLowerCase().equals(botAgent.getIdentifier().toLowerCase()))) {

			// get all triggers of the bot
			Set<Trigger> tlist = bot.getTriggerList();
			for (Trigger trigger : tlist) {
				TriggerFunction tf = trigger.getTriggerFunction();

				// in this function we only handle service functions
				if (tf instanceof ServiceFunction) {
					ServiceFunction sf = (ServiceFunction) tf;
					// check if the function name we got equals the service function name
					if (sf.getFunctionName().equals(triggerFunctionName)) {
						ServiceFunction triggeredFunction = trigger.getTriggeredFunction();

						String functionPath = "";
						// add path if the triggered function is a service function
						if (triggeredFunction.getActionType().equals(ServiceType.SERVICE))
							functionPath = triggeredFunction.getFunctionPath();
						JSONObject triggeredBody = new JSONObject();
						HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();
						for (ServiceFunction bsf : bot.getBotServiceFunctions().values())
							for (ServiceFunctionAttribute bsfa : bsf.getAttributes())
								attlist.put(bsfa.getId(), bsfa);

						JSONObject triggerAttributes = (JSONObject) body.get("attributes");
						for (ServiceFunctionAttribute triggeredFunctionAttribute : triggeredFunction.getAttributes())
							formAttributes(vle, triggeredFunctionAttribute, bot, triggeredBody, functionPath, attlist,
									triggerAttributes);

						System.out.println("Performing...");
						performTrigger(vle, triggeredFunction, botAgent, functionPath, triggerUID, triggeredBody);
					}
				}
			}

		} else {
			// TODO
		}
	}

	// Aaron : if name of body is empty add as part of an array of contents ?
	private void formAttributes(VLE vle, ServiceFunctionAttribute triggeredFunctionAttribute, Bot bot,
			JSONObject triggeredBody, String functionPath, HashMap<String, ServiceFunctionAttribute> attlist,
			JSONObject triggerAttributes) throws ServiceNotFoundException, ServiceNotAvailableException,
			InternalServiceException, ServiceMethodNotFoundException, ServiceInvocationFailedException,
			ServiceAccessDeniedException, ServiceNotAuthorizedException, ParseBotException {
		// Attributes of the triggered function
		if (triggeredFunctionAttribute.isSameAsTrigger())
			mapAttributes(triggeredBody, triggeredFunctionAttribute, functionPath, attlist, triggerAttributes);
		else if (triggeredFunctionAttribute.getName() == "body") {
			JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
			for (ServiceFunctionAttribute subsfa : triggeredFunctionAttribute.getChildAttributes())
				if (subsfa.isSameAsTrigger()) {
					ServiceFunctionAttribute mappedTo = subsfa.getMappedTo();
					if (triggerBody.get(mappedTo.getName()) != null)
						triggeredBody.put(subsfa.getName(), triggerBody.get(mappedTo.getName()));
					else
						triggeredBody.put(subsfa.getName(), triggerAttributes.get(mappedTo.getName()));
				} else {
					// Use AI to generate body
					ContentGenerator g = subsfa.getGenerator();
					if (g != null)
						mapWithContentGenerator(triggeredBody, g, subsfa.getName(), subsfa.getContentType(),
								functionPath, attlist, triggerAttributes, vle.getEnvironmentSeparator());
					else if (triggeredFunctionAttribute.getItb() != null)
						mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute, triggeredBody,
								attlist, triggerAttributes, functionPath);
					else if (subsfa.hasStaticContent())
						mapWithStaticContent(subsfa, triggeredBody);
					else {
						// TODO no match!
					}

				}
		} else {
			ContentGenerator g = triggeredFunctionAttribute.getGenerator();
			if (g != null)
				mapWithContentGenerator(triggeredBody, g, triggeredFunctionAttribute.getName(),
						triggeredFunctionAttribute.getContentType(), functionPath, attlist, triggerAttributes,
						vle.getEnvironmentSeparator());
			else {

				System.out.println(triggeredFunctionAttribute.getName());
				if (triggeredFunctionAttribute.getItb() != null)
					mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute, triggeredBody,
							attlist, triggerAttributes, functionPath);
				else if (triggeredFunctionAttribute.hasStaticContent())
					mapWithStaticContent(triggeredFunctionAttribute, triggeredBody);
				else
					// TODO
					System.out.println("Unknown mapping");
			}
		}
	}

	private void mapWithIfThen(IfThenBlock itb, ServiceFunctionAttribute triggeredFunctionAttribute,
			JSONObject triggeredBody, HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes,
			String functionPath) {
		IfThenBlock ifThenIterator = itb;
		while (ifThenIterator.getPrev() != null)
			ifThenIterator = ifThenIterator.getPrev();
		System.out.println(triggerAttributes.toJSONString());
		ServiceFunctionAttribute triggerAttribute = ifThenIterator.getSourceAttribute();
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		String source = "";
		if (triggerBody != null && triggerBody.containsKey(triggerAttribute.getName()))
			source = triggerBody.getAsString(triggerAttribute.getName());
		else if (triggerAttributes.containsKey(triggerAttribute.getName()))
			source = triggerAttributes.getAsString(triggerAttribute.getName());

		do {
			if (checkIfCondition(ifThenIterator, source))
				source = manipulateString(ifThenIterator, source);
			ifThenIterator = ifThenIterator.getNext();
		} while (ifThenIterator != null);
		triggeredBody.put(triggeredFunctionAttribute.getName(), source);
	}

	private void mapWithStaticContent(ServiceFunctionAttribute triggeredFunctionAttribute, JSONObject triggeredBody) {
		if (triggeredFunctionAttribute.getContent().length() > 0)
			if (triggeredBody.containsKey(triggeredFunctionAttribute.getName())) {
				JSONArray array = new JSONArray();
				array.add(triggeredBody.get(triggeredFunctionAttribute.getName()));
				array.add(triggeredFunctionAttribute.getContent());
				triggeredBody.put(triggeredFunctionAttribute.getName(), array);
			} else
				triggeredBody.put(triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContent());
		if (triggeredFunctionAttribute.getContentURL().length() > 0) {
			URL url;
			String body = "";
			try {
				url = new URL(triggeredFunctionAttribute.getContentURL());
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setDoOutput(true);
				con.setDoInput(true);

				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null)
					sb.append(line + "\n");
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
		if (triggerAttributes.containsKey(sourceAttributeName))
			inferInput = triggerAttributes.getAsString(sourceAttributeName);
		else if (triggerBody != null && triggerBody.containsKey(sourceAttributeName))
			inferInput = triggerBody.getAsString(sourceAttributeName);
		else {
			// TODO could not map attribtue
		}
		String unit = "";
		if (triggerAttributes.getAsString(envSep) != null)
			unit = triggerAttributes.getAsString(envSep);
		Serializable rmiResult = Context.get().invoke(g.getServiceName(), "inference", unit, inferInput);
		if (rmiResult instanceof String) {
			if (functionPath.contains("{" + sfaName + "}"))
				functionPath = functionPath.replace("{" + sfaName + "}", (String) rmiResult);
			else if (sfaType.equals("int"))
				b.put(sfaName, Integer.parseInt((String) rmiResult));
			else
				b.put(sfaName, rmiResult);
		} else
			throw new InternalServiceException(
					"Unexpected result (" + rmiResult.getClass().getCanonicalName() + ") of RMI call");
	}

	private void mapAttributes(JSONObject b, ServiceFunctionAttribute sfa, String functionPath,
			HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes) {
		// get id of the trigger function
		ServiceFunctionAttribute mappedTo = sfa.getMappedTo();
		// attributes of the function that triggered the bot
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		System.out.println("Aray now");
		if (triggerAttributes.containsKey(mappedTo.getName())) {
			String replaceWith = triggerAttributes.getAsString(mappedTo.getName());
			if (functionPath.contains("{" + sfa.getName() + "}"))
				functionPath = functionPath.replace("{" + sfa.getName() + "}", replaceWith);
			else
				b.put(sfa.getName(), replaceWith);
		} else if (triggerBody != null && triggerBody.containsKey(mappedTo.getName())) {
			String replaceWith = triggerBody.getAsString(mappedTo.getName());
			if (functionPath.contains("{" + sfa.getName() + "}"))
				functionPath = functionPath.replace("{" + sfa.getName() + "}", replaceWith);
			else
				b.put(sfa.getName(), replaceWith);
		} else {
			// TODO Error could not map attributes
		}
	}

	private void performTrigger(VLE vle, ServiceFunction sf, BotAgent botAgent, String functionPath, String triggerUID,
			JSONObject triggeredBody) throws AgentNotFoundException, AgentOperationFailedException {
		if (sf.getActionType().equals(ServiceType.SERVICE)) {
			System.out.println(sf.getFunctionName());
			// This part is "hardcoded" and will need improvements, but currently makes
			// using the assessment function work
			MiniClient client = new MiniClient();
			client.setConnectorEndpoint(vle.getAddress());
			// client.setLogin("alice", "pwalice");
			System.out.println(botAgent.getLoginName() + "    pass " + botPass);
			// client.setLogin(botAgent.getLoginName(), botPass);
			triggeredBody.put("botName", botAgent.getIdentifier());
			System.out.println("botagent is " + botAgent.getIdentifier());
			HashMap<String, String> headers = new HashMap<String, String>();
			System.out.println(sf.getServiceName() + functionPath + " ; " + triggeredBody.toJSONString() + " "
					+ sf.getConsumes() + " " + sf.getProduces() + " My string iss:" + triggeredBody.toJSONString());
			System.out.println(sf.toString());
			ClientResponse r = client.sendRequest(sf.getHttpMethod().toUpperCase(), sf.getServiceName() + functionPath,
					triggeredBody.toJSONString(), sf.getConsumes(), sf.getProduces(), headers);

			System.out.println("Connect Success");
			System.out.println(r.getResponse());

			if (Boolean.parseBoolean(triggeredBody.getAsString("contextOn"))) {
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				try {
					Bot bot = vle.getBots().get(botAgent.getIdentifier());
					String messengerID = sf.getMessengerName();
					JSONObject response = (JSONObject) parser.parse(r.getResponse());
					System.out.println(response);
					triggeredBody.put("text", response.getAsString("text"));
					ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
					triggerChat(chat, triggeredBody);
					if (response.get("closeContext") == null || Boolean.valueOf(response.getAsString("closeContext"))) {
						System.out.println("Closed Context");
						bot.getMessenger(messengerID).setContextToBasic(triggeredBody.getAsString("channel"));
					}
				} catch (ParseException e) {
					e.printStackTrace();

				}
			}

		} else if (sf.getActionType().equals(ActionType.SENDMESSAGE)) {
			if (triggeredBody.get("channel") == null && triggeredBody.get("email") == null) {
				// TODO Anonymous agent error
				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(vle.getAddress());
				HashMap<String, String> headers = new HashMap<String, String>();
				ClientResponse result = client.sendRequest("GET", "SBFManager/email/" + triggerUID, "",
						MediaType.TEXT_HTML, MediaType.TEXT_HTML, headers);
				String mail = result.getResponse().trim();
				triggeredBody.put("email", mail);
			}
			Bot bot = vle.getBots().get(botAgent.getIdentifier());
			String messengerID = sf.getMessengerName();
			if (messengerID == null || bot.getMessenger(messengerID) == null) {
				System.out.println("Bot Action is missing Messenger");
				return;
			}

			ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
			triggerChat(chat, triggeredBody);
		}
	}

	public void triggerChat(ChatMediator chat, JSONObject body) {
		String text = body.getAsString("text");
		String channel = null;

		if (body.containsKey("channel"))
			channel = body.getAsString("channel");
		else if (body.containsKey("email")) {
			String email = body.getAsString("email");
			channel = ((RocketChatMediator) chat).getChannelByEmail(email);
		}
		System.out.println(channel);
		chat.sendMessageToChannel(channel, text);
	}

	@Api(value = "Model Resource")
	@SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.0.13", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "", url = "")))
	@Path("/models")
	public static class BotModelResource {
		SocialBotManagerService service = (SocialBotManagerService) Context.get().getService();

		/**
		 * Put Model function.
		 *
		 * @param name name of the model
		 * @param body content of the model
		 * @return Returns an HTTP response with plain text string content derived from
		 *         the path input param.
		 */
		@POST
		@Path("/{name}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Model stored") })
		@ApiOperation(value = "Save BotModel", notes = "Stores the BotModel in the shared storage.")
		public Response putModel(@PathParam("name") String name, BotModel body) {
			// fetch or create envelope by file identifier
			Envelope fileEnv = null;
			JSONObject models = new JSONObject();
			try {
				try {
					// load existing models
					fileEnv = Context.get().requestEnvelope(ENVELOPE_MODEL);
					Serializable s = fileEnv.getContent();
					if (s instanceof JSONObject)
						models = (JSONObject) s;
					else
						// return wrong content exception error
						System.out.println("Wrong content");
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
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of BotModels") })
		@ApiOperation(value = "Retrieve BotModels", notes = "Get all stored BotModels.")
		public Response getModels() {
			// fetch or create envelope by file identifier
			Envelope fileEnv = null;
			JSONObject models = new JSONObject();
			try {
				try {
					fileEnv = Context.get().requestEnvelope(ENVELOPE_MODEL);
					Serializable s = fileEnv.getContent();
					if (s instanceof JSONObject)
						models = (JSONObject) s;
					else
						System.out.println("Wrong content");
				} catch (EnvelopeNotFoundException e) {
					// logger.info("File (" + ENVELOPE_MODEL + ") not found. Creating new one. " +
					// e.toString());
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
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return BotModel") })
		@ApiOperation(value = "Get BotModel by name", notes = "Returns the BotModel for the given name.")
		public Response getModelByName(@PathParam("name") String name) {
			// fetch or create envelope by file identifier
			Envelope fileEnv = null;
			JSONObject models = new JSONObject();
			try {
				try {
					fileEnv = Context.get().requestEnvelope(ENVELOPE_MODEL);
					Serializable s = fileEnv.getContent();
					if (s instanceof JSONObject)
						models = (JSONObject) s;
					else
						System.out.println("Wrong content");
				} catch (EnvelopeNotFoundException e) {
					// logger.info("File (" + ENVELOPE_MODEL + ") not found. Creating new one. " +
					// e.toString());
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

	/**
	 * Incoming MobSOS events
	 * 
	 * @param messages
	 * @return
	 */
	public boolean getMessages(ArrayList<BotMessage> messages) {
		System.out.println("Bot: Got " + messages.size() + " bot messages!");
		for (BotMessage m : messages) {

			EventMessage event = new EventMessage(m.getRemarks());
			System.out.println("message: " + event.toString());
			if (!(event.invariant())) {
				System.out.println("received invalid event message");
				return false;
			}

			if (event.getEventId() != null) {
				TriggerHandler handler = new TriggerHandler();
				handler.trigger(event, getConfig().getActiveBots());
			} else {
				BotResource br = new BotResource();
				br.trigger(m.getRemarks(), "");
			}
		}
		return true;
	}

	private boolean checkIfCondition(IfThenBlock itb, String text) {
		String conditionType = itb.getConditionType();
		if (conditionType.equals("Contains"))
			return text.contains(itb.getConditionValueA());
		else if (conditionType.equals("Equals"))
			return text.equals(itb.getConditionValueA());
		else if (conditionType.equals("True"))
			return true;
		else if (conditionType.equals("Less Than"))
			return text.length() < Integer.parseInt(itb.getConditionValueA());
		else if (conditionType.equals("Greater Than"))
			return text.length() > Integer.parseInt(itb.getConditionValueA());
		// TODO implement more
		return false;
	}

	private String manipulateString(IfThenBlock itb, String text) {
		String manipulationType = itb.getStatementType();
		if (manipulationType.equals("Return"))
			text = itb.getStatementValueA();
		else if (manipulationType.equals("Replace"))
			text = text.replace(itb.getStatementValueA(), itb.getStatementValueB());
		else if (manipulationType.equals("Append"))
			text = text + itb.getStatementValueA();
		else if (manipulationType.equals("Prepend"))
			text = itb.getStatementValueA() + text;
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
		@Override
		public void run() {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
			Gson gson = new Gson();
			for (VLE vle : getConfig().getVLEs().values()) {

				for (Bot bot : vle.getBots().values()) {
					ArrayList<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
					bot.handleMessages(messageInfos);

					// TODO: Handle multiple environments (maybe?)
					MiniClient client = new MiniClient();
					client.setConnectorEndpoint(vle.getAddress());

					HashMap<String, String> headers = new HashMap<String, String>();
					for (MessageInfo m : messageInfos)
						try {
							ClientResponse result = client.sendRequest("POST",
									"SBFManager/bots/" + m.getBotName() + "/trigger/intent", gson.toJson(m),
									MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
							System.out.println(result.getResponse());
						} catch (Exception e) {
							e.printStackTrace();
						}
				}

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
					if (r.getInterval().equals("Minute"))
						if (min >= Integer.parseInt(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
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
					} else if (r.getInterval().equals("Every day"))
						if (min >= 1 && df2.format(d1).equals(r.getTime())) {
							trigger = true;
							r.setLastUpdate(d1);
						}

					if (trigger)
						for (Bot b : vle.getBots().values()) {
							Map<String, Boolean> activeBots = b.getActive();
							HashSet<Trigger> tList = r.getTrigger();
							for (Trigger t : tList)
								for (Entry<String, Boolean> entry : activeBots.entrySet())
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
										String path = "SBFManager/bots/" + b.getName() + "/trigger/routine";
										try {
											path = "SBFManager/bots/" + URLEncoder.encode(b.getName(), "UTF-8")
													+ "/trigger/routine";
										} catch (UnsupportedEncodingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										ClientResponse result = client.sendRequest("POST", path, body.toJSONString(),
												MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
										System.out.println(result.getResponse());
									}
						}
				}
			}
		}
	}

	@Api(value = "Training Resource")
	@SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.0.13", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "", url = "")))
	@Path("/training")
	public static class TrainingResource {
		SocialBotManagerService service = (SocialBotManagerService) Context.get().getService();

		/**
		 * Store training data in the database.
		 *
		 * @param body training data body
		 *
		 * @param name training data name
		 *
		 * @return Returns an HTTP response with plain text string content.
		 */
		@POST
		@Path("/{dataName}")
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Data stored.") })
		@ApiOperation(value = "Store Training Data", notes = "Stores the current training data.")
		public Response storeData(String body, @PathParam("dataName") String name) {
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;

			try {
				// Open database connection
				con = service.database.getDataSource().getConnection();

				// Check if data with given name already exists in database. If yes, update it.
				// Else, insert it
				ps = con.prepareStatement("SELECT * FROM training WHERE name = ?");
				ps.setString(1, name);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					ps.close();
					ps = con.prepareStatement("UPDATE training SET data = ? WHERE name = ?");
					ps.setString(1, body);
					ps.setString(2, name);
					ps.executeUpdate();
				} else {
					ps.close();
					ps = con.prepareStatement("INSERT INTO training(name, data) VALUES (?, ?)");
					ps.setString(1, name);
					ps.setString(2, body);
					ps.executeUpdate();
				}

				resp = Response.ok().entity("Training data stored.").build();
			} catch (SQLException e) {
				e.printStackTrace();
				resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
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

			return resp;
		}

		/**
		 * Retrieve training data from database.
		 *
		 * @param name training data name
		 *
		 * @return Returns an HTTP response with plain text string content.
		 */
		@GET
		@Path("/{dataName}")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Data stored.") })
		@ApiOperation(value = "Fetch Training Data", notes = "Fetches the current training data.")
		public Response getData(@PathParam("dataName") String name) {
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;

			try {
				// Open database connection
				con = service.database.getDataSource().getConnection();

				// Fetch data with given name
				ps = con.prepareStatement("SELECT * FROM training WHERE name = ?");
				ps.setString(1, name);
				ResultSet rs = ps.executeQuery();
				rs.next();

				// Write serialised model in Blob
				String s = rs.getString("data");

				resp = Response.ok().entity(s).build();
			} catch (SQLException e) {
				e.printStackTrace();
				resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
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
			return resp;
		}

		/**
		 * @param bot The bot model
		 * @return ok
		 */
		@POST
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Data stored.") })
		@ApiOperation(value = "Create bot", notes = "creates the bot.")
		public Response createBot(i5.las2peer.services.socialBotManagerService.parser.creation.Bot bot) {

			BotModel botModel = null;
			try {

				System.out.println("Parse bot into BotModel");
				System.out.println(bot);

				BotModelParser botModelParser = new BotModelParser();
				botModel = botModelParser.parse(bot, SocialBotManagerService.getConfig());
				System.out.println("botModel");
				System.out.println(botModel.toString());
				botModel = botModelParser.order(botModel);
				System.out.println("botModel");
				System.out.println(botModel.toString());

			} catch (Exception e) {
				e.printStackTrace();
				return Response.serverError().entity("Bot creation failed. Can't parse into bot model.").build();
			}

			try {

				BotModelResource bmr = new BotModelResource();
				bmr.putModel("botName", botModel);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {

				BotResource br = new BotResource();
				br.init(botModel);

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String res = gson.toJson(botModel);
				return Response.ok().entity(res).build();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return Response.serverError().entity("bot creation failed").build();

		}

		/**
		 * Retrieve the names of all datasets in the database.
		 * 
		 * 
		 * @return Returns an HTTP response with plain text string content.
		 */
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of datasets") })
		@ApiOperation(value = "Retrieve datasets", notes = "Get all stored datasets.")
		public Response getDatasets() {
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;

			try {
				// Open database connection
				con = service.database.getDataSource().getConnection();

				ps = con.prepareStatement("SELECT name FROM training");
				ResultSet rs = ps.executeQuery();

				// Fetch all model names in the database
				JSONArray models = new JSONArray();
				while (rs.next()) {
					models.add(rs.getString("name"));
				}

				resp = Response.ok().entity(models.toJSONString()).build();
			} catch (SQLException e) {
				e.printStackTrace();
				resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
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

			return resp;
		}

		/**
		 * @return ok
		 */
		@GET
		@Path("/nlu")
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Data stored.") })
		@ApiOperation(value = "getNLUModels", notes = "get NLU models")
		public Response getNLUModels() {

			System.out.println("getNLUModels()");
			try {
				return Response.ok().entity(getConfig().getNlus().values()).build();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return Response.serverError().entity("cant retrieve knowledge models").build();

		}

		/**
		 * @return ok
		 */
		@POST
		@Path("/nlu")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Data stored.") })
		@ApiOperation(value = "Creates NLU model", notes = "creates the nlu model.")
		public Response createNLU(NLUKnowledge nlu) {

			try {

				if (getConfig().getNlus().containsKey(nlu.getUrl().toString()))
					return Response.ok().entity("I did not create a new NLU module, because a module with the URL "
							+ nlu.getUrl() + " already exists 😉").build();

				RasaNlu rasa = NLUGenerator.createRasaNLU(nlu);
				getConfig().addNLU(rasa);

				if (getConfig().getNLU(nlu.getName()) == null)
					return Response.serverError().entity("nlu creation failed").build();

				String message = "The NLU Knowledge was created: " + rasa.toString();
				return Response.ok().entity(message).build();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return Response.serverError().entity("nlu creation failed").build();

		}

		/**
		 * @return ok
		 */
		@POST
		@Path("/nlu/train")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Data stored.") })
		@ApiOperation(value = "Create Nlu Model", notes = "creates the nlu model.")
		public Response trainNLU(TrainingData training,
				@ApiParam(hidden = true) @QueryParam("botEventId") String botEventId) {

			try {
				System.out.println("received event id: " + botEventId);
				Collection<String> intents = training.intents();
				LanguageUnderstander lu = getConfig().getNLU(training.getNluName());

				if (lu == null)
					return Response.serverError().entity("nlu module not found").build();

				lu.addIntents(intents);
				System.out.println(training.toMarkdown());
				TrainingHelper nluTrain = new TrainingHelper(Context.get(), botEventId, lu.getUrl(), null,
						training.toMarkdown());
				nluTrain.setDefaultConfig();
				Thread nluThread = new Thread(nluTrain);
				nluThread.start();

				return Response.ok().entity(
						"I started the NLU training for you 😄 \n I will notify you as soon as the training is finished")
						.build();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return Response.serverError().entity("nlu creation failed").build();

		}
	}
}
