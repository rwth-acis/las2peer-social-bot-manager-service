package services.socialBotManagerService.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.mongodb.MongoException;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import services.socialBotManagerService.chat.ChatMediator;
import services.socialBotManagerService.chat.ChatMessage;
import services.socialBotManagerService.chat.RESTfulChatMediator;
import services.socialBotManagerService.chat.RESTfulChatMessageCollector;
import services.socialBotManagerService.chat.RESTfulChatResponse;
import services.socialBotManagerService.model.ActionType;
import services.socialBotManagerService.model.Bot;
import services.socialBotManagerService.model.BotConfiguration;
import services.socialBotManagerService.model.IncomingMessage;
import services.socialBotManagerService.model.MessageInfo;
import services.socialBotManagerService.model.Messenger;
import services.socialBotManagerService.model.ServiceFunction;
import services.socialBotManagerService.service.SocialBotManagerService;

@Tag(name="RESTfulChat Resource", description = "A service for managing social bots.")
@RestController
@RequestMapping("/RESTfulChat")
public class RestfulChatResourceController {
    @Autowired
    private SocialBotManagerService sbfService;
    static HashMap<String, JSONObject> userFileIds = new HashMap<String, JSONObject>();
    static HashMap<String, JSONObject> userMessage = new HashMap<String, JSONObject>();
    static HashMap<String, String> userKey = new HashMap<String, String>();
    // adding this temporarily to avoid needing to add stuff elsewhere
    static HashMap<String, String> emailToChannel = new HashMap<String, String>();

    // adding this temporarily to avoid needing to add stuff elsewhere
    static HashMap<String, Messenger> channelToMessenger = new HashMap<String, Messenger>();

