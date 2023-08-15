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

import com.google.gson.Gson;

import i5.las2peer.services.socialBotManagerService.chat.*;
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubAppHelper;
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubIssueMediator;
import i5.las2peer.services.socialBotManagerService.chat.github.GitHubPRMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.Entity;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;

public class Messenger {
	private String name;

	// URL of the social bot manager service (used for setting up the webhook)
	private String url;

	private ChatMediator chatMediator;

	/**
	 * The messenger application provider this object corresponds to
	 */
	private ChatService chatService;

	private HashMap<String, Collection<ConversationMessage>> conversationMap;

	// Key: intent keyword
	/**
	 * The IncomingMessages that are connected to the Messenger element. Those are
	 * the messages that are used to start a conversation
	 */
	private HashMap<String, IncomingMessage> conversationStarters;
	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;
	// Used for keeping remembering entities during conversation state per channel
	private HashMap<String, Collection<Entity>> recognizedEntities;
	// Used for keeping context between assessment and non-assessment states
	// Key is the channelId
	private HashMap<String, String> currentNluModel;
	// Used to know to which Function the received intents/messages are to be sent
	// Is additionally used to check if we are currently communicating with a
	// service(if set, then yes otherwise no)
	private HashMap<String, String> triggeredFunction;
	// Keep up with how many times a default message was given out in a conversation
	// state
	private HashMap<String, Integer> defaultAnswered;

	private HashMap<String, IncomingMessage> storedSession;

	private HashMap<String, HashMap<String, String>> userVariables;

	private Random random;

	private SQLDatabase db;

	public Messenger(String id, String chatService, String token, SQLDatabase database)
			throws IOException, DeploymentException, ParseBotException, AuthTokenException {

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
				System.out.println("RESTful Chat selected");
				break;
			default:
				throw new ParseBotException("Unimplemented chat service: " + chatService);
		}
		System.out.println("no exceptions");

		this.name = id;
		this.conversationMap = new HashMap<String, Collection<ConversationMessage>>();
		this.conversationStarters = new HashMap<String, IncomingMessage>();
		this.stateMap = new HashMap<String, IncomingMessage>();
		this.recognizedEntities = new HashMap<String, Collection<Entity>>();
		this.random = new Random();
		// Initialize the assessment setup
		this.currentNluModel = new HashMap<String, String>();
		this.triggeredFunction = new HashMap<String, String>();
		this.defaultAnswered = new HashMap<String, Integer>();
		this.storedSession = new HashMap<String, IncomingMessage>();
		this.userVariables = new HashMap<String, HashMap<String, String>>();
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

	public void updateConversationInConversationMap(String channel, Collection<ConversationMessage> conversation) {
		conversationMap.put(channel, conversation);
	}

	public void addMessage(IncomingMessage msg) {
		if (msg.getIntentKeyword().contains("defaultX")) {
			this.conversationStarters.put("defaultX", msg);
		} else
			this.conversationStarters.put(msg.getIntentKeyword(), msg);
	}

	public HashMap<String, IncomingMessage> getConversationStarters() {
		return this.conversationStarters;
	}

	public ChatMediator getChatMediator() {
		return this.chatMediator;
	}

