package i5.las2peer.services.socialBotManagerService;

import java.math.BigInteger;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
import i5.las2peer.api.security.AgentException;
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
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubWebhookReceiver;
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
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.MessageInfo;
import i5.las2peer.services.socialBotManagerService.model.Messenger;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Trigger;
import i5.las2peer.services.socialBotManagerService.model.TriggerFunction;
import i5.las2peer.services.socialBotManagerService.model.BotRoutine;
import i5.las2peer.services.socialBotManagerService.model.ConversationMessage;
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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.pengrad.telegrambot.model.User;

import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * las2peer-SocialBotManager-Service
 *
 * A REST service that manages social bots in a las2peer network.
 *
 */
@Api(value = "test")
@SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.6.0", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "BSD 3-Clause License", url = "https://raw.githubusercontent.com/rwth-acis/las2peer-social-bot-manager-service/master/LICENSE")))

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
	private String webconnectorUrl = "http://localhost:8080"; // address of running webconnector
	private static String webconnectorUrlStatic;
	private String restarterBotName; // name of restarterBot
	private static String restarterBotNameStatic;
	private String restarterBotPW; // PW of restarterBot
	private static String restarterBotPWStatic; // PW of restarterBot

	private static String lrsAuthTokenStatic;
	private static String lrsURLStatic;

	private String lrsAuthToken;
	private String lrsURL;

	private String mongoHost;
	private String mongoUser;
	private String mongoPassword;
	private String mongoDB;
	private String mongoUri;
	private String mongoAuth = "admin";

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

	public Context getL2pcontext() {
		return l2pcontext;
	}

	public void setL2pcontext(Context l2pcontext) {
		this.l2pcontext = l2pcontext;
	}

	public SocialBotManagerService() throws Exception {
		super();
		setFieldValues(); // This sets the values of the configuration file
		restarterBotNameStatic = restarterBotName;
		restarterBotPWStatic = restarterBotPW;
		webconnectorUrlStatic = webconnectorUrl;
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
			getConfig().setBotConfiguration(new HashMap<String, Bot>());
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

		// mongo db connection for exchanging files
		mongoUri = "mongodb://" + mongoUser + ":" + mongoPassword + "@" + mongoHost + "/?authSource=" + mongoAuth;
		// Construct a ServerApi instance using the ServerApi.builder() method
		CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
		CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
		MongoClientSettings settings = MongoClientSettings.builder()
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.applyConnectionString(new ConnectionString(mongoUri))
				.codecRegistry(codecRegistry)
				.build();

		// Create a new client and connect to the server
		MongoClient mongoClient = MongoClients.create(settings);
		// Create a new client and connect to the server
		try {
			MongoDatabase database = mongoClient.getDatabase(mongoDB);
			// Send a ping to confirm a successful connection
			Bson command = new BsonDocument("ping", new BsonInt64(1));
			Document commandResult = database.runCommand(command);
			System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
		} catch (MongoException me) {
			System.err.println(me);
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			mongoClient.close();
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
		getResourceConfig().register(GitHubWebhookReceiver.class);
		getResourceConfig().register(RESTfulChatResource.class);
	}

	/**
	 * Returns the custom message descriptions for the service.
	 */
	@Override
	public HashMap<String, String> getCustomMessageDescriptions() {
		HashMap<String, String> descriptions = new HashMap<>();
		descriptions.put(
				"SERVICE_CUSTOM_MESSAGE_1",
				"Monitors whenever a bot gets initialized");
		descriptions.put(
				"SERVICE_CUSTOM_MESSAGE_2",
				"(deprecated) Was used to log the user messages.");
		descriptions.put(
				"SERVICE_CUSTOM_MESSAGE_3",
				"Logs whenever a new bot gets created. " +
						"| Key         | Description     					        |"
						+ "|---------------|--------------------------------|"
						+ "| botName       | Represents the bot name.       |"
						+ "| agentId       | Represents the agent of the bot|");
		descriptions.put(
				"SERVICE_CUSTOM_MESSAGE_42",
				"| Key         | Description                                                                                                 |"
						+ "|-------------|-------------------------------------------------------------------------------------------------------------|"
						+ "| task        | Represents the task being performed.                                                                         |"
						+ "| email       | Represents the email address associated with the user.                                              |"
						+ "| time        | Represents the time taken to perform the task, calculated as the difference between the current time and `start` time. |");
		descriptions.put(
				"SERVICE_CUSTOM_MESSAGE_80",
				"Logs the whole json body of the request.");
		return descriptions;
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


						} catch (EnvelopeNotFoundException | EnvelopeAccessDeniedException
								| EnvelopeOperationFailedException e) {

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
			return Response.ok().entity("Bots restarted").build();
		}

		@GET
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of bots") })
		@ApiOperation(value = "Get all bots", notes = "Returns a list of all registered bots.")
		public Response getBots() {
			JSONObject botList = new JSONObject();
			// Iterate through VLEs
			Gson g = new Gson();
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			for (Entry<String, Bot> botEntry : getConfig().getBots().entrySet()) {
				String botName = botEntry.getKey();
				Bot b = botEntry.getValue();
				// Iterate bots
				JSONObject jb = new JSONObject();
				JSONObject ac = new JSONObject();
				ac.putAll(b.getActive());
				jb.put("active", ac);
				jb.put("id", b.getId());
				jb.put("name", b.getName());
				jb.put("version", b.getVersion());
				try {
					jb.put("nlu", p.parse(g.toJson(b.getRasaServers())));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				botList.put(botName, jb);
			}
			return Response.ok().entity(botList).build();
		}

		@GET
		@Path("/{botName}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns bot information") })
		@ApiOperation(value = "Retrieve bot by name", notes = "Returns bot information by the given name.")
		public Response getBotsForVLE(@PathParam("botName") String name) {
			Bot b = getConfig().getBots().get(name);
			if (b == null) {
				return Response.status(Status.NOT_FOUND).entity("Bot " + name + " not found.").build();
			}
			JSONObject bot = new JSONObject();
			JSONObject ac = new JSONObject();
			ac.putAll(b.getActive());
			bot.put("active", ac);
			bot.put("id", b.getId());
			bot.put("name", b.getName());
			bot.put("version", b.getVersion());
			return Response.ok().entity(bot).build();
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
			try {
				sbfservice.setL2pcontext(Context.getCurrent());
				BotParser bp = BotParser.getInstance();

				String returnString = "";
				LinkedHashMap<String, BotModelNode> nodes = botModel.getNodes();
				LinkedHashMap<String, BotModelEdge> edges = botModel.getEdges();
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
						for (Entry<String, BotModelNodeAttribute> subEntry : entry.getValue().getAttributes()
								.entrySet()) {
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
								.fetchAgent(
										Context.getCurrent().getUserAgentIdentifierByLoginName(restarterBotNameStatic));
						restarterBot.unlock(restarterBotPWStatic);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Envelope env = null;
				HashMap<String, BotModel> old = null;
				try {
					bp.parseNodesAndEdges(SocialBotManagerService.getConfig(), SocialBotManagerService.getBotAgents(),
							nodes, edges, sbfservice.database, webconnectorUrlStatic);
				} catch (ParseBotException | IllegalArgumentException | IOException | DeploymentException
						| AuthTokenException e) {
					e.printStackTrace();
					if (e.toString().toLowerCase().contains("login name longer")) {
						return Response.status(Status.BAD_REQUEST)
								.entity("Bot Name needs to have at least 4 characters!")
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
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
			}

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
					return Response.status(Status.NOT_FOUND).entity("Botagent " + botName + " not found").build();
				}
				body = body.replace("$botId", botAgent.getIdentifier());
				JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject j = (JSONObject) p.parse(body);
				String basePath = (String) j.get("basePath");
				Bot bot = getConfig().getBot(botName);

				if (bot == null) {
					return Response.status(Status.NOT_FOUND).entity("Bot " + botName + " not found").build();
				}

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
		@Consumes(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Successfully handled webhook call."),
				@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Bot not found."),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Parse exception, field event is missing or event is unsupported.")
		})
		@ApiOperation(value = "Handle webhook calls", notes = "Handles incoming webhook calls.")
		public Response webhook(String body, @PathParam("botName") String botName) {
			// check if bot exists
			Bot bot = null;
			for (String botId : getConfig().getBots().keySet()) {
				if (getConfig().getBots().get(botId).getName().toLowerCase().equals(botName.toLowerCase())) {
					bot = getConfig().getBot(botId);
					break;
				}
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
				String triggerFunctionName = parsedBody.getAsString("functionName");
				String triggerUID = parsedBody.getAsString("uid");

				for (BotAgent botAgent : getBotAgents().values()) {
					try {
						this.sbfservice.checkTriggerBot(config, parsedBody, botAgent, triggerUID, triggerFunctionName);
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
			String addr = sbf.webconnectorUrl;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

						JSONObject j = (JSONObject) p.parse(body);
						String service = (String) j.get("serviceAlias");

						JSONObject context = new JSONObject();
						context.put("addr", addr);

						String botFunctionId = j.getAsString("function");
						BotAgent botAgent = getBotAgents().get(j.getAsString("bot"));

						try {
							sbf.checkRoutineTrigger(config, j, botAgent, botFunctionId, context);
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

			try {
				String result = java.net.URLDecoder.decode(body, StandardCharsets.UTF_8.name());

				// slack adds payload= in front of the result, so deleting that to parse it to
				// json
				result = result.substring(8);


				JSONObject bodyInput = (JSONObject) p.parse(result);

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
				Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, body);

				SocialBotManagerService sbf = this.sbfservice;
				new Thread(new Runnable() {
					@Override
					public void run() {
						String messengerName = "";
						try {
							BotAgent botAgent = getBotAgents().get(messageInfo.getBotName());
							String service = messageInfo.getServiceAlias();

							// get triggered function id, by getting bot, the messengers and then the intent
							// hash map
							HashMap<String, Bot> botsHM = getConfig().getBots();
							// System.out.println("botsHM: " + botsHM);
							String triggerdFunctionId = "";
							for (Bot bot : botsHM.values()) {
								// System.out.println(bot);
								HashMap<String, i5.las2peer.services.socialBotManagerService.model.Messenger> messengers = bot
										.getMessengers();
								for (Messenger m : messengers.values()) {
									// System.out.println("messenger: " + m);
									HashMap<String, i5.las2peer.services.socialBotManagerService.model.IncomingMessage> intentsHM = m
											.getRootChildren();
									// System.out.println("intentsHM: " + intentsHM);
									for (String s : intentsHM.keySet()) {
										if (s.equals(expectedIntent)) {
											i5.las2peer.services.socialBotManagerService.model.IncomingMessage incomingMessage = intentsHM
													.get(s);
											i5.las2peer.services.socialBotManagerService.model.IncomingMessage chatResponses = incomingMessage;
											// System.out.println(chatResponses);
											// System.out.println(chatResponses.getTriggeredFunctionId());
											// get first trigger function for now
											triggerdFunctionId = chatResponses.getTriggeredFunctionId();
											messengerName = m.getName();
										}
									}
								}
							}
							MessageInfo newMessageInfo = new MessageInfo(chatMessage, intent, triggerdFunctionId, name,
									instanceAlias, true, new ArrayList<>(), messengerName);
							System.out.println("Got 2nd info: " + newMessageInfo.getMessage().getText() + " "
									+ newMessageInfo.getTriggeredFunctionId());
							try {
								sbf.performIntentTrigger(getConfig(), botAgent, newMessageInfo);
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
					Bot bot = null;

					for (Bot b : getConfig().getBots().values()) {
						if (bot.getMessenger(ChatService.SLACK) != null) {
							ChatMediator mediator = bot.getMessenger(ChatService.SLACK)
									.getChatMediator();
							if (mediator.hasToken(token))
								bot = b;
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
					JSONObject xAPI = createXAPIStatement(cleanedJson.getAsString("email"), name,
							m.getIntent().getKeyword(), m.getMessage().getText());
					sendXAPIStatement(xAPI, lrsAuthTokenStatic);
				}
				Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, cleanedJson.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}

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
						try {
							sbf.performIntentTrigger(config, botAgent, m);
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

				}
			}).start();
			return Response.ok().build();
		}

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
							+ botName + "', 'objectType':'Activity'}"));
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
			return xAPI;
		}

		public void sendXAPIStatement(JSONObject xAPI, String lrsAuthToken) {
			// Copy pasted from LL service
			// POST statements
			try {
				if (lrsURLStatic == null || lrsAuthTokenStatic == null) {
					return;
				}

				URL url = new URL(lrsURLStatic + "/data/xAPI/statements");

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
			Bot b = getConfig().getBots().get(bot);
			if (b != null) {
				b.setIdActive(unit, false);
				return Response.ok().entity(bot + " deactivated.").build();
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
			Bot b = getConfig().getBot(bot);
			if (b != null) {
				ArrayList messengers = (ArrayList) body.get("messengers");
				if (b.deactivateAllWithCheck(messengers)) {
					getConfig().removeBot(bot);
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

			return Response.status(Status.NOT_FOUND).entity(bot + " not found.").build();
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
					Bot bot = null;

					for (Bot b : getConfig().getBots().values()) {
						if (b.getMessenger(ChatService.TELEGRAM) != null) {
							bot = b;
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

	public void checkRoutineTrigger(BotConfiguration botConfig, JSONObject j, BotAgent botAgent, String botFunctionId,
			JSONObject context)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException, AgentNotFoundException, AgentOperationFailedException {
		String botId = botAgent.getIdentifier();
		Bot bot = botConfig.getBots().get(botId);
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
				formAttributes(botConfig, sfa, bot, body, functionPath, attlist, triggerAttributes);
			}
			performTrigger(botConfig, botFunction, botAgent, functionPath, "", body);
		}
	}

	// TODO: Use entity value, handle environment separator, handle other things
	// than static content
	public void performIntentTrigger(BotConfiguration botConfig, BotAgent botAgent, MessageInfo messageInfo)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException, AgentNotFoundException, AgentOperationFailedException {
		String botId = botAgent.getIdentifier();
		Bot bot = botConfig.getBots().get(botId);

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
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes()) {
				// if (botFunction.getServiceName().equals("https://api.openai.com/v1")) {
				// body.put(sfa.getName(), sfa.getContent());
				// } else {
				formAttributes(botConfig, sfa, bot, body, functionPath, attlist, triggerAttributes);
				// }
			}
			// Patch attributes so that if a chat message is sent, it is sent
			// to the same channel the action was triggered from.
			// TODO: Handle multiple messengers

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
			body.put("conversationId", messageInfo.getConversationId());
			botFunction.setMessengerName(messageInfo.getMessengerName());
			performTrigger(botConfig, botFunction, botAgent, functionPath, "", body);
		}
	}

	public void prepareRequestParameters(BotConfiguration botConfig, BotAgent botAgent, MessageInfo messageInfo,
			String functionPath, JSONObject body, ServiceFunction botFunction)
			throws ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException, AgentNotFoundException, AgentOperationFailedException {
		String botId = botAgent.getIdentifier();
		Bot bot = botConfig.getBots().get(botId);
		if (bot != null) {
			botFunction = bot.getBotServiceFunctions().get(messageInfo.getTriggeredFunctionId());
			functionPath = "";
			if (botFunction.getActionType().equals(ActionType.SERVICE)) {
				functionPath = botFunction.getFunctionPath();
			} else if (botFunction.getActionType().equals(ActionType.OPENAPI)) {
				functionPath = botFunction.getFunctionPath();
			}
			HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();
			JSONObject triggerAttributes = new JSONObject();
			for (ServiceFunctionAttribute sfa : botFunction.getAttributes()) {
				formAttributes(botConfig, sfa, bot, body, functionPath, attlist, triggerAttributes);
			}
			// Patch attributes so that if a chat message is sent, it is sent
			// to the same channel the action was triggered from.
			// TODO: Handle multiple messengers
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
			body.put("functionPath", functionPath);
			// return body;
			// performTrigger(botConfig, botFunction, botAgent, functionPath, "", body);
		}
		// return null;
	}

	public void checkTriggerBot(BotConfiguration botConfig, JSONObject body, BotAgent botAgent, String triggerUID,
			String triggerFunctionName) throws AgentNotFoundException, AgentOperationFailedException,
			ServiceNotFoundException, ServiceNotAvailableException, InternalServiceException,
			ServiceMethodNotFoundException, ServiceInvocationFailedException, ServiceAccessDeniedException,
			ServiceNotAuthorizedException, ParseBotException {
		String botId = botAgent.getIdentifier();

		Bot bot = botConfig.getBots().get(botId);
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
						if (triggeredFunction.getActionType().equals(ActionType.SERVICE) || triggeredFunction.getActionType().equals(ActionType.OPENAPI)){
							functionPath = triggeredFunction.getFunctionPath();
						}
						JSONObject triggeredBody = new JSONObject();
						HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();
						for (ServiceFunction bsf : bot.getBotServiceFunctions().values()) {
							for (ServiceFunctionAttribute bsfa : bsf.getAttributes()) {
								attlist.put(bsfa.getId(), bsfa);
							}
						}
						String user = body.getAsString("user");
						String channel = body.getAsString("channel");
						String email = body.getAsString("email");
						String convId = body.getAsString("conversationId");
						String intent = body.getAsString("intent");
						String contextOn = body.getAsString("contextOn");
						triggeredBody.put("user", user);
						triggeredBody.put("channel", channel);
						triggeredBody.put("email", email);
						triggeredBody.put("conversationId", convId);
						triggeredBody.put("intent", intent);
						triggeredBody.put("contextOn", contextOn);
						triggeredBody.put("entities", body.get("entities"));
						triggeredBody.put("organization", body.get("organization"));



						JSONObject triggerAttributes = (JSONObject) body.get("attributes");
						for (ServiceFunctionAttribute triggeredFunctionAttribute : triggeredFunction.getAttributes()) {
							formAttributes(botConfig, triggeredFunctionAttribute, bot, triggeredBody, functionPath,
									attlist,
									triggerAttributes);
						}

						performTrigger(botConfig, triggeredFunction, botAgent, functionPath, triggerUID, triggeredBody);
					}
				}
			}

		} else {
			// TODO
		}
	}

	// Aaron : if name of body is empty add as part of an array of contents ?
	private void formAttributes(BotConfiguration botConfig, ServiceFunctionAttribute triggeredFunctionAttribute,
			Bot bot,
			JSONObject triggeredBody, String functionPath, HashMap<String, ServiceFunctionAttribute> attlist,
			JSONObject triggerAttributes) throws ServiceNotFoundException, ServiceNotAvailableException,
			InternalServiceException, ServiceMethodNotFoundException, ServiceInvocationFailedException,
			ServiceAccessDeniedException, ServiceNotAuthorizedException, ParseBotException {
		// Attributes of the triggered function
		System.out.println(triggeredFunctionAttribute.getName());
		if (triggeredFunctionAttribute.isSameAsTrigger()) {
			mapAttributes(triggeredBody, triggeredFunctionAttribute, functionPath, attlist, triggerAttributes);
		} else if (triggeredFunctionAttribute.getParameterType().equals("body")) { // triggeredFunctionAttribute.getName()
																					// == "body", doesn't make sense?
			JSONObject triggerBody = (JSONObject) triggerAttributes.get("body");
			// sfa has child attributes
			if (!triggeredFunctionAttribute.getChildAttributes().isEmpty()) {
				JSONArray jsonArray = new JSONArray();

				for (ServiceFunctionAttribute subsfa : triggeredFunctionAttribute.getChildAttributes()) {
					// Same as trigger is never set because the edge doesn't even exist in the
					// frontend
					if (subsfa.isSameAsTrigger()) {
						ServiceFunctionAttribute mappedTo = subsfa.getMappedTo();
						if (triggerBody.get(mappedTo.getName()) != null) {
							triggeredBody.put(subsfa.getName(), triggerBody.get(mappedTo.getName()));
						} else
							triggeredBody.put(subsfa.getName(), triggerAttributes.get(mappedTo.getName()));
					} else {
						if (triggeredFunctionAttribute.getItb() != null) {
							mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute,
									triggeredBody, attlist, triggerAttributes, functionPath);
						} else {
							if (subsfa.hasStaticContent()) {
								mapWithStaticContent(subsfa, triggeredBody);
							} else {
								// sfa's content is empty, treat sfa as a list and subsfa as a list item
								if (triggeredFunctionAttribute.getContent().isEmpty()) {
									HashMap<String, String> listItemMap = new HashMap<String, String>();
									// Put the attributes of the list item into a map
									for (ServiceFunctionAttribute listItemAttributes : subsfa.getChildAttributes()) {
										listItemMap.put(listItemAttributes.getName(), listItemAttributes.getContent());
									}
									JSONObject jsonlistItemMap = new JSONObject(listItemMap);
									jsonArray.add(jsonlistItemMap);
								}
							}
						}
					}
				}
				if (triggeredFunctionAttribute.getContent().isEmpty()) {
					triggeredBody.put(triggeredFunctionAttribute.getName(), jsonArray);
				}
			} else { // sfa has no child attributes
				triggeredBody.put(triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContent());
			}
		} else {
			if (triggeredFunctionAttribute.getItb() != null) {
				mapWithIfThen(triggeredFunctionAttribute.getItb(), triggeredFunctionAttribute, triggeredBody,
						attlist, triggerAttributes, functionPath);
			} else {
				if (triggeredFunctionAttribute.hasStaticContent()) {
					if (triggeredFunctionAttribute.getParameterType().equals("form")) {
						mapWithStaticFormContent(triggeredFunctionAttribute, triggeredBody);
					} else {
						mapWithStaticContent(triggeredFunctionAttribute, triggeredBody);
					}
				} else {
					// TODO
					if (triggeredFunctionAttribute.getParameterType().equals("form")) {
						mapWithStaticFormContent(triggeredFunctionAttribute, triggeredBody);
					} else {
						System.out.println("Unknown mapping " + triggeredFunctionAttribute.getContentType()
								+ triggeredFunctionAttribute.getParameterType());
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

	private void mapWithStaticFormContent(ServiceFunctionAttribute triggeredFunctionAttribute,
			JSONObject triggeredBody) {
		if (triggeredFunctionAttribute.getContent().length() > 0) {
			if (triggeredBody.containsKey(triggeredFunctionAttribute.getName())) {
				JSONArray array = new JSONArray();
				array.add(triggeredBody.get(triggeredFunctionAttribute.getName()));
				array.add(triggeredFunctionAttribute.getContent());
				if (triggeredBody.get("form") == null) {
					JSONObject form = new JSONObject();
					form.put(triggeredFunctionAttribute.getName(), array);
					triggeredBody.put("form", form);
				} else {
					JSONObject form = (JSONObject) triggeredBody.get("form");
					form.put(triggeredFunctionAttribute.getName(), array);
					triggeredBody.put("form", form);
				}
			} else {
				if (triggeredBody.get("form") == null) {
					JSONObject form = new JSONObject();
					form.put(triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContent());
					triggeredBody.put("form", form);
				} else {
					JSONObject form = (JSONObject) triggeredBody.get("form");
					form.put(triggeredFunctionAttribute.getName(), triggeredFunctionAttribute.getContent());
					triggeredBody.put("form", form);
				}
			}

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

	private void performTrigger(BotConfiguration botConfig, ServiceFunction sf, BotAgent botAgent, String functionPath,
			String triggerUID,
			JSONObject triggeredBody) throws AgentNotFoundException, AgentOperationFailedException {
		JSONObject remarks = new JSONObject();
		String serviceEndpoint = "";
		remarks.put("user", encryptThisString(triggeredBody.getAsString("user")));
		if (sf.getActionType().equals(ActionType.SERVICE) || sf.getActionType().equals(ActionType.OPENAPI)) {
			MiniClient client = new MiniClient();
			if (sf.getActionType().equals(ActionType.SERVICE)) {
				client.setConnectorEndpoint(webconnectorUrl);
				serviceEndpoint = webconnectorUrl;
			} else if (sf.getActionType().equals(ActionType.OPENAPI)) {
				client.setConnectorEndpoint(sf.getServiceName() + functionPath);
				serviceEndpoint = sf.getServiceName() + functionPath;
			}
			remarks.put("serviceEndpoint", serviceEndpoint);
			l2pcontext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, remarks.toJSONString(),
					triggeredBody.get("conversationId").toString(), sf.getFunctionName(),
					botAgent.getIdentifier().toString(), "bot", "start", System.currentTimeMillis());
			// client.setLogin("alice", "pwalice");
			// client.setLogin(botAgent.getLoginName(), botPass);
			String userId = triggeredBody.getAsString("user");
			Bot bot = botConfig.getBots().get(botAgent.getIdentifier());
			String messengerID = sf.getMessengerName();
			String channel = triggeredBody.getAsString("channel");
			HashMap<String, String> headers = new HashMap<String, String>();
			// HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String,
			// ServiceFunctionAttribute>();
			// JSONObject triggerAttributes = new JSONObject();

			// Get the channel's conversation and add them to the json array
			JSONArray jsonArray = new JSONArray();
			HashMap<String, Collection<ConversationMessage>> conversation = bot.getMessenger(messengerID)
					.getConversationMap();
			for (ConversationMessage msg : conversation.get(channel)) {
				HashMap<String, String> msgMap = new HashMap<String, String>();
				msgMap.put("role", msg.getRole());
				msgMap.put("content", msg.getContent());
				JSONObject jsonmsgMap = new JSONObject(msgMap);
				jsonArray.add(jsonmsgMap);
			}
			triggeredBody.put("conversationPath", jsonArray);
			triggeredBody.put("messenger", bot.getMessenger(messengerID).getChatService().toString());
			triggeredBody.put("botId", bot.getId());
			triggeredBody.put("botName", bot.getName());


			ClientResponse r = null;
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			if (triggeredBody.containsKey("form")) {
				try {
					File f = null;
					if (triggeredBody.containsKey("fileBody")) {
						byte[] decodedBytes = java.util.Base64.getDecoder()
								.decode(triggeredBody.getAsString("fileBody"));
						f = new File(
								triggeredBody.getAsString("fileName") + "." + triggeredBody.getAsString("fileType"));
						/*
						 * if(fileType.equals("")){
						 * file = new File(fileName);
						 * }
						 */
						try {
							FileUtils.writeByteArrayToFile(f, decodedBytes);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					Client textClient = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
					functionPath = functionPath.replace("[channel]", channel);
					// will need to add email here as well
					// functionPath = functionPath.replace("[email]", email);
					functionPath = functionPath.replace("[intent]", triggeredBody.getAsString("intent"));
					functionPath = bot.getMessenger(messengerID).replaceVariables(channel, functionPath);
					JSONObject entities = (JSONObject) triggeredBody.get("entities");
					for (String eName : entities.keySet()) {
						;
						if (functionPath.toLowerCase().contains("[" + eName + "]")) {
							functionPath = functionPath.replace("[" + eName + "]",
									((JSONObject) entities.get(eName)).get("value").toString());
						}
					}

					JSONObject form = (JSONObject) triggeredBody.get("form");
					FormDataMultiPart mp = new FormDataMultiPart();
					String queryParams = "?";
					if (form != null) {
						for (String key : form.keySet()) {
							if (sf.getHttpMethod().equals("get")) {
								if (form.getAsString(key).equals("[channel]")) {
									queryParams += key + "=" + channel + "&";
								} else if (form.getAsString(key).equals("[email]")) {
									// queryParams+=key+"="+email+"&";
								} else if (form.getAsString(key).equals("[organization]")) {
									queryParams += key + "=" + triggeredBody.getAsString("organization") + "&";
								} else {
									queryParams += key + "=" + form.getAsString(key) + "&";
								}
							} else {
								if (form.getAsString(key).equals("[channel]")) {
									mp = mp.field(key, channel);
								} else if (form.getAsString(key).equals("[email]")) {
									// mp = mp.field(key, email);
								} else if (form.getAsString(key).equals("[organization]")) {
									mp = mp.field(key, triggeredBody.get("organization").toString());
								} else if (form.getAsString(key).contains("[")) {
									for (String eName : entities.keySet()) {
										if (form.getAsString(key).toLowerCase().contains(eName)) {
											mp = mp.field(key,
													((JSONObject) entities.get(eName)).get("value").toString());
										}
									}
								} else {
									mp = mp.field(key, form.getAsString(key));
								}
							}
						}
					}
					for (String key : form.keySet()) {
						if (form.getAsString(key).equals("[channel]")) {
							mp = mp.field(key, channel);
						} else {
							mp = mp.field(key, form.getAsString(key));
						}

					}
					if (f != null && f.exists()) {
						FileDataBodyPart filePart = new FileDataBodyPart("file", f);
						mp.bodyPart(filePart);
					}

					WebTarget target = textClient.target(sf.getServiceName() + functionPath + queryParams);
					if (f != null && f.exists()) {
						FileDataBodyPart filePart = new FileDataBodyPart("file", f);
						mp.bodyPart(filePart);
					}
					Response response = null;
					if (sf.getHttpMethod().equals("get")) {
						response = target.request().get();
					} else {
						response = target.request()
								.post(javax.ws.rs.client.Entity.entity(mp, mp.getMediaType()));
					}
					String test = response.readEntity(String.class);
					mp.close();
					try {
						java.nio.file.Files.deleteIfExists(Paths.get(
								triggeredBody.getAsString("fileName") + "." + triggeredBody.getAsString("fileType")));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
					triggeredBody = new JSONObject();
					triggeredBody.put("channel", channel);
					triggeredBody.put("text", test);
					JSONObject jsonResponse = (JSONObject) parser.parse(test);
					for (String key : jsonResponse.keySet()) {
						bot.getMessenger(messengerID).addVariable(channel, key, jsonResponse.getAsString(key));
					}
					bot.getMessenger(messengerID).setContextToBasic(channel,
							userId);
					// triggerChat(chat, triggeredBody);
					l2pcontext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, remarks.toJSONString(),
							triggeredBody.get("conversationId").toString(), sf.getFunctionName(),
							botAgent.getIdentifier().toString(), "bot", "complete", System.currentTimeMillis());
					return;

					// FormDataMultiPart multipart = (FormDataMultiPart) mp.field("msg",
					// newText).field("description", "")
					// .bodyPart(filePart);
					/*
					 * FileDataBodyPart filePart = new FileDataBodyPart("file", f);
					 * if(f.getName().toLowerCase().contains("json")){
					 * filePart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
					 * }
					 * FormDataMultiPart mp = new FormDataMultiPart();
					 * FormDataMultiPart multipart = (FormDataMultiPart) mp.field("msg",
					 * newText).field("description", "")
					 * .bodyPart(filePart);
					 * Response response = target.request().header("X-User-Id",
					 * client.getMyUserId()).header("X-Auth-Token", token)
					 * .post(Entity.entity(multipart, multipart.getMediaType()));
					 * System.out.println(response.getEntity().toString());
					 * mp.close();
					 * multipart.close();
					 * try {
					 * java.nio.file.Files.deleteIfExists(Paths.get(f.getName()));
					 * } catch (IOException e) {
					 * // TODO Auto-generated catch block
					 * e.printStackTrace();
					 * }
					 */
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e.getMessage());

				}
			} else {
				if (sf.getActionType().equals(ActionType.SERVICE)) {
					r = client.sendRequest(sf.getHttpMethod().toUpperCase(), sf.getServiceName() + functionPath,
							triggeredBody.toJSONString(), sf.getConsumes(), sf.getProduces(), headers);
				} else if (sf.getActionType().equals(ActionType.OPENAPI)) {
					r = client.sendRequest(sf.getHttpMethod().toUpperCase(), "", triggeredBody.toJSONString(),
							sf.getConsumes(), sf.getProduces(), headers);
					System.out.println("Response Statuscode:" + r.getHttpCode());
				}
			}

			// if the result is successful
			if (r.getResponse() != null) {
				if (r.getResponse().toString().length() > 30) {
					System.out.println(r.getResponse().toString().substring(0, 30) + "...");
				} else {
					System.out.println(r.getResponse());
				}

				if (Boolean.parseBoolean(triggeredBody.getAsString("contextOn"))) {
					try {
						JSONObject response = (JSONObject) parser.parse(r.getResponse());
						for (String key : response.keySet()) {
							bot.getMessenger(messengerID).addVariable(channel, key, response.getAsString(key));
						}
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
						if (response.containsKey("multiFiles")) {
							for (Object o : (JSONArray) response.get("multiFiles")) {
								JSONObject jsonO = (JSONObject) o;
								jsonO.put("channel", triggeredBody.getAsString("channel"));
								jsonO.put("email", triggeredBody.getAsString("email"));
								triggerChat(chat, jsonO);
							}
						} else {
							// if the service function triggers another service function, do not trigger chat, add the response to the conversationpath
							if (!sf.getTrigger().isEmpty()){
								HashMap<String, Collection<ConversationMessage>> convMap = bot.getMessenger(messengerID).getConversationMap();
								Collection<ConversationMessage> conv = convMap.get(triggeredBody.getAsString("channel"));
								ArrayList<ConversationMessage> convList = new ArrayList<>(conv);
								ConversationMessage botMsg = convList.get(convList.size() - 1);
								String convId = botMsg.getConversationId();
								triggeredBody.put("conversationId", convId);
								ConversationMessage newConvMsg = new ConversationMessage(convId, "assistant",
										triggeredBody.getAsString("text"));
								conv.add(newConvMsg);
								bot.getMessenger(messengerID).updateConversationInConversationMap(triggeredBody.getAsString("channel"), conv);
								
								//Trigger trigger = sf.getTrigger().iterator().next();
								//ServiceFunction triggeredSf = trigger.getTriggeredFunction();
								Gson gson = new Gson();
								String service = (String) sf.getServiceName();
								triggeredBody.put("serviceAlias", service);
								String triggerFunctionName = sf.getFunctionName();
								triggeredBody.put("functionName", triggerFunctionName);
								String triggerID = sf.getId();
								triggeredBody.put("uid", triggerID);
								JSONObject triggerAttributes = new JSONObject();
								triggeredBody.put("attributes", triggerAttributes);

								//Set the endpoint to the base url for the SBFManager
								if (sf.getActionType().equals(ActionType.OPENAPI)){
									client.setConnectorEndpoint(webconnectorUrl);
								}

								try {
									ClientResponse result = client.sendRequest("POST",
											"SBFManager/bots/" + bot.getName() + "/trigger/service", gson.toJson(triggeredBody),
											MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
								} catch (Exception e) {
									e.printStackTrace();
								}

							} else{
								triggerChat(chat, triggeredBody);
							}
							// if the response is from the openai service,
							// replace the last message in the conversation map which should be the normal
							// bot response with the enhanced bot message
							if (Boolean.parseBoolean(response.getAsString("openai"))) {
								// add token count to body
								triggeredBody.put("tokens", response.getAsNumber("tokens"));
								
								HashMap<String, Collection<ConversationMessage>> convMap = bot.getMessenger(messengerID)
										.getConversationMap();
								Collection<ConversationMessage> conv = convMap.get(triggeredBody.getAsString("channel"));
								ArrayList<ConversationMessage> convList = new ArrayList<>(conv);
								ConversationMessage botMsg = convList.get(convList.size() - 1);
								String convId = botMsg.getConversationId();
								convList.remove(botMsg);
								ConversationMessage newConvMsg = new ConversationMessage(convId, "assistant",
										triggeredBody.getAsString("text"));
								convList.add(newConvMsg);
								conv = convList;
								bot.getMessenger(messengerID)
										.updateConversationInConversationMap(triggeredBody.getAsString("channel"), conv);
							}
						}
						//We check if there is a leadsTo after the bot action:
						if (!sf.getLeadsTo().isEmpty()) {
							
							/** ADD CHECKS HERE TO DETERMINE WHETHER OR NOT TO ADD THE LEADS TO INCOMING MESSAGE TO THE FOLLOW UP STATE
							 * 
							 * Example: Check the response code and flags returned by the service call
							 *
							 *  */ 

							// We add the incoming message to the followupmessage of the current conversation state
							IncomingMessage currentState = bot.getMessenger(messengerID).getStateMap().get(channel);

							for (int i = 0; i < sf.getLeadsTo().size(); i++) {
								IncomingMessage msg = (IncomingMessage) sf.getLeadsTo().keySet().toArray()[i];
								String intentKey = (String) sf.getLeadsTo().values().toArray()[i];
								currentState.addFollowupMessage(intentKey, msg);
							}
							currentState.setFreezeMessageSend(true);
						} else if (!sf.getTrigger().isEmpty()){
							IncomingMessage currentState = bot.getMessenger(messengerID).getStateMap().get(channel);
							currentState.setFreezeMessageSend(true);
						}
						if (response.get("closeContext") == null || Boolean.valueOf(response.getAsString("closeContext"))) {
							System.out.println("Closed Context");
							bot.getMessenger(messengerID).setContextToBasic(triggeredBody.getAsString("channel"),
								triggeredBody.getAsString("user"));
						} else if (Boolean.valueOf(response.getAsString("closeContext")) == false) {
							System.out.println("Keep Context open");
							bot.getMessenger(messengerID).restoreConversationState(triggeredBody.getAsString("channel"));
						}
						
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println("Response from request is null");
			}
			l2pcontext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, remarks.toJSONString(),
					triggeredBody.get("conversationId").toString(), sf.getFunctionName(),
					botAgent.getIdentifier().toString(), "bot", "complete", System.currentTimeMillis());
		} else if (sf.getActionType().equals(ActionType.SENDMESSAGE)) {
			// deprecated
			Bot bot = botConfig.getBots().get(botAgent.getIdentifier());
			if (triggeredBody.get("channel") == null && triggeredBody.get("email") == null) {
				// TODO Anonymous agent error
				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(bot.getAddress());
				HashMap<String, String> headers = new HashMap<String, String>();
				ClientResponse result = client.sendRequest("GET", "SBFManager/email/" + triggerUID, "",
						MediaType.TEXT_HTML, MediaType.TEXT_HTML, headers);
				String mail = result.getResponse().trim();
				triggeredBody.put("email", mail);
			}
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
						chat.sendMessageToChannel(channel, textArray[i], "text");
					}
					i++;
				}
			} else {
				// if no specific text, send the regular text
				for (String s : emailArray) {
					channel = chat.getChannelByEmail(s);

					if (text != null && channel != null) {
						chat.sendMessageToChannel(channel, text, "text");
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
			chat.sendMessageToChannel(channel, "ContactList contacted.", "text");

		} else {
			if (body.containsKey("channel")) {
				channel = body.getAsString("channel");
			} else if (body.containsKey("email")) {
				String email = body.getAsString("email");
				monitorEvent42.put("email", email);
				channel = chat.getChannelByEmail(email);
			}
			if (text != null && !body.containsKey("fileBody")) {
				chat.sendMessageToChannel(channel, text, "text");
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
		HashMap<String, Bot> bots = config.getBots();

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
				clientRestart.setConnectorEndpoint(webconnectorUrl);
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
			for (Bot bot : getConfig().getBots().values()) {
				ArrayList<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
				for (MessageInfo m : messageInfos) {
					ChatStatement chatStatement = ChatStatement.generate(m.getMessage().getUser(), m.getBotName(),
							m.getMessage().getText(), m.getMessage().getTime(), m.getMessage().getDomain());
					String chatStatementJSON = gson.toJson(chatStatement);
					// l2pcontext.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2,
					// chatStatementJSON);
				}
				bot.handleMessages(messageInfos);

				// TODO: Handle multiple environments (maybe?)

				MiniClient client = new MiniClient();
				client.setConnectorEndpoint(webconnectorUrl);

				HashMap<String, String> headers = new HashMap<String, String>();
				for (MessageInfo m : messageInfos) {
					try {
						ClientResponse result = client.sendRequest("POST",
								"SBFManager/bots/" + m.getBotName() + "/trigger/intent", gson.toJson(m),
								MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				for (BotRoutine r : bot.getRoutines().values()) {
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
						for (Bot b : getConfig().getBots().values()) {
							HashMap<String, Boolean> activeBots = b.getActive();
							HashSet<Trigger> tList = r.getTrigger();
							for (Trigger t : tList) {
								// for (Entry<String, Boolean> entry : activeBots.entrySet()) {
								// If bot is active
								// if (entry.getValue()) {

								JSONObject body = new JSONObject();
								body.put("serviceAlias", ""); // TODO

								JSONObject atts = new JSONObject();

								body.put("function", t.getTriggeredFunction().getId());
								body.put("bot", b.getName());
								// atts.put(vle.getEnvironmentSeparator(), entry.getKey());
								body.put("attributes", atts);

								headers = new HashMap<String, String>();
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
				chatMediator.sendMessageToChannel(channel, msgtext, "text");

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
	@Path("/sendMessageToRocketChat/{token}/{email}/{channel}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Trigger rocket chat message to given rocket chat channel")
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "triggered chat message") })
	public Response sendMessageToRocketChat(@PathParam("token") String token, @PathParam("email") String email, @PathParam("channel") String channel,
			String input) {
		try {
			RocketChatMediator chatMediator = new RocketChatMediator(token, database);
			System.out.println("rocket chat mediator initialized");

			try {
				JSONParser p = new JSONParser(0);
				JSONObject bodyInput = (JSONObject) p.parse(input);
				String msgtext = bodyInput.getAsString("msg");
				chatMediator.sendMessageToChannel(channel, msgtext, "text");
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
	@Path("/sendMessageToRocketChatCallback/{token}/{email}/{channel}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Trigger rocket chat message to given rocket chat channel")
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "triggered chat message") })
	public Response sendMessageToRocketChatCallback(@PathParam("token") String token, @PathParam("email") String email, @PathParam("channel") String channel,
			String input) {
		try {
			RocketChatMediator chatMediator = new RocketChatMediator(token, database);
			System.out.println("rocket chat mediator initialized");

			try {
				JSONParser p = new JSONParser();
				JSONObject bodyInput = (JSONObject) p.parse(input);
				String msgtext = bodyInput.getAsString("msg");
				chatMediator.sendMessageToChannelCallback(channel, msgtext, "text");
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
				JSONParser p = new JSONParser(0);
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

	// Should be an own resource.. this whole class needs refactoring.
	@Api(value = "RESTfulChat Resource")
	@SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.6.0", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "BSD 3-Clause License", url = "https://raw.githubusercontent.com/rwth-acis/las2peer-social-bot-manager-service/master/LICENSE")))
	@Path("/RESTfulChat")
	public static class RESTfulChatResource {
		SocialBotManagerService service = (SocialBotManagerService) Context.get().getService();
		static HashMap<String, JSONObject> userFileIds = new HashMap<String, JSONObject>();
		static HashMap<String, JSONObject> userMessage = new HashMap<String, JSONObject>();
		static HashMap<String, UserAgent> currentAgent = new HashMap<String, UserAgent>();
		// adding this temporarily to avoid needing to add stuff elsewhere
		static HashMap<String, String> emailToChannel = new HashMap<String, String>();

		// adding this temporarily to avoid needing to add stuff elsewhere
		static HashMap<String, Messenger> channelToMessenger = new HashMap<String, Messenger>();

		// adding new Hashmap with true if asynchron messages (with callback) should be used. 
		// based on the value if-else case which functionPath should be used whether the one with synchron messages or the one with asynchron messages. The service must be able to do both. 
		// 

		/**
		 * Handles RESTful chat requests.
		 *
		 * @param bot          the name of the bot to send the message to
		 * @param organization the organization to send the message to
		 * @param channel      the channel to send the message to
		 * @param input        the input message, in JSON format
		 * @return the response from the bot, in plain text format
		 */
		@POST
		@Path("/{bot}/{organization}/{channel}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Sends a message to the RESTful chat bot and channel", notes = "Provides a service to send a message to the specified bot and channel through a RESTful API endpoint")
		@ApiResponses(value = { @ApiResponse(code = 200, message = "Message successfully sent"),
				@ApiResponse(code = 500, message = "Internal server error"),
				@ApiResponse(code = 400, message = "Bad request, required parameters not provided") })
		public Response handleRESTfulChat(@PathParam("bot") String bot, @PathParam("organization") String organization,
				@PathParam("channel") String channel,
				String input) {
			RESTfulChatResponse answerMsg = null;
			String email = "";
			try {
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				email = userAgent.getEmail();
				emailToChannel.put(email, organization + "-" + channel);
			} catch (Exception e) {
				e.printStackTrace();
				for (String mail : emailToChannel.keySet()) {
					if (emailToChannel.get(mail).equals(organization + "-" + channel)) {
						email = mail;
						System.out.println("Get the email from channel:" + email);
						break;
					}
				}

				if (email.isBlank()){
					e.printStackTrace();
				}
			}

			try {
				Bot b = null;
				for (Bot botIterator : getConfig().getBots().values()) {
					if (botIterator.getName().equalsIgnoreCase(bot)) {
						b = botIterator;
					}
				}
				// there should be one or no bot available (we will remove instance in a later
				// version)
				if (b != null) {
					ArrayList<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
					boolean found = false;
					for (Messenger m : b.getMessengers().values()) {
						if (m.getChatMediator() != null && m.getChatMediator() instanceof RESTfulChatMediator) {

							RESTfulChatMediator chatMediator = (RESTfulChatMediator) m.getChatMediator();
							JSONParser p = new JSONParser();
							JSONObject bodyInput = (JSONObject) p.parse(input);
							String orgChannel = organization + "-" + channel;
							channelToMessenger.put(orgChannel, m);
							String msgtext = bodyInput.getAsString("message");
							if (msgtext == null || msgtext.equals("")) {
								return Response.status(Status.BAD_REQUEST).entity("No message provided.").build();
							}
							//Adds the user message to the message collector
							ChatMessage msg = new ChatMessage(orgChannel, orgChannel, msgtext);
							chatMediator.getMessageCollector().addMessage(msg);

							if (m.getAsync(orgChannel) && !userMessage.isEmpty()){
								String currMsg = userMessage.get(orgChannel).getAsString("AIResponse");
								msg.setCurrMessage(currMsg);
								System.out.println("Current Message set to AI Response");
							}

							System.out.println(msg);
							
							//hadnle messages checks the message collector for new user messages, handles them by determinig the intent and corresponding incoming message, setting the state
							m.handleMessages(messageInfos, b);
							answerMsg = chatMediator.getMessageForChannel(orgChannel);
							for (MessageInfo messageInfo : messageInfos) {
								try {
									/*
									 * ClientResponse result = client.sendRequest("POST",
									 * "SBFManager/bots/" + b.getName() + "/trigger/intent",
									 * gson.toJson(messageInfo),
									 * MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
									 */

									String functionPath = "";
									JSONObject body = new JSONObject();
									Boolean async = m.getAsync(orgChannel);
									System.out.println("Async value:"+async);
									BotAgent botAgent = getBotAgents().get(b.getName());
									ServiceFunction sf = new ServiceFunction();
									service.prepareRequestParameters(config, botAgent, messageInfo, functionPath, body,
											sf);
									if (body.containsKey("functionPath")) {
										functionPath = body.getAsString("functionPath");
										sf = b.getBotServiceFunctions().get(messageInfo.getTriggeredFunctionId());
										body.put("email", email);
										body.put("organization", organization);
										sf.setMessengerName(messageInfo.getMessengerName());

										performTrigger(config, sf, botAgent, functionPath, functionPath, body);

										RESTfulChatResponse oldAnswerMsg = answerMsg;

										IncomingMessage userState = m.getStateMap().get(orgChannel);
										System.out.println("user in state: "+userState.getIntentKeyword());
										String newResponse2 = userState.getResponse(new Random());
										System.out.println("new response2: " + newResponse2);

										answerMsg = chatMediator.getMessageForChannel(orgChannel);
										answerMsg.setMessage(m.replaceVariables(orgChannel,newResponse2));
										if ((oldAnswerMsg.getMessage() != answerMsg.getMessage())
												|| (answerMsg.getMessage().contains(oldAnswerMsg.getMessage()))) {
										}
										answerMsg.setReqBody(body);
										if (body.containsKey("resBody") && ((JSONObject) body.get("resBody"))
												.containsKey("interactiveElements")) {
											List<Object> ils = (List<Object>) ((JSONObject) body.get("resBody"))
													.get("interactiveElements");
											answerMsg.setInteractiveElements(ils);
											;
										}
																			
									}
									System.out.println("Functionpath do not exist.");
								} catch (Exception e) {

								}
							}
							// chatMediator.sendMessageToChannel(orgChannel, "msgtext", new
							// HashMap<String,IncomingMessage>(), "text", null);

							found = true;
						}
					}
					if (!found) {
						return Response.status(Status.NOT_FOUND).entity("No RESTfulChat found for Bot " + bot + ".")
								.build();
					}
				} else {
					return Response.status(Status.NOT_FOUND).entity("Bot " + bot + " not found.").build();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Gson gson = new Gson();
			return Response.ok().entity(gson.toJson(answerMsg)).build();

		}

		private void performTrigger(BotConfiguration botConfig, ServiceFunction sf, BotAgent botAgent,
				String functionPath, String triggerUID,
				JSONObject triggeredBody) throws AgentNotFoundException, AgentOperationFailedException {
			if (sf.getActionType().equals(ActionType.SERVICE) || sf.getActionType().equals(ActionType.OPENAPI)) {
				System.out.println(triggeredBody);
				String userId = triggeredBody.getAsString("user");
				Bot bot = botConfig.getBots().get(botAgent.getIdentifier());
				String messengerID = sf.getMessengerName();
				String email = triggeredBody.getAsString("email");
				String msg = triggeredBody.getAsString("msg");
				ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
				Messenger m = bot.getMessenger(messengerID);
				HashMap<String, String> headers = new HashMap<String, String>();
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				try {
					File f = null;
					if (triggeredBody.containsKey("fileBody")) {
						byte[] decodedBytes = java.util.Base64.getDecoder()
								.decode(triggeredBody.getAsString("fileBody"));
						f = new File(triggeredBody.getAsString("fileName") + "."
								+ triggeredBody.getAsString("fileType"));
						/*
						 * if(fileType.equals("")){
						 * file = new File(fileName);
						 * }
						 */
						try {
							FileUtils.writeByteArrayToFile(f, decodedBytes);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					String channel = triggeredBody.getAsString("channel");
					Client textClient = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
					functionPath = functionPath.replace("[channel]", channel);
					functionPath = functionPath.replace("[email]", email);
					functionPath = functionPath.replace("[organization]", triggeredBody.getAsString("organization"));
					functionPath = functionPath.replace("[intent]", triggeredBody.getAsString("intent"));
					functionPath = m.replaceVariables(channel, functionPath);
					System.out.println(functionPath);
					JSONObject entities = (JSONObject) triggeredBody.get("entities");
					for (String eName : entities.keySet()) {
						;
						if (functionPath.toLowerCase().contains("[" + eName + "]")) {
							functionPath = functionPath.replace("[" + eName + "]",
									((JSONObject) entities.get(eName)).get("value").toString());
						}
					}

					JSONObject form = (JSONObject) triggeredBody.get("form");
					System.out.println(form);

					// if asynchronous is true, add callback url to the formdata for the botaction
					if(m.getAsync(channel)){
						SocialBotManagerService sbfservice = (SocialBotManagerService) Context.get().getService();
						String addr = sbfservice.webconnectorUrl + "/sbfmanager/RESTfulChat/" + bot.getName() + "/" + triggeredBody.getAsString("organization") + "/" + triggeredBody.getAsString("channel").split("-")[1];
						form.put("sbfmUrl", addr);
						triggeredBody.put("form", form);
						System.out.println("Async triggeredbody:" + triggeredBody.toJSONString());
						userMessage.put(channel, triggeredBody);
					}

					FormDataMultiPart mp = new FormDataMultiPart();
					mp.field("msg", msg, MediaType.APPLICATION_JSON_TYPE);
					String queryParams = "?";
					if (form != null) {
						for (String key : form.keySet()) {
							if (sf.getHttpMethod().equals("get")) {
								if (form.getAsString(key).equals("[channel]")) {
									queryParams += key + "=" + channel + "&";
								} else if (form.getAsString(key).equals("[email]")) {
									queryParams += key + "=" + email + "&";
								} else if (form.getAsString(key).equals("[organization]")) {
									queryParams += key + "=" + triggeredBody.getAsString("organization") + "&";
								} else {
									queryParams += key + "=" + form.getAsString(key) + "&";
								}
							} else {
								if (form.getAsString(key).equals("[channel]")) {
									mp = mp.field(key, channel);
								} else if (form.getAsString(key).equals("[email]")) {
									mp = mp.field(key, email);
								} else if (form.getAsString(key).equals("[organization]")) {
									mp = mp.field(key, triggeredBody.get("organization").toString());
								} else if (form.getAsString(key).contains("[")) {
									for (String eName : entities.keySet()) {
										if (form.getAsString(key).toLowerCase().contains(eName)) {
											mp = mp.field(key,
													((JSONObject) entities.get(eName)).get("value").toString());
										}
									}
								} else {
									mp = mp.field(key, form.getAsString(key));
								}
							}
						}
					}

					System.out.println("Calling following URL: " + sf.getServiceName() + functionPath + queryParams);
					WebTarget target = textClient
							.target(sf.getServiceName() + functionPath + queryParams);
					if (f != null && f.exists()) {
						FileDataBodyPart filePart = new FileDataBodyPart("file", f);
						mp.bodyPart(filePart);
					}

					Response response = null;
					if (sf.getHttpMethod().equals("get")) {
						response = target.request().get();
					} else {
						response = target.request()
								.post(javax.ws.rs.client.Entity.entity(mp, mp.getMediaType()));
						System.out.println("Response Code:" + response.getStatus());
						System.out.println("Response Entitiy:" + response.getEntity());
					}

					String test = response.readEntity(String.class);
					System.out.println("Response Text:" + test);
					mp.close();
					try {
						java.nio.file.Files.deleteIfExists(Paths.get(triggeredBody.getAsString("fileName") + "."
								+ triggeredBody.getAsString("fileType")));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/*
					 * triggeredBody = new JSONObject();
					 * triggeredBody.put("channel", channel);
					 * triggeredBody.put("text", test);
					 */
					JSONObject jsonResponse = (JSONObject) parser.parse(test);
					for (String key : jsonResponse.keySet()) {
						bot.getMessenger(messengerID).addVariable(channel, key, jsonResponse.getAsString(key));
					}
					
					triggeredBody.put("resBody", jsonResponse);
					if (jsonResponse.get("closeContext") == null || Boolean.valueOf(jsonResponse.getAsString("closeContext"))) {
						System.out.println("Closed Context");
						bot.getMessenger(messengerID).setContextToBasic(channel,
							userId);
					} else if (Boolean.valueOf(jsonResponse.getAsString("closeContext")) == false) {
						System.out.println("Keep Context open");
						bot.getMessenger(messengerID).restoreConversationState(channel);
					}
					// this.service.triggerChat(chat, triggeredBody);
					return;

				} catch (Exception e) {
					System.out.println(e.getMessage());

				}
			}
		}

		/**
		 * Handle RESTful chat file.
		 *
		 * @param bot                 the bot name
		 * @param organization        the organization name
		 * @param channel             the channel name
		 * @param uploadedInputStream the uploaded input stream
		 * @param fileDetail          the file detail
		 * @return the response
		 */
		@POST
		@Path("/{bot}/{organization}/{channel}/file")
		@Consumes(MediaType.MULTIPART_FORM_DATA)
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Uploads a file to the RESTful chat bot and channel", notes = "Provides a service to upload a file to the specified bot and channel through a RESTful API endpoint")
		@ApiResponses(value = { @ApiResponse(code = 200, message = "File successfully uploaded"),
				@ApiResponse(code = 500, message = "Internal server error"),
				@ApiResponse(code = 400, message = "Bad request, required parameters not provided") })
		public Response handleRESTfulChatFile(@PathParam("bot") String bot,
				@PathParam("organization") String organization, @PathParam("channel") String channel,
				@FormDataParam("file") InputStream uploadedInputStream,
				@FormDataParam("file") FormDataContentDisposition fileDetail) {
			RESTfulChatResponse answerMsg = new RESTfulChatResponse("");
			try {
				Bot b = null;
				String addr = this.service.webconnectorUrl;
				for (Bot botIterator : getConfig().getBots().values()) {
					if (botIterator.getName().equalsIgnoreCase(bot)) {
						b = botIterator;
					}
				}
				// there should be one or no bot available (we will remove instance in a later
				// version)
				if (b != null) {
					ArrayList<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
					boolean found = false;
					boolean err = false;
					for (Messenger m : b.getMessengers().values()) {
						if (m.getChatMediator() != null && m.getChatMediator() instanceof RESTfulChatMediator) {
							byte[] bytes = toBytes(uploadedInputStream);
							String encoded = Base64.getEncoder().encodeToString(bytes);
							RESTfulChatMediator chatMediator = (RESTfulChatMediator) m.getChatMediator();
							String fname = fileDetail.getFileName();
							String ftype = getFileType(uploadedInputStream);

							RESTfulChatMessageCollector msgcollector = (RESTfulChatMessageCollector) chatMediator
									.getMessageCollector();
							String orgChannel = organization + "-" + channel;
							msgcollector.handle(encoded, fname, ftype, orgChannel);
							m.handleMessages(messageInfos, b);
							answerMsg = chatMediator.getMessageForChannel(orgChannel);
							String email = "";
							for (MessageInfo messageInfo : messageInfos) {
								try {
									try {
										UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
										email = userAgent.getEmail();
										emailToChannel.put(email, organization + "-" + channel);
									} catch (Exception e) {
										e.printStackTrace();
										for (String mail : emailToChannel.keySet()) {
											if (emailToChannel.get(mail).equals(organization + "-" + channel)) {
												email = mail;
												break;
											}
										}
									}
									/*
									 * ClientResponse result = client.sendRequest("POST",
									 * "SBFManager/bots/" + b.getName() + "/trigger/intent",
									 * gson.toJson(messageInfo),
									 * MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
									 */

									String functionPath = "";
									JSONObject body = new JSONObject();
									BotAgent botAgent = getBotAgents().get(b.getName());
									ServiceFunction sf = new ServiceFunction();
									this.service.prepareRequestParameters(config, botAgent, messageInfo, functionPath,
											body,
											sf);
									if (body.containsKey("functionPath")) {
										functionPath = body.getAsString("functionPath");
										sf = b.getBotServiceFunctions().get(messageInfo.getTriggeredFunctionId());
										body.put("email", email);
										body.put("organization", organization);
										sf.setMessengerName(messageInfo.getMessengerName());
										performTrigger(config, sf, botAgent, functionPath, functionPath, body);
										RESTfulChatResponse oldAnswerMsg = answerMsg;
										answerMsg = chatMediator.getMessageForChannel(orgChannel);
										body.remove("fileBody");
										for (String key : body.keySet()) {
											if (body.get(key) != null && body.get(key).toString().equals("[channel]")) {
												body.put(key, messageInfo.getMessage().getChannel());
											}
											if (body.get(key) != null && body.get(key).toString().contains("[")
													&& body.get(key).toString().contains("]") && !key.equals("form")) {
												body.put(key, m.replaceVariables(orgChannel, body.get(key).toString()));
											}
										}
										if (oldAnswerMsg.getMessage() != answerMsg.getMessage()) {
											// answerMsg.setMessage(oldAnswerMsg.getMessage() + "\n" +
											// answerMsg.getMessage());
										}
										answerMsg.setReqBody(body);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							found = true;

							// start to perform bot action in case it is triggered

							/*
							 * MiniClient client = new MiniClient();
							 * System.out.println("Addr: "+addr);
							 * client.setConnectorEndpoint(addr);
							 * 
							 * HashMap<String, String> headers = new HashMap<String, String>();
							 * for (MessageInfo mInfo : messageInfos) {
							 * try {
							 * Gson gson = new Gson();
							 * ClientResponse result = client.sendRequest("POST",
							 * "SBFManager/bots/" + mInfo.getBotName() + "/trigger/intent",
							 * gson.toJson(mInfo),
							 * MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
							 * System.out.println(result.getResponse());
							 * } catch (Exception e) {
							 * e.printStackTrace();
							 * }
							 * }
							 */
						}
					}
					if (!found) {
						return Response.status(Status.NOT_FOUND).entity("No RESTfulChat found for Bot " + bot + ".")
								.build();
					}
				} else {
					return Response.status(Status.NOT_FOUND).entity("Bot " + bot + " not found.").build();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			Gson gson = new Gson();
			return Response.ok().entity(gson.toJson(answerMsg)).build();
		}

		@GET
		@Path("/{bot}/{organization}/{channel}/file/{fileId}")
		@Produces(MediaType.APPLICATION_OCTET_STREAM)
		@ApiOperation(value = "Download file", produces = MediaType.APPLICATION_OCTET_STREAM)
		@ApiResponses(value = {
				@ApiResponse(code = 200, message = "File downloaded successfully"),
				@ApiResponse(code = 404, message = "File not found"),
				@ApiResponse(code = 500, message = "Internal server error") })
		public Response getRESTfulChatFile(@PathParam("bot") String bot, @PathParam("organization") String organization,
				@PathParam("channel") String channel, @PathParam("fileId") String fileId) {
			RESTfulChatResponse answerMsg = null;
			try {
				String path = bot + organization + channel + "-" + fileId;

				CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
				CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
						pojoCodecRegistry);
				MongoClientSettings settings = MongoClientSettings.builder()
						.uuidRepresentation(UuidRepresentation.STANDARD)
						.applyConnectionString(new ConnectionString(this.service.mongoUri))
						.codecRegistry(codecRegistry)
						.build();

				// Create a new client and connect to the server
				MongoClient mongoClient = MongoClients.create(settings);

				try {
					MongoDatabase database = mongoClient.getDatabase(this.service.mongoDB);
					GridFSBucket gridFSBucket = GridFSBuckets.create(database, "files");
					gridFSBucket.find(Filters.empty());
					ObjectId oId = new ObjectId(fileId);
					BsonObjectId bId = new BsonObjectId(oId);
					GridFSFile file = gridFSBucket.find(Filters.eq(bId)).first();
					if (file == null) {
						return Response.status(Response.Status.NOT_FOUND)
								.entity("File with ID " + fileId + " not found").build();
					}
					String extension = "";
					if (file.getFilename().contains("json")) {
						extension = ".json";
					} else if (file.getFilename().contains("pdf")) {
						extension = ".pdf";
					}
					Response.ResponseBuilder response = Response.ok(file.getFilename() + extension);
					response.header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");
					if (file.getFilename().contains("json")) {
						response.header("Content-Type", "application/json");
					} else if (file.getFilename().contains("pdf")) {
						response.header("Content-Type", "application/pdf");
					}
					// Download the file to a ByteArrayOutputStream
					String contentType = "";
					if (file.getFilename().contains("json")) {
						contentType = "application/json";
					} else if (file.getFilename().contains("pdf")) {
						contentType = "application/pdf";
					}
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					gridFSBucket.downloadToStream(file.getObjectId(), baos);
					return Response.ok(baos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
							.header("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"")
							.build();
				} catch (MongoException me) {
					System.err.println(me);
				} finally {
					// Close the MongoDB client
					mongoClient.close();
				}

				File file = new File(path);
				if (!file.exists()) {
					return Response.status(Status.NOT_FOUND).entity("File not found.").build();
				}
				String contentType = "";
				if (path.contains("json")) {
					contentType = "application/json";
				} else if (path.contains("pdf")) {
					contentType = "application/pdf";
				}
				return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
						.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
						.header("Content-Type", contentType)
						.build();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return Response.status(Status.BAD_REQUEST).entity("Something went wrong.").build();
		}
		@GET
		@Path("/{bot}/{organization}/{channel}/{label1}/{label2}/files")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiOperation(value = "Download file", produces = MediaType.APPLICATION_OCTET_STREAM)
		@ApiResponses(value = {
				@ApiResponse(code = 200, message = "File downloaded successfully"),
				@ApiResponse(code = 404, message = "File not found"),
				@ApiResponse(code = 500, message = "Internal server error") })
		public Response getRESTfulChatFileIds(@PathParam("bot") String bot,
				@PathParam("organization") String organization,
				@PathParam("channel") String channel) {
			if (userFileIds.containsKey(organization + "-" + channel)) {
				JSONObject r = userFileIds.get(organization + "-" + channel);
				userFileIds.remove(organization + "-" + channel);
				if (r.containsKey("error")) {
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity(r).build();
				}
				JSONObject input = new JSONObject();
				input.put("message", "!files");
				Response response = handleRESTfulChat(bot, organization, channel, input.toString());
				JSONParser p = new JSONParser(0);
				try {
					JSONObject answer = (JSONObject) p.parse(response.getEntity().toString());
					answer.put("files", r);
					return Response.status(Status.OK).entity(answer.toString()).build();
				} catch (Exception e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity(r).build();
				}

			} else {
				return Response.status(Status.NOT_FOUND).entity(new JSONObject()).build();
			}
		}

		@POST
		@Path("/{channel}/{label1}/{label2}/files")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiOperation(value = "Download file", produces = MediaType.APPLICATION_OCTET_STREAM)
		@ApiResponses(value = {
				@ApiResponse(code = 200, message = "File downloaded successfully"),
				@ApiResponse(code = 404, message = "File not found"),
				@ApiResponse(code = 500, message = "Internal server error") })
		public Response updateRESTfulChatFileIds(
				@PathParam("channel") String channel, @FormDataParam("files") byte[] files) {
			String content = new String(files);
			if (emailToChannel.containsKey(channel)) {
				// kinda abusing code here
				channel = emailToChannel.get(channel);
			}
			if (content.equals(null)) {
				return Response.status(Status.BAD_REQUEST).entity("Something went wrong.").build();
			}
			try {
				JSONObject o = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).parse(content);
				userFileIds.put(channel, o);
				Messenger m = channelToMessenger.get(channel);
				if (m == null) {
					m = channelToMessenger.get(channel.split("-")[1]);
				}
				for (String key : o.keySet()) {
					m.addVariable(channel, key, o.getAsString(key));
				}
				return Response.status(Status.BAD_REQUEST).entity("cool").build();
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(new JSONObject()).build();
			}
		}

		private String getFileType(InputStream uploadedInputStream) throws IOException {
			Tika tika = new Tika();
			return tika.detect(uploadedInputStream);
		}

		private byte[] toBytes(InputStream uploadedInputStream) throws IOException {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = uploadedInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			byte[] bytes = outputStream.toByteArray();
			return bytes;
		}

		@GET
		@Path("/{bot}/{organization}/{channel}/AsyncMessage")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Response successful"),
				@ApiResponse(code = 404, message = "Response not found"),
				@ApiResponse(code = 500, message = "Internal server error") })
		public Response getRESTfulChatBiwibot(@PathParam("bot") String bot,
					@PathParam("organization") String organization,
					@PathParam("channel") String channel) {
					
			JSONObject response = new JSONObject();
			System.out.println("Called GET AsyncMessage function");
			System.out.println(userMessage.containsKey(organization+"-"+channel));
			if (userMessage.containsKey(organization + "-" + channel)) {
				JSONObject ch = userMessage.get(organization + "-" + channel);
				
				if (ch.containsKey("error")) {
					System.out.println("Error occurred");
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ch).build();
				}

				if (ch.containsKey("AIResponse") && !ch.getAsString("AIResponse").startsWith("Bitte warte")) {
					JSONObject input = new JSONObject();
					input.put("message", "!default");
					Response responseService = handleRESTfulChat(bot, organization, channel, input.toString());
					JSONParser p = new JSONParser(0);
					try {
						JSONObject answer = (JSONObject) p.parse(responseService.getEntity().toString());
						answer.put("AIResponse", answer.getAsString("message"));
						System.out.println(answer);
						userMessage.remove(organization + "-" + channel);
						return Response.status(Status.OK).entity(answer.toString()).build();
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error after handle input.");
						return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ch).build();
					}
				} else {
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ch).build();
				} 
				
			} else {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new JSONObject()).build();
			}
		}

		@POST
		@Path("/{bot}/{organization}/{channel}/AsyncMessage")
		@Produces(MediaType.TEXT_PLAIN)
		@ApiResponses(value = {
				@ApiResponse(code = 200, message = "Response successful"),
				@ApiResponse(code = 404, message = "Response not found"),
				@ApiResponse(code = 500, message = "Internal server error") })
		public Response updateRESTfulChatResponse(@PathParam("bot") String bot,
				@PathParam("organization") String organization, 
				@PathParam("channel") String channel, 
				String response) throws ParseException {
		
			String orgaChannel = organization + "-" + channel;
			Messenger messenger = channelToMessenger.get(orgaChannel);
			System.out.println("organChannel: " + orgaChannel);
			JSONObject o = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).parse(response);
			JSONObject input = new JSONObject();
			input.put("channel", orgaChannel);
			String key = o.keySet().toArray()[0].toString();

			if (o.getAsString(key).isEmpty()) {
				return Response.status(Status.BAD_REQUEST).entity("Something went wrong.").build();
			}
			
			if (o.getAsString(key).equals("!exit")) {
				input.put("message", "!exit");
				messenger.addVariable(orgaChannel, "closeContext", "true");
				handleRESTfulChat(bot, organization, channel, input.toString());
				return Response.status(Status.BAD_REQUEST).entity("ack").build();
			}

			try {	
				userMessage.put(orgaChannel, o);
				System.out.println("usermessage" + userMessage);
				
				if (messenger == null) {
					messenger = channelToMessenger.get(channel);
				}
				for (String keys : o.keySet()) {
					messenger.addVariable(orgaChannel, keys, o.getAsString(keys));
				}

				System.out.println("Variables added");
				return Response.status(Status.BAD_REQUEST).entity("ack").build();
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Status.BAD_REQUEST).entity(new JSONObject()).build();
			}
		}
	}

}
