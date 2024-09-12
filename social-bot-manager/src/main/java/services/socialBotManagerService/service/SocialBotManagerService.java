package services.socialBotManagerService.service;

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
// import javax.ws.rs.Consumes;
// import javax.ws.rs.DELETE;
// import javax.ws.rs.GET;
// import javax.ws.rs.POST;
// import javax.ws.rs.Path;
// import javax.ws.rs.PathParam;
// import javax.ws.rs.Produces;
// import javax.ws.rs.core.MediaType;
// import javax.ws.rs.core.Response;
// import javax.ws.rs.core.Response.Status;
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

// import i5.las2peer.api.Context;
// import i5.las2peer.api.ManualDeployment;
// import i5.las2peer.api.execution.InternalServiceException;
// import i5.las2peer.api.execution.ServiceAccessDeniedException;
// import i5.las2peer.api.execution.ServiceInvocationFailedException;
// import i5.las2peer.api.execution.ServiceMethodNotFoundException;
// import i5.las2peer.api.execution.ServiceNotAuthorizedException;
// import i5.las2peer.api.execution.ServiceNotAvailableException;
// import i5.las2peer.api.execution.ServiceNotFoundException;
// import i5.las2peer.api.logging.MonitoringEvent;
// import i5.las2peer.api.persistency.Envelope;
// import i5.las2peer.api.persistency.EnvelopeAccessDeniedException;
// import i5.las2peer.api.persistency.EnvelopeNotFoundException;
// import i5.las2peer.api.persistency.EnvelopeOperationFailedException;
// import i5.las2peer.api.security.AgentException;
// import i5.las2peer.api.security.AgentNotFoundException;
// import i5.las2peer.api.security.AgentOperationFailedException;
// import i5.las2peer.api.security.UserAgent;
// import i5.las2peer.connectors.webConnector.client.ClientResponse;
// import i5.las2peer.connectors.webConnector.client.MiniClient;
// import i5.las2peer.logging.L2pLogger;
// import i5.las2peer.logging.bot.BotMessage;
// import i5.las2peer.restMapper.RESTService;
// import i5.las2peer.restMapper.annotations.ServicePath;
// import i5.las2peer.security.BotAgent;
// import i5.las2peer.tools.CryptoException;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiResponse;
// import io.swagger.annotations.ApiResponses;
// import io.swagger.annotations.Contact;
// import io.swagger.annotations.Info;
// import io.swagger.annotations.License;
// import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import services.socialBotManagerService.chat.*;
import services.socialBotManagerService.chat.github.GitHubWebhookReceiver;
import services.socialBotManagerService.chat.xAPI.ChatStatement;
import services.socialBotManagerService.database.SQLDatabase;
import services.socialBotManagerService.database.SQLDatabaseType;
import services.socialBotManagerService.model.ActionType;
import services.socialBotManagerService.model.Bot;
import services.socialBotManagerService.model.BotConfiguration;
import services.socialBotManagerService.model.BotModel;
import services.socialBotManagerService.model.BotModelEdge;
import services.socialBotManagerService.model.BotModelNode;
import services.socialBotManagerService.model.BotModelNodeAttribute;
import services.socialBotManagerService.model.BotModelValue;
import services.socialBotManagerService.model.BotRoutine;
import services.socialBotManagerService.model.ConversationMessage;
import services.socialBotManagerService.model.IfThenBlock;
import services.socialBotManagerService.model.IncomingMessage;
import services.socialBotManagerService.model.MessageInfo;
import services.socialBotManagerService.model.Messenger;
import services.socialBotManagerService.model.ServiceFunction;
import services.socialBotManagerService.model.ServiceFunctionAttribute;
import services.socialBotManagerService.model.Trigger;
import services.socialBotManagerService.model.TriggerFunction;
import services.socialBotManagerService.nlu.Entity;
import services.socialBotManagerService.nlu.TrainingHelper;
import services.socialBotManagerService.parser.BotParser;
import services.socialBotManagerService.parser.ParseBotException;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.ConnectionString;
// import com.mongodb.MongoClientSettings;
// import com.mongodb.MongoException;
// import com.mongodb.client.MongoDatabase;
// import com.mongodb.client.gridfs.GridFSBucket;
// import com.mongodb.client.gridfs.GridFSBuckets;
// import com.mongodb.client.gridfs.model.GridFSFile;
// import com.mongodb.client.model.Filters;
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
import org.glassfish.jersey.client.ClientResponse;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * las2peer-SocialBotManager-Service
 *
 * A REST service that manages social bots in a las2peer network.
 *
 */