	public IncomingMessage checkDefault(IncomingMessage state, ChatMessage message) {
		if (this.conversationStarters.get("defaultX") != null && Integer.valueOf(
				this.conversationStarters.get("defaultX").getIntentKeyword()
						.split("defaultX")[1]) > this.defaultAnswered
								.get(message.getChannel())) {
			IncomingMessage newState = this.conversationStarters.get("defaultX");
			newState.followupMessages = state.followupMessages;
			state = newState;
			this.defaultAnswered.put(message.getChannel(), this.defaultAnswered.get(message.getChannel()) + 1);
		} else {
			state = this.conversationStarters.get("default");
			this.defaultAnswered.put(message.getChannel(), 0);
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
		System.out.println("SET CONTEXT TO BASIC");
		triggeredFunction.remove(channel);
		IncomingMessage state = this.stateMap.get(channel);
		if (state != null) {
			if (state.getFollowingMessages() == null || state.getFollowingMessages().size() == 0) {
				System.out.println("Conversation flow ended now");
				if (storedSession.containsKey(channel)) {
					stateMap.put(channel, storedSession.get(channel));
					state = storedSession.get(channel);
					storedSession.remove(channel);
					System.out.println("Restoring session");
					String response = state.getResponse(random);
					if (response != null && !response.equals("")) {
						System.out.println("Found old message");

						this.chatMediator.sendMessageToChannel(channel, replaceVariables(channel, response), "text");
					}
				}
			} else if (state.getFollowingMessages().get("") != null) {
				// check whether bot action needs to be triggered without user input
				state = state.getFollowingMessages().get("");
				stateMap.put(channel, state);
				if (!state.getResponse(random).equals("")) {
					if (this.chatService == ChatService.RESTful_Chat && state.getFollowingMessages() != null
							&& !state.getFollowingMessages().isEmpty()) {
						this.chatMediator.sendMessageToChannel(channel,
								replaceVariables(channel, state.getResponse(random)), state.getFollowingMessages(),
								"text");

					} else {
						this.chatMediator.sendMessageToChannel(channel,
								replaceVariables(channel, state.getResponse(random)), "text");

					}
				}
				/*
				 * if (state.getResponse(random).triggeredFunctionId != null
				 * && !state.getResponse(random).triggeredFunctionId.equals("")) {
				 * ChatMessage chatMsg = new ChatMessage(channel, userid, "Empty Message");
				 * this.triggeredFunction.put(channel,
				 * state.getResponse(random).triggeredFunctionId);
				 * this.chatMediator.getMessageCollector().addMessage(chatMsg);
				 * }
				 */
			} else {
				// If only message to be sent
				String response = state.getResponse(random);
				if (response != null && !response.equals("") && !state.getOpenAIEnhance()) {
					System.out.println("SET CONTEXT TO BASIC: SEND MESSAGE TO CHANNEL");
					this.chatMediator.sendMessageToChannel(channel, replaceVariables(channel, response),
							state.getFollowingMessages(), state.getFollowupMessageType(), Optional.of(userid));
				}
				if (state.getFollowingMessages().size() == 0) {
					this.stateMap.remove(channel);

				}
			}
		} else {
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
	}

	public String replaceVariables(String channel, String text) {
		HashMap<String, String> variables = this.getUserVariables().get(channel);
		if (variables != null) {
			for (String key : variables.keySet()) {
				String composed = "[" + key + "]";
				text = text.replace(composed, variables.get(key));
			}
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

				if (!this.userVariables.containsKey(message.getChannel())) {
					this.userVariables.put(message.getChannel(), new HashMap<String, String>());
				}

				if (this.defaultAnswered.get(message.getChannel()) == null) {
					this.defaultAnswered.put(message.getChannel(), 0);
				}
				Intent intent = this.determineIntent(message, bot);

				Boolean messageSent = Boolean.FALSE;
				String botMessage = "";
				// Special case: `!` commands
				if (message.getText().startsWith("!")) {

					// Split at first occurring whitespace

					String splitMessage[] = message.getText().split("\\s+", 2);
					// First word without '!' prefix
					String intentKeyword = splitMessage[0].substring(1);
					IncomingMessage incMsg = this.conversationStarters.get(intentKeyword);
					// TODO: Log this? (`!` command with unknown intent / keyword)
					if (incMsg == null && !intentKeyword.toLowerCase().equals("exit")) {
						if (this.currentNluModel.get(message.getChannel()) == "0") {
							continue;
						} else {
							ArrayList<String> empty = new ArrayList<String>();
							empty.add("");
							incMsg = new IncomingMessage(intentKeyword, "", false, empty, null, "", null, "", "text");
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
						intent = bot.getRasaServer().getIntent(Intent.replaceUmlaute(message.getText()));
					}

				}
				System.out.println("found following intent: " + intent.getKeyword());
				try {
					safeEntities(message, bot, intent);

				} catch (Exception e) {
					e.printStackTrace();
				}

				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(message.getChannel());
				if (state == null) {
					System.out.println("No current state, we will start from scratch.");
					if (message.getText().startsWith("!")
							&& this.conversationStarters.get(intent.getKeyword()) == null) {
						// in case a command is triggered which does not exist
						this.chatMediator.sendMessageToChannel(message.getChannel(), "",
								new HashMap<String, IncomingMessage>(), "text");
						return;
					}
				} else {
					System.out.println("Current state: " + state.getIntentKeyword());
				}
				if (state != null && message.getText().startsWith("!")
						&& !state.getFollowingMessages().keySet().contains(intent.getKeyword())) {
					if (this.conversationStarters.get(intent.getKeyword()) == null) {
						// in case a command is triggered which does not exist
						this.chatMediator.sendMessageToChannel(message.getChannel(), "",
								new HashMap<String, IncomingMessage>(), "text");
						return;
					}
					if (!intent.getKeyword().equals("exit")) {
						storedSession.put(message.getChannel(), state);
						state = null;
					}
				}
				if (state != null && message.getText().startsWith("!")
						&& storedSession.containsKey(message.getChannel())) {
					// think about something else to do here
					// this.chatMediator.sendMessageToChannel(message.getChannel(),"Dont start
					// command inside command lol","text");
				}

				// No conversation state present, starting from scratch
				// TODO: Tweak this
				if (!this.triggeredFunction.containsKey(message.getChannel())) {
					if (intent.getKeyword().equals("exit")) {
						recognizedEntities.remove(message.getChannel());
						state = this.conversationStarters.get(intent.getKeyword());
						stateMap.put(message.getChannel(), state);
						if (storedSession.containsKey(message.getCurrMessage())) {
							storedSession.remove(message.getChannel());
						}
					} else
					// add file case to default if part
					if (intent.getConfidence() >= 0.40 || message.getFileName() != null) {
						if (state == null) {
							recognizedEntities.put(message.getChannel(), new ArrayList<Entity>());
							conversationMap.put(message.getChannel(), new ArrayList<ConversationMessage>());
							if (message.getFileName() != null) {
								// check whether incoming message with intent expects file or without intent,
								// such that
								// you can send a file regardless the intent
								if (this.conversationStarters.get(intent.getKeyword()) != null
										&& this.conversationStarters.get(intent.getKeyword()).expectsFile()) {
									state = this.conversationStarters.get(intent.getKeyword());
									// get("0") refers to an empty intent that is accessible from the start state
								} else if (this.conversationStarters.get("anyFile") != null) {
									state = this.conversationStarters.get("anyFile");
								} else {
									state = this.conversationStarters.get("default");
								}
								stateMap.put(message.getChannel(), state);
								recognizedEntities.put(message.getChannel(), intent.getEntities());
							} else {
								state = this.conversationStarters.get(intent.getKeyword());
								// Incoming Message which expects file should not be chosen when no file was
								// sent
								if (state == null || state.expectsFile()) {
									if (this.conversationStarters.get("0") != null) {
										state = this.conversationStarters.get("0");
									} else {
										state = this.conversationStarters.get("default");
									}
								}
								System.out.println(intent.getKeyword() + " detected with " + intent.getConfidence()
										+ " confidence.");
								stateMap.put(message.getChannel(), state);
								addEntityToRecognizedList(message.getChannel(), intent.getEntities());
							}
						} else {
							// any is a static forward
							// TODO include entities of intents
							// If there is no next state, stay in the same state
							if (state.getFollowingMessages() == null || state.getFollowingMessages().isEmpty()) {
								System.out.println("no follow up messages");
								state = this.conversationStarters.get(intent.getKeyword());
								this.currentNluModel.put(message.getChannel(), "0");
								System.out.println(intent.getKeyword() + " detected with " + intent.getConfidence()
										+ " confidence.");
								stateMap.put(message.getChannel(), state);
								addEntityToRecognizedList(message.getChannel(), intent.getEntities());
							} else if (state.getFollowingMessages().get(intent.getKeyword()) != null) {
								System.out.println("try follow up message");
								// check if a file was received during a conversation and search for a follow up
								// incoming message which expects a file.
								if (message.getFileBody() != null) {
									if (state.getFollowingMessages().get(intent.getKeyword()).expectsFile()) {
										state = state.getFollowingMessages().get(intent.getKeyword());
										stateMap.put(message.getChannel(), state);
										addEntityToRecognizedList(message.getChannel(), intent.getEntities());
									} else {
										state = checkDefault(state, message);
									}
								} else if (state.getFollowingMessages().get(intent.getKeyword()).expectsFile()) {
									state = checkDefault(state, message);
								} else {
									state = state.getFollowingMessages().get(intent.getKeyword());
									stateMap.put(message.getChannel(), state);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
								}
							} else {
								// System.out.println("\u001B[33mDebug --- Followups: " +
								// state.getFollowingMessages() + "\u001B[0m");
								// System.out.println("\u001B[33mDebug --- Emptiness: " +
								// state.getFollowingMessages().keySet().isEmpty() + "\u001B[0m");
								// System.out.println("\u001B[33mDebug --- State: " + state.getIntentKeyword() +
								// "\u001B[0m");
								System.out.println(intent.getKeyword() + " not found in state map. Confidence: "
										+ intent.getConfidence() + " confidence.");
								// try any

								if (state.getFollowingMessages().get("any") != null) {
									state = state.getFollowingMessages().get("any");
									stateMap.put(message.getChannel(), state);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
									// In a conversation state, if no fitting intent was found and an empty leadsTo
									// label is found
								} else if (state.getFollowingMessages().get("") != null
										|| state.getFollowingMessages().get("anyFile") != null) {
									if (message.getFileBody() != null) {
										if (state.getFollowingMessages().get("anyFile") != null) {
											state = state.getFollowingMessages().get("anyFile");
											stateMap.put(message.getChannel(), state);
											addEntityToRecognizedList(message.getChannel(), intent.getEntities());
										} else {
											state = this.conversationStarters.get("default");
										}

									} else {
										if (state.getFollowingMessages().get("") != null) {
											state = state.getFollowingMessages().get("");
											stateMap.put(message.getChannel(), state);
											addEntityToRecognizedList(message.getChannel(), intent.getEntities());
										} else {
											state = checkDefault(state, message);
										}
									}
								} else if (intent.getEntities().size() > 0
										&& !this.triggeredFunction.containsKey(message.getChannel())) {
									Collection<Entity> entities = intent.getEntities();
									for (Entity e : entities) {
										state = this.conversationStarters.get(e.getEntityName());
										// Dont fully understand the point of this, maybe I added it and forgot...
										// Added return for a quick fix, will need to check more in detail
										if (state != null) {
											stateMap.put(message.getChannel(), state);
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
							if (message.getFileBody() != null) {
								if (state.getFollowingMessages().get("").expectsFile()) {
									state = state.getFollowingMessages().get("");
								} else {
									state = checkDefault(state, message);
								}
							} else {
								if (!state.getFollowingMessages().get("").expectsFile()) {
									state = state.getFollowingMessages().get("");
									stateMap.put(message.getChannel(), state);
									addEntityToRecognizedList(message.getChannel(), intent.getEntities());
								} else {
									state = checkDefault(state, message);
								}
							}
						} else {
							if (state != null) {
								state = checkDefault(state, message);
							} else {
								System.out.println(intent.getKeyword() + " not detected with " + intent.getConfidence()
										+ " confidence.");
								state = this.conversationStarters.get("default");
							}
						}
						// System.out.println(state.getIntentKeyword() + " set");
					}
					// If a user sends a file, without wanting to use intent extraction on the name,
					// then intent
					// extraction will still be done, but the result ignored in this case
				} else if (message.getFileName() != null) {
					if (this.conversationStarters.get("0").expectsFile()) {
						state = this.conversationStarters.get("0");
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

				Boolean contextOn = false;
				if (this.triggeredFunction.containsKey(message.getChannel())) {
					triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
					contextOn = true;
				} else {
					// check if skip is wished or not
					if (state != null) {
						System.out.println("Getting response for: " + state.intentKeyword);
						if (state.getFollowingMessages().get("skip") != null) {
							state = state.getFollowingMessages().get("skip");
						}

						String response = state.getResponse(random);
						if (state.getTriggeredFunctionIds().get(0) != ""
								&& state.getTriggeredFunctionIds().get(0) != null) {
							this.triggeredFunction.put(message.getChannel(), state.getTriggeredFunctionIds().get(0));
							contextOn = true;
						}

						if (state.getNluID() != "") {
							this.currentNluModel.put(message.getChannel(), state.getNluID());
						}
						if (response != null) {
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
								// check if message parses buttons or is simple text
								if (state.getType().equals("Interactive Message")) {
									this.chatMediator.sendBlocksMessageToChannel(message.getChannel(), split,
											this.chatMediator.getAuthToken(), state.getFollowingMessages(),
											java.util.Optional.empty());
								} else {
									// TODO: Block sending message to channel if the service is replacing the bot
									// message with its own message
									if (state.getOpenAIEnhance() && state.getTriggeredFunctionIds().size() == 1) {
										System.out.println("STATE HAS OPENAIENHANCE AND ONLY HAS ONE BOT ACTION");
										System.out.println(state.getOpenAIEnhance());
										messageSent = true;
									} else if (state.getOpenAIEnhance() && state.getTriggeredFunctionIds().size() > 1) {
										System.out.println(
												"STATE HAS OPENAIENHANCE AND HAS TWO BOT ACTIONS, SEND THE MESSAGE FROM FIRST BOT ACTION");
										System.out.println(state.getOpenAIEnhance());
										messageSent = this.chatMediator.sendMessageToChannel(message.getChannel(),
												replaceVariables(message.getChannel(), split),
												state.getFollowingMessages(), state.followupMessageType);
									} else {
										System.out.println("STATE DOES NOT HAS OPENAIENHANCE");
										System.out.println(state.getOpenAIEnhance());
										messageSent = this.chatMediator.sendMessageToChannel(message.getChannel(),
												replaceVariables(message.getChannel(), split),
												state.getFollowingMessages(), state.followupMessageType);
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
								if (state.getTriggeredFunctionIds().get(0) != null) {
									this.triggeredFunction.put(message.getChannel(),
											state.getTriggeredFunctionIds().get(0));
									contextOn = true;
								}
							} else {
								if (state.getTriggeredFunctionIds().get(0) != "") {
									this.triggeredFunction.put(message.getChannel(),
											state.getTriggeredFunctionIds().get(0));
									contextOn = true;
								} else {
									System.out.println("No Bot Action was given to the Response");
								}
							}
						}
						if (this.triggeredFunction.containsKey(message.getChannel())) {
							triggeredFunctionId = this.triggeredFunction.get(message.getChannel());
						} else
							triggeredFunctionId = state.getTriggeredFunctionIds().get(0);
						// If conversation flow is terminated, reset state
						if (state.getFollowingMessages().isEmpty()) {
							this.stateMap.remove(message.getChannel());
							if (storedSession.containsKey(message.getChannel())
									&& !this.triggeredFunction.containsKey(message.getChannel())) {

								stateMap.put(message.getChannel(), storedSession.get(message.getChannel()));
								storedSession.remove(message.getChannel());
							} else if (storedSession.containsKey(message.getChannel())
									&& this.triggeredFunction.containsKey(message.getChannel())) {
								this.stateMap.put(message.getChannel(), state);
							}
							this.recognizedEntities.remove(message.getChannel());
						}
					}
				}
				if (state == null || !state.getIntentKeyword().contains("defaultX")) {
					this.defaultAnswered.put(message.getChannel(), 0);
				}
				messageInfos.add(new MessageInfo(message, intent, triggeredFunctionId, bot.getName(),
						"", contextOn, recognizedEntities.get(message.getChannel()), this.getName()));
				// Chain bot action with openai, add another message info with same message info
				// but with the openai trigger function
				if (state.getTriggeredFunctionIds().size() > 1) {
					messageInfos
							.add(new MessageInfo(message, intent, state.getTriggeredFunctionIds().get(1), bot.getName(),
									"", contextOn, recognizedEntities.get(message.getChannel()), this.getName()));
				}
				// ConversationMessage conversationMsg = new
				// ConversationMessage(message.getConversationId(), "user", message.getText());
				ConversationMessage userConvMsg = new ConversationMessage("", "user", message.getText());
				Collection<ConversationMessage> conversation = conversationMap.get(message.getChannel());
				conversation.add(userConvMsg);
				conversationMap.put(message.getChannel(), conversation);
				System.out.println("USER MESSAGE " + userConvMsg.getContent() + "WAS ADDED TO CONVERSATION PATH");
				// if message was sent to channel, then add to conversation path here after the
				// user message
				if (messageSent = Boolean.TRUE) {
					ConversationMessage botConvMsg = new ConversationMessage("", "assistant", botMessage);
					conversation.add(botConvMsg);
					conversationMap.put(message.getChannel(), conversation);
					System.out.println("BOT MESSAGE " + botConvMsg.getContent() + "WAS ADDED TO CONVERSATION PATH");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void setUrl(String Url) throws AuthTokenException {
		this.url = Url;
		if (this.chatMediator instanceof TelegramChatMediator) {
			((TelegramChatMediator) this.chatMediator).settingWebhook(Url);
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

	private Intent determineIntent(ChatMessage message, Bot bot) {
		boolean messageIsCommand = message.getText().startsWith("!"); // Special case: `!` commands. For this we don't
																		// need
		// intent recognition
		if (messageIsCommand) {
			String splitMessage[] = message.getText().split("\\s+", 2);
			String intentKeyword = splitMessage[0].substring(1);
			IncomingMessage incomingMessage = this.conversationStarters.get(intentKeyword); // check whether command is
																							// a
																							// conversation starter
			if (incomingMessage == null && !intentKeyword.toLowerCase().equals("exit")) {
				if (this.currentNluModel.get(message.getChannel()) == "0") {
					return null;
				} else {
					incomingMessage = new IncomingMessage(); // create empty incoming message
					if (splitMessage.length > 1) {
						incomingMessage.setEntityKeyword(incomingMessage.getIntentKeyword());
					} else {
						incomingMessage.setEntityKeyword("newEntity");
					}
				}
			}
			if (incomingMessage == null) {
				throw new RuntimeException("Command " + intentKeyword + " not allowed in this state.");
			}
			if (splitMessage.length > 1) {
				incomingMessage.setEntityKeyword(incomingMessage.getIntentKeyword());
			} else {
				incomingMessage.setEntityKeyword("newEntity");
			}
			String entityKeyword = incomingMessage.getEntityKeyword();
			String entityValue = null;
			// Entity value is the rest of the message. The whole rest
			// is in the second element, since we only split it into two parts.
			if (splitMessage.length > 1) {
				entityValue = splitMessage[1];
			}

			return new Intent(intentKeyword, entityKeyword, entityValue);
		}

		// Not a command -> determine intent using rasa
		RasaNlu rasa = bot.getRasaServer(currentNluModel.get(message.getChannel()));
		if (rasa != null)
			return rasa.getIntent(Intent.replaceUmlaute(message.getText()));

		return bot.getRasaServer().getIntent(Intent.replaceUmlaute(message.getText()));
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
}