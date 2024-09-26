package services.socialBotManagerService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import services.socialBotManagerService.model.Bot;
import services.socialBotManagerService.model.BotModel;
import services.socialBotManagerService.model.BotModelEdge;
import services.socialBotManagerService.model.BotModelNode;
import services.socialBotManagerService.model.BotModelNodeAttribute;
import services.socialBotManagerService.model.BotModelValue;
import services.socialBotManagerService.nlu.Intent;
import services.socialBotManagerService.model.MessageInfo;
import services.socialBotManagerService.model.Messenger;
import services.socialBotManagerService.service.SocialBotManagerService;
import services.socialBotManagerService.botParser.BotParser;
import services.socialBotManagerService.chat.ChatMediator;
import services.socialBotManagerService.chat.ChatMessage;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name="SocialBotManagerService", description = "A service for managing social bots.")
@RestController
@RequestMapping("/bots")
public class BotResourceController {

    @Autowired
    private SocialBotManagerService service;

    @Operation(tags = "getBots", summary = "Get all bots", description = "Returns a list of all registered bots.")
    @GetMapping(value = "/getBots", produces = "application/json")
    public ResponseEntity<JSONObject> getBots() {
        JSONObject botList = new JSONObject();
        // Iterate through VLEs
        Gson g = new Gson();
        JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
        for (Entry<String, Bot> botEntry : service.getConfig().getBots().entrySet()) {
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
        return ResponseEntity.ok(botList);
    }

    @Operation(tags = "getBotsForVLE", summary = "Get bot by name", description = "Returns bot information by the given name.")
    @GetMapping(value = "/{botName}", produces = "application/json")
    public ResponseEntity<JSONObject> getBotsForVLE(String botName) {
        Bot b = service.getConfig().getBots().get(botName);
        if (b == null) {
            return ResponseEntity.notFound().build();
        }
        JSONObject bot = new JSONObject();
        JSONObject ac = new JSONObject();
        ac.putAll(b.getActive());
        bot.put("active", ac);
        bot.put("id", b.getId());
        bot.put("name", b.getName());
        bot.put("version", b.getVersion());
        return ResponseEntity.ok(bot);
    }

    @Operation(tags = "initBot", summary = "Initialize a bot", description = "Initialize a bot.")
    @PostMapping(value = "/init", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> init(HttpEntity<BotModel> request) {
        try {

            BotParser bp = BotParser.getInstance();
            BotModel botModel = request.getBody();
            String returnString = "";
            LinkedHashMap<String, BotModelNode> nodes = botModel.getNodes();
            LinkedHashMap<String, BotModelEdge> edges = botModel.getEdges();

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

            try {
                bp.parseNodesAndEdges(service.getConfig(), nodes, edges, service.webconnectorUrlStatic);
            } catch (Exception e) {
                // e.printStackTrace();
                if (e.toString().toLowerCase().contains("login name longer")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Bot Name needs to have at least 4 characters!");
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            // initialized = true;
            JSONObject logData = new JSONObject();
            logData.put("status", "initialized");

            return ResponseEntity.ok(returnString);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Join function
     *
     * @param body    TODO
     * @param botName TODO
     * @return Returns an HTTP response with plain text string content derived from
     *         the path input param.
     * @throws InterruptedException 
     * @throws IOException 
     */
    @Operation(tags = "joinBot", summary = "Activate a bot", description = "Has the capability to join the digital space to get rights.")
	@PostMapping(value = "/{botName}", consumes = "application/json", produces = "text/plain")
	public ResponseEntity<String> join(HttpEntity<JSONObject> request, @PathVariable("botName") String botName) throws IOException, InterruptedException {
        String returnString = "";
        try {
            // BotAgent botAgent = service.getBotAgents().get(botName);
            // if (botAgent == null) {
            //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Botagent " + botName + " not found");
            // }
            // body = body.replace("$botId", botAgent.getIdentifier());
            // JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject j = request.getBody();
            String basePath = (String) j.get("basePath");
            Bot bot = service.getConfig().getBot(botName);

            if (bot == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bot " + botName + " not found");
            }

            if (j.get("directJoin") == null) {
                String joinPath = (String) j.get("joinPath");

                // joinPath.replace("$botId", botAgent.getIdentifier());
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(UriBuilder.fromUri(joinPath).build())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(j.toJSONString()))
                        .build();
                // Send the request
                HttpResponse<String> result = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println(result.body());

                j.remove("joinPath");
                j.remove("basePath");
                j.remove("uid");

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResponseEntity.ok(returnString);
    }
	
    /**
     * Endpoint that handles incoming webhook calls.
     * 
     * @param body    JSONObject
     * @param botName Name of the bot.
     * @return HTTP response
     */
    @Operation(tags = "webhook", summary = "Handle webhook calls", description = "Handles incoming webhook calls.")
    @PostMapping(value = "/{botName}/webhook", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> webhook(String body, @PathVariable("botName") String botName) {
        try {
            // check if bot exists
            Bot bot = null;
            for (String botId : service.getConfig().getBots().keySet()) {
                if (service.getConfig().getBots().get(botId).getName().toLowerCase().equals(botName.toLowerCase())) {
                    bot = service.getConfig().getBot(botId);
                    break;
                }
            }
            if (bot == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bot " + botName + " not found.");
            }

            JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject parsedBody = (JSONObject) p.parse(body);

            if (!parsedBody.containsKey("event")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Field event is missing.");
            }

            String event = parsedBody.get("event").toString();
            if (event.equals("chat_message")) {
                String messenger = parsedBody.get("messenger").toString();
                if (!parsedBody.containsKey("messenger")) {
                    for (String m : bot.getMessengers().keySet()) {
                        messenger = m;
                    }
                }
                ChatMediator chat = bot.getMessenger(messenger).getChatMediator();

                JSONObject chatBody = new JSONObject();
                chatBody.put("channel", parsedBody.get("channel"));
                chatBody.put("text", parsedBody.get("message"));
                service.triggerChat(chat, chatBody);

                return ResponseEntity.ok("");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported event.");
        } catch (ParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Body parse exception.");
        }
    }

    @Operation(tags = "trigger", summary = "Trigger bot by service function", description = "Service Function triggers bot")
    @PostMapping(value = "/{botName}/trigger/service", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> trigger(String body, @PathVariable("botName") String botName) {
        String returnString = "";
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject parsedBody = (JSONObject) p.parse(body);
            String service = (String) parsedBody.get("serviceAlias");
            String triggerFunctionName = parsedBody.get("functionName").toString();
            String triggerUID = parsedBody.get("uid").toString();

            // for (BotAgent botAgent : service.getBotAgents().values()) {
            //     try {
            //         service.checkTriggerBot(service.getConfig(), parsedBody, botAgent, triggerUID, triggerFunctionName);
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //     }
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(returnString);
    }

    @Operation(tags = "triggerRoutine", summary = "Trigger bot by routine", description = "Routine triggers bot")
    @PostMapping(value = "/{botName}/trigger/routine", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> triggerRoutine(String body, @PathVariable("botName") String botName) {
        String returnString = "Routine is running.";
        String addr = service.webconnectorUrl;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);

                    JSONObject j = (JSONObject) p.parse(body);
                    // String service = (String) j.get("serviceAlias");

                    JSONObject context = new JSONObject();
                    context.put("addr", addr);

                    // String botFunctionId = j.get("function").toString();
                    // BotAgent botAgent = getBotAgents().get(j.getAsString("bot"));

                    // try {
                    //     service.checkRoutineTrigger(config, j, botAgent, botFunctionId, context);
                    //     // checkTriggerBot(vle, j, botAgent, "", f);
                    // } catch (Exception e) {
                    //     e.printStackTrace();
                    // }
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
        return ResponseEntity.ok(returnString);
    }

    @Operation(tags = "triggerButton", summary = "Trigger bot by button", description = "Used as an slack app request url to send button clicks")
    @PostMapping(value = "/{botName}/appRequestURL/{instanceAlias}/{intent}/{token}", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> triggerButton(String body, @PathVariable("botName") String name,
				@PathVariable("instanceAlias") String instanceAlias, @PathVariable("intent") String expectedIntent,
				@PathVariable("token") String token, HttpServletRequest request) {
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

            if (bodyInput.get("type").toString().equals("view_submission")) {
                // handle modal submission
                // the channel should be added in "{"private_metadata":{"channel": "channel_id",
                // ...}}"
                // you can add other infos in private_metadata, it would be sent to the channel
                // and sent back after submission
                // info in private_metadata would not be shown in the channel
                JSONObject view = (JSONObject) p.parse(bodyInput.get("view").toString());
                channel = ((JSONObject) p.parse(view.get("private_metadata").toString())).get("channel").toString();
                user = ((JSONObject) p.parse(bodyInput.get("user").toString())).get("id").toString();
                ts = "view_submission";
                text = "view_submission";
                // use callback_id to recognize which kind of view have been submitted
                actionInfoJson.put("actionId", view.get("callback_id").toString());
                actionInfoJson.put("value", view.get("state").toString());
            } else {
                String actionId = "";
                StringBuilder value = new StringBuilder();
                JSONObject containerJson = (JSONObject) p.parse(bodyInput.get("container").toString());
                ts = containerJson.get("message_ts").toString();
                JSONObject channelJson = (JSONObject) p.parse(bodyInput.get("channel").toString());
                channel = channelJson.get("id").toString();
                JSONObject userJson = (JSONObject) p.parse(bodyInput.get("user").toString());
                user = userJson.get("id").toString();

                JSONArray actions = (JSONArray) p.parse(bodyInput.get("actions").toString());
                // this for loop only executed once
                for (Object actionsObject : actions) {
                    String selectedOptionsString = ((JSONObject) actionsObject).get("selected_options").toString();
                    String selectedOptionString = ((JSONObject) actionsObject).get("selected_option").toString();
                    actionId = ((JSONObject) actionsObject).get("action_id").toString();
                    if (selectedOptionsString != null) {
                        // multiple choice with one or more than one selected option
                        // System.out.println("selected options string: " + selectedOptionsString);
                        JSONArray selectedOptionsJson = (JSONArray) p.parse(selectedOptionsString);
                        text = selectedOptionsJson.toString();
                        value.append("[");
                        for (Object singleOptionJson : selectedOptionsJson) {
                            value.append(((JSONObject) singleOptionJson).get("value").toString()).append(',');
                        }
                        value.append("]");

                    } else if (selectedOptionString != null) {
                        // single choice with one selected option (possible)
                        // System.out.println("selected option: " + selectedOptionString);
                        JSONObject selectedOptionJson = (JSONObject) p.parse(selectedOptionString);

                        String textString = selectedOptionJson.get("text").toString();
                        JSONObject textJson = (JSONObject) p.parse(textString);
                        text += textJson.get("text").toString();
                        value.append(((JSONObject) actionsObject).get("value")).toString();

                    } else {
                        // System.out.println("No selectedOption and no selectedOptions.");
                        // System.out.println("No selectedOption and no selectedOptions. Just a normal
                        // button press.");

                        String textString = ((JSONObject) actionsObject).get("text").toString();
                        JSONObject textJson = (JSONObject) p.parse(textString);
                        text += textJson.get("text").toString();
                        value.append(((JSONObject) actionsObject).get("value").toString());
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
            actionInfoJson.put("triggerId", bodyInput.get("trigger_id").toString());

            ChatMessage chatMessage = new ChatMessage(channel, user, text, ts, actionInfoJson.toString());
            JSONObject intentJO = new JSONObject();
            JSONObject innerIntent = new JSONObject();
            innerIntent.put("name", expectedIntent);
            innerIntent.put("confidence", 1.0);
            intentJO.put("intent", innerIntent);
            JSONArray ja = new JSONArray();
            intentJO.put("entities", ja);
            Intent intent = new Intent(intentJO);
            // set email, since it is not passed on in body
            chatMessage.setEmail(user);
            // adjust triggered function id
            MessageInfo messageInfo = new MessageInfo(chatMessage, intent, "", name, instanceAlias, true,
                    new ArrayList<>());

            // this.triggeredFunction.get(message.getChannel());
            // Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, body);

            // SocialBotManagerService sbf = this.sbfservice;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String messengerName = "";
                    try {
                        // BotAgent botAgent = getBotAgents().get(messageInfo.getBotName());
                        String serviceM = messageInfo.getServiceAlias();

                        // get triggered function id, by getting bot, the messengers and then the intent
                        // hash map
                        HashMap<String, Bot> botsHM = service.getConfig().getBots();
                        // System.out.println("botsHM: " + botsHM);
                        String triggerdFunctionId = "";
                        for (Bot bot : botsHM.values()) {
                            // System.out.println(bot);
                            HashMap<String, services.socialBotManagerService.model.Messenger> messengers = bot
                                    .getMessengers();
                            for (Messenger m : messengers.values()) {
                                // System.out.println("messenger: " + m);
                                HashMap<String, services.socialBotManagerService.model.IncomingMessage> intentsHM = m
                                        .getRootChildren();
                                // System.out.println("intentsHM: " + intentsHM);
                                for (String s : intentsHM.keySet()) {
                                    if (s.equals(expectedIntent)) {
                                        services.socialBotManagerService.model.IncomingMessage incomingMessage = intentsHM
                                                .get(s);
                                        services.socialBotManagerService.model.IncomingMessage chatResponses = incomingMessage;
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
                            service.performIntentTrigger(service.getConfig(), name, newMessageInfo, request);
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
            return ResponseEntity.ok("");

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.ok("");
    }

    // @Operation(tags = "triggerButton", summary = "Trigger bot by button", description = "Used as an slack app request url to send button clicks")
    // @PostMapping(value = "/{botName}/appRequestURL/{instanceAlias}/{token}", consumes = "application/json", produces = "text/plain")
    // public ResponseEntity<String> triggerButton(String body, @PathVariable("botName") String name,
    //         @PathVariable("instanceAlias") String instanceAlias,
    //         @PathVariable("token") String token) {

    //     new Thread(new Runnable() {
    //         @Override
    //         public void run() {

    //             // Identify bot
    //             Bot bot = null;

    //             for (Bot b : service.getConfig().getBots().values()) {
    //                 if (bot.getMessenger(ChatService.SLACK) != null) {
    //                     ChatMediator mediator = bot.getMessenger(ChatService.SLACK)
    //                             .getChatMediator();
    //                     if (mediator.hasToken(token))
    //                         bot = b;
    //                 }
    //             }

    //             if (bot == null)
    //                 System.out.println("cannot relate slack action to a bot with token: " + token);
    //             System.out.println("slack action: bot identified: " + bot.getName());

    //             // Handle action
    //             Messenger messenger = bot.getMessenger(ChatService.SLACK);
    //             SlackChatMediator mediator = (SlackChatMediator) messenger.getChatMediator();
    //             JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    //             JSONObject parsedBody;
    //             try {
    //                 parsedBody = (JSONObject) jsonParser
    //                         .parse(java.net.URLDecoder.decode(body, StandardCharsets.UTF_8.name()).substring(8));
    //                 mediator.handleEvent(parsedBody);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }).start();
    //     return ResponseEntity.ok("");
    // }

    @Operation(tags = "triggerIntent", summary = "Log message to MobSOS and trigger bot by intent if necessary", description = "Log message to MobSOS and trigger bot by intent if necessary")
    @PostMapping(value = "/{botName}/trigger/intent", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> triggerIntent(String body, @PathVariable("botName") String name, HttpServletRequest request) {
        Gson gson = new Gson();
        MessageInfo m = gson.fromJson(body, MessageInfo.class);
        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        try {
            JSONObject message = (JSONObject) parser.parse(body);
            JSONObject cleanedJson = (JSONObject) message.get("message");
            cleanedJson.put("user", SocialBotManagerService.encryptThisString(cleanedJson.get("user").toString()));
            if (cleanedJson.containsKey("email")) {
                cleanedJson.put("email", SocialBotManagerService.encryptThisString(cleanedJson.get("email").toString()));
                String channel = m.getMessage().getChannel();

                if (channel.contains("-")){
                    channel = channel.split("-")[0];
                } 

                JSONObject xAPI = service.createXAPIStatement(cleanedJson.get("email").toString(), name,
                        m.getIntent().getKeyword(), m.getMessage().getText(), channel);
                service.sendXAPIStatement(xAPI, service.lrsAuthTokenStatic);
            }
            // Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, cleanedJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_80, body);
        // If no action should be triggered, just return
        if (m.getTriggeredFunctionId() == null) {
            return ResponseEntity.ok("");
        }

        // SocialBotManagerService sbf = service;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        service.performIntentTrigger(service.getConfig(), name, m, request);
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
        return ResponseEntity.ok("");
    }

    @Operation(tags = "deactivateBot", summary = "Deactivate bot for unit", description = "Deactivates a bot for a unit.")
    @DeleteMapping(value = "/{botName}/{unit}", produces = "application/json")
    public ResponseEntity<String> deactivateBot(@PathVariable("botName") String bot, @PathVariable("unit") String unit) {
        Bot b = service.getConfig().getBots().get(bot);
        if (b != null) {
            b.setIdActive(unit, false);
            return ResponseEntity.ok(bot + " deactivated.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(bot + " not found.");
    }

    // the body needs to contain the names of all the messenger elements which the
    // bot uses with "messengerNames" as the attribute name
    // @Operation(tags = "deactivateBotAll", summary = "Deactivate bot for unit", description = "Deactivates a bot for a unit.")
    // @DeleteMapping(value = "/deactivateBots", consumes = "application/json", produces = "application/json")
    // public ResponseEntity<String> deactivateBotAll(@PathVariable("botAgentId") String bot, JSONObject body) {
    //     Bot b = service.getConfig().getBot(bot);
    //     if (b != null) {
    //         try {
    //             service.getConfig().removeBot(bot);
    //             return ResponseEntity.ok().body(bot + " deactivated.");
    //         } catch (Exception e) {
    //             return ResponseEntity.status(HttpStatus.HTTP_VERSION_NOT_SUPPORTED).body(bot + " not deactivated.");
    //         }
    //         // ArrayList messengers = (ArrayList) body.get("messengers");
    //         // if (b.deactivateAllWithCheck(messengers)) {
    //         //     service.getConfig().removeBot(bot);
    //         //     return ResponseEntity.ok().body(bot + " deactivated.");
    //         // } else {
    //         //     return ResponseEntity.status(HttpStatus.HTTP_VERSION_NOT_SUPPORTED).body(bot + " not deactivated.");
    //         // }
    //     }

    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(bot + " not found.");
    // }

    // @Operation(tags = "telegramEvent", summary = "Receive an Telegram event", description = "Receive an Telegram event")
    // @PostMapping(value = "/events/telegram/{token}", consumes = "application/json", produces = "text/plain")
    // public ResponseEntity<String> telegramEvent(String body, @PathVariable("token") String token) {

    //     new Thread(new Runnable() {
    //         @Override
    //         public void run() {

    //             // Identify bot
    //             Bot bot = null;

    //             // for (Bot b : service.getConfig().getBots().values()) {
    //                 // if (b.getMessenger(ChatService.TELEGRAM) != null) {
    //                 //     bot = b;
    //                 // }
    //             // }
    //             if (bot == null)
    //                 System.out.println("cannot relate telegram event to a bot with token: " + token);
    //             System.out.println("telegram event: bot identified: " + bot.getName());

    //             // Handle event
    //             Messenger messenger = bot.getMessenger(ChatService.TELEGRAM);
    //             EventChatMediator mediator = (EventChatMediator) messenger.getChatMediator();
    //             JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
    //             JSONObject parsedBody;
    //             try {
    //                 parsedBody = (JSONObject) jsonParser.parse(body);
    //                 mediator.handleEvent(parsedBody);
    //             } catch (ParseException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     }).start();

    //     return ResponseEntity.ok("");
    // }
}