// @Api(value = "test")
// @SwaggerDefinition(info = @Info(title = "las2peer Bot Manager Service", version = "1.6.0", description = "A las2peer service for managing social bots.", termsOfService = "", contact = @Contact(name = "Alexander Tobias Neumann", url = "", email = "neumann@dbis.rwth-aachen.de"), license = @License(name = "BSD 3-Clause License", url = "https://raw.githubusercontent.com/rwth-acis/las2peer-social-bot-manager-service/master/LICENSE")))

// @ServicePath("/SBFManager")
// @ManualDeployment
@Service
public class SocialBotManagerService {

	private String databaseName;
	private int databaseTypeInt = 1; // See SQLDatabaseType for more information
	private SQLDatabaseType databaseType;
	private String databaseHost;
	private int databasePort;
	private String databaseUser;
	private String databasePassword;
	public SQLDatabase database; // The database instance to write to.
	public String webconnectorUrl = "http://localhost:8080"; // address of running webconnector
	public String webconnectorUrlStatic;
	private String restarterBotName; // name of restarterBot
	private static String restarterBotNameStatic;
	private String restarterBotPW; // PW of restarterBot
	private static String restarterBotPWStatic; // PW of restarterBot

	public String lrsAuthTokenStatic;
	private static String lrsURLStatic;

	private String lrsAuthToken;
	private String lrsURL;

	private String mongoHost;
	private String mongoUser;
	private String mongoPassword;
	private String mongoDB;
	private String mongoUri;
	private String mongoAuth = "admin";

	private String xapiUrl;
	private String xapiHomepage;

	private static final String ENVELOPE_MODEL = "SBF_MODELLIST";

	private static HashMap<String, Boolean> botIsActive = new HashMap<String, Boolean>();
	public static HashMap<String, String> rasaIntents = new HashMap<String, String>();
	private static HashMap<String, String> courseMap = null;

	private static BotConfiguration config;

	// private static HashMap<String, BotAgent> botAgents;
	private static final String botPass = "actingAgent";

	private static ScheduledExecutorService rt = null;

	private int BOT_ROUTINE_PERIOD = 5; // 1 second

	public TrainingHelper nluTrain = null;
	public Thread nluTrainThread = null;
	// private static final L2pLogger logger = L2pLogger.getInstance(SocialBotManagerService.class.getName());
	// private Context l2pcontext = null;
	// private BotAgent restarterBot = null;

	// public Context getL2pcontext() {
	// 	return l2pcontext;
	// }

	// public void setL2pcontext(Context l2pcontext) {
	// 	this.l2pcontext = l2pcontext;
	// }

	public SocialBotManagerService() throws Exception {
		super();
		// setFieldValues(); // This sets the values of the configuration file
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
		// if (getConfig() == null) {
		// 	setConfig(new BotConfiguration());
		// 	getConfig().setBotConfiguration(new HashMap<String, Bot>());
		// }
		// if (getBotAgents() == null) {
		// 	setBotAgents(new HashMap<String, BotAgent>());
		// }

		this.databaseType = SQLDatabaseType.getSQLDatabaseType(databaseTypeInt);
		this.database = new SQLDatabase(this.databaseType, this.databaseUser, this.databasePassword, this.databaseName,
				this.databaseHost, this.databasePort);
		try {
			Connection con = database.getDataSource().getConnection();
			con.close();
		} catch (SQLException e) {
			System.out.println("Failed to Connect: " + e.getMessage());
		}

		// // mongo db connection for exchanging files
		// mongoUri = "mongodb://" + mongoUser + ":" + mongoPassword + "@" + mongoHost + "/?authSource=" + mongoAuth;
		// // Construct a ServerApi instance using the ServerApi.builder() method
		// CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
		// CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
		// MongoClientSettings settings = MongoClientSettings.builder()
		// 		.uuidRepresentation(UuidRepresentation.STANDARD)
		// 		.applyConnectionString(new ConnectionString(mongoUri))
		// 		.codecRegistry(codecRegistry)
		// 		.build();

		// // Create a new client and connect to the server
		// MongoClient mongoClient = MongoClients.create(settings);
		// // Create a new client and connect to the server
		// try {
		// 	MongoDatabase database = mongoClient.getDatabase(mongoDB);
		// 	// Send a ping to confirm a successful connection
		// 	Bson command = new BsonDocument("ping", new BsonInt64(1));
		// 	Document commandResult = database.runCommand(command);
		// 	System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
		// } catch (MongoException me) {
		// 	System.err.println(me);
		// } catch (Exception e) {
		// 	System.err.println(e);
		// } finally {
		// 	mongoClient.close();
		// }

		// if (rt == null) {
		// 	rt = Executors.newSingleThreadScheduledExecutor();
		// 	rt.scheduleAtFixedRate(new RoutineThread(), 0, BOT_ROUTINE_PERIOD, TimeUnit.SECONDS);
		// }
		// L2pLogger.setGlobalConsoleLevel(Level.WARNING);
	}

