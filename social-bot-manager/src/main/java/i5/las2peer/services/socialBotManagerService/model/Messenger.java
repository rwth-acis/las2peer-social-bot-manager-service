package i5.las2peer.services.socialBotManagerService.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.Vector;
import javax.websocket.DeploymentException;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.services.socialBotManagerService.SocialBotManagerService;
import i5.las2peer.services.socialBotManagerService.chat.*;
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubAppHelper;
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubIssueMediator;
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubPRMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import net.minidev.json.JSONObject;

import java.util.UUID;

public class Messenger {
	private String name;

	// URL of the social bot manager service (used for setting up the webhook)
	private String url;

	private ChatMediator chatMediator;

	/**
	 * The messenger application provider this object corresponds to
	 */
	private ChatService chatService;

	/**
	 * Contains all IncomingMessages that are reachable from the start state
	 * Key: intent keyword
	 * Value: IncomingMessage object
	 */
	private HashMap<String, IncomingMessage> rootChildren;

	/**
	 * Used for keeping conversation state per channel
	 * Key: channel ID
	 * Value: current state of the conversation (last IncomingMessage)
	 * 
	 */
	private HashMap<String, Collection<ConversationMessage>> conversationMap;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;

	/**
	 * Used to determine if a new conversation was started. A new conversation
	 * starts
	 * if no followup messages are found for the current state and the context of
	 * the last function call is closed.
	 * (Note that the bot might stay infinitely long in the same state if the
	 * context is not closed)
	 */
	private HashMap<String, IncomingMessage> previousStateInConversation;
	private HashMap<String, IncomingMessage> previousStateInConversationBackup;

	/**
	 * Used for keeping remembering entities during conversation state per channel
	 * Key: channel ID
	 * Value: Collection of entities that were recognized during the conversation
	 */
	private HashMap<String, Collection<Entity>> recognizedEntities;
	/**
	 * Used for keeping context between assessment and non-assessment states
	 * Key: channel ID
	 * Value: current NLU model ID
	 */
	private HashMap<String, String> currentNluModel;
	/**
	 * Used to know to which Function the received intents/messages are to be sent
	 * Is additionally used to check if we are currently communicating with a
	 * service(if set, then yes otherwise no)
	 * Key: channel ID
	 * Value: current triggered function name
	 */
	private HashMap<String, String> triggeredFunction;
	/**
	 * Key: channel ID
	 * Value: number of times a default message was given out in a conversation
	 * state
	 */
	private HashMap<String, Integer> defaultAnswerCount;

	/**
	 * Used to store the current state of the conversation in case a command is
	 * triggered. Whenever this happens the current state is stored in this map and
	 * we jump into the command's conversation path. After the command is finished
	 * the state is restored.
	 * Key: channel ID
	 * Value: current state of the conversation (last IncomingMessage)
	 */
	private HashMap<String, IncomingMessage> storedSession;

	private HashMap<String, HashMap<String, String>> userVariables;

	private Context l2pContext;

	private Random random;

	private SQLDatabase db;

	public Messenger(String id, String chatService, String token, SQLDatabase database, Context l2pContext)
			throws IOException, DeploymentException, ParseBotException, AuthTokenException {
		this.l2pContext = l2pContext;

		// this.rasa = new RasaNlu(rasaUrl);
		// this.rasaAssessment = new RasaNlu(rasaAssessmentUrl);
		this.db = database;
		// Chat Mediator
		this.chatService = ChatService.fromString(chatService);
		switch (this.chatService) {
			case SLACK:
				this.chatMediator = new SlackChatMediator(token);
				break;
			case TELEGRAM:
				this.chatMediator = new TelegramChatMediator(token);
				String username = ((TelegramChatMediator) this.chatMediator).getBotName();
				if (username != null)
					this.name = username;
				break;
			case ROCKET_CHAT:
				this.chatMediator = new RocketChatMediator(token, database, new RasaNlu("rasaUrl"));
				break;
			case MOODLE_CHAT:
				this.chatMediator = new MoodleChatMediator(token);
				break;
			case MOODLE_FORUM:
				this.chatMediator = new MoodleForumMediator(token);
				break;
			case GITHUB_ISSUES:
				try {
					this.chatMediator = new GitHubIssueMediator(token);
				} catch (GitHubAppHelper.GitHubAppHelperException e) {
					throw new AuthTokenException(e.getMessage());
				}
				break;
			case GITHUB_PR:
				try {
					this.chatMediator = new GitHubPRMediator(token);
				} catch (GitHubAppHelper.GitHubAppHelperException e) {
					throw new AuthTokenException(e.getMessage());
				}
				break;
			case RESTful_Chat:
				this.chatMediator = new RESTfulChatMediator(token);

				break;
			default:
				throw new ParseBotException("Unimplemented chat service: " + chatService);
		}

		this.name = id;
		this.rootChildren = new HashMap<String, IncomingMessage>();
		this.conversationMap = new HashMap<String, Collection<ConversationMessage>>();
		this.stateMap = new HashMap<String, IncomingMessage>();
		this.recognizedEntities = new HashMap<String, Collection<Entity>>();
		this.random = new Random();
		// Initialize the assessment setup
		this.currentNluModel = new HashMap<String, String>();
		this.triggeredFunction = new HashMap<String, String>();
		this.defaultAnswerCount = new HashMap<String, Integer>();
		this.storedSession = new HashMap<String, IncomingMessage>();
		this.userVariables = new HashMap<String, HashMap<String, String>>();
		this.previousStateInConversation = new HashMap<>();
		this.previousStateInConversationBackup = new HashMap<>();
	}

