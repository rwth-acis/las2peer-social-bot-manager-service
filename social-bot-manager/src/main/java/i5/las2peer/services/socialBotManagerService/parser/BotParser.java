package i5.las2peer.services.socialBotManagerService.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.websocket.DeploymentException;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import i5.las2peer.api.Context;
import i5.las2peer.api.Service;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.security.BotAgent;
import i5.las2peer.services.socialBotManagerService.chat.AuthTokenException;
import i5.las2peer.services.socialBotManagerService.database.SQLDatabase;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.BotModelEdge;
import i5.las2peer.services.socialBotManagerService.model.BotModelNode;
import i5.las2peer.services.socialBotManagerService.model.BotModelNodeAttribute;
import i5.las2peer.services.socialBotManagerService.model.BotModelValue;
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.IncomingMessage;
import i5.las2peer.services.socialBotManagerService.model.IntentEntity;
import i5.las2peer.services.socialBotManagerService.model.Messenger;

import i5.las2peer.services.socialBotManagerService.model.NLUKnowledge;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Trigger;
import i5.las2peer.services.socialBotManagerService.model.BotRoutine;
import i5.las2peer.tools.CryptoException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class BotParser {
	private static BotParser instance = null;
	private static final String botPass = "actingAgent";
	private static Context l2pContext;

	protected BotParser() {
	}

	public static BotParser getInstance() {
		if (instance == null) {
			instance = new BotParser();
		}
		return instance;
	}
	public static BotParser getInstance(Context context) {
		l2pContext = context;
		if (instance == null) {
			instance = new BotParser();
		}
		return instance;
	}

	public void parseNodesAndEdges(BotConfiguration config, HashMap<String, BotAgent> botAgents,
			LinkedHashMap<String, BotModelNode> nodes, LinkedHashMap<String, BotModelEdge> edges, SQLDatabase database, String address)
			throws ParseBotException, IOException, DeploymentException, AuthTokenException {

		HashMap<String, Messenger> messengers = new HashMap<String, Messenger>();
		HashMap<String, IncomingMessage> incomingMessages = new HashMap<String, IncomingMessage>();
		HashMap<String, IncomingMessage> responses = new HashMap<String, IncomingMessage>();
		HashMap<String, IntentEntity> intentEntities = new HashMap<String, IntentEntity>();
		HashMap<String, Bot> bots = new HashMap<String, Bot>();
        
        HashMap<String, NLUKnowledge> nluKnowledge = new HashMap<String, NLUKnowledge>();

		HashMap<String, ServiceFunction> bsfList = new HashMap<String, ServiceFunction>();
		HashMap<String, ServiceFunction> usfList = new HashMap<String, ServiceFunction>();
		HashMap<String, ServiceFunctionAttribute> sfaList = new HashMap<String, ServiceFunctionAttribute>();

		HashMap<String, IfThenBlock> itbList = new HashMap<String, IfThenBlock>();
		HashMap<String, BotRoutine> rlist = new HashMap<String, BotRoutine>();

		HashMap<String, ServiceFunction> onBotStartList = new HashMap<String, ServiceFunction>();

		Bot bot = null;
		Gson g = new Gson();
		// NODES
		for (Entry<String, BotModelNode> entry : nodes.entrySet()) {
			BotModelNode elem = entry.getValue();
			String nodeType = elem.getType();
			
			if (nodeType.equals("Bot")) {
				try{
					bot = addBot(elem, botAgents);
					bot.setAddress(address);
					config.addBot(bot.getId(), bot);
					bots.put(entry.getKey(), bot);
				} catch (Exception e){
					throw e;
				}
				// Messenger
			}else if (nodeType.equals("Messenger")) {
				Messenger m = addMessenger(entry.getKey(), elem, config, database);
				messengers.put(entry.getKey(), m);
			} else if (nodeType.equals("Incoming Message")) {
				IncomingMessage m = addIncomingMessage(entry.getKey(), elem, config);
				incomingMessages.put(entry.getKey(), m);
			} else if (nodeType.equals("Intent Entity")) {
				IntentEntity entity = addIntentEntity(entry.getKey(), elem, config);
				intentEntities.put(entry.getKey(), entity);
				// Nlu Url's
			} else if (nodeType.equals("NLU Knowledge")) {
				NLUKnowledge nlu = addNLUKnowledge(entry.getKey(), elem, config);
				nluKnowledge.put(entry.getKey(), nlu);
				// VLE Routine              
			} else if (nodeType.equals("Routine")) {
				BotRoutine routine = addRoutine(elem);
				rlist.put(entry.getKey(), routine);
				// BOT Action
			} else if (nodeType.equals("IfThen")) {
				IfThenBlock ifThenBlock = addIfThenBlock(elem);
				itbList.put(entry.getKey(), ifThenBlock);
				// BOT Action
			} else if (nodeType.equals("Bot Action")) {
				ServiceFunction sf = addAction(entry.getKey(), elem, config);
				bsfList.put(entry.getKey(), sf);
				// User Action
			} else if (nodeType.equals("Action Parameter")) {
				ServiceFunctionAttribute sfa = addActionParameter(entry.getKey(), elem);
				sfaList.put(entry.getKey(), sfa);
			} 
		}

		if (bots.isEmpty()) {
			throw new ParseBotException("Missing Bot!");
		} else if (bsfList.isEmpty() && rlist.isEmpty() && incomingMessages.isEmpty()) {
			throw new ParseBotException("Missing Bot Action and Chat interaction! (You need at least one chat interaction OR a bot action for the bot to work)");
		} 

		// ToDo 
		bot.setRoutines(rlist);

		if (bots.size() == 0) {
			throw new ParseBotException("Missing Bot!");
		}

		int checkGeneratorIns = 0;
		int checkGeneratorOuts = 0;

		// EDGES
		for (Entry<String, BotModelEdge> entry : edges.entrySet()) {
			BotModelEdge elem = entry.getValue();
			String type = elem.getType();
			String source = elem.getSource();
			String target = elem.getTarget();
			String value = elem.getLabel().getValue().getValue();
			// HAS
			if (type.equals("has")) {
				// VLE has...
				if (bots.get(source) != null) {
					Bot b = bots.get(source);
					// ...messenger / ChatMediator
					if (messengers.get(target) != null) {
						Messenger m = messengers.get(target);
						b.addMessenger(m, b.getAddress());
                        // NLU Servers
					} else if (nluKnowledge.get(target) != null){
                        NLUKnowledge nlu = nluKnowledge.get(target);
                        b.addRasaServer(nlu.getId(), nlu.getUrl());
                    }
					// User Function has...
				} else if (usfList.get(source) != null) {
					ServiceFunction sf = usfList.get(source);
					// ...Parameter
					if (sfaList.get(target) != null) {
						ServiceFunctionAttribute sfa = sfaList.get(target);
						sf.addAttribute(sfa);
						sfa.setFunction(sf);
					}
					// Bot Function has...
				} else if (bsfList.get(source) != null) {
					ServiceFunction sf = bsfList.get(source);
					// ...Parameter
					if (sfaList.get(target) != null) {
						ServiceFunctionAttribute sfa = sfaList.get(target);
						sf.addAttribute(sfa);
						sfa.setFunction(sf);
					}
					// Function Parameter has...
				} else if (sfaList.get(source) != null) {
					ServiceFunctionAttribute sfaParent = sfaList.get(source);
					// ...Parameter
					if (sfaList.get(target) != null) {
						System.out.println("PARAMETER HAS CHILD");
						ServiceFunctionAttribute sfaChild = sfaList.get(target);
						sfaParent.addChildAttribute(sfaChild);
						//sfaChild.setParent(sfaParent);
						//System.out.println("PARENT ATTRIBUTE");
						//System.out.println(sfaParent);
						System.out.println("HELLO");
					}
					// Incoming Message has...
				} else if (incomingMessages.get(source) != null) {
					IncomingMessage message = incomingMessages.get(source);
					// ...Intent Entity
					if (intentEntities.get(target) != null) {
						IntentEntity entity = intentEntities.get(target);
						message.setEntityKeyword(entity.getEntityKeyword());
					}
				}
				// PERFORMS
			} else if (type.equals("performs")) {
				// Bot performs Action
				if (bots.get(source) != null && bots.get(source).equals(bot)) {
					//Bot bot = bots.get(source);
					ServiceFunction bsfListItem = bsfList.get(target);
					if (bsfListItem != null) {
						bot.addBotServiceFunction(bsfListItem.getId(), bsfListItem);
						bsfListItem.addBot(bot);
						bsfListItem.setOnStart(bot.getId());
				} 

					
				} else if (bots.get(source) != null) {
					Bot v = bots.get(source);
					BotRoutine r = rlist.get(target);
					if (r != null) {
						v.addRoutine(target, r);
						r.setBot(v);
					}
				}

				// same as
				// TODO
			} else if (type.equals("same as")) {
				sfaList.get(source).setSameAsTrigger(true);
				sfaList.get(source).setMappedTo(sfaList.get(target));
				sfaList.get(target).setSameAsTrigger(true);
				sfaList.get(target).setMappedTo(sfaList.get(source));
				// USES
			} else if (type.equals("uses")) {
				if (itbList.get(source) != null) {
					IfThenBlock itb = itbList.get(source);
					if (itbList.get(target) != null) {
						// chain
						IfThenBlock prev = itbList.get(target);
						itb.setPrev(prev);
						prev.setNext(itb);
					} else if (sfaList.get(target) != null) {
						ServiceFunctionAttribute sAtt = sfaList.get(target);
						itb.setSourceAttribute(sAtt);
					}
					// Bot Action uses Messenger
				} else if (bsfList.containsKey(source)) {
					ServiceFunction sf = bsfList.get(source);
					Messenger m = messengers.get(target);
					if (m == null) {
						throw new ParseBotException("Bot Action uses Messenger, but is not connected to Messenger");
					}
					sf.setMessengerName(m.getName());
					// Incoming Message uses Bot Action
				} else if (incomingMessages.containsKey(source)){
                    IncomingMessage cr = incomingMessages.get(source);
                    if (bsfList.get(target) != null) {
						ServiceFunction botFunction = bsfList.get(target);
						
						// toggle incoming message's openaienhance flag here
						if (botFunction.getServiceName().equals("openai") && botFunction.getFunctionName().equals("personalize")){
							cr.addTriggeredFunctionId(botFunction.getId());
							cr.setOpenAIEnhance(true);
						} else {
							cr.addTriggeredFunctionIdFirst(botFunction.getId());
						}
					}
                }	 else if (responses.containsKey(source)){
                    IncomingMessage cr = responses.get(source);
                    if (bsfList.get(target) != null) {
						ServiceFunction botFunction = bsfList.get(target);
						cr.addTriggeredFunctionId(botFunction.getId());
					}
                }

				// GENERATES
			} else if (type.equals("generates")) {
				if (itbList.get(source) != null) {
					IfThenBlock itb = itbList.get(source);
					if (sfaList.get(target) != null) {
						ServiceFunctionAttribute sAtt = sfaList.get(target);
						sAtt.setItb(itb);
						itb.setTargetAttribute(sAtt);
					}
					// Messenger generates...
				} else if (messengers.containsKey(source)) {
					Messenger messenger = messengers.get(source);
					// ...IncomingMessage
					if (incomingMessages.containsKey(target)) {
						IncomingMessage incMsg = incomingMessages.get(target);
						messenger.addMessage(incMsg);
					}
				}
				// TRIGGERS
				// LEADSTO
				// left precedes in the query so that older bots can still be used with the manager, but will need to get removed later on
			} else if (type.equals("leadsTo")  ) {
				// IncomingMessage leads to...
				if (incomingMessages.containsKey(source)) {
					IncomingMessage sourceMessage = incomingMessages.get(source);
					// ...another IncomingMessage
					if (incomingMessages.containsKey(target)) {
						IncomingMessage targetMessage = incomingMessages.get(target);
						sourceMessage.addTriggerEntity(targetMessage, value);
						sourceMessage.addFollowupMessage(value, targetMessage);
					}
				}
			}
		}

		System.out.println("AFTER EDGES");


		for(ServiceFunction sf : bsfList.values()){
			if (sf != null && !sf.getOnStart().containsKey(bot.getId())) {
				bot.addBotServiceFunction(sf.getId(), sf);
				sf.addBot(bot);
			}
		}
		for(IncomingMessage m : incomingMessages.values()){
			String nluId = m.getNluID();
			if(bot.getRasaServer(nluId)!=null){
				bot.getRasaServer(nluId).addIntent(m.getIntentKeyword());
			}else{
				throw new ParseBotException("Missing NLU Knowledge, ID: "+ nluId);
			}
		}

		// EDGES
		for (Entry<String, BotModelEdge> entry : edges.entrySet()) {
			BotModelEdge elem = entry.getValue();
			String type = elem.getType();
			String source = elem.getSource();
			String target = elem.getTarget();
			String value = elem.getLabel().getValue().getValue();
			if (type.equals("triggers")) {
				// Action triggers action
				if (usfList.get(source) != null) {
					ServiceFunction userFunction = usfList.get(source);
					if (bsfList.get(target) != null) {
						ServiceFunction botFunction = bsfList.get(target);
						Trigger t = new Trigger(userFunction, botFunction);
						userFunction.addTrigger(t);
						for (Bot b : botFunction.getBots()) {
							b.addTrigger(t);
						}
					}
					// Routine triggers action
				} else if (rlist.get(source) != null) {
					BotRoutine r = rlist.get(source);
					if (bsfList.get(target) != null) {
						ServiceFunction botFunction = bsfList.get(target);
						Trigger t = new Trigger(r, botFunction);
						r.addTrigger(t);
					}
					// Incoming Message triggers...
				} else if (incomingMessages.get(source) != null) {
					IncomingMessage m = incomingMessages.get(source);
					// ...Bot Action
					 if (bsfList.get(target) != null) {
						ServiceFunction botFunction = bsfList.get(target);
						m.addTriggeredFunction(botFunction);
					}
				}
			}
		}

		if (checkGeneratorIns != checkGeneratorOuts) {
			throw new ParseBotException("Check the Content Generator connections! There are " + checkGeneratorIns
					+ " inputs and " + checkGeneratorOuts + " outputs.");
		}


		JSONArray jaf = swaggerHelperFunction(bot);


		JSONObject j = new JSONObject();
		j.put("triggerFunctions", jaf);
		System.out.println(jaf.toJSONString());
		JSONArray jarr = new JSONArray();
		for (BotAgent b : botAgents.values()) {
			jarr.add(b.getIdentifier());
		}
		j.put("botIds", jarr);

		Context.get().monitorEvent(MonitoringEvent.BOT_ADD_TO_MONITORING, j.toJSONString());
	}

	private Messenger addMessenger(String key, BotModelNode elem, BotConfiguration config, SQLDatabase database)
			throws ParseBotException, IOException, DeploymentException, AuthTokenException{
		String messengerName = null;
		String messengerType = null;
		String token = null;
		String url = null;

		// TODO: Reduce code duplication
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.contentEquals("Name")) {
				messengerName = subVal.getValue();
			} else if (name.contentEquals("Messenger Type")) {
				messengerType = subVal.getValue();
			} else if (name.contentEquals("Authentication Token")) {
				token = subVal.getValue();
			}
		}
		if (messengerName == null) {
			throw new ParseBotException("Messenger is missing a name");
		}
		if (messengerType == null) {
			throw new ParseBotException("Messenger is missing \"Messenger Type\" attribute");
		}
		if (token == null || token == "") {
			throw new ParseBotException("Messenger is missing \"Authentication Token\" attribute");
		}
		
		Messenger newMessenger = new Messenger(messengerName, messengerType, token, database, Context.get());
		return newMessenger;

	}
    
	private NLUKnowledge addNLUKnowledge(String key, BotModelNode elem, BotConfiguration config)
			throws ParseBotException {
		String rasaName = null;
        String id = null;
        String url = null;
		// TODO: Reduce code duplication
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.contentEquals("Name")) {
				rasaName = subVal.getValue();
				// is easiest way to remove this attribue from elements without needing to change more of the code
				id = rasaName;
			} else if(name.contentEquals("ID")){
                id = subVal.getValue();
            } else if(name.contentEquals("URL")){
                url = subVal.getValue();
            }
		}

		if (url == null || url == "") {
			throw new ParseBotException("NLU Knowledge without URL");
		}

		return new NLUKnowledge(rasaName, id, url);
	}

	private IncomingMessage addIncomingMessage(String key, BotModelNode elem, BotConfiguration config)
			throws ParseBotException {
		String intentKeyword = null;
        String NluID = null;
        Boolean containsFile = null;
		ArrayList<String> messages = new ArrayList<String>();
		String fileURL = null;
		String errorMessage = null;
		String type = null;
		String intentLabel = null;
		String followupMessageType = null; 

		// TODO: Reduce code duplication
		try{
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.contentEquals("Intent Keyword")) {
				intentKeyword = subVal.getValue();
			} else if (name.contentEquals("NLU ID")){
                NluID = subVal.getValue();
            } else if (name.contentEquals("IsFile")){
                containsFile = Boolean.valueOf(subVal.getValue());
            } else if (name.contentEquals("Message")) {
				JSONParser p = new JSONParser(0);
				try{
					JSONObject o = (JSONObject) p.parse(subVal.getValue());
					for (String tKey : o.keySet()){
						messages.add(o.getAsString(tKey));
					}
				} catch (ParseException e ){
					e.printStackTrace();
					messages.add(subVal.getValue());
				}
			} else if (name.contentEquals("FileURL")) {
				fileURL = subVal.getValue();
			} else if (name.contentEquals("ErrorMessage")) {
				errorMessage = subVal.getValue();
			} else if (name.contentEquals("Type")) {
				type = subVal.getValue();
			} else if (name.contentEquals("Intent Label")) {
				intentLabel = subVal.getValue();
			} else if (name.contentEquals("Followup Message Type")) {
				followupMessageType = subVal.getValue();
			}
		}
		} catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			
		}

		if (intentKeyword == null) {
			throw new ParseBotException("Incoming Message is missing Intent Keyword");
		} else if (NluID== null) {
			throw new ParseBotException("Incoming Message is missing NluID");
		} 

		if(intentKeyword.equals("")) {
			intentKeyword = "0";
		}

		if (messages == null) {
			throw new ParseBotException("Response is missing Message");
		} 
		if (fileURL == null) {
			throw new ParseBotException("Response is missing File URL");
		}
		if (errorMessage == null) {
			throw new ParseBotException("Response is missing Error Message");
		}
		if (type == null) {
			throw new ParseBotException("Response is missing Type");
		}
		
		return new IncomingMessage(intentKeyword, NluID, containsFile, messages, fileURL, errorMessage, type, intentLabel, followupMessageType);
	}

	private IntentEntity addIntentEntity(String key, BotModelNode elem, BotConfiguration config)
			throws ParseBotException {
		String intentEntity = null;

		// TODO: Reduce code duplication
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.contentEquals("Keyword")) {
				intentEntity = subVal.getValue();
			}
		}

		if (intentEntity == null) {
			throw new ParseBotException("Intent Entity is missing Keyword");
		}

		return new IntentEntity(intentEntity);
	}

	private Bot addBot(BotModelNode elem, HashMap<String, BotAgent> botAgents) {
		Bot b = new Bot();
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			if (subVal.getName().equals("Name")) {
				String botName = subVal.getValue();
				BotAgent botAgent = null;
				try {
					try {
						botAgent = (BotAgent) Context.getCurrent()
								.fetchAgent(Context.getCurrent().getUserAgentIdentifierByLoginName(botName));
					} catch (AgentNotFoundException e) {
						// AgentOperationFailedException should be handled separately
						botAgent = BotAgent.createBotAgent(botPass);
						botAgent.unlock(botPass);
						botAgent.setLoginName(botName);
						System.out.println(botName);
						Context.getCurrent().storeAgent(botAgent);
						System.out.println("Here?");
					}
					botAgent.unlock(botPass);
					Context.getCurrent().registerReceiver(botAgent);
				} catch (AgentException | IllegalArgumentException | CryptoException e2) {
					// TODO Errorhandling
					System.out.println("Caught the error here");
					e2.printStackTrace();
					throw new IllegalArgumentException(e2);
				}
				JSONObject monitoringMessage = new JSONObject();
				monitoringMessage.put("botName", botName);
				monitoringMessage.put("agentId", botAgent.getIdentifier());
				// runningAt = botAgent.getRunningAtNode();
				Context.getCurrent().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3,
						monitoringMessage.toJSONString());
				System.out.println("Bot " + botName + " registered at: " + botAgent.getRunningAtNode().getNodeId());

				// config.addBot(botAgent.getIdentifier(), botAgent.getLoginName());
				b.setId(botAgent.getIdentifier());
				b.setName(botAgent.getLoginName());
				botAgents.put(botName, botAgent);
			}
		}
		return b;
	}

	private BotRoutine addRoutine(BotModelNode elem) {
		BotRoutine r = new BotRoutine();
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.equals("Name")) {
				String routineName = subVal.getValue();
				r.setName(routineName);
			} else if (name.equals("Time")) {
				String routineTime = subVal.getValue();
				r.setTime(routineTime);
			} else if (name.equals("Interval")) {
				String routineInterval = subVal.getValue();
				r.setInterval(routineInterval);
			}
		}
		return r;
	}

	private IfThenBlock addIfThenBlock(BotModelNode elem) {
		IfThenBlock itb = new IfThenBlock();
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.equals("Condition Type")) {
				itb.setConditionType(subVal.getValue());
			} else if (name.equals("Statement Type")) {
				itb.setStatementType(subVal.getValue());
			} else if (name.equals("Condition A")) {
				itb.setConditionValueA(subVal.getValue());
			} else if (name.equals("Condition B")) {
				itb.setConditionValueB(subVal.getValue());
			} else if (name.equals("Statement A")) {
				itb.setStatementValueA(subVal.getValue());
			} else if (name.equals("Statement B")) {
				itb.setStatementValueB(subVal.getValue());
			}
		}
		return itb;
	}

	private ServiceFunction addAction(String key, BotModelNode elem, BotConfiguration config)
			throws IOException, DeploymentException, ParseBotException {
		ServiceFunction sf = new ServiceFunction();
		sf.setId(key);
		String actionType = "";
		String messengerID = "";
		String service = "";
		String sfName = "";
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.equals("Function Name")) {
				sfName = subVal.getValue();
			} else if (name.equals("Service Alias")) {
				service = subVal.getValue();
			} else if (name.equals("Action Type")) {
				actionType = subVal.getValue();
			} else if (name.equals("Messenger Name")) {
				messengerID = subVal.getValue();
			}          
		}
		if(sfName.equals("")){
			throw new ParseBotException("Bot Action missing function name");
		}
		if(service.equals("")){
			throw new ParseBotException("Bot Action missing service name");
		}

		if (actionType.equals("SendMessage")) {
			sf.setActionType(ActionType.SENDMESSAGE);
			sf.setMessengerName(messengerID);
			sf.setServiceName(service);
			sf.setFunctionName(sfName);
		} else if (actionType.equals("OpenAPI")) {
			URL functionURL = null;
			// Open API with full path
			// service alias should contain base path, so that /swagger can be added
			// afterwards
			try {
				functionURL = new URL(service);
				sf.setFunctionName(sfName);
				sf.setFunctionPath(service);
				sf.setServiceName(service);
				sf.setActionType(ActionType.OPENAPI);
				// maybe check here whether there is a swagger.json? if not, return null
			} catch (Exception e) {
				System.out.println("Given URL in service alias is not correct");
				return null;
			}
		} else {
			// default case
			sf.setFunctionName(sfName);
			sf.setServiceName(service);
		}
		return sf;
	}

	private ServiceFunctionAttribute addActionParameter(String key, BotModelNode elem) {

		ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
		sfa.setId(key);
		for (Entry<String, BotModelNodeAttribute> subEntry : elem.getAttributes().entrySet()) {
			BotModelNodeAttribute subElem = subEntry.getValue();
			BotModelValue subVal = subElem.getValue();
			String name = subVal.getName();
			if (name.equals("Content Type")) {
				if(subVal.getValue() == "Quiz") {
					sfa.setContentType("String");
				} else sfa.setContentType(subVal.getValue());	
			} else if (name.equals("Name")) {
				sfa.setName(subVal.getValue());
			} else if (name.equals("Static")) {
				System.out.println(Boolean.parseBoolean(subVal.getValue()));
				sfa.setStaticContent(Boolean.parseBoolean(subVal.getValue()));
			} else if (name.equals("Content")) {
				sfa.setContent(subVal.getValue());
			} else if (name.equals("URL")) {
				sfa.setContentURL(subVal.getValue());
			} else if (name.equals("Parameter Type")) {
				String pType = subVal.getValue();
				sfa.setParameterType(pType);
			}           
		}
		return sfa;
	}
    

	private void addServiceInformation(ServiceFunction f, JSONObject elem) {
		// pfade
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("paths")).entrySet()) {
			// type
			for (HashMap.Entry<String, Object> subsubEntry : ((JSONObject) subEntry.getValue()).entrySet()) {
				JSONObject functionInfo = (JSONObject) subsubEntry.getValue();
				String opId = (String) functionInfo.get("operationId");
				JSONArray consumes = (JSONArray) functionInfo.get("consumes");
				JSONArray produces = (JSONArray) functionInfo.get("produces");
				if (opId.toLowerCase().equals(f.getFunctionName().toLowerCase())) {
					f.setFunctionPath(subEntry.getKey());
					f.setHttpMethod(subsubEntry.getKey());
					if (consumes == null) {
						f.setConsumes("text/html");
					} else {
						f.setConsumes(consumes.get(0).toString());
					}
					if (produces == null) {
						f.setProduces("text/html");
					} else {
						f.setProduces(produces.get(0).toString());
					}
				}
			}
		}
	}

	private JSONArray swaggerHelperFunction(Bot b) {
		JSONArray jaf = new JSONArray();
		HashMap<String, ServiceFunction> allFunctions = new HashMap<String, ServiceFunction>();
			
		for (Trigger t : b.getTriggerList()) {
			if (t.getTriggerFunction() instanceof ServiceFunction) {
				ServiceFunction sf = (ServiceFunction) t.getTriggerFunction();
				jaf.add(sf.getFunctionName());
			}

		}

		allFunctions.putAll(b.getBotServiceFunctions());
		for (ServiceFunction s : allFunctions.values()) {
			// try to get swagger information
			if (b.getServiceInformation().get(s.getServiceName()) == null
					/*&& s.getActionType().equals(ActionType.SERVICE)*/ ) {
				try {

					System.out.println("Service name is:" + s.getServiceName() + "\nBot is : " + b.getName());
					if (s.getActionType().equals(ActionType.OPENAPI)) {
						JSONObject j = readJsonFromUrl(s.getFunctionPath() + "/swagger.json");
						System.out.println("Information is: " + j);
						b.addServiceInformation(s.getServiceName(), j);
					} else {
						JSONObject j = readJsonFromUrl(
						b.getAddress() + "/" + s.getServiceName() + "/swagger.json");
						System.out.println("Information is: " + j);
						b.addServiceInformation(s.getServiceName(), j);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (b.getServiceInformation().get(s.getServiceName()) != null && s.getFunctionName() != null) {
				addServiceInformation(s, b.getServiceInformation().get(s.getServiceName()));
			}
			
			if (s.getOnStart().containsKey(b.getId())){
				MiniClient client = new MiniClient();
				// client.setLogin(, password);
				if(s.getActionType() == ActionType.SERVICE){
					client.setConnectorEndpoint(b.getAddress()+"/" + s.getServiceName() + s.getFunctionPath());
				} else {
					client.setConnectorEndpoint(s.getServiceName() + s.getFunctionPath());
				}
				HashMap<String, String> headers = new HashMap<String, String>();
				client.setLogin("alice", "pwalice");
				JSONObject body = new JSONObject();
				String botName = "";
				body.put("botId", b.getId());
				body.put("botName", b.getName());
				for(ServiceFunctionAttribute a : s.getAttributes()){
					if (a.getContent().isEmpty()){
						JSONArray jsonArray = new JSONArray();
						body.put(a.getName(), jsonArray);
					} else {
						body.put(a.getName(), a.getContent());
					}
				}
				ClientResponse result = client.sendRequest(s.getHttpMethod().toUpperCase(), "",
						body.toString(), s.getConsumes(), s.getProduces(), headers);
			}
		}
		return jaf;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
			JSONObject json = (JSONObject) p.parse(jsonText);
			return json;
		} finally {
			is.close();
		}
	}
}