	// @Override
	// protected void initResources() {
	// 	getResourceConfig().register(BotResource.class);
	// 	getResourceConfig().register(BotModelResource.class);
	// 	getResourceConfig().register(TrainingResource.class);
	// 	getResourceConfig().register(this);
	// 	getResourceConfig().register(GitHubWebhookReceiver.class);
	// 	getResourceConfig().register(RESTfulChatResource.class);
	// }

	/**
	 * Returns the custom message descriptions for the service.
	 */
	// @Override
	// public HashMap<String, String> getCustomMessageDescriptions() {
	// 	HashMap<String, String> descriptions = new HashMap<>();
	// 	descriptions.put(
	// 			"SERVICE_CUSTOM_MESSAGE_1",
	// 			"Monitors whenever a bot gets initialized");
	// 	descriptions.put(
	// 			"SERVICE_CUSTOM_MESSAGE_2",
	// 			"(deprecated) Was used to log the user messages.");
	// 	descriptions.put(
	// 			"SERVICE_CUSTOM_MESSAGE_3",
	// 			"Logs whenever a new bot gets created. " +
	// 					"| Key         | Description     					        |"
	// 					+ "|---------------|--------------------------------|"
	// 					+ "| botName       | Represents the bot name.       |"
	// 					+ "| agentId       | Represents the agent of the bot|");
	// 	descriptions.put(
	// 			"SERVICE_CUSTOM_MESSAGE_42",
	// 			"| Key         | Description                                                                                                 |"
	// 					+ "|-------------|-------------------------------------------------------------------------------------------------------------|"
	// 					+ "| task        | Represents the task being performed.                                                                         |"
	// 					+ "| email       | Represents the email address associated with the user.                                              |"
	// 					+ "| time        | Represents the time taken to perform the task, calculated as the difference between the current time and `start` time. |");
	// 	descriptions.put(
	// 			"SERVICE_CUSTOM_MESSAGE_80",
	// 			"Logs the whole json body of the request.");
	// 	return descriptions;
	// }