	public String getName() {
		return name;
	}

	public ChatService getChatService() {
		return chatService;
	}

	public HashMap<String, Collection<ConversationMessage>> getConversationMap() {
		return conversationMap;
	}

	public HashMap<String, IncomingMessage> getStateMap() {
		return stateMap;
	}

	public void updateConversationInConversationMap(String channel, Collection<ConversationMessage> conversation) {
		conversationMap.put(channel, conversation);
	}

	public void addMessage(IncomingMessage msg) {
		if (msg.getIntentKeyword().contains("defaultX")) {
			this.rootChildren.put("defaultX", msg);
		} else
			this.rootChildren.put(msg.getIntentKeyword(), msg);
	}

	public HashMap<String, IncomingMessage> getRootChildren() {
		return this.rootChildren;
	}

	public ChatMediator getChatMediator() {
		return this.chatMediator;
	}

	public IncomingMessage checkDefault(IncomingMessage state, ChatMessage message) {
		if (this.rootChildren.get("defaultX") != null && Integer.valueOf(
				this.rootChildren.get("defaultX").getIntentKeyword().split("defaultX")[1]) > this.defaultAnswerCount
						.get(message.getChannel())) {
			IncomingMessage newState = this.rootChildren.get("defaultX");
			newState.followupMessages = state.followupMessages;
			state = newState;
			this.defaultAnswerCount.put(message.getChannel(), this.defaultAnswerCount.get(message.getChannel()) + 1);
		} else {
			state = this.rootChildren.get("default");
			this.defaultAnswerCount.put(message.getChannel(), 0);
		}
		return state;
	}

	private void addEntityToRecognizedList(String channel, Collection<Entity> entities) {

		Collection<Entity> recognizedEntitiesNew = recognizedEntities.get(channel);
		if (recognizedEntitiesNew != null) {
			for (Entity entity : entities) {
				recognizedEntitiesNew.add(entity);
			}
			recognizedEntities.put(channel, recognizedEntitiesNew);
		}
	}
	// set the context of the specified channel
	/*
	 * public void setContext(String channel, String contextName){
	 * context.put(channel, contextName);
	 * 
	 * }
	 */

	/*
	 * public String getEmail(String channel) throws IOException, SlackApiException
	 * { return chatMediator.getEmail(channel); };
	 */

	public void setContextToBasic(String channel, String userid) {

		triggeredFunction.remove(channel);
		IncomingMessage state = this.stateMap.get(channel);
		this.previousStateInConversationBackup.remove(channel);

		if (state == null) {
			this.previousStateInConversation.remove(channel);
			return;
		}

		if (state.getFollowingMessages() == null || state.getFollowingMessages().size() == 0) {
			// no other messages to follow
			System.out.println("No following messages");
			if (storedSession.containsKey(channel)) {
				stateMap.put(channel, storedSession.get(channel));
				state = storedSession.get(channel);
				storedSession.remove(channel);

				String response = state.getResponse(random);
				if (response != null && !response.equals("") && !state.getOpenAIEnhance()) {

					this.chatMediator.sendMessageToChannel(channel, replaceVariables(channel, response),
							state.getFollowingMessages(), state.getFollowupMessageType(), state, Optional.of(userid));
				}
			} else {
				System.out.println("No session state found");
				this.stateMap.remove(channel);
				this.previousStateInConversation.remove(channel);
			}
		} else {

			// If only message to be sent
			String response = state.getResponse(random);
			if (response != null && !response.equals("") && !state.freezeMessageSend) {
				this.chatMediator.sendMessageToChannel(channel, replaceVariables(channel, response),
						state.getFollowingMessages(), state.getFollowupMessageType(), state, Optional.of(userid));
				state.setFreezeMessageSend(false);
			}
			if (state.getFollowingMessages().size() == 0) {
				// no other messages to follow
				this.stateMap.remove(channel);
				this.previousStateInConversation.remove(channel);

			}
		}
	}

	public String getContext(String channel, String user) {
		return this.triggeredFunction.get(channel);
	}

	public HashMap<String, HashMap<String, String>> getUserVariables() {
		return userVariables;
	}

	public void setUserVariables(HashMap<String, HashMap<String, String>> userVariables) {
		this.userVariables = userVariables;
	}

	public void resetUserVariables(String channel) {
		this.userVariables.get(channel).clear();
	}

	public void addVariable(String channel, String key, String value) {
		HashMap<String, String> variables = this.getUserVariables().get(channel);
		variables.put(key, value);
		this.userVariables.put(channel, variables);
		System.out.println("Variable set for "+channel+". " + key + ":" + value);
	}