    /**
     * Handles RESTful chat requests.
     *
     * @param bot          the name of the bot to send the message to
     * @param organization the organization to send the message to
     * @param channel      the channel to send the message to
     * @param input        the input message, in JSON format
     * @return the response from the bot, in plain text format
     */
	@Operation(summary = "Sends a message to the RESTful chat bot and channel", description = "Provides a service to send a message to the specified bot and channel through a RESTful API endpoint")
	@PostMapping(value = "/{bot}/{organization}/{channel}", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> handleRESTfulChat(@PathVariable("bot") String bot, @PathVariable("organization") String organization,
				@PathVariable("channel") String channel,
				String input, HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		RESTfulChatResponse answerMsg = null;
		String email = "";

		try {
			Bot b = null;
			for (Bot botIterator : sbfService.getConfig().getBots().values()) {
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
						String msgtext = bodyInput.get("message").toString();
						if (msgtext == null || msgtext.equals("")) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No message provided.");
						}
						//Adds the user message to the message collector
						ChatMessage msg = new ChatMessage(orgChannel, orgChannel, msgtext);
						chatMediator.getMessageCollector().addMessage(msg);

						if (m.getAsync(orgChannel) && !userMessage.isEmpty()){
							String currMsg = userMessage.get(orgChannel).get("AIResponse").toString();
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
								// BotAgent botAgent = getBotAgents().get(b.getName());
								ServiceFunction sf = new ServiceFunction();
								sbfService.prepareRequestParameters(SocialBotManagerService.config, bot, messageInfo, functionPath, body,
										sf);
								if (body.containsKey("functionPath")) {
									functionPath = body.get("functionPath").toString();
									sf = b.getBotServiceFunctions().get(messageInfo.getTriggeredFunctionId());
									body.put("email", email);
									body.put("organization", organization);
									sf.setMessengerName(messageInfo.getMessengerName());

									performTrigger(SocialBotManagerService.config, bot, sf, functionPath, functionPath, body, token);

									RESTfulChatResponse oldAnswerMsg = answerMsg;

									IncomingMessage userState = m.getStateMap().get(orgChannel);
									String newResponse2 = userState.getResponse(new Random());

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
									System.out.println("Print answer: " + answerMsg);								
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
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No RESTfulChat found for Bot " + bot + ".");
				}
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bot " + bot + " not found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Gson gson = new Gson();
		return ResponseEntity.ok().body(gson.toJson(answerMsg));

	}

	private void performTrigger(BotConfiguration botConfig, String botname, ServiceFunction sf, String functionPath, String triggerUID, JSONObject triggeredBody, String token) {
		if (sf.getActionType().equals(ActionType.SERVICE) || sf.getActionType().equals(ActionType.OPENAPI)) {

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", token);
			HttpEntity<String> entity = new HttpEntity<>(headers);

			String userId = triggeredBody.get("user").toString();
			Bot bot = botConfig.getBots().get(botname);
			String messengerID = sf.getMessengerName();
			String email = triggeredBody.get("email").toString();
			String msg = triggeredBody.get("msg").toString();
			ChatMediator chat = bot.getMessenger(messengerID).getChatMediator();
			Messenger m = bot.getMessenger(messengerID);
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
			try {
				File f = null;
				if (triggeredBody.containsKey("fileBody")) {
					byte[] decodedBytes = java.util.Base64.getDecoder()
							.decode(triggeredBody.get("fileBody").toString());
					f = new File(triggeredBody.get("fileName") + "."
							+ triggeredBody.get("fileType"));
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

				String channel = triggeredBody.get("channel").toString();
				Client textClient = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
				functionPath = functionPath.replace("[channel]", channel);
				functionPath = functionPath.replace("[email]", email);
				functionPath = functionPath.replace("[organization]", triggeredBody.get("organization").toString());
				functionPath = functionPath.replace("[intent]", triggeredBody.get("intent").toString());
				functionPath = m.replaceVariables(channel, functionPath);
				JSONObject entities = (JSONObject) triggeredBody.get("entities");
				for (String eName : entities.keySet()) {
					;
					if (functionPath.toLowerCase().contains("[" + eName + "]")) {
						functionPath = functionPath.replace("[" + eName + "]",
								((JSONObject) entities.get(eName)).get("value").toString());
					}
				}

				JSONObject form = (JSONObject) triggeredBody.get("form");
				// if asynchronous is true, add callback url to the formdata for the botaction
				if(m.getAsync(channel)){
					String addr = sbfService.webconnectorUrl + "/sbfmanager/RESTfulChat/" + bot.getName() + "/" + triggeredBody.get("organization") + "/" + triggeredBody.get("channel").toString().split("-")[1];
					form.put("sbfmUrl", addr);
					triggeredBody.put("form", form);
					userMessage.put(channel, triggeredBody);
				}

				FormDataMultiPart mp = new FormDataMultiPart();
				mp.field("msg", msg.toString());
				String queryParams = "?";
				if (form != null) {
					for (String key : form.keySet()) {
						if (sf.getHttpMethod().equals("get")) {
							if (form.get(key).equals("[channel]")) {
								queryParams += key + "=" + channel + "&";
							} else if (form.get(key).equals("[email]")) {
								queryParams += key + "=" + email + "&";
							} else if (form.get(key).equals("[organization]")) {
								queryParams += key + "=" + triggeredBody.get("organization") + "&";
							} else {
								queryParams += key + "=" + form.get(key) + "&";
							}
						} else {
							if (form.get(key).equals("[channel]")) {
								mp = mp.field(key, channel);
							} else if (form.get(key).equals("[email]")) {
								mp = mp.field(key, email);
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
								mp = mp.field(key, form.get(key).toString());
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

				ResponseEntity<String> response = null;
				if (sf.getHttpMethod().equals("get")) {
					response = sbfService.restTemplate.exchange(target.getUri(), HttpMethod.GET, entity, String.class);
				} else {
					HttpEntity<FormDataMultiPart> entityMp = new HttpEntity<FormDataMultiPart>(mp, headers);
					response = sbfService.restTemplate.exchange(target.getUri(), HttpMethod.POST, entityMp, String.class);
					System.out.println("Response Code:" + response.getStatusCode());
					System.out.println("Response Entitiy:" + response.getBody().toString());
				}

				String test = response.getBody().toString();
				System.out.println("Response Text:" + test);
				mp.close();
				try {
					java.nio.file.Files.deleteIfExists(Paths.get(triggeredBody.get("fileName") + "."
							+ triggeredBody.get("fileType")));
				} catch (Exception e) {
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
					bot.getMessenger(messengerID).addVariable(channel, key, jsonResponse.get(key).toString());
				}
				
				triggeredBody.put("resBody", jsonResponse);
				if (jsonResponse.get("closeContext") == null || Boolean.valueOf(jsonResponse.get("closeContext").toString())) {
					System.out.println("Closed Context");
					bot.getMessenger(messengerID).setContextToBasic(channel,
						userId);
				} else if (Boolean.valueOf(jsonResponse.get("closeContext").toString()) == false) {
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
	@Operation(summary = "Uploads a file to the RESTful chat bot and channel", description = "Provides a service to upload a file to the specified bot and channel through a RESTful API endpoint")
	@PostMapping(value = "/{bot}/{organization}/{channel}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
	public ResponseEntity<String> handleRESTfulChatFile(@PathVariable("bot") String bot,
				@PathVariable("organization") String organization, @PathVariable("channel") String channel,
				@FormDataParam("file") InputStream uploadedInputStream,
				@FormDataParam("file") FormDataContentDisposition fileDetail, HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		RESTfulChatResponse answerMsg = new RESTfulChatResponse("");
		try {
			Bot b = null;
			String addr = sbfService.webconnectorUrl;
			for (Bot botIterator : sbfService.getConfig().getBots().values()) {
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

								String functionPath = "";
								JSONObject body = new JSONObject();
								// BotAgent botAgent = getBotAgents().get(b.getName());
								ServiceFunction sf = new ServiceFunction();
								sbfService.prepareRequestParameters(SocialBotManagerService.config, bot, messageInfo, functionPath,
										body,
										sf);
								if (body.containsKey("functionPath")) {
									functionPath = body.get("functionPath").toString();
									sf = b.getBotServiceFunctions().get(messageInfo.getTriggeredFunctionId());
									body.put("email", email);
									body.put("organization", organization);
									sf.setMessengerName(messageInfo.getMessengerName());
									performTrigger(SocialBotManagerService.config, bot, sf, functionPath, functionPath, body, token);
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
					}
				}
				if (!found) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No RESTfulChat found for Bot " + bot + ".");
				}
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bot " + bot + " not found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Gson gson = new Gson();
		return ResponseEntity.ok().body(gson.toJson(answerMsg));
	}

	@Operation(summary = "Downloads a file from the RESTful chat bot and channel", description = "Provides a service to download a file from the specified bot and channel through a RESTful API endpoint")
	@GetMapping(value = "/{bot}/{organization}/{channel}/file/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> getRESTfulChatFile(@PathVariable("bot") String bot, @PathVariable("organization") String organization,
			@PathVariable("channel") String channel, @PathVariable("fileId") String fileId) {
		RESTfulChatResponse answerMsg = null;
		try {
			String path = bot + organization + channel + "-" + fileId;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {
				GridFSBucket gridFSBucket = GridFSBuckets.create(sbfService.getMongoDatabase(), "files");
				gridFSBucket.find(Filters.empty());
				ObjectId oId = new ObjectId(fileId);
				BsonObjectId bId = new BsonObjectId(oId);
				GridFSFile file = gridFSBucket.find(Filters.eq(bId)).first();
				if (file == null) {
					return ResponseEntity.notFound().build();
				}

				String contentType = determineContentType(file.getFilename());
				String filename = file.getFilename();


				gridFSBucket.downloadToStream(file.getObjectId(), baos);
        		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                	.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                	.body(baos.toByteArray());
			} catch (MongoException me) {
				System.err.println(me);
			}

			File file = new File(path);
			if (!file.exists()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}

			String contentType = "";
			if (path.contains("json")) {
				contentType = "application/json";
			} else if (path.contains("pdf")) {
				contentType = "application/pdf";
			}

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
			.body(baos.toByteArray());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	private String determineContentType(String filename) {
		if (filename.toLowerCase().endsWith(".json")) {
			return "application/json";
		} else if (filename.toLowerCase().endsWith(".pdf")) {
			return "application/pdf";
		}
		// Default to octet-stream if the type is unknown
		return "application/octet-stream";
	}
	@Operation(summary = "Downloads a file from the RESTful chat bot and channel", description = "Provides a service to download a file from the specified bot and channel through a RESTful API endpoint")
	@GetMapping(value = "/{bot}/{organization}/{channel}/{label1}/{label2}/files", produces = "application/json")
	public ResponseEntity<JSONObject> getRESTfulChatFileIds(@PathVariable("bot") String bot,
			@PathVariable("organization") String organization,
			@PathVariable("channel") String channel, HttpServletRequest request) {
		if (userFileIds.containsKey(organization + "-" + channel)) {
			JSONObject r = userFileIds.get(organization + "-" + channel);
			userFileIds.remove(organization + "-" + channel);
			if (r.containsKey("error")) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
			}
			JSONObject input = new JSONObject();
			input.put("message", "!files");
			ResponseEntity<String> response = handleRESTfulChat(bot, organization, channel, input.toString(), request);
			JSONParser p = new JSONParser(0);
			try {
				JSONObject answer = (JSONObject) p.parse(response.getBody());
				answer.put("files", r);
				return ResponseEntity.ok().body(answer);
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
			}

		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject());
		}
	}

	@Operation(summary = "Downloads a file from the RESTful chat bot and channel", description = "Provides a service to download a file from the specified bot and channel through a RESTful API endpoint")
	@PostMapping(value = "/{channel}/{label1}/{label2}/files", consumes = "application/json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<String> updateRESTfulChatFileIds(
			@PathVariable("channel") String channel, @FormDataParam("files") byte[] files) {
		String content = new String(files);
		if (emailToChannel.containsKey(channel)) {
			// kinda abusing code here
			channel = emailToChannel.get(channel);
		}
		if (content.equals(null)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong.");
		}
		try {
			JSONObject o = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).parse(content);
			userFileIds.put(channel, o);
			Messenger m = channelToMessenger.get(channel);
			if (m == null) {
				m = channelToMessenger.get(channel.split("-")[1]);
			}
			for (String key : o.keySet()) {
				m.addVariable(channel, key, o.get(key).toString());
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("cool");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
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

	private static HashMap <String, Integer> counter = new HashMap<String, Integer>();

	@Operation(summary = "Get Biwibot response", description = "Provides a service to get a response from the specified bot and channel through a RESTful API endpoint")
	@GetMapping(value = "/{bot}/{organization}/{channel}/AsyncMessage", produces = "text/plain")
	public ResponseEntity<JSONObject> getRESTfulChatBiwibot(@PathVariable("bot") String bot,
				@PathVariable("organization") String organization,
				@PathVariable("channel") String channel, HttpServletRequest request) {
		System.out.println("Called GET AsyncMessage function");
		JSONObject errRes = new JSONObject();
		String s = "Leider konnte deine Nachricht nicht verarbeitet werden. Bitte versuche es erneut.";
		errRes.put("message", s);
		errRes.put("asynchron", false);
		if (counter.containsKey(channel)) {
			int currentValue = counter.get(channel);
			if (currentValue > 7) {
				System.out.println("Count reached 7.");
				counter.remove(channel);
				return ResponseEntity.status(HttpStatus.OK).body(errRes);
			} else {
				counter.put(channel, currentValue + 1);
			}
		} else {
			counter.put(channel, 1);
		}

		String orgaChannel = organization + "-" + channel;
		if (userMessage.containsKey(orgaChannel)) {
			JSONObject ch = userMessage.get(orgaChannel);
			
			if (ch.containsKey("error")) {
				System.out.println("Error occurred");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ch);
			}

			String key = userKey.get(orgaChannel);
			if (ch.containsKey(key) && !ch.get(key).toString().startsWith("Bitte warte")) {
				JSONObject input = new JSONObject();
				input.put("message", "!default");
				ResponseEntity<String> responseService = handleRESTfulChat(bot, organization, channel, input.toString(), request);
				JSONParser p = new JSONParser();
				try {
					JSONObject answer = (JSONObject) p.parse(responseService.getBody());
					answer.put(key, answer.get("message"));
					answer.put("asynchron", false);
					userMessage.remove(orgaChannel);
					userKey.remove(orgaChannel);
					return ResponseEntity.status(HttpStatus.OK).body(answer);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error after handle input.");
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ch);
				}
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ch);
			} 
			
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject());
		}
	}

	@Operation(summary = "Update Biwibot response", description = "Provides a service to update a response from the specified bot and channel through a RESTful API endpoint")
	@PostMapping(value = "/{bot}/{organization}/{channel}/AsyncMessage", consumes = "application/json", produces = "text/plain")
	public ResponseEntity<String> updateRESTfulChatResponse(@PathVariable("bot") String bot,
			@PathVariable("organization") String organization,
			@PathVariable("channel") String channel, 
			String response, HttpServletRequest request) throws ParseException {
		String orgaChannel = organization + "-" + channel;
		Messenger messenger = channelToMessenger.get(orgaChannel);
		System.out.println("orgaChannel: " + orgaChannel);
		JSONObject o = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE)).parse(response);
		JSONObject input = new JSONObject();
		input.put("channel", orgaChannel);
		String key = o.keySet().toArray()[0].toString();

		if (o.get(key).toString().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong.");
		}
		
		if (o.get(key).equals("!exit")) {
			input.put("message", "!exit");
			messenger.addVariable(orgaChannel, "closeContext", "true");
			handleRESTfulChat(bot, organization, channel, input.toString(), request);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ack");
		}

		try {	
			userMessage.put(orgaChannel, o);
			userKey.put(orgaChannel, key);
			if (messenger == null) {
				messenger = channelToMessenger.get(channel);
			}
			for (String keys : o.keySet()) {
				messenger.addVariable(orgaChannel, keys, o.get(keys).toString());
			}

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ack");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
		}
	}
}