	public JSONObject createXAPIStatement(String userMail, String botName,
			String intent, String text, String channel)
			throws ParseException {
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject actor = new JSONObject();
		JSONObject verb = new JSONObject();
		JSONObject object = new JSONObject();
		JSONObject context = new JSONObject();

		Bot b = getConfig().getBot(botName);
		actor.put("objectType", "Agent");
		JSONObject account = new JSONObject();

		account.put("name", userMail);
		//für Restfulchat eher workbench homepage 
		//vom messenger abhängig die Homepage 
		if (b.getMessenger(ChatService.RESTful_Chat) != null) {
			account.put("homePage", xapiHomepage);
			verb = (JSONObject) p
			.parse(new String(
					"{'display':{'en-US':'sent_chat_message'},'id':'" + xapiUrl + "/definitions/mwb/verbs/sent_message'}"));
			object = (JSONObject) p
					.parse(new String("{'definition':{'interactionType':'other', 'name':{'en-US':'" + intent
							+ "'}, 'extensions': {'" + xapiUrl + "/definitions/mwb/object/course': {'id':"+ channel + "}}}, 'description':{'en-US':'" + intent
							+ "'}, 'id':'" + xapiUrl + "/definitions/chat/activities/message'},'type':'/chat/activities/message/"
							+ intent + "', 'objectType':'Activity', 'text':'"
							+ text + "'}"));
			context = (JSONObject) p.parse(new String(
					"{'extensions':{'" + xapiUrl + "/definitions/mwb/extensions/context/activity_data': {courseID + "
							+ channel
							+ ", 'intent':'" + intent +"'}}}"));
		} else if (b.getMessenger(ChatService.ROCKET_CHAT) != null){
			account.put("homePage", "https://chat.tech4comp.dbis.rwth-aachen.de");
			verb = (JSONObject) p
			.parse(new String(
					"{'display':{'en-US':'sent_chat_message'},'id':'" + xapiUrl + "/definitions/chat/verbs/sent'}"));
			object = (JSONObject) p
					.parse(new String("{'definition':{'interactionType':'other', 'name':{'en-US':'" + intent
							+ "'}, 'description':{'en-US':'" + intent
							+ "'}, 'id':'" + xapiUrl + "/definitions/chat/activities/message'},'type':'/chat/activities/message/"
							+ intent + "', 'objectType':'Activity', 'text':'"
							+ text + "'}"));
			context = (JSONObject) p.parse(new String(
					"{'extensions':{'" + xapiUrl + "/definitions/lms/activities/course': {courseID + '"
							+ channel
							+ "'}}}"));
		} else {
			account.put("homePage", "https://tech4comp.dbis.rwth-aachen.de");
			verb = (JSONObject) p
			.parse(new String(
					"{'display':{'en-US':'sent_chat_message'},'id':'" + xapiUrl + "/definitions/chat/verbs/sent'}"));
			object = (JSONObject) p
					.parse(new String("{'definition':{'interactionType':'other', 'name':{'en-US':'" + intent
							+ "'}, 'description':{'en-US':'" + intent
							+ "'}, 'id':'" + xapiUrl + "/definitions/chat/activities/message'},'type':'/chat/activities/message/"
							+ intent + "', 'objectType':'Activity', 'text':'"
							+ text + "'}"));
			context = (JSONObject) p.parse(new String(
					"{'extensions':{'" + xapiUrl + "/definitions/lms/activities/course': {courseID + '"
							+ channel
							+ "'}}}"));
		}

		actor.put("account", account);
		
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
			// logger.info(response.toString());

			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	// public void checkRoutineTrigger(BotConfiguration botConfig, JSONObject j, BotAgent botAgent, String botFunctionId,
	// 		JSONObject context) {
	// 	String botId = botAgent.getIdentifier();
	// 	Bot bot = botConfig.getBots().get(botId);
	// 	if (bot != null) {
	// 		System.out.println("Bot " + botAgent.getLoginName() + " triggered:");
	// 		ServiceFunction botFunction = bot.getBotServiceFunctions().get(botFunctionId);
	// 		String functionPath = "";
	// 		if (botFunction.getActionType().equals(ActionType.SERVICE))
	// 			functionPath = botFunction.getFunctionPath();
	// 		JSONObject body = new JSONObject();
	// 		HashMap<String, ServiceFunctionAttribute> attlist = new HashMap<String, ServiceFunctionAttribute>();

	// 		JSONObject triggerAttributes = (JSONObject) j.get("attributes");
	// 		for (ServiceFunctionAttribute sfa : botFunction.getAttributes()) {
	// 			formAttributes(botConfig, sfa, bot, body, functionPath, attlist, triggerAttributes);
	// 		}
	// 		performTrigger(botConfig, botFunction, botAgent, functionPath, "", body);
	// 	}
	// }

	// TODO: Use entity value, handle environment separator, handle other things
	// than static content
	public void performIntentTrigger(BotConfiguration botConfig, MessageInfo messageInfo)
			throws ParseBotException {
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
			performTrigger(botConfig, botFunction, functionPath, "", body);
		}
	}