	public String replaceVariables(String channel, String text) {
		HashMap<String, String> variables = this.getUserVariables().get(channel);
		if (variables != null || !variables.isEmpty()) {
			for (String key : variables.keySet()) {
				System.out.println("Replace Variable for "+channel+", Key:" + key + ", value"+text);
				String composed = "[" + key + "]";
				text = text.replace(composed, variables.get(key));
			}
		} else {
			System.out.println("Replace Variables are null or empty.");
		}
		String split[] = text.split("\\[");
		for (int i = 1; i < split.length; i++) {

			String name = split[i].split("\\]")[0];
			String val = getEntityValue(channel, name);
			if (!val.equals("")) {
				String composed = "[" + name + "]";
				text = text.replace(composed, val);

			}
		}
		return text;
	}

	// Handles simple responses ("Chat Response") directly, logs all messages and
	// extracted intents into `messageInfos` for further processing later on.
	public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
		Vector<ChatMessage> newMessages = this.chatMediator.getMessages();
		for (ChatMessage message : newMessages) {
			try {
				// // If a channel/user pair still isn't assigned to a state, assign it to null
				// if (this.stateMap.get(message.getChannel()) == null) {
				// HashMap<String, IncomingMessage> initMap = new HashMap<String,
				// IncomingMessage>();
				// initMap.put(message.getUser(), null);
				// this.stateMap.put(message.getChannel(), initMap);
				// }

				// If a channel/user pair still isn't assigned to a NLU Model, assign it to the
				// Model 0
				if (this.currentNluModel.get(message.getChannel()) == null) {
					this.currentNluModel.put(message.getChannel(), "0");
				}

				// If channel/user pair is not assigned to a triggered function, assign it to
				// null
				// if (this.triggeredFunction.get(message.getChannel()) == null) {
				// HashMap<String, String> initMap = new HashMap<String, String>();
				// initMap.put(message.getUser(), null);
				// this.triggeredFunction.put(message.getChannel(), initMap);
				// }
				UUID conversationId = null;
				Boolean messageSent = Boolean.FALSE;
				String botMessage = "";
				if (!this.userVariables.containsKey(message.getChannel())) {
					System.out.println("Create empty hashmap for user variables: "+message.getChannel());
					this.userVariables.put(message.getChannel(), new HashMap<String, String>());
				}

				if (this.defaultAnswerCount.get(message.getChannel()) == null) {
					System.out.println("Set default answer count to 0 for " + message.getChannel());
					this.defaultAnswerCount.put(message.getChannel(), 0);
				}
				Intent intent = this.determineIntent(message, bot);
				
				System.out.println("determined intent:" + intent.getKeyword());
				try {
					safeEntities(message, bot, intent);

				} catch (Exception e) {
					e.printStackTrace();
				}

				String encryptedUser = SocialBotManagerService.encryptThisString(message.getUser());
				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(message.getChannel());
				if(state!=null){
					System.out.println("State: "+state.getIntentKeyword());
				}else{
					System.out.println("State: null");
				}
				JSONObject remarks = new JSONObject();
				remarks.put("user", encryptedUser);

				conversationId = this.determineConversationId(message.getChannel());

				remarks.put("in-service-context", this.triggeredFunction.containsKey(message.getChannel()));

				this.l2pContext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, remarks.toJSONString(),
						conversationId.toString(),
						intent.getKeyword(),
						bot.getId(), "bot", "start", System.currentTimeMillis());
				this.l2pContext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, remarks.toJSONString(),
						conversationId.toString(),
						intent.getKeyword(),
						bot.getId(), "bot", "complete", System.currentTimeMillis());

				// ________________ start modification of state machine__________________

				if (state == null && message.getText().startsWith("!")
						&& this.rootChildren.get(intent.getKeyword()) == null) {
					// in case a command is triggered which does not exist
					
					System.out.println("command triggered, but does not exist " + intent.getKeyword());
					this.chatMediator.sendMessageToChannel(message.getChannel(), "",
							new HashMap<String, IncomingMessage>(), "text", null);
					return;
				}

				if (state != null && message.getText().startsWith("!")
						&& !state.getFollowingMessages().keySet().contains(intent.getKeyword())) {
					if (this.rootChildren.get(intent.getKeyword()) == null) {
						// in case a command is triggered which does not exist
						System.out.println("command triggered, but does not exist " + intent.getKeyword());
						this.chatMediator.sendMessageToChannel(message.getChannel(), "",
								new HashMap<String, IncomingMessage>(), "text", null);
						return;
					}
					if (!intent.getKeyword().equals("exit")) {
						storedSession.put(message.getChannel(), state);
						state = null;
					}
				}
				if (state != null && message.getText().startsWith("!")
						&& storedSession.containsKey(message.getChannel())) {
					System.out.println("state not null, text starts with ! and session exists... Nothing happens here");
					// think about something else to do here
					// this.chatMediator.sendMessageToChannel(message.getChannel(),"Dont start
					// command inside command lol","text");
				}

