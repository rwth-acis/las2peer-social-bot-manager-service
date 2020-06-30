package i5.las2peer.services.socialBotManagerService.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import javax.websocket.DeploymentException;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.chat.ChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.ChatMessage;
import i5.las2peer.services.socialBotManagerService.chat.RocketChatMediator;
import i5.las2peer.services.socialBotManagerService.chat.SlackChatMediator;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.nlu.Intent;
import i5.las2peer.services.socialBotManagerService.nlu.RasaNlu;
import i5.las2peer.services.socialBotManagerService.parser.ParseBotException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class Messenger {
	private String name;

	private ChatMediator chatMediator;
	private RasaNlu rasa;

	// Key: intent keyword
	private HashMap<String, IncomingMessage> knownIntents;

	// Used for keeping conversation state per channel
	private HashMap<String, IncomingMessage> stateMap;

	private Random random;

	public Messenger(String id, String chatService, String token, String rasaUrl, SQLDatabase database)
			throws IOException, DeploymentException, ParseBotException {

		this.rasa = new RasaNlu(rasaUrl);
		if (chatService.contentEquals("Slack")) {
			this.chatMediator = new SlackChatMediator(token);
		} else if (chatService.contentEquals("Rocket.Chat")) {
			this.chatMediator = new RocketChatMediator(token, database, this.rasa);
		} else { // TODO: Implement more backends
			throw new ParseBotException("Unimplemented chat service: " + chatService);
		}
		this.name = id;

		this.knownIntents = new HashMap<String, IncomingMessage>();
		this.stateMap = new HashMap<String, IncomingMessage>();
		this.random = new Random();
	}

	public String getName() {
		return name;
	}

	public void addMessage(IncomingMessage msg) {
		this.knownIntents.put(msg.getIntentKeyword(), msg);
	}

	public ChatMediator getChatMediator() {
		return this.chatMediator;
	}

	// Handles simple responses ("Chat Response") directly, logs all messages and
	// extracted intents into `messageInfos` for further processing later on.
	// TODO: This would be much nicer if we could get a las2peer context here, but this
	// is usually called from the routine thread. Maybe a context can be shared across
	// threads somehow?
	public void handleMessages(ArrayList<MessageInfo> messageInfos, Bot bot) {
		Vector<ChatMessage> newMessages = this.chatMediator.getMessages();
		// System.out.println(newMessages.size());
		for (ChatMessage message : newMessages) {
			try {
				Intent intent = null;
				// Special case: `!` commands
				// System.out.println(this.knownIntents.toString());
				if (message.getText().startsWith("!")) {
					// Split at first occurring whitespace
					String splitMessage[] = message.getText().split("\\s+", 2);

					// First word without '!' prefix
					String intentKeyword = splitMessage[0].substring(1);
					IncomingMessage incMsg = this.knownIntents.get(intentKeyword);
					// TODO: Log this? (`!` command with unknown intent / keyword)
					if (incMsg == null) {
						continue;
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
					intent = this.rasa.getIntent(message.getText());
				}

				String triggeredFunctionId = null;
				IncomingMessage state = this.stateMap.get(message.getChannel());

				// No conversation state present, starting from scratch
				// TODO: Tweak this
				if (intent.getConfidence() >= 0.1f) {

					if (state == null) {
						state = this.knownIntents.get(intent.getKeyword());
						System.out.println(
								intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
						stateMap.put(message.getChannel(), state);
					} else {
						// any is a static forward
						// TODO include entities of intents
						if (state.getFollowingMessages() == null) {
							System.out.println("no follow up messages");
							state = this.knownIntents.get(intent.getKeyword());
							System.out.println(
									intent.getKeyword() + " detected with " + intent.getConfidence() + " confidence.");
							stateMap.put(message.getChannel(), state);
						} else if (state.getFollowingMessages().get(intent.getKeyword()) != null) {
							System.out.println("try follow up message");
							String keyword = intent.getKeyword();
							// check ratings
							String txt = message.getText();
							if (keyword.equals("highrating")
									&& (txt.equals("1") || txt.equals("2") || txt.equals("3"))) {
								keyword = "lowrating";
							} else if (keyword.equals("lowrating") && (txt.equals("4") || txt.equals("5"))) {
								keyword = "highrating";
							}

							state = state.getFollowingMessages().get(keyword);
							stateMap.put(message.getChannel(), state);
						} else {
							System.out.println(intent.getKeyword() + " not found in state map. Confidence: "
									+ intent.getConfidence() + " confidence.");
							// try any
							if (state.getFollowingMessages().get("any") != null) {
								state = state.getFollowingMessages().get("any");
								stateMap.put(message.getChannel(), state);
							} else {
								state = this.knownIntents.get("default");
								System.out.println(state.getIntentKeyword() + " set");
							}
						}
					}
				} else {
					System.out.println(
							intent.getKeyword() + " not detected with " + intent.getConfidence() + " confidence.");
					state = this.knownIntents.get("default");
					System.out.println(state.getIntentKeyword() + " set");
				}

				if (intent.getKeyword().equals("zeige") || intent.getKeyword().equals("hast")
						|| intent.getKeyword().equals("will")) {
					if (intent.getEntity("muster") != null) {
						state = this.knownIntents.get("mustertext");
					} else if (intent.getEntity("video") != null) {
						state = this.knownIntents.get("video");
					} else if (intent.getEntity("help") != null) {
						state = this.knownIntents.get("help");
					} else if (intent.getEntity("pause") != null) {
						state = this.knownIntents.get("pause");
					} else if (intent.getEntity("upload") != null) {
						state = this.knownIntents.get("upload");
					} else if (intent.getEntity("schreibaufgabe") != null) {
						state = this.knownIntents.get("beschreibung");
					} else {
						state = this.knownIntents.get("default");
					}
				}

				if (state != null) {
					String response = state.getResponse(this.random);
					if (response != null) {
						// TODO get rid of this...
						if (intent.getEntity("schreibaufgabe") != null || intent.getKeyword().equals("beschreibung")) {
							File f = new File("Schreibauftrag.pdf");
							this.chatMediator.sendFileMessageToChannel(message.getChannel(), f, response);
						} else if (intent.getEntity("muster") != null || intent.getKeyword().equals("mustertext")) {
							File f = new File("Mustertext.pdf");
							this.chatMediator.sendFileMessageToChannel(message.getChannel(), f, response);
						} else if (state.getIntentKeyword().equals("suggestMaterial")) {
							// chatbot wl
							String text = message.getText();
							String[] words = text.split(",");
							MiniClient client = new MiniClient();
							client.setConnectorEndpoint("http://137.226.232.175:32303");

							HashMap<String, String> headers = new HashMap<String, String>();
							int counter = 0;
							String s = "";
							for (int i = 0; i < words.length; i++) {
								JSONObject body = new JSONObject();
								JSONArray terms = new JSONArray();
								terms.add(words[i].trim());
								body.put("terms", terms);
								ClientResponse r = client.sendRequest("POST", "materials", body.toJSONString(),
										"application/json", "application/json", headers);

								JSONParser p = new JSONParser();
								JSONObject result = (JSONObject) p.parse(r.getResponse());
								if (result.keySet().size() > 1) {
									counter++;
									JSONArray materials = (JSONArray) result.get("@graph");
									for (Object j : materials) {
										JSONObject jo = (JSONObject) j;
										s += "\\n" + words[i] + ": [" + jo.getAsString("title") + "]("
												+ jo.getAsString("link") + ")";
									}
								}
							}
							response = response.replace("$X", "" + s);
							this.chatMediator.sendMessageToChannel(message.getChannel(), response);
						} else if (state.getIntentKeyword().equals("liste")) {
							String text = message.getText();
							String[] words = text.split(",");
							JSONArray wordsCleaned = new JSONArray();
							for (int i = 0; i < words.length; i++) {
								wordsCleaned.add(words[i].trim());
							}
							MiniClient client = new MiniClient();
							client.setConnectorEndpoint("http://137.226.232.175:32303");

							HashMap<String, String> headers = new HashMap<String, String>();

							JSONObject body = new JSONObject();
							body.put("terms", wordsCleaned);
							ClientResponse r = client.sendRequest("POST", "compare", body.toJSONString(),
									"application/json", "application/json", headers);

							JSONParser p = new JSONParser();
							JSONObject result = (JSONObject) p.parse(r.getResponse());

							response = response.replace("$X", result.getAsString("matchCount"));
							this.chatMediator.sendMessageToChannel(message.getChannel(), response);
						} else {
							this.chatMediator.sendMessageToChannel(message.getChannel(), response);
						}
					}
					triggeredFunctionId = state.getTriggeredFunctionId();

					// If conversation flow is terminated, reset state
					if (state.getFollowingMessages().isEmpty()) {
						this.stateMap.remove(message.getChannel());
					}
				} else {
					System.out.println("Something went wrong...");
				}

				messageInfos.add(
						new MessageInfo(message, intent, triggeredFunctionId, bot.getName(), bot.getVle().getName()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void close() {
		chatMediator.close();
	}
}