	public void prepareRequestParameters(BotConfiguration botConfig, MessageInfo messageInfo,
			String functionPath, JSONObject body, ServiceFunction botFunction)
			throws ParseBotException{
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

	public void checkTriggerBot(BotConfiguration botConfig, JSONObject body, String triggerUID,
			String triggerFunctionName) throws ParseBotException {
		// String botId = botAgent.getIdentifier();

		Bot bot = botConfig.getBots().get(botId);
		if (bot != null 
		// && !(triggerUID.toLowerCase().equals(botAgent.getIdentifier().toLowerCase()))
		) {

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
						String user = body.get("user").toString();
						String channel = body.get("channel").toString();
						String email = body.get("email").toString();
						String convId = body.get("conversationId").toString();
						String intent = body.get("intent").toString();
						String contextOn = body.get("contextOn").toString();
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

						performTrigger(botConfig, triggeredFunction, functionPath, triggerUID, triggeredBody);
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
			JSONObject triggerAttributes) throws ParseBotException {
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
			source = triggerBody.get(triggerAttribute.getName()).toString();
		} else if (triggerAttributes.containsKey(triggerAttribute.getName())) {
			source = triggerAttributes.get(triggerAttribute.getName()).toString();
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
			String replaceWith = triggerAttributes.get(mappedTo.getName()).toString();
			if (functionPath.contains("{" + sfa.getName() + "}")) {
				functionPath = functionPath.replace("{" + sfa.getName() + "}", replaceWith);
			} else {
				b.put(sfa.getName(), replaceWith);
			}
		} else if (triggerBody != null && triggerBody.containsKey(mappedTo.getName())) {
			String replaceWith = triggerBody.get(mappedTo.getName()).toString();
			if (functionPath.contains("{" + sfa.getName() + "}")) {
				functionPath = functionPath.replace("{" + sfa.getName() + "}", replaceWith);
			} else {
				b.put(sfa.getName(), replaceWith);
			}
		} else {
			// TODO Error could not map attributes
		}
	}

	private void performTrigger(BotConfiguration botConfig, ServiceFunction sf, String functionPath,
			String triggerUID,
			JSONObject triggeredBody) {
		JSONObject remarks = new JSONObject();
		String serviceEndpoint = "";
		remarks.put("user", encryptThisString(triggeredBody.get("user").toString()));
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
			// l2pcontext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, remarks.toJSONString(),
			// 		triggeredBody.get("conversationId").toString(), sf.getFunctionName(),
			// 		botAgent.getIdentifier().toString(), "bot", "start", System.currentTimeMillis());
			// client.setLogin("alice", "pwalice");
			// client.setLogin(botAgent.getLoginName(), botPass);
			String userId = triggeredBody.get("user").toString();
			Bot bot = botConfig.getBots().get(botAgent.getIdentifier());
			String messengerID = sf.getMessengerName();
			String channel = triggeredBody.get("channel").toString();
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
								.decode(triggeredBody.get("fileBody").toString());
						f = new File(
								triggeredBody.get("fileName").toString() + "." + triggeredBody.get("fileType").toString());
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
					functionPath = functionPath.replace("[intent]", triggeredBody.get("intent").toString());
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
								if (form.get(key).equals("[channel]")) {
									queryParams += key + "=" + channel + "&";
								} else if (form.get(key).equals("[email]")) {
									// queryParams+=key+"="+email+"&";
								} else if (form.get(key).equals("[organization]")) {
									queryParams += key + "=" + triggeredBody.get("organization") + "&";
								} else {
									queryParams += key + "=" + form.get(key) + "&";
								}
							} else {
								if (form.get(key).equals("[channel]")) {
									mp = mp.field(key, channel);
								} else if (form.get(key).equals("[email]")) {
									// mp = mp.field(key, email);
								} else if (form.get(key).equals("[organization]")) {
									mp = mp.field(key, triggeredBody.get("organization").toString());
								} else if (form.get(key).toString().contains("[")) {
									for (String eName : entities.keySet()) {
										if (form.get(key).toString().toLowerCase().contains(eName)) {
											mp = mp.field(key,
													((JSONObject) entities.get(eName)).get("value").toString());
										}
									}
								} else {
									mp = mp.field(key, form.get(key));
								}
							}
						}
					}
					for (String key : form.keySet()) {
						if (form.get(key).equals("[channel]")) {
							mp = mp.field(key, channel);
						} else {
							mp = mp.field(key, form.get(key).toString());
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
					ResponseEntity response = null;
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
								triggeredBody.get("fileName") + "." + triggeredBody.get("fileType")));
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
						bot.getMessenger(messengerID).addVariable(channel, key, jsonResponse.get(key).toString());
					}
					bot.getMessenger(messengerID).setContextToBasic(channel,
							userId);
					// triggerChat(chat, triggeredBody);
					// l2pcontext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, remarks.toJSONString(),
					// 		triggeredBody.get("conversationId").toString(), sf.getFunctionName(),
					// 		botAgent.getIdentifier().toString(), "bot", "complete", System.currentTimeMillis());
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

				if (Boolean.parseBoolean(triggeredBody.get("contextOn").toString())) {
					try {
						JSONObject response = (JSONObject) parser.parse(r.getResponse());
						for (String key : response.keySet()) {
							bot.getMessenger(messengerID).addVariable(channel, key, response.get(key).toString());
						}
						triggeredBody.put("text", response.get("text"));

						ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
						if (response.containsKey("fileBody")) {
							triggeredBody.put("fileBody", response.get("fileBody"));
							triggeredBody.put("fileName", response.get("fileName"));
							triggeredBody.put("fileType", response.get("fileType"));
						} else
							triggeredBody.remove("fileBody");
						if (response.containsKey("contactList")) {
							triggeredBody.put("contactList", response.get("contactList"));
						}
						if (response.containsKey("contactText")) {
							triggeredBody.put("contactText", response.get("contactText"));
						}
						if (response.containsKey("blocks")) {
							triggeredBody.put("blocks", response.get("blocks"));
							if (response.containsKey("updateBlock")) {
								triggeredBody.put("updateBlock", response.get("updateBlock"));
								if (response.containsKey("ts")) {
									triggeredBody.put("ts", response.get("ts"));
								}
							}
						}
						if (response.containsKey("multiFiles")) {
							for (Object o : (JSONArray) response.get("multiFiles")) {
								JSONObject jsonO = (JSONObject) o;
								jsonO.put("channel", triggeredBody.get("channel"));
								jsonO.put("email", triggeredBody.get("email"));
								triggerChat(chat, jsonO);
							}
						} else {
							// if the service function triggers another service function, do not trigger chat, add the response to the conversationpath
							if (!sf.getTrigger().isEmpty()){
								HashMap<String, Collection<ConversationMessage>> convMap = bot.getMessenger(messengerID).getConversationMap();
								Collection<ConversationMessage> conv = convMap.get(triggeredBody.get("channel"));
								ArrayList<ConversationMessage> convList = new ArrayList<>(conv);
								ConversationMessage botMsg = convList.get(convList.size() - 1);
								String convId = botMsg.getConversationId();
								triggeredBody.put("conversationId", convId);
								ConversationMessage newConvMsg = new ConversationMessage(convId, "assistant",
										triggeredBody.get("text").toString());
								conv.add(newConvMsg);
								bot.getMessenger(messengerID).updateConversationInConversationMap(triggeredBody.get("channel").toString(), conv);
								
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
							if (Boolean.parseBoolean(response.get("openai").toString())) {
								// add token count to body
								triggeredBody.put("tokens", response.get("tokens"));
								
								HashMap<String, Collection<ConversationMessage>> convMap = bot.getMessenger(messengerID)
										.getConversationMap();
								Collection<ConversationMessage> conv = convMap.get(triggeredBody.get("channel"));
								ArrayList<ConversationMessage> convList = new ArrayList<>(conv);
								ConversationMessage botMsg = convList.get(convList.size() - 1);
								String convId = botMsg.getConversationId();
								convList.remove(botMsg);
								ConversationMessage newConvMsg = new ConversationMessage(convId, "assistant",
										triggeredBody.get("text").toString());
								convList.add(newConvMsg);
								conv = convList;
								bot.getMessenger(messengerID)
										.updateConversationInConversationMap(triggeredBody.get("channel").toString(), conv);
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
						if (response.get("closeContext") == null || Boolean.valueOf(response.get("closeContext").toString())) {
							System.out.println("Closed Context");
							bot.getMessenger(messengerID).setContextToBasic(triggeredBody.get("channel").toString(),
								triggeredBody.get("user").toString());
						} else if (Boolean.valueOf(response.get("closeContext").toString()) == false) {
							System.out.println("Keep Context open");
							bot.getMessenger(messengerID).restoreConversationState(triggeredBody.get("channel").toString());
						}
						
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println("Response from request is null");
			}
			// l2pcontext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, remarks.toJSONString(),
					// triggeredBody.get("conversationId").toString(), sf.getFunctionName(),
					// botAgent.getIdentifier().toString(), "bot", "complete", System.currentTimeMillis());
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

		String text = body.get("text").toString();
		String blocks = body.get("blocks").toString();
		String channel = null;
		String user = "";
		JSONObject monitorEvent42 = new JSONObject();
		final long start = System.currentTimeMillis();
		monitorEvent42.put("task", "Send message");

		if (body.containsKey("contactList")) {
			// Send normal message to users on contactlist
			String email = body.get("contactList").toString();
			monitorEvent42.put("email", email);
			System.out.println("Goes to pick channel(s) by provided email(s)");
			String[] emailArray = email.split(",");

			if (body.containsKey("contactText")) {
				// specific text at position 1 should be sent to person on contactlist at pos 1
				String ctext = body.get("contactText").toString();
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
				channel = body.get("channel").toString();
			} else if (body.containsKey("email")) {
				email = body.get("email").toString();
				channel = chat.getChannelByEmail(email);
			}
			chat.sendMessageToChannel(channel, "ContactList contacted.", "text");

		} else {
			if (body.containsKey("channel")) {
				channel = body.get("channel").toString();
			} else if (body.containsKey("email")) {
				String email = body.get("email").toString();
				monitorEvent42.put("email", email);
				channel = chat.getChannelByEmail(email);
			}
			if (text != null && !body.containsKey("fileBody")) {
				chat.sendMessageToChannel(channel, text, "text");
			}
			if (body.containsKey("blocks")) {
				System.out.println("Body has blocks");
				if (body.containsKey("updateBlock") && Boolean.parseBoolean(body.get("updateBlock").toString())) {
					if (body.containsKey("ts")) {
						System.out.println("A block would be updated");
						chat.updateBlocksMessageToChannel(channel, blocks, chat.getAuthToken(), body.get("ts").toString());
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
				chat.sendFileMessageToChannel(channel, body.get("fileBody").toString(), body.get("fileName").toString(),
						body.get("fileType").toString(), text);
			}
			monitorEvent42.put("time", System.currentTimeMillis() - start);
		}
		// if (l2pcontext != null) {
		// 	l2pcontext.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_42, monitorEvent42.toString());
		// }
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
			courseMap.put(key, map.get(key).toString());
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
						.get(xapiUrl + "/definitions/lms/activities/course");
				String courseid = courseInfo.get("courseid").toString();

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

	public BotConfiguration getConfig() {
		return config;
	}

	public void setConfig(BotConfiguration config) {
		SocialBotManagerService.config = config;
	}

	// public HashMap<String, BotAgent> getBotAgents() {
	// 	return botAgents;
	// }

	// public void setBotAgents(HashMap<String, BotAgent> botAgents) {
	// 	SocialBotManagerService.botAgents = botAgents;
	// }

	// private class RoutineThread implements Runnable {
	// 	@Override
	// 	public void run() {

	// 		// System.out.println("bob is " + restarterBot);

	// 		if (restarterBot == null) {
	// 			MiniClient clientRestart = new MiniClient();
	// 			clientRestart.setConnectorEndpoint(webconnectorUrl);
	// 			clientRestart.setLogin("alice", "pwalice");
	// 			HashMap<String, String> headers = new HashMap<String, String>();
	// 			try {
	// 				if (restarterBotName != null && restarterBotPW != null && !restarterBotName.equals("")
	// 						&& !restarterBotPW.equals("")) {
	// 					ClientResponse result2 = clientRestart.sendRequest("GET", "SBFManager/bots/restart", "",
	// 							headers);
	// 					if (result2 != null) {
	// 						restarterBot = BotAgent.createBotAgent("restarterBot");
	// 					}
	// 				} else {
	// 					restarterBot = BotAgent.createBotAgent("restarterBot");
	// 				}
	// 			} catch (Exception e) {
	// 				restarterBot = null;
	// 				e.printStackTrace();
	// 			}

	// 		}
	// 		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	// 		SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
	// 		Gson gson = new Gson();
	// 		for (Bot bot : getConfig().getBots().values()) {
	// 			ArrayList<MessageInfo> messageInfos = new ArrayList<MessageInfo>();
	// 			for (MessageInfo m : messageInfos) {
	// 				ChatStatement chatStatement = ChatStatement.generate(m.getMessage().getUser(), m.getBotName(),
	// 						m.getMessage().getText(), m.getMessage().getTime(), m.getMessage().getDomain(), xapiUrl);
	// 				String chatStatementJSON = gson.toJson(chatStatement);
	// 				// l2pcontext.monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2,
	// 				// chatStatementJSON);
	// 			}
	// 			bot.handleMessages(messageInfos);

	// 			// TODO: Handle multiple environments (maybe?)

	// 			MiniClient client = new MiniClient();
	// 			client.setConnectorEndpoint(webconnectorUrl);

	// 			HashMap<String, String> headers = new HashMap<String, String>();
	// 			for (MessageInfo m : messageInfos) {
	// 				try {
	// 					ClientResponse result = client.sendRequest("POST",
	// 							"SBFManager/bots/" + m.getBotName() + "/trigger/intent", gson.toJson(m),
	// 							MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
	// 				} catch (Exception e) {
	// 					e.printStackTrace();
	// 				}
	// 			}

	// 			for (BotRoutine r : bot.getRoutines().values()) {
	// 				// current time
	// 				Calendar c = Calendar.getInstance();
	// 				long d1 = c.getTime().getTime();
	// 				// last time updated
	// 				long d2 = r.getLastUpdate();

	// 				long diffInMillies = d1 - d2;

	// 				int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

	// 				boolean trigger = false;
	// 				long min = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
	// 				if (r.getInterval().equals("Minute")) {
	// 					if (min >= Integer.parseInt(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				}
	// 				if (r.getInterval().equals("Hour")) {
	// 					long hour = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	// 					if (hour >= Integer.parseInt(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				}
	// 				if (r.getInterval().equals("Day")) {
	// 					long day = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	// 					if (day >= Integer.parseInt(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				}
	// 				if (r.getInterval().equals("Month")) {
	// 					long day = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
	// 					// TODO
	// 					day = day / 28;
	// 					if (day >= Integer.parseInt(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				} else if (r.getInterval().equals("Working days") && dayOfWeek != Calendar.SATURDAY
	// 						&& dayOfWeek != Calendar.SUNDAY) {
	// 					if (min >= 1 && df2.format(d1).equals(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				} else if (r.getInterval().equals("Weekend")
	// 						&& (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)) {
	// 					if (min >= 1 && df2.format(d1).equals(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				} else if (r.getInterval().equals("Every day")) {
	// 					if (min >= 1 && df2.format(d1).equals(r.getTime())) {
	// 						trigger = true;
	// 						r.setLastUpdate(d1);
	// 					}
	// 				}
	// 				if (trigger) {
	// 					for (Bot b : getConfig().getBots().values()) {
	// 						HashMap<String, Boolean> activeBots = b.getActive();
	// 						HashSet<Trigger> tList = r.getTrigger();
	// 						for (Trigger t : tList) {
	// 							// for (Entry<String, Boolean> entry : activeBots.entrySet()) {
	// 							// If bot is active
	// 							// if (entry.getValue()) {

	// 							JSONObject body = new JSONObject();
	// 							body.put("serviceAlias", ""); // TODO

	// 							JSONObject atts = new JSONObject();

	// 							body.put("function", t.getTriggeredFunction().getId());
	// 							body.put("bot", b.getName());
	// 							// atts.put(vle.getEnvironmentSeparator(), entry.getKey());
	// 							body.put("attributes", atts);

	// 							headers = new HashMap<String, String>();
	// 							String path = "SBFManager/bots/" + b.getName() + "/trigger/routine";
	// 							try {
	// 								path = "SBFManager/bots/" + URLEncoder.encode(b.getName(), "UTF-8")
	// 										+ "/trigger/routine";
	// 							} catch (UnsupportedEncodingException e) {
	// 								// TODO Auto-generated catch block
	// 								e.printStackTrace();
	// 							}
	// 							ClientResponse result = client.sendRequest("POST", path, body.toJSONString(),
	// 									MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, headers);
	// 							// }
	// 						}
	// 					}
	// 				}

	// 			}
	// 		}

	// 	}

	// }

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

}