				// TODO: Tweak this
				if (!this.triggeredFunction.containsKey(message.getChannel())) {
					// we are not in a function context
					if (intent.getKeyword().equals("exit")) {
						recognizedEntities.remove(message.getChannel());
						state = this.rootChildren.get(intent.getKeyword());
						this.updateConversationState(message.getChannel(), state, conversationId);
						if (storedSession.containsKey(message.getCurrMessage())) {
							storedSession.remove(message.getChannel());
						}
					} else if (intent.getConfidence() >= 0.40 || message.getFileName() != null) { // add file case to
																									// default if part

						if (state == null) {

							System.out.println("State is null");
							recognizedEntities.put(message.getChannel(), new ArrayList<Entity>());
							conversationMap.put(message.getChannel(), new ArrayList<ConversationMessage>());
							if (message.getFileName() != null) {
								
								System.out.println("check whether incoming message with intent expects file");
								// check whether incoming message with intent expects file or without intent,
								// such that
								// you can send a file regardless the intent
								if (this.rootChildren.get(intent.getKeyword()) != null
										&& this.rootChildren.get(intent.getKeyword()).expectsFile()) {
									state = this.rootChildren.get(intent.getKeyword());
									// get("0") refers to an empty intent that is accessible from the start state
								} else if (this.rootChildren.get("anyFile") != null) {
									state = this.rootChildren.get("anyFile");
								} else {
									state = this.rootChildren.get("default");
								}
								this.updateConversationState(message.getChannel(), state, conversationId);
								recognizedEntities.put(message.getChannel(), intent.getEntities());
							} else {
								
								System.out.println("Incoming Message which expects file should not be chosen when no file: " + intent.getKeyword());
								state = this.rootChildren.get(intent.getKeyword());
								// Incoming Message which expects file should not be chosen when no file was
								// sent
								if (state == null || state.expectsFile()) {
									System.out.println("state still null");
									if (this.rootChildren.get("0") != null) {
										state = this.rootChildren.get("0");
									} else {
										if (intent.getEntitieValues().size() > 0) {
											state = this.rootChildren.get(intent.getEntitieValues().get(0));
											if (state == null) {
												state = this.rootChildren.get("default");
											}
										} else {
											state = this.rootChildren.get("default");
										}

									}
								}
								this.updateConversationState(message.getChannel(), state, conversationId);
								addEntityToRecognizedList(message.getChannel(), intent.getEntities());
							}
						} else {
							
							System.out.println("state not null");
							if (state.getFollowingMessages() == null || state.getFollowingMessages().isEmpty()) {
								
								System.out.println("no followup");
								state = this.rootChildren.get(intent.getKeyword()); // set to the first matching state
																					// from start state (might be null
																					// // if none is found)
								this.currentNluModel.put(message.getChannel(), "0");
								this.updateConversationState(message.getChannel(), state, conversationId);
								addEntityToRecognizedList(message.getChannel(), intent.getEntities());
							} else if (state.getFollowingMessages().get(intent.getKeyword()) != null) {
								
								System.out.println("has followup");
								// check if a file was received during a conversation and search for a follow up
								// incoming message which expects a file.
								if (message.getFileBody() != null) {
									if (state.getFollowingMessages().get(intent.getKeyword()).expectsFile()) {
										state = state.getFollowingMessages().get(intent.getKeyword());
										this.updateConversationState(message.getChannel(), state, conversationId);
										addEntityToRecognizedList(message.getChannel(), intent.getEntities());
									} else {
										state = checkDefault(state, message);
										System.out.println("FileMessage:" + message.getCurrMessage());
									}
								} else if (state.getFollowingMessages().get(intent.getKeyword()).expectsFile()) {
									state = checkDefault(state, message);
									System.out.println("FileBody null but expects File true:" + message.getCurrMessage());
								} else {
									state = state.getFollowingMessages().get(intent.getKeyword());
									this.updateConversationState(message.getChannel(), state, conversationId);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
								}
							} else if (intent.getEntitieValues().size() > 0
									&& state.getFollowingMessages().get(intent.getEntitieValues().get(0)) != null) {
										
								System.out.println("sth with entities. check if a file was received during a conversation");

								// check if a file was received during a conversation and search for a follow up
								// incoming message which expects a file.
								if (message.getFileBody() != null) {
									if (state.getFollowingMessages().get(intent.getEntitieValues().get(0))
											.expectsFile()) {
										state = state.getFollowingMessages().get(intent.getEntitieValues().get(0));
										this.updateConversationState(message.getChannel(), state, conversationId);
										addEntityToRecognizedList(message.getChannel(), intent.getEntities());
									} else {
										state = checkDefault(state, message);
									}
								} else if (state.getFollowingMessages().get(intent.getEntitieValues().get(0))
										.expectsFile()) {
									state = checkDefault(state, message);
								} else {
									state = state.getFollowingMessages().get(intent.getEntitieValues().get(0));
									this.updateConversationState(message.getChannel(), state, conversationId);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
								}
							} else {
								// System.out.println("\u001B[33mDebug --- Followups: " +
								// state.getFollowingMessages() + "\u001B[0m");
								// System.out.println("\u001B[33mDebug --- Emptiness: " +
								// state.getFollowingMessages().keySet().isEmpty() + "\u001B[0m");
								// System.out.println("\u001B[33mDebug --- State: " + state.getIntentKeyword() +
								// "\u001B[0m");

								// try any

								System.out.println("Try any...");
								if (state.getFollowingMessages().get("any") != null) {
									state = state.getFollowingMessages().get("any");
									this.updateConversationState(message.getChannel(), state, conversationId);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
									// In a conversation state, if no fitting intent was found and an empty leadsTo
									// label is found
								} else if (state.getFollowingMessages().get("") != null
										|| state.getFollowingMessages().get("anyFile") != null) {
									if (message.getFileBody() != null) {
										if (state.getFollowingMessages().get("anyFile") != null) {
											state = state.getFollowingMessages().get("anyFile");
											this.updateConversationState(message.getChannel(), state, conversationId);
											addEntityToRecognizedList(message.getChannel(), intent.getEntities());
										} else {
											state = this.rootChildren.get("default");
										}

									} else {
										if (state.getFollowingMessages().get("") != null) {
											state = state.getFollowingMessages().get("");
											this.updateConversationState(message.getChannel(), state, conversationId);
											addEntityToRecognizedList(message.getChannel(), intent.getEntities());
										} else {
											state = checkDefault(state, message);
										}
									}
								} else if (intent.getEntities().size() > 0
										&& !this.triggeredFunction.containsKey(message.getChannel())) {
											
									System.out.println("Dont fully understand the point of this, maybe I added it and forgot...");
									Collection<Entity> entities = intent.getEntities();
									for (Entity e : entities) {
										state = this.rootChildren.get(e.getEntityName());
										// Dont fully understand the point of this, maybe I added it and forgot...
										// Added return for a quick fix, will need to check more in detail
										if (state != null) {
											this.updateConversationState(message.getChannel(), state, conversationId);
											return;
										}
									}

								} else {
									state = checkDefault(state, message);
								}
							}
						}
					} else {
						if (state != null && state.getFollowingMessages().get("") != null) {
							System.out.println("state not null and follow up with empty key not null");
							if (message.getFileBody() != null) {
								if (state.getFollowingMessages().get("").expectsFile()) {
									state = state.getFollowingMessages().get("");
								} else {
									state = checkDefault(state, message);
								}
							} else {
								if (!state.getFollowingMessages().get("").expectsFile()) {
									state = state.getFollowingMessages().get("");
									this.updateConversationState(message.getChannel(), state, conversationId);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
								} else {
									state = checkDefault(state, message);
								}
							}
						} else {
							
							System.out.println("Else");
							if (state != null) {
								state = checkDefault(state, message);
							} else {
								state = this.rootChildren.get("default");
							}
						}
						// System.out.println(state.getIntentKeyword() + " set");
					}
					// If a user sends a file, without wanting to use intent extraction on the name,
					// then intent
					// extraction will still be done, but the result ignored in this case
				} else if (message.getFileName() != null) {
					if (this.rootChildren.get("0").expectsFile()) {
						state = this.rootChildren.get("0");
						// System.out.println(state.getResponse(random));
					} else {
						// if no Incoming Message is fitting, return default message
						intent = new Intent("default", "", "");
					}
					// Default message if the message does not contain a file or the Intent was too
					// low
				} else if (intent.getConfidence() < 0.40f) {
					intent = new Intent("default", "", "");
				}

