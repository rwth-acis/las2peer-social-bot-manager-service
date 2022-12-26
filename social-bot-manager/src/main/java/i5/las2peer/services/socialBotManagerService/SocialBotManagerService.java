package i5.las2peer.services.socialBotManagerService;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.Collections;

import javax.imageio.ImageIO;
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

import org.apache.commons.io.FileUtils;

import com.slack.api.Slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.ServiceException;
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
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentAlreadyExistsException;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.bot.BotMessage;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.BotAgent;
import i5.las2peer.services.socialBotManagerService.chat.*;
import i5.las2peer.services.socialBotManagerService.chat.xAPI.ChatStatement;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabaseType;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModel;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.BotModelNodeAttribute;
import i5.las2peer.services.socialBotManagerService.model.BotModelValue;
import i5.las2peer.services.socialBotManagerService.model.ContentGenerator;
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Trigger;
import i5.las2peer.services.socialBotManagerService.model.TriggerFunction;
import i5.las2peer.services.socialBotManagerService.model.VLE;
import i5.las2peer.services.socialBotManagerService.model.VLERoutine;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.TrainingHelper;
import i5.las2peer.services.socialBotManagerService.parser.BotParser;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import i5.las2peer.tools.CryptoException;
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
	private String address; // address of running webconnector
	private String restarterBotName; // name of restarterBot
	private static String restarterBotNameStatic;
	private String restarterBotPW; // PW of restarterBot
	private static String restarterBotPWStatic; // PW of restarterBot

	private static final String ENVELOPE_MODEL = "SBF_MODELLIST";

	private static HashMap<String, Boolean> botIsActive = new HashMap<String, Boolean>();
	private static HashMap<String, String> rasaIntents = new HashMap<String, String>();
	private static HashMap<String, String> courseMap = null;

	private static BotConfiguration config;

	private static HashMap<String, BotAgent> botAgents;
	private static final String botPass = "actingAgent";

	private static ScheduledExecutorService rt = null;

	private int BOT_ROUTINE_PERIOD = 5; // 1 second

	private TrainingHelper nluTrain = null;
	private Thread nluTrainThread = null;
	private static final L2pLogger logger = L2pLogger.getInstance(SocialBotManagerService.class.getName());
	private Context l2pcontext = null;
	private static BotAgent restarterBot = null;

	private static String lrsAuthTokenStatic;
	private static String lrsURLStatic;

	private String lrsAuthToken;
	private String lrsURL;

	public Context getL2pcontext() {
		return l2pcontext;
	}

	public void setL2pcontext(Context l2pcontext) {
		this.l2pcontext = l2pcontext;
	}

	public SocialBotManagerService() {
		super();
		setFieldValues(); // This sets the values of the configuration file
		restarterBotNameStatic = restarterBotName;
		restarterBotPWStatic = restarterBotPW;
		lrsAuthTokenStatic = lrsAuthToken;
		lrsURLStatic = lrsURL;
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
		if (getBotAgents() == null) {
			setBotAgents(new HashMap<String, BotAgent>());
		}

		this.databaseType = SQLDatabaseType.getSQLDatabaseType(databaseTypeInt);
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
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		if (this.nluTrainThread != null && this.nluTrainThread.isAlive()) {
			return Response.status(Status.SERVICE_UNAVAILABLE).entity("Training still in progress.").build();
		}
		try {
			JSONObject bodyJson = (JSONObject) p.parse(body);
			String url = bodyJson.getAsString("url");
			String config = bodyJson.getAsString("config");
			String markdownTrainingData = bodyJson.getAsString("markdownTrainingData");
			String intents = bodyJson.getAsString("intents");
			// added to have a way to access the intents of the rasa server
			this.rasaIntents.put(url.split("://")[1], intents);
			this.nluTrain = new TrainingHelper(url, config, markdownTrainingData);
			this.nluTrainThread = new Thread(this.nluTrain);
			this.nluTrainThread.start();
			// TODO: Create a member for this thread, make another REST method to check
			// whether
			// training was successful.
		} catch (ParseException e) {
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
		if (this.nluTrainThread == null) {
			return Response.ok("No training process was started yet.").build();
		} else if (this.nluTrainThread.isAlive()) {
			return Response.ok("Training still in progress.").build();
		} else if (this.nluTrain.getSuccess()) {
			return Response.ok("Training was successful.").build();
		} else {
			return Response.ok("Training failed.").build();
		}
	}

	@GET
	@Path("/{rasaUrl}/intents")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Returns the intents of a current Rasa Model.", notes = "")
	public Response getIntents(@PathParam("rasaUrl") String url) {
		if (this.rasaIntents.get(url) == null) {
			return Response.ok("failed.").build();
		} else {
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
		@Path("/restart")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of bots") })
		@ApiOperation(value = "Restart all bots automatically", notes = "Returns a list of all registered bots.")
		public Response restartBots() {
			// works only after service start
			if (restarterBot == null) {
				try {
					try {
						System.out.println(
								"Fetching restarter bot");
						restarterBot = (BotAgent) Context.getCurrent().fetchAgent(
								Context.getCurrent().getUserAgentIdentifierByLoginName(restarterBotNameStatic));
						// if bot didn't exist before, no need to try to restart the previous bots, as
						// the bot will have no way of accessing the envelope
						restarterBot.unlock(restarterBotPWStatic);
						Envelope env = null;
						HashMap<String, BotModel> models = null;
						try {
							// try to add project to project list (with service group agent)
							env = Context.get().requestEnvelope(restarterBotNameStatic, restarterBot);

							models = (HashMap<String, BotModel>) env.getContent();
							for (Entry<String, BotModel> entry : models.entrySet()) {
								init(entry.getValue());
							}

							System.out.println("Restarting bots completed");
						} catch (EnvelopeNotFoundException | EnvelopeAccessDeniedException
								| EnvelopeOperationFailedException e) {
							System.out.println("no bot models found in storage");
						}

					} catch (Exception e) {
						System.out.println("error: " + e.toString());
						// here, we assume that this is the first time the service is started
						restarterBot = BotAgent.createBotAgent(restarterBotPWStatic);
						restarterBot.unlock(restarterBotPWStatic);
						restarterBot.setLoginName(restarterBotNameStatic);
						Context.getCurrent().storeAgent(restarterBot);
						System.out.println("Restarter bot stored");
					}
					// restarterBot.unlock("123");
					// Context.getCurrent().registerReceiver(restarterBot);
				} catch (AgentException | CryptoException e2) {
					// TODO Errorhandling
					e2.printStackTrace();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
			return Response.ok().entity("vleList").build();
		}

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
			sbfservice.setL2pcontext(Context.getCurrent());
			BotParser bp = BotParser.getInstance();

			String returnString = "";
			LinkedHashMap<String, BotModelNode> nodes = botModel.getNodes();
			LinkedHashMap<String, BotModelEdge> edges = botModel.getEdges();
			// System.out.println(SocialBotManagerService.getBotAgents().keySet());
			Set<String> list = SocialBotManagerService.getBotAgents().keySet();
			ArrayList<String> oldArray = new ArrayList<String>();
			// do agentid here maybe instead of loginname, as some people use the same login
			// name
			for (String entry : list) {
				oldArray.add(entry);
			}
			String botToken = "";
			for (Entry<String, BotModelNode> entry : nodes.entrySet()) {
				if (entry.getValue().getType().equals("Messenger")) {
					for (Entry<String, BotModelNodeAttribute> subEntry : entry.getValue().getAttributes().entrySet()) {
						BotModelNodeAttribute subElem = subEntry.getValue();
						BotModelValue subVal = subElem.getValue();
						if (subVal.getName().equals("Authentication Token")) {
							botToken = subVal.getValue();
						}
					}
				}
			}
			if (restarterBotNameStatic != null && restarterBotPWStatic != null && !restarterBotNameStatic.equals("")
					&& !restarterBotPWStatic.equals("")) {
				try {
					restarterBot = (BotAgent) Context.getCurrent()
							.fetchAgent(Context.getCurrent().getUserAgentIdentifierByLoginName(restarterBotNameStatic));
					restarterBot.unlock(restarterBotPWStatic);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Envelope env = null;
			HashMap<String, BotModel> old = null;
			try {
				bp.parseNodesAndEdges(SocialBotManagerService.getConfig(), SocialBotManagerService.getBotAgents(),
						nodes, edges, sbfservice.database);
			} catch (ParseBotException | IllegalArgumentException | IOException | DeploymentException
					| AuthTokenException e) {
				e.printStackTrace();
				if (e.toString().toLowerCase().contains("login name longer")) {
					return Response.status(Status.BAD_REQUEST).entity("Bot Name needs to have at least 4 characters!")
							.build();
				}
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			}
			// initialized = true;
			JSONObject logData = new JSONObject();
			logData.put("status", "initialized");
			env = null;
			old = null;
			if (restarterBotNameStatic != null && restarterBotPWStatic != null && !restarterBotNameStatic.equals("")
					&& !restarterBotPWStatic.equals("")) {
				try {
					// try to add project to project list (with service group agent)
					env = Context.get().requestEnvelope(restarterBotNameStatic, restarterBot);
					old = (HashMap<String, BotModel>) env.getContent();
					old.put(botToken, botModel);
					env.setContent(old);
					Context.get().storeEnvelope(env, restarterBot);
				} catch (EnvelopeNotFoundException | EnvelopeAccessDeniedException
						| EnvelopeOperationFailedException e) {
					System.out.println(e);
					try {
						env = Context.get().createEnvelope(restarterBotNameStatic, restarterBot);
						env.setPublic();
						old = new HashMap<String, BotModel>();
						old.put(botToken, botModel);
						// System.out.println(botToken);
						env.setContent(old);
						Context.get().storeEnvelope(env, restarterBot);
					} catch (EnvelopeOperationFailedException | EnvelopeAccessDeniedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (Exception e2) {
						e2.printStackTrace();
					}

				}
			}
			Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, logData.toString());

			return Response.ok().entity(returnString).build();
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
				if (botAgent == null) {
					return Response.status(Status.NOT_FOUND).entity("Bot " + botName + " not found1").build();
				}
				body = body.replace("$botId", botAgent.getIdentifier());
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject j = (JSONObject) p.parse(body);
				String basePath = (String) j.get("basePath");
				String[] s = basePath.split("/");
				String service = s[s.length - 1];
				VLE vle = getConfig().getServiceConfiguration(service);

				Bot bot = null;
				for (Bot b : vle.getBots().values()) {
					if (b.getName().equals(botName)) {
						bot = b;
						break;
					}
				}
				if (bot == null) {
					return Response.status(Status.NOT_FOUND).entity("Bot " + botName + " not found2").build();
				}
				String sepName = getConfig().getServiceConfiguration(service).getEnvironmentSeparator();

				String sepValue;
				if (sepName.equals("singleEnvironment")) {
					sepValue = sepName;
				} else {
					sepValue = j.getAsString(sepName);
				}

				botIsActive.put(sepValue, true);
				bot.setIdActive(sepValue, true);

				if (j.get("directJoin") == null) {
					String joinPath = (String) j.get("joinPath");

					joinPath.replace("$botId", botAgent.getIdentifier());

					MiniClient client = new MiniClient();
					client.setConnectorEndpoint(basePath);
					client.setLogin(botAgent.getLoginName(), botPass);
					// client.setLogin("alice", "pwalice");

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

		/**
		 * Endpoint that handles incoming webhook calls.
		 * 
		 * @param body    JSONObject
		 * @param botName Name of the bot.
		 * @return HTTP response
		 */
		@POST
		@Path("/{botName}/webhook")
		@Consumes(MediaType.TEXT_PLAIN)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Successfully handled webhook call."),
				@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bot not found."),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Parse exception, field event is missing or event is unsupported.")
		})
		@ApiOperation(value = "Handle webhook calls", notes = "Handles incoming webhook calls.")
		public Response webhook(String body, @PathParam("botName") String botName) {
			// check if bot exists
			Bot bot = null;
			for (VLE vle : getConfig().getVLEs().values()) {
				bot = vle.getBots().values().stream().filter(b -> b.getName().equals(botName)).findFirst().get();
				if (bot != null)
					break;
			}
			if (bot == null)
				return Response.status(HttpURLConnection.HTTP_NOT_FOUND).entity("Bot " + botName + " not found.")
						.build();

			try {
				// parse body
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject parsedBody = (JSONObject) p.parse(body);

				// all webhook calls need to include the "event" property
				if (!parsedBody.containsKey("event"))
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("Field event is missing.")
							.build();

				String event = parsedBody.getAsString("event");
				// handle webhook depending on the event (currently only chat_message supported)
				if (event.equals("chat_message")) {
					String messenger = parsedBody.getAsString("messenger");
					if (!parsedBody.containsKey("messenger")) {
						for (String m : bot.getMessengers().keySet()) {
							messenger = m;
						}
					}
					ChatMediator chat = bot.getMessenger(messenger).getChatMediator();

					// send message
					JSONObject chatBody = new JSONObject();
					chatBody.put("channel", parsedBody.getAsString("channel"));
					chatBody.put("text", parsedBody.getAsString("message"));
					this.sbfservice.triggerChat(chat, chatBody);

					return Response.status(HttpURLConnection.HTTP_OK).build();
				}
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("Unsupported event.").build();
			} catch (ParseException e) {
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity("Body parse exception.").build();
			}
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

				if (!vle.getEnvironmentSeparator().equals("singleEnvironment")) {
					if (vle == null || vle.getEnvironmentSeparator() == null
							|| ((JSONObject) parsedBody.get("attributes")).get(vle.getEnvironmentSeparator()) == null
							|| botIsActive.get(((JSONObject) parsedBody.get("attributes"))
									.get(vle.getEnvironmentSeparator())) != true) {
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
						// System.out.println(vle);
						JSONObject context = new JSONObject();
						context.put("addr", vle.getAddress());
						if (!vle.getEnvironmentSeparator().equals("singleEnvironment")) {
							if (vle == null || vle.getEnvironmentSeparator() == null
									|| ((JSONObject) j.get("attributes")).get(vle.getEnvironmentSeparator()) == null
									|| botIsActive.get(((JSONObject) j.get("attributes"))
											.get(vle.getEnvironmentSeparator())) != true) {
								return;
							} else {
								JSONObject atts = (JSONObject) j.get("attributes");
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
		@Path("/{botName}/appRequestURL/{instanceAlias}/{intent}/{token}")
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Used as an slack app request url to send button clicks")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "") })
		public Response triggerButton(String body, @PathParam("botName") String name,
				@PathParam("instanceAlias") String instanceAlias, @PathParam("intent") String expectedIntent,
				@PathParam("token") String token) {
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			System.out.println("name " + name + " , instance alias " + instanceAlias);

			try {
				String result = java.net.URLDecoder.decode(body, StandardCharsets.UTF_8.name());

				// slack adds payload= in front of the result, so deleting that to parse it to
				// json
				result = result.substring(8);

				System.out.println("Handling message...");
				JSONObject bodyInput = (JSONObject) p.parse(result);
				System.out.println("Parsed json: " + bodyInput);

				String channel = "";
				String text = "";
				String user = "";
				String ts = "";
				JSONObject actionInfoJson = new JSONObject();

				if (bodyInput.getAsString("type").equals("view_submission")) {
					// handle modal submission
					// the channel should be added in "{"private_metadata":{"channel": "channel_id",
					// ...}}"
					// you can add other infos in private_metadata, it would be sent to the channel
					// and sent back after submission
					// info in private_metadata would not be shown in the channel
					JSONObject view = (JSONObject) p.parse(bodyInput.getAsString("view"));
					channel = ((JSONObject) p.parse(view.getAsString("private_metadata"))).getAsString("channel");
					user = ((JSONObject) p.parse(bodyInput.getAsString("user"))).getAsString("id");
					ts = "view_submission";
					text = "view_submission";
					// use callback_id to recognize which kind of view have been submitted
					actionInfoJson.put("actionId", view.getAsString("callback_id"));
					actionInfoJson.put("value", view.getAsString("state"));
				} else {
					String actionId = "";
					StringBuilder value = new StringBuilder();
					JSONObject containerJson = (JSONObject) p.parse(bodyInput.getAsString("container"));
					ts = containerJson.getAsString("message_ts");
					JSONObject channelJson = (JSONObject) p.parse(bodyInput.getAsString("channel"));
					channel = channelJson.getAsString("id");
					JSONObject userJson = (JSONObject) p.parse(bodyInput.getAsString("user"));
					user = userJson.getAsString("id");

					JSONArray actions = (JSONArray) p.parse(bodyInput.getAsString("actions"));
					// this for loop only executed once
					for (Object actionsObject : actions) {
						String selectedOptionsString = ((JSONObject) actionsObject).getAsString("selected_options");
						String selectedOptionString = ((JSONObject) actionsObject).getAsString("selected_option");
						actionId = ((JSONObject) actionsObject).getAsString("action_id");
						if (selectedOptionsString != null) {
							// multiple choice with one or more than one selected option
							// System.out.println("selected options string: " + selectedOptionsString);
							JSONArray selectedOptionsJson = (JSONArray) p.parse(selectedOptionsString);
							text = selectedOptionsJson.toString();
							value.append("[");
							for (Object singleOptionJson : selectedOptionsJson) {
								value.append(((JSONObject) singleOptionJson).getAsString("value")).append(',');
							}
							value.append("]");

						} else if (selectedOptionString != null) {
							// single choice with one selected option (possible)
							// System.out.println("selected option: " + selectedOptionString);
							JSONObject selectedOptionJson = (JSONObject) p.parse(selectedOptionString);

							String textString = selectedOptionJson.getAsString("text");
							JSONObject textJson = (JSONObject) p.parse(textString);
							text += textJson.getAsString("text");
							value.append(((JSONObject) actionsObject).getAsString("value"));

						} else {
							// System.out.println("No selectedOption and no selectedOptions.");
							// System.out.println("No selectedOption and no selectedOptions. Just a normal
							// button press.");

							String textString = ((JSONObject) actionsObject).getAsString("text");
							JSONObject textJson = (JSONObject) p.parse(textString);
							text += textJson.getAsString("text");
							value.append(((JSONObject) actionsObject).getAsString("value"));
						}
					}

					System.out.println("Text from triggerButton is: " + text);
					// remove the last ","
					if ((String.valueOf(text.charAt(text.length() - 1)).equals(","))) {
						text = text.substring(0, text.length() - 1);
					}

					actionInfoJson.put("actionId", actionId);
					actionInfoJson.put("value", value.toString());
				}
				actionInfoJson.put("triggerId", bodyInput.getAsString("trigger_id"));

				ChatMessage chatMessage = new ChatMessage(channel, user, text, ts, actionInfoJson.toString());
				JSONObject intentJO = new JSONObject();
				JSONObject innerIntent = new JSONObject();
				innerIntent.put("name", expectedIntent);
				innerIntent.put("confidence", 1.0);
				intentJO.put("intent", innerIntent);
				JSONArray ja = new JSONArray();
				intentJO.put("entities", ja);
				i5.las2peer.services.socialBotManagerService.nlu.Intent intent = new i5.las2peer.services.socialBotManagerService.nlu.Intent(
						intentJO);
				// set email, since it is not passed on in body
				chatMessage.setEmail(user);
				// adjust triggered function id
				MessageInfo messageInfo = new MessageInfo(chatMessage, intent, "", name, instanceAlias, true,
						new ArrayList<>());

				// this.triggeredFunction.get(message.getChannel());
				System.out.println(
						"Got info: " + messageInfo.getMessage().getText() + " " + messageInfo.getTriggeredFunctionId());
				Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, body);

				SocialBotManagerService sbf = this.sbfservice;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							BotAgent botAgent = getBotAgents().get(messageInfo.getBotName());
							String service = messageInfo.getServiceAlias();
							System.out.println("service name: " + service);
							VLE vle = getConfig().getServiceConfiguration(service);

							// get triggered function id, by getting bot, the messengers and then the intent
							// hash map
							HashMap<String, Bot> botsHM = vle.getBots();
							// System.out.println("botsHM: " + botsHM);
							String triggerdFunctionId = "";
							for (Bot bot : botsHM.values()) {
								// System.out.println(bot);
								HashMap<String, i5.las2peer.services.socialBotManagerService.model.Messenger> messengers = bot
										.getMessengers();
								for (Messenger m : messengers.values()) {
									// System.out.println("messenger: " + m);
									HashMap<String, i5.las2peer.services.socialBotManagerService.model.IncomingMessage> intentsHM = m
											.getKnownIntents();
									// System.out.println("intentsHM: " + intentsHM);
									for (String s : intentsHM.keySet()) {
										if (s.equals(expectedIntent)) {
											i5.las2peer.services.socialBotManagerService.model.IncomingMessage incomingMessage = intentsHM
													.get(s);
											i5.las2peer.services.socialBotManagerService.model.ChatResponse chatResponses = incomingMessage
													.getResponse(new Random());
											// System.out.println(chatResponses);
											// System.out.println(chatResponses.getTriggeredFunctionId());
											triggerdFunctionId = chatResponses.getTriggeredFunctionId();
										}
									}
								}
							}
							MessageInfo newMessageInfo = new MessageInfo(chatMessage, intent, triggerdFunctionId, name,
									instanceAlias, true, new ArrayList<>());
							System.out.println("Got 2nd info: " + newMessageInfo.getMessage().getText() + " "
									+ newMessageInfo.getTriggeredFunctionId());
							try {
								sbf.performIntentTrigger(vle, botAgent, newMessageInfo);
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

			} catch (Exception e) {
				e.printStackTrace();
			}

			return Response.ok().build();
		}

		@POST
		@Path("/{botName}/appRequestURL/{instanceAlias}/{token}")
		@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Used as an slack app request url to send button clicks")
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "") })
		public Response triggerButton(String body, @PathParam("botName") String name,
				@PathParam("instanceAlias") String instanceAlias,
				@PathParam("token") String token) {

			new Thread(new Runnable() {
				@Override
				public void run() {

					// Identify bot
					Collection<VLE> vles = getConfig().getVLEs().values();
					Bot bot = null;

					for (VLE vle : vles) {
						Bot slackBot = vle.getBotByServiceToken(token, ChatService.SLACK);
						if (slackBot != null) {
							bot = slackBot;
						}
					}
					if (bot == null)
						System.out.println("cannot relate slack action to a bot with token: " + token);
					System.out.println("slack action: bot identified: " + bot.getName());

					// Handle action
					Messenger messenger = bot.getMessenger(ChatService.SLACK);
					SlackChatMediator mediator = (SlackChatMediator) messenger.getChatMediator();
					JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
					JSONObject parsedBody;
					try {
						parsedBody = (JSONObject) jsonParser
								.parse(java.net.URLDecoder.decode(body, StandardCharsets.UTF_8.name()).substring(8));
						mediator.handleEvent(parsedBody);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			return Response.ok().build();
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
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			try {
				JSONObject message = (JSONObject) parser.parse(body);
				JSONObject cleanedJson = (JSONObject) message.get("message");
				cleanedJson.put("user", encryptThisString(cleanedJson.getAsString("user")));
				if (cleanedJson.containsKey("email")) {
					cleanedJson.put("email", encryptThisString(cleanedJson.getAsString("email")));
					JSONObject xAPI = createXAPIStatement(cleanedJson.getAsString("email"), name, m.getIntent().getKeyword(), m.getMessage().getText());
					sendXAPIStatement(xAPI, lrsAuthTokenStatic);
				}
				System.out.println("Got info: " + m.getMessage().getText() + " " + m.getTriggeredFunctionId());
				System.out.println(cleanedJson);
				// send xAPI statement?
				Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, cleanedJson.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println("Got info: " + m.getMessage().getText() + " " + m.getTriggeredFunctionId());
			Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, body);
			// If no action should be triggered, just return
			if (m.getTriggeredFunctionId() == null) {
				return Response.ok().build();
			}

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

		// currently added to make development easier, once done, will do everythung
		// with mobsos
		public JSONObject createXAPIStatement(String userMail, String botName,
				String intent, String text)
				throws ParseException {
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject actor = new JSONObject();
			actor.put("objectType", "Agent");
			JSONObject account = new JSONObject();

			account.put("name", userMail);
			account.put("homePage", "https://chat.tech4comp.dbis.rwth-aachen.de");
			actor.put("account", account);

			JSONObject verb = (JSONObject) p
					.parse(new String(
							"{'display':{'en-US':'sent_chat_message'},'id':'https://tech4comp.de/xapi/verb/sent_chat_message'}"));
			JSONObject object = (JSONObject) p
					.parse(new String("{'definition':{'interactionType':'other', 'name':{'en-US':'" + intent
							+ "'}, 'description':{'en-US':'" + intent
							+ "'}, 'type':'https://tech4comp.de/xapi/activitytype/bot'},'id':'https://tech4comp.de/bot/"
							+ botName+ "', 'objectType':'Activity'}"));
			JSONObject context = (JSONObject) p.parse(new String(
					"{'extensions':{'https://tech4comp.de/xapi/context/extensions/intent':{'botName':'"
							+ botName + "','text':'"
							+ text
							+ "'}}}"));
			JSONObject xAPI = new JSONObject();

			xAPI.put("authority", p.parse(
					new String(
							"{'objectType': 'Agent','name': 'New Client', 'mbox': 'mailto:hello@learninglocker.net'}")));
			xAPI.put("context", context); 
			// xAPI.put("timestamp", java.time.LocalDateTime.now());
			xAPI.put("actor", actor);
			xAPI.put("object", object);
			xAPI.put("verb", verb);
			System.out.println(xAPI);
			return xAPI;
		}

		public void sendXAPIStatement(JSONObject xAPI, String lrsAuthToken) {
			// Copy pasted from LL service
			// POST statements
			try {
				System.out.println(xAPI);
				URL url = new URL(lrsURLStatic + "/data/xAPI/statements");
				System.out.println(url + lrsAuthTokenStatic);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
				conn.setRequestProperty("Authorization", "Basic " + lrsAuthTokenStatic);
				conn.setRequestProperty("Cache-Control", "no-cache");
				conn.setUseCaches(false);

				OutputStream os = conn.getOutputStream();
				os.write(xAPI.toString().getBytes("utf-8"));
				os.flush();

				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				String line = "";
				StringBuilder response = new StringBuilder();

				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				logger.info(response.toString());

				conn.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

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

		// the body needs to contain the names of all the messenger elements which the
		// bot uses with "messengerNames" as the attribute name
		@DELETE
		@Path("/{botAgentId}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Bot deactivated"),
				@ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = "Messenger names do not all match!") })
		@ApiOperation(value = "Deactivate bot for unit", notes = "Deactivates a bot for a unit.")
		public Response deactivateBotAll(@PathParam("botAgentId") String bot, JSONObject body) {
			Collection<VLE> vles = getConfig().getVLEs().values();
			for (VLE vle : vles) {
				Bot b = vle.getBots().get(bot);
				if (b != null) {
					ArrayList messengers = (ArrayList) body.get("messengers");
					if (b.deactivateAllWithCheck(messengers)) {
						vle.getBots().remove(bot);
						if (restarterBot != null) {
							Envelope env = null;
							HashMap<String, BotModel> old = null;
							if (restarterBotNameStatic != null && restarterBotPWStatic != null
									&& !restarterBotNameStatic.equals("") && !restarterBotPWStatic.equals("")) {
								try {
									restarterBot = (BotAgent) Context.getCurrent().fetchAgent(Context.getCurrent()
											.getUserAgentIdentifierByLoginName(restarterBotNameStatic));
									restarterBot.unlock(restarterBotPWStatic);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							try {
								// try to add project to project list (with service group agent)
								env = Context.get().requestEnvelope(restarterBotNameStatic, restarterBot);
								old = (HashMap<String, BotModel>) env.getContent();
								for (Object object : messengers) {
									HashMap<String, String> jsonObject = (HashMap<String, String>) object;
									if (old.containsKey(jsonObject.get("authToken"))) {
										old.remove(jsonObject.get("authToken"));
									}
								}
								env.setContent(old);
								Context.get().storeEnvelope(env, restarterBot);
							} catch (EnvelopeNotFoundException | EnvelopeAccessDeniedException
									| EnvelopeOperationFailedException e) {
								e.printStackTrace();
							}
						}
						return Response.ok().entity(bot + " deactivated.").build();
					} else {
						return Response.status(HttpURLConnection.HTTP_NOT_ACCEPTABLE).entity(bot + " not deactivated.")
								.build();
					}
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
						Bot teleBot = vle.getBotByServiceToken(token, ChatService.TELEGRAM);
						if (teleBot != null) {
							bot = teleBot;
						}
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
						mediator.handleEvent(parsedBody);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}).start();

			return Response.status(200).build();
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
			HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();

			JSONObject triggerAttributes = (JSONObject) j.get("attributes");
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes()) {
				formAttributes(vle, sfa, bot, body, functionPath, attlist, triggerAttributes);
			}
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
			if (botFunction.getActionType().equals(ActionType.SERVICE)) {
				functionPath = botFunction.getFunctionPath();
			} else if (botFunction.getActionType().equals(ActionType.OPENAPI)) {
				functionPath = botFunction.getFunctionPath();
			}
			JSONObject body = new JSONObject();
			HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();
			JSONObject triggerAttributes = new JSONObject();
			System.out.println(botFunction.getAttributes());
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes()) {
				formAttributes(vle, sfa, bot, body, functionPath, attlist, triggerAttributes);
			}
			// Patch attributes so that if a chat message is sent, it is sent
			// to the same channel the action was triggered from.
			// TODO: Handle multiple messengers
			System.out.println(messageInfo.getMessage().getEmail());
			String mail = messageInfo.getMessage().getEmail();
			if (mail == null)
				mail = "";
			body.put("email", messageInfo.getMessage().getEmail());
			body.put("channel", messageInfo.getMessage().getChannel());
			body.put("user", messageInfo.getMessage().getUser());
			body.put("intent", messageInfo.getIntent().getKeyword());
			body.put("time", messageInfo.getMessage().getTime());
			if (messageInfo.getMessage().getMessageId() != null) {
				body.put("message_id", messageInfo.getMessage().getMessageId());
				// actionInfo needed for citbot...
				body.put("actionInfo", messageInfo.getMessage().getMessageId());
			}
			if (messageInfo.getMessage().getFileBody() != null) {
				body.put("fileBody", messageInfo.getMessage().getFileBody());
				body.put("fileName", messageInfo.getMessage().getFileName());
				body.put("fileType", messageInfo.getMessage().getFileType());
			}
			if (messageInfo.getMessage().getActionInfo() != null) {
				body.put("actionInfo", messageInfo.getMessage().getActionInfo());
			}

			// Insert entities detected from the message
			JSONObject entities = new JSONObject();
			for (Entity entityName : messageInfo.getIntent().getEntities()) {
				body.put(entityName.getEntityName(), entityName.getValue());// Kept for compatibility reasons
				JSONObject entity = new JSONObject();
				entity.put("value", entityName.getValue());
				entity.put("confidence", entityName.getConfidence());
				entities.put(entityName.getEntityName(), entity);
			}

			// Insert entities that was passed over from previous message
			if (messageInfo.getRecognizedEntities() != null) {
				for (Entity entityName : messageInfo.getRecognizedEntities()) {
					JSONObject entity = new JSONObject();
					entity.put("value", entityName.getValue());
					entity.put("confidence", entityName.getConfidence());
					entities.put(entityName.getEntityName(), entity);
				}
			}
			if ((messageInfo.getMessage().getPreviousMessage() != null)
					&& (messageInfo.getMessage().getCurrMessage() != null)) {
				// if a message has been edited
				body.put("previousMessage", messageInfo.getMessage().getPreviousMessage());
				body.put("currMessage", messageInfo.getMessage().getCurrMessage());
			}

			body.put("entities", entities);
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
						if (triggeredFunction.getActionType().equals(ActionType.SERVICE))
							functionPath = triggeredFunction.getFunctionPath();
						JSONObject triggeredBody = new JSONObject();
						HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();
						for (ServiceFunction bsf : bot.getBotServiceFunctions().values()) {
							for (ServiceFunctionAttribute bsfa : bsf.getAttributes()) {
								attlist.put(bsfa.getId(), bsfa);
							}
						}

						JSONObject triggerAttributes = (JSONObject) body.get("attributes");
						for (ServiceFunctionAttribute triggeredFunctionAttribute : triggeredFunction.getAttributes()) {
							formAttributes(vle, triggeredFunctionAttribute, bot, triggeredBody, functionPath, attlist,
									triggerAttributes);
						}

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
		if (triggeredFunctionAttribute.isSameAsTrigger()) {
			mapAttributes(triggeredBody, triggeredFunctionAttribute, functionPath, attlist, triggerAttributes);
		} else if (triggeredFunctionAttribute.getName() == "body") {
			JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
			for (ServiceFunctionAttribute subsfa : triggeredFunctionAttribute.getChildAttributes()) {
				if (subsfa.isSameAsTrigger()) {
					ServiceFunctionAttribute mappedTo = subsfa.getMappedTo();
					if (triggerBody.get(mappedTo.getName()) != null) {
						triggeredBody.put(subsfa.getName(), triggerBody.get(mappedTo.getName()));
					} else
						triggeredBody.put(subsfa.getName(), triggerAttributes.get(mappedTo.getName()));
				} else {
					// Use AI to generate body
					ContentGenerator g = subsfa.getGenerator();
					if (g != null) {
						mapWithContentGenerator(triggeredBody, g, subsfa.getName(), subsfa.getContentType(),
								functionPath, attlist, triggerAttributes, vle.getEnvironmentSeparator());
					} else {
						if (triggeredFunctionAttribute.getItb() != null) {
							mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute,
									triggeredBody, attlist, triggerAttributes, functionPath);
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
				mapWithContentGenerator(triggeredBody, g, triggeredFunctionAttribute.getName(),
						triggeredFunctionAttribute.getContentType(), functionPath, attlist, triggerAttributes,
						vle.getEnvironmentSeparator());
			} else {

				System.out.println(triggeredFunctionAttribute.getName());
				if (triggeredFunctionAttribute.getItb() != null) {
					mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute, triggeredBody,
							attlist, triggerAttributes, functionPath);
				} else {
					if (triggeredFunctionAttribute.hasStaticContent()) {
						mapWithStaticContent(triggeredFunctionAttribute, triggeredBody);
					} else {
						// TODO
						System.out.println("Unknown mapping");
					}
				}
			}
		}
	}

	private void mapWithIfThen(IfThenBlock itb, ServiceFunctionAttribute triggeredFunctionAttribute,
			JSONObject triggeredBody, HashMap<String, ServiceFunctionAttribute> attlist, JSONObject triggerAttributes,
			String functionPath) {
		IfThenBlock ifThenIterator = itb;
		while (ifThenIterator.getPrev() != null) {
			ifThenIterator = ifThenIterator.getPrev();
		}
		System.out.println(triggerAttributes.toJSONString());
		ServiceFunctionAttribute triggerAttribute = ifThenIterator.getSourceAttribute();
		JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
		String source = "";
		if (triggerBody != null && triggerBody.containsKey(triggerAttribute.getName())) {
			source = triggerBody.getAsString(triggerAttribute.getName());
		} else if (triggerAttributes.containsKey(triggerAttribute.getName())) {
			source = triggerAttributes.getAsString(triggerAttribute.getName());
		}

		do {
			if (checkIfCondition(ifThenIterator, source)) {
				source = manipulateString(ifThenIterator, source);
			}
			ifThenIterator = ifThenIterator.getNext();
		} while (ifThenIterator != null);
		triggeredBody.put(triggeredFunctionAttribute.getName(), source);
	}

	private void mapWithStaticContent(ServiceFunctionAttribute triggeredFunctionAttribute, JSONObject triggeredBody) {
		if (triggeredFunctionAttribute.getContent().length() > 0) {
			if (triggeredBody.containsKey(triggeredFunctionAttribute.getName())) {
				JSONArray array = new JSONArray();
				array.add(triggeredBody.get(triggeredFunctionAttribute.getName()));
				array.add(triggeredFunctionAttribute.getContent());
				triggeredBody.put(triggeredFunctionAttribute.getName(), array);
			} else
				triggeredBody.put(triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContent());

		}
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
					b.put(sfaName, rmiResult);
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
		System.out.println("Aray now");
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
		if (sf.getActionType().equals(ActionType.SERVICE) || sf.getActionType().equals(ActionType.OPENAPI)) {
			MiniClient client = new MiniClient();
			if (sf.getActionType().equals(ActionType.SERVICE)) {
				client.setConnectorEndpoint(vle.getAddress());
			} else if (sf.getActionType().equals(ActionType.OPENAPI)) {
				client.setConnectorEndpoint(sf.getServiceName() + functionPath);
			}
			// client.setLogin("alice", "pwalice");
		//	client.setLogin(botAgent.getLoginName(), botPass);

			Bot bot = vle.getBots().get(botAgent.getIdentifier());
			String messengerID = sf.getMessengerName();
			triggeredBody.put("messenger", bot.getMessenger(messengerID).getChatService().toString());
			triggeredBody.put("botId", bot.getId());
			triggeredBody.put("botName", bot.getName());
			HashMap<String, String> headers = new HashMap<String, String>();
			System.out.println(sf.getServiceName() + functionPath + " ; " + triggeredBody.toJSONString() + " "
					+ sf.getConsumes() + " " + sf.getProduces() + " My string is" + ":" + triggeredBody.toJSONString());
			ClientResponse r = null;
			if (sf.getActionType().equals(ActionType.SERVICE)) {
				r = client.sendRequest(sf.getHttpMethod().toUpperCase(), sf.getServiceName() + functionPath,
						triggeredBody.toJSONString(), sf.getConsumes(), sf.getProduces(), headers);
			} else if (sf.getActionType().equals(ActionType.OPENAPI)) {
				r = client.sendRequest(sf.getHttpMethod().toUpperCase(), "", triggeredBody.toJSONString(),
						sf.getConsumes(), sf.getProduces(), headers);
			}
			System.out.println("Connect Success");
			System.out.println(r.getResponse());
			if (Boolean.parseBoolean(triggeredBody.getAsString("contextOn"))) {
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				try {
					JSONObject response = (JSONObject) parser.parse(r.getResponse());
					System.out.println(response);
					triggeredBody.put("text", response.getAsString("text"));
					ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
					if (response.containsKey("fileBody")) {
						triggeredBody.put("fileBody", response.getAsString("fileBody"));
						triggeredBody.put("fileName", response.getAsString("fileName"));
						triggeredBody.put("fileType", response.getAsString("fileType"));
					} else
						triggeredBody.remove("fileBody");
					if (response.containsKey("contactList")) {
						triggeredBody.put("contactList", response.getAsString("contactList"));
					}
					if (response.containsKey("contactText")) {
						triggeredBody.put("contactText", response.getAsString("contactText"));
					}
					if (response.containsKey("blocks")) {
						triggeredBody.put("blocks", response.getAsString("blocks"));
						if (response.containsKey("updateBlock")) {
							triggeredBody.put("updateBlock", response.getAsString("updateBlock"));
							if (response.containsKey("ts")) {
								triggeredBody.put("ts", response.getAsString("ts"));
							}
						}
					}
					if(response.containsKey("multiFiles")){
						for(Object o : (JSONArray) response.get("multiFiles")){
							JSONObject jsonO = (JSONObject) o;
							System.out.println("handling multifiles");
							jsonO.put("channel", triggeredBody.getAsString("channel"));
							jsonO.put("email", triggeredBody.getAsString("email"));
							triggerChat(chat, jsonO);
						}
					} else {
						triggerChat(chat, triggeredBody);
					}
					
					if (response.get("closeContext") == null || Boolean.valueOf(response.getAsString("closeContext"))) {
						System.out.println("Closed Context");
						bot.getMessenger(messengerID).setContextToBasic(triggeredBody.getAsString("channel"),
								triggeredBody.getAsString("user"));
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
		String blocks = body.getAsString("blocks");
		String channel = null;
		String user = "";
		JSONObject monitorEvent42 = new JSONObject();
		final long start = System.currentTimeMillis();
		monitorEvent42.put("task", "Send message");

		System.out.println(body);
		if (body.containsKey("contactList")) {
			// Send normal message to users on contactlist
			String email = body.getAsString("contactList");
			monitorEvent42.put("email", email);
			System.out.println("Goes to pick channel(s) by provided email(s)");
			String[] emailArray = email.split(",");

			if (body.containsKey("contactText")) {
				// specific text at position 1 should be sent to person on contactlist at pos 1
				String ctext = body.getAsString("contactText");
				System.out.println("Goes to send text from contextText");
				String[] textArray = ctext.split(",");
				int i = 0;
				for (String s : emailArray) {
					channel = chat.getChannelByEmail(s);

					if (textArray[i] != null) {
						chat.sendMessageToChannel(channel, textArray[i]);
					}
					i++;
				}
			} else {
				// if no specific text, send the regular text
				for (String s : emailArray) {
					System.out.println(s);
					channel = chat.getChannelByEmail(s);

					if (text != null && channel != null) {
						chat.sendMessageToChannel(channel, text);
					}

				}
			}
			monitorEvent42.put("time", System.currentTimeMillis() - start);
			if (body.containsKey("channel")) {
				channel = body.getAsString("channel");
			} else if (body.containsKey("email")) {
				email = body.getAsString("email");
				channel = chat.getChannelByEmail(email);
			}
			chat.sendMessageToChannel(channel, "ContactList contacted.");

		} else {
			if (body.containsKey("channel")) {
				channel = body.getAsString("channel");
			} else if (body.containsKey("email")) {
				String email = body.getAsString("email");
				monitorEvent42.put("email", email);
				channel = chat.getChannelByEmail(email);
			}
			System.out.println(channel);
			if (text != null && !body.containsKey("fileBody")) {
				chat.sendMessageToChannel(channel, text);
			}
			if (body.containsKey("blocks")) {
				System.out.println("Body has blocks");
				if (body.containsKey("updateBlock") && Boolean.parseBoolean(body.getAsString("updateBlock"))) {
					if (body.containsKey("ts")) {
						System.out.println("A block would be updated");
						chat.updateBlocksMessageToChannel(channel, blocks, chat.getAuthToken(), body.getAsString("ts"));
					} else {
						System.out.println(
								"No ts information is available. No block would be updated, a new block will be sent instead.");
					}

				} else {
					chat.sendBlocksMessageToChannel(channel, blocks, chat.getAuthToken());
				}
			}
			if (body.containsKey("fileBody")) {
				if (text == null) {

					text = "";
				}
				System.out.println("text is sss" + text);
				chat.sendFileMessageToChannel(channel, body.getAsString("fileBody"), body.getAsString("fileName"),
						body.getAsString("fileType"), text);
			}
			monitorEvent42.put("time", System.currentTimeMillis() - start);
		}
		if (l2pcontext != null) {
			l2pcontext.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_42, monitorEvent42.toString());
		}
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
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;

			try {
				// Open database connection
				con = service.database.getDataSource().getConnection();

				// Write serialised model in Blob
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bOut);
				out.writeObject(body);
				Blob blob = con.createBlob();
				blob.setBytes(1, bOut.toByteArray());

				// Check if model with given name already exists in database. If yes, update it.
				// Else, insert it
				ps = con.prepareStatement("SELECT * FROM models WHERE name = ?");
				ps.setString(1, name);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					ps.close();
					ps = con.prepareStatement("UPDATE models SET model = ? WHERE name = ?");
					ps.setBlob(1, blob);
					ps.setString(2, name);
					ps.executeUpdate();
				} else {
					ps.close();
					ps = con.prepareStatement("INSERT INTO models(name, model) VALUES (?, ?)");
					ps.setString(1, name);
					ps.setBlob(2, blob);
					ps.executeUpdate();
				}

				resp = Response.ok().entity("Model stored.").build();
			} catch (SQLException e) {
				e.printStackTrace();
				resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (IOException e) {
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

		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of BotModels") })
		@ApiOperation(value = "Retrieve BotModels", notes = "Get all stored BotModels.")
		public Response getModels() {
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;

			try {
				// Open database connection
				con = service.database.getDataSource().getConnection();

				ps = con.prepareStatement("SELECT name FROM models");
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

		@GET
		@Path("/{name}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return BotModel") })
		@ApiOperation(value = "Get BotModel by name", notes = "Returns the BotModel for the given name.")
		public Response getModelByName(@PathParam("name") String name) {
			Connection con = null;
			PreparedStatement ps = null;
			Response resp = null;

			try {
				// Open database connection
				con = service.database.getDataSource().getConnection();

				// Fetch model with given name
				ps = con.prepareStatement("SELECT * FROM models WHERE name = ?");
				ps.setString(1, name);
				ResultSet rs = ps.executeQuery();
				rs.next();

				// Write serialised model in Blob
				Blob b = rs.getBlob("model");
				InputStream stream = b.getBinaryStream();
				ObjectInputStream in = new ObjectInputStream(stream);
				BotModel model = (BotModel) in.readObject();

				resp = Response.ok().entity(model).build();
			} catch (SQLException e) {
				e.printStackTrace();
				resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (IOException e) {
				e.printStackTrace();
				resp = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			} catch (ClassNotFoundException e) {
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
	}

	public boolean getMessages(ArrayList<BotMessage> messages) {
		System.out.println("Bot: Got " + messages.size() + " bot messages!");
		for (BotMessage m : messages) {
			BotResource br = new BotResource();
			br.trigger(m.getRemarks(), "");
		}
		return true;
	}

	public void setCourseMap(JSONObject map) {
		if (courseMap == null) {
			courseMap = new HashMap<String, String>();
		}
		for (String key : map.keySet()) {
			courseMap.put(key, map.getAsString(key));
		}
		System.out.println("Bot: Got courses: " + courseMap.toString());
	}

	public void getXapiStatements(ArrayList<String> statements) {
		System.out.println("Bot: Got " + statements.size() + " statements!");
		System.out.println(statements.toString());

		HashMap<String, ArrayList<String>> statementsPerCourse = new HashMap<String, ArrayList<String>>();
		Collections.reverse(statements);
		for (String statementObj : statements) {
			try {
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject obj = (JSONObject) parser.parse(statementObj);
				JSONObject statement = (JSONObject) obj.get("statement");
				JSONObject context = (JSONObject) statement.get("context");
				JSONObject extensions = (JSONObject) context.get("extensions");
				JSONObject courseInfo = (JSONObject) extensions
						.get("https://tech4comp.de/xapi/context/extensions/courseInfo");
				String courseid = Integer.toString(courseInfo.getAsNumber("courseid").intValue());

				if (!statementsPerCourse.containsKey(courseid)) {
					statementsPerCourse.put(courseid, new ArrayList<String>());
				}
				statementsPerCourse.get(courseid).add(statement.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("\u001B[33mDebug --- Partition: " + statementsPerCourse.toString() + "\u001B[0m");

		// Check if any bots take xAPI statements first
		HashMap<String, VLE> vles = config.getVLEs();
		for (Entry<String, VLE> vleEntry : vles.entrySet()) {
			HashMap<String, Bot> bots = vleEntry.getValue().getBots();

			for (Entry<String, Bot> botEntry : bots.entrySet()) {
				HashMap<String, Messenger> messengers = botEntry.getValue().getMessengers();
				String botName = botEntry.getValue().getName();
				for (Entry<String, Messenger> messengerEntry : messengers.entrySet()) {
					ChatMediator mediator = messengerEntry.getValue().getChatMediator();
					if (mediator instanceof MoodleForumMediator) {
						MoodleForumMediator moodleMediator = (MoodleForumMediator) mediator;
						if (courseMap != null && courseMap.containsKey(botName)) {
							if (statementsPerCourse.containsKey(courseMap.get(botName))) {
								System.out.println("\u001B[33mDebug --- Statement: "
										+ statementsPerCourse.get(courseMap.get(botName)) + "\u001B[0m");
								moodleMediator.handle(statementsPerCourse.get(courseMap.get(botName)));
							}
						} else {
							moodleMediator.handle(statements);
						}
					}
				}
			}
		}
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
		@Override
		public void run() {

			// System.out.println("bob is " + restarterBot);

			if (restarterBot == null) {
				MiniClient clientRestart = new MiniClient();
				System.out.println(address);
				clientRestart.setConnectorEndpoint(address);
				clientRestart.setLogin("alice", "pwalice");
				HashMap<String, String> headers = new HashMap<String, String>();
				try {
					if (restarterBotName != null && restarterBotPW != null && !restarterBotName.equals("")
							&& !restarterBotPW.equals("")) {
						ClientResponse result2 = clientRestart.sendRequest("GET", "SBFManager/bots/restart", "",
								headers);
						if (result2 != null) {
							restarterBot = BotAgent.createBotAgent("restarterBot");
						}
					} else {
						restarterBot = BotAgent.createBotAgent("restarterBot");
					}
				} catch (Exception e) {
					restarterBot = null;
					e.printStackTrace();
				}

			}
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
			Gson gson = new Gson();
			for (VLE vle : getConfig().getVLEs().values()) {
				for (Bot bot : vle.getBots().values()) {
					ArrayList<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
					for (MessageInfo m : messageInfos) {
						System.out.println("ugaugaugaug" + m.getMessage());
						ChatStatement chatStatement = ChatStatement.generate(m.getMessage().getUser(), m.getBotName(),
								m.getMessage().getText(), m.getMessage().getTime(), m.getMessage().getDomain());
						String chatStatementJSON = gson.toJson(chatStatement);
						// l2pcontext.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2,
						// chatStatementJSON);
					}
					
					bot.handleMessages(messageInfos);

					// TODO: Handle multiple environments (maybe?)

					MiniClient client = new MiniClient();
					client.setConnectorEndpoint(vle.getAddress());

					HashMap<String, String> headers = new HashMap<String, String>();
					for (MessageInfo m : messageInfos) {
						try {
							System.out.println("augaugaugaug" + m.getIntent().getKeyword());
							System.out.println(m.getMessage().getCurrMessage() + m.getMessage().getPreviousMessage());
							ClientResponse result = client.sendRequest("POST",
									"SBFManager/bots/" + m.getBotName() + "/trigger/intent", gson.toJson(m),
									MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
							System.out.println(result.getResponse());
						} catch (Exception e) {
							e.printStackTrace();
						}
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
						for (Bot b : vle.getBots().values()) {
							HashMap<String, Boolean> activeBots = b.getActive();
							HashSet<Trigger> tList = r.getTrigger();
							for (Trigger t : tList) {
								// for (Entry<String, Boolean> entry : activeBots.entrySet()) {
								// If bot is active
								// if (entry.getValue()) {

								System.out.println(df.format(d1) + ": " + b.getName());
								MiniClient client = new MiniClient();
								System.out.println("vle2" + vle);
								client.setConnectorEndpoint(vle.getAddress());

								JSONObject body = new JSONObject();
								body.put("serviceAlias", vle.getName());

								JSONObject atts = new JSONObject();

								body.put("function", t.getTriggeredFunction().getId());
								body.put("bot", b.getName());
								// atts.put(vle.getEnvironmentSeparator(), entry.getKey());
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
								// }
							}
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
	}

	public static String encryptThisString(String input) {
		if (input != null) {
			try {
				// getInstance() method is called with algorithm SHA-384
				MessageDigest md = MessageDigest.getInstance("SHA-384");

				// digest() method is called
				// to calculate message digest of the input string
				// returned as array of byte
				byte[] messageDigest = md.digest(input.getBytes());

				// Convert byte array into signum representation
				BigInteger no = new BigInteger(1, messageDigest);

				// Convert message digest into hex value
				String hashtext = no.toString(16);

				// Add preceding 0s to make it 32 bit
				try {
					while (hashtext.getBytes("UTF-16BE").length * 8 < 1536) {
						hashtext = "0" + hashtext;
					}
				} catch (Exception e) {
					System.out.println(e);
				}

				// return the HashText
				return hashtext;
			}

			// For specifying wrong message digest algorithms
			catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	@POST
	@Path("/sendMessageToSlack/{token}/{email}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Trigger slack chat message to slack user with given email")
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "triggered chat message") })
	public Response testRoute(@PathParam("token") String token, @PathParam("email") String email, String input) {
		// This function is a proof of concept. It is not the best in terms of run time,
		// but optimization would require bigger changes
		// in the code structure. To make it faster, the channel could be saved in a db
		// once at first access, so the expensive API do not have to be called
		// everytime.
		try {
			SlackChatMediator chatMediator = new SlackChatMediator(token);
			System.out.println("slack mediator initialized");

			// get user id from slack
			try {
				// slack api call to get email for user id
				JSONParser p = new JSONParser();
				JSONObject bodyInput = (JSONObject) p.parse(input);
				String msgtext = bodyInput.getAsString("msg");
				System.out.println("Using token " + token);
				System.out.println("Using email " + email);

				String channel = chatMediator.getChannelByEmail(email);
				chatMediator.sendMessageToChannel(channel, msgtext);

			} catch (Exception e) {
				e.printStackTrace();
				return Response.ok("Sending message failed.").build();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.ok().build();

	}

	@POST
	@Path("/sendMessageToRocketChat/{token}/{email}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Trigger rocket chat message to given rocket chat channel")
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "triggered chat message") })
	public Response sendMessageToRocketChat(@PathParam("token") String token, @PathParam("email") String email,
			String input) {
		try {
			RocketChatMediator chatMediator = new RocketChatMediator(token, database);
			System.out.println("rocket chat mediator initialized");

			try {
				JSONParser p = new JSONParser();
				JSONObject bodyInput = (JSONObject) p.parse(input);
				String msgtext = bodyInput.getAsString("msg");
				String channel = chatMediator.getChannelByEmail(email);
				chatMediator.sendMessageToChannel(channel, msgtext);

			} catch (Exception e) {
				e.printStackTrace();
				return Response.ok("Sending message failed.").build();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.ok().build();

	}

	@POST
	@Path("/editMessage/{token}/{email}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Edit Chat Message")
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "triggered chat message") })
	public Response editMessage(@PathParam("token") String token, @PathParam("email") String email,
			String input) {
		System.out.println("received api call to edit message");
		try {

			ChatMediator chatMediator = null;
			String channel = "";
			if (token.startsWith("xoxb")) {
				chatMediator = (SlackChatMediator) new SlackChatMediator(token);
				channel = chatMediator.getChannelByEmail(email);
			} else {
				chatMediator = (TelegramChatMediator) new TelegramChatMediator(token);
				channel = email;
			}

			try {
				JSONParser p = new JSONParser();
				JSONObject bodyInput = (JSONObject) p.parse(input);
				String ts = bodyInput.getAsString("ts");
				String blocks = bodyInput.getAsString("blocks");
				System.out.println("Using token " + token + " ts " + ts + " blocks " + blocks);

				chatMediator.editMessage(channel, ts, blocks, Optional.empty());

			} catch (Exception e) {
				e.printStackTrace();
				return Response.ok("Editing chat failed.").build();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return Response.ok().build();

	}

}