				System.out.println("-");
				Boolean contextOn = false;
				if (this.triggeredFunction.containsKey(message.getChannel())) {
					
					System.out.println("triggeredfunction found");
					triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
					contextOn = true;
				} else {
					// check if skip is wished or not
					if (state != null) {

						System.out.println("state not null");
						if (state.getFollowingMessages().get("skip") != null) {
							state = state.getFollowingMessages().get("skip");
						}

						String response = state.getResponse(random);
						
						System.out.println("current response: "+response);
						triggeredFunctionId = state.getTriggeredFunctionId() == null
									|| state.getTriggeredFunctionId().equals("") ? null
										: state.getTriggeredFunctionId();
						if (triggeredFunctionId != null && triggeredFunctionId != "") {
							this.triggeredFunction.put(message.getChannel(), triggeredFunctionId);
							contextOn = true;
						}

						if (state.getNluID() != "") {
							this.currentNluModel.put(message.getChannel(), state.getNluID());
						}
						if (response != null) {
							
							System.out.println("response not null");
							if (response != "") {
								String split = "";
								// allows users to use linebreaks \n during the modeling for chat responses
								for (int i = 0; i < response.split("\\\\n").length; i++) {
									split += response.split("\\\\n")[i] + " \n ";
								}
								if (split.contains("[") && split.contains("]")) {
									String[] entitySplit1 = split.split("\\[");
									ArrayList<String> entitySplit2 = new ArrayList<String>();
									for (int i = 1; i < entitySplit1.length; i++) {
										entitySplit2.add(entitySplit1[i].split("\\]")[0]);
									}
									for (String entityName : entitySplit2) {
										if (recognizedEntities != null
												&& recognizedEntities.get(message.getChannel()) != null) {
											for (Entity entity : recognizedEntities.get(message.getChannel())) {
												if (entityName.equals(entity.getEntityName())
														&& entity.getValue() != null) {
													String replace = "[" + entity.getEntityName() + "]";
													split = split.replace(replace, entity.getValue());
												}
											}
										}
									}

								}
								String activityName = state.getIntentKeyword() + ":response";
								this.l2pContext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2,
										remarks.toJSONString(),
										conversationId.toString(), activityName, bot.getId(), "bot", "start",
										System.currentTimeMillis());
								this.l2pContext.monitorXESEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2,
										remarks.toJSONString(),
										conversationId.toString(), activityName, bot.getId(), "bot", "complete",
										System.currentTimeMillis());
								// check if message parses buttons or is simple text
								if (state.getType().equals("Interactive Message")) {
									System.out.println("Interactive msg");
									this.chatMediator.sendBlocksMessageToChannel(message.getChannel(), split,
											this.chatMediator.getAuthToken(), state.getFollowingMessages(),
											java.util.Optional.empty());
								} else {
									
									System.out.println("message with its own message.");
									// TODO: Block sending message to channel if the service is replacing the bot
									// message with its own message
									if (state.getOpenAIEnhance()) {
										System.out.println("Enhance with ai.");
										messageSent = true;
									} else {
										
										System.out.println("send msg.");
										System.out.println("Current state: " + state.getIntentKeyword());
										System.err.println("Current state's rateable: " + state.isRateable());
										messageSent = this.chatMediator.sendMessageToChannel(message.getChannel(),
												replaceVariables(message.getChannel(), split),
												state.getFollowingMessages(), state.followupMessageType, state);
									}
									if (messageSent) {
										botMessage = replaceVariables(message.getChannel(), split);
									}
								}
								// check whether a file url is attached to the chat response and try to send it
								// to
								// the user
								if (!state.getFileURL().equals("")) {
									String fileName = "";
									try {
										// Replacable variable in url menteeEmail
										String urlEmail = state.getFileURL();
										if (message.getEmail() != null) {
											urlEmail = state.getFileURL().replace("menteeEmail",
													message.getEmail());
										}
										URL url = new URL(urlEmail);
										HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
										// Header for l2p services
										httpConn.addRequestProperty("Authorization", "Basic " + Base64.getEncoder()
												.encodeToString((bot.getName() + ":actingAgent").getBytes()));

										String fieldValue = httpConn.getHeaderField("Content-Disposition");
										if (fieldValue == null || !fieldValue.contains("filename=\"")) {
											fieldValue = "pdf.pdf";
										}
										// parse the file name from the header field
										fileName = "pdf.pdf";
										if (!fieldValue.equals("pdf.pdf")) {
											fileName = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10,
													fieldValue.length() - 1);
										} else {
											// check if name is part of url
											if (urlEmail.contains(".pdf") || urlEmail.contains(".png")
													|| urlEmail.contains(".svg") || urlEmail.contains(".json")
													|| urlEmail.contains(".txt")) {
												fileName = urlEmail.split("/")[urlEmail.split("/").length - 1];
											}
										}
										InputStream in = httpConn.getInputStream();
										FileOutputStream fileOutputStream = new FileOutputStream(fileName);
										int file_size = httpConn.getContentLength();
										if (file_size < 1) {
											file_size = 2048;
										}
										byte dataBuffer[] = new byte[file_size];
										int bytesRead;
										while ((bytesRead = in.read(dataBuffer, 0, file_size)) != -1) {
											fileOutputStream.write(dataBuffer, 0, bytesRead);
										}
										fileOutputStream.close();
										this.chatMediator.sendFileMessageToChannel(message.getChannel(),
												new File(fileName), "");

									} catch (Exception e) {
										System.out.println("Could not extract File for reason " + e);
										e.printStackTrace();
										java.nio.file.Files.deleteIfExists(Paths.get(fileName));
										this.chatMediator.sendMessageToChannel(message.getChannel(),
												state.getErrorMessage(), state.getFollowupMessageType());
									}
								}
								if (triggeredFunctionId != null) {
									this.triggeredFunction.put(message.getChannel(),
											triggeredFunctionId);
									contextOn = true;
								}
							} else {
								if (triggeredFunctionId != null) {
									this.triggeredFunction.put(message.getChannel(),
											triggeredFunctionId);
									contextOn = true;
								} else {
									System.out.println("No Bot Action was given to the Response");
								}
							}
						}
						if (this.triggeredFunction.containsKey(message.getChannel())) {
							triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
						} else
						// If conversation flow is terminated, reset state
						if (state.getFollowingMessages().isEmpty()) {
							System.out.println("No following messages");
							this.stateMap.remove(message.getChannel());
							this.previousStateInConversationBackup.put(message.getChannel(), state); // backup state in
																										// case we have
																										// to restore it
																										// later on
							this.previousStateInConversation.remove(message.getChannel());
							if (storedSession.containsKey(message.getChannel())) {

								if (this.triggeredFunction.containsKey(message.getChannel())) {

									this.updateConversationState(message.getChannel(),
											storedSession.get(message.getChannel()), conversationId);
									storedSession.remove(message.getChannel());
								} else if (this.triggeredFunction.containsKey(message.getChannel())) {

									this.updateConversationState(message.getChannel(), state, conversationId);
								}
							}

							this.recognizedEntities.remove(message.getChannel());
						}
					}
				}
				if (state == null || !state.getIntentKeyword().contains("defaultX")) {
					this.defaultAnswerCount.put(message.getChannel(), 0);
				}

				messageInfos.add(new MessageInfo(message, intent, triggeredFunctionId, bot.getName(),
						"", contextOn, recognizedEntities.get(message.getChannel()), this.getName(), conversationId));
				// Chain bot action with openai, add another message info with same message info
				// but with the openai trigger function
				// if (state != null && state.getTriggeredFunctionIds().size() > 1) {
				// 	messageInfos
				// 			.add(new MessageInfo(message, intent, state.getTriggeredFunctionIds().get(1), bot.getName(),
				// 					"", contextOn, recognizedEntities.get(message.getChannel()), this.getName(),
				// 					conversationId));
				// }
				// ConversationMessage conversationMsg = new
				// ConversationMessage(message.getConversationId(), "user", message.getText());
				ConversationMessage userConvMsg = new ConversationMessage("", "user", message.getText());
				Collection<ConversationMessage> conversation = conversationMap.get(message.getChannel());
				conversation.add(userConvMsg);
				conversationMap.put(message.getChannel(), conversation);

				// if message was sent to channel, then add to conversation path here after the
				// user message
				if (messageSent = Boolean.TRUE) {
					ConversationMessage botConvMsg = new ConversationMessage("", "assistant", botMessage);
					conversation.add(botConvMsg);
					conversationMap.put(message.getChannel(), conversation);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Determines the conversation id for a given channel. This depends on the
	 * current state, the last user message and whether we
	 * are in a service context.
	 * 
	 * @param channelId
	 * @return
	 */
	private UUID determineConversationId(String channelId) {
		boolean currentlyInServiceContext = this.triggeredFunction.containsKey(channelId);
		IncomingMessage lastUserMessage = this.previousStateInConversation.get(channelId);
		IncomingMessage state = this.stateMap.get(channelId);
		UUID conversationId = null;
		System.out.println("currentlyInServiceContext: " + currentlyInServiceContext);

		if (lastUserMessage != null)
			System.out.println("lastUserMessage is : " + lastUserMessage.getIntentKeyword());
		if (state != null)
			System.out.println("state: " + state.getIntentKeyword());

		if (currentlyInServiceContext) {
			System.out.println(
					"currentlyInServiceContext: true. Thus state takes precedence over lastUserMessage.");
			if (state == null) {
				state = this.previousStateInConversationBackup.get(channelId);
			}
			if (state != null) {
				System.out.println(
						". state intent is: "
								+ state.getIntentKeyword());
				conversationId = state.getConversationId();
			}
		} else if (lastUserMessage != null) {
			System.out.println("using lastUserMessage intent: " + lastUserMessage.getIntentKeyword());
			System.out.println("LastUserMessage:" + lastUserMessage);
			conversationId = lastUserMessage.getConversationId();
		} else if (state != null) {
			System.out.println("lastUserMessage is not null. using state intent: " + state.getIntentKeyword());
			conversationId = state.getConversationId();
		}

		if (conversationId == null) {
			System.out.println("conversationId: null. generated new one");
			conversationId = UUID.randomUUID();
			
			System.out.println("new id: "+conversationId.toString());
		}
		return conversationId;
	}

	public void setUrl(String Url) throws AuthTokenException {
		this.url = Url;
		if (this.chatMediator instanceof TelegramChatMediator) {
		}
	}

	public void close() {
		chatMediator.close();
	}

	public String getEntityValue(String channel, String entityName) {
		String val = "";
		PreparedStatement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		try {

			conn = db.getDataSource().getConnection();
			stmt = conn.prepareStatement("SELECT value FROM attributes WHERE `channel`=? AND `key`=? ORDER BY id DESC");
			stmt.setString(1, channel);
			stmt.setString(2, entityName);
			rs = stmt.executeQuery();
			if (rs.next()) {
				val = rs.getString("value");
				if (val == null) {
					val = "";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		try {
			if (stmt != null)
				stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		try {
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return val;

	}

	private void safeEntities(ChatMessage msg, Bot bot, Intent intent) {
		String user = msg.getUser();
		String channel = msg.getChannel();
		String b = bot.getId();
		if (intent.getEntities() == null) {
			return;
		}
		if (intent.getEntitieValues() == null) {
			return;
		}
		intent.getEntities().forEach((entity) -> {
			if (entity.getValue() == null) {
				return;
			}
			String k = entity.getEntityName();
			String v = entity.getValue();
			PreparedStatement stmt = null;
			PreparedStatement stmt2 = null;
			Connection conn = null;
			ResultSet rs = null;
			try {
				conn = db.getDataSource().getConnection();
				stmt = conn.prepareStatement(
						"SELECT id FROM attributes WHERE `bot`=? AND `channel`=? AND `user`=? AND `key`=?");
				stmt.setString(1, b);
				stmt.setString(2, channel);
				stmt.setString(3, user);
				stmt.setString(4, k);
				rs = stmt.executeQuery();
				boolean f = false;
				while (rs.next())
					f = true;
				if (f) {
					// Update
					stmt2 = conn.prepareStatement(
							"UPDATE attributes SET `value`=? WHERE `bot`=? AND `channel`=? AND `user`=? AND `key`=?");
					stmt2.setString(1, v);
					stmt2.setString(2, b);
					stmt2.setString(3, channel);
					stmt2.setString(4, user);
					stmt2.setString(5, k);
					stmt2.executeUpdate();
				} else {
					// Insert
					stmt2 = conn.prepareStatement(
							"INSERT INTO attributes (`bot`, `channel`, `user`, `key`, `value`) VALUES (?,?,?,?,?)");
					stmt2.setString(1, b);
					stmt2.setString(2, channel);
					stmt2.setString(3, user);
					stmt2.setString(4, k);
					stmt2.setString(5, v);
					stmt2.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					stmt2.close();
					stmt2 = conn.prepareStatement(
							"INSERT INTO attributes (`bot`, `channel`, `user`, `key`, `value`) VALUES (?,?,?,?,?)");
					stmt2.setString(1, b);
					stmt2.setString(2, channel);
					stmt2.setString(3, user);
					stmt2.setString(4, k);
					stmt2.setString(5, v);
					stmt2.executeUpdate();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} finally {
				try {
					if (rs != null)
						rs.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				;
				try {
					if (stmt != null)
						stmt.close();
					if (stmt2 != null)
						stmt2.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				;
				try {
					if (conn != null)
						conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				;
			}
		});
	}

	private Intent determineIntent(ChatMessage message, Bot bot) {
		Intent intent = null;

		// Special case: `!` commands
		if (message.getText().startsWith("!")) {

			// Split at first occurring whitespace

			String splitMessage[] = message.getText().split("\\s+", 2);
			// First word without '!' prefix
			String intentKeyword = splitMessage[0].substring(1);
			IncomingMessage incMsg = this.rootChildren.get(intentKeyword);
			// TODO: Log this? (`!` command with unknown intent / keyword)
			if (incMsg == null && !intentKeyword.toLowerCase().equals("exit")) {
				if (this.currentNluModel.get(message.getChannel()) == "0") {
					return null;
				} else {
					ArrayList<String> empty = new ArrayList<String>();
					empty.add("");
					incMsg = new IncomingMessage(intentKeyword, "", false, empty, null, "", null, "", "text", false);
					if (splitMessage.length > 1) {
						incMsg.setEntityKeyword(incMsg.getIntentKeyword());
					} else {
						incMsg.setEntityKeyword("newEntity");
					}

				}
			}
			if (splitMessage.length > 1) {
				incMsg.setEntityKeyword(incMsg.getIntentKeyword());
			} else {
				incMsg.setEntityKeyword("newEntity");
			}
			String entityKeyword = incMsg.getEntityKeyword();
			String entityValue = null;
			// Entity value is the rest of the message. The whole rest
			// is in the second element, since we only split it into two parts.
			if (splitMessage.length > 1) {
				entityValue = splitMessage[1];
			}

			intent = new Intent(intentKeyword, entityKeyword, entityValue);
		} else {
			if (bot.getRasaServer(currentNluModel.get(message.getChannel())) != null) {
				intent = bot.getRasaServer(currentNluModel.get(message.getChannel()))
						.getIntent(Intent.replaceUmlaute(message.getText()));
			} else {
				// if the given id is not fit to any server, pick the first one. (In case
				// someone specifies only
				// one server and does not give an ID)
				intent = bot.getFirstRasaServer().getIntent(Intent.replaceUmlaute(message.getText()));
			}

		}
		return intent;
	}

	/**
	 * Updates the state of the conversation for the given channel.
	 * Also sets the conversation id for the given state.
	 * 
	 * @param channelId      The channel id for which the state should be updated.
	 * @param state          The new state of the conversation.
	 * @param conversationId The conversation id to set.
	 */
	private void updateConversationState(String channelId, IncomingMessage state, UUID conversationId) {
		if (state == null) {
			System.out.println("State is null. Resetting state for channel " + channelId);
			this.stateMap.remove(channelId);
			this.previousStateInConversationBackup.put(channelId, state);
			this.previousStateInConversation.remove(channelId);
		} else {
			state.setConversationId(conversationId);
			this.previousStateInConversation.put(channelId, state);
			this.stateMap.put(channelId, state);
		}

	}

	public void restoreConversationState(String channelId) {
		IncomingMessage state = this.previousStateInConversationBackup.get(channelId);
		if (state != null) {
			this.previousStateInConversation.put(channelId, state);
			// System.out.println("Restored state for channel " + channelId + " to " +
			// state.getIntentKeyword());
		}
	}
}