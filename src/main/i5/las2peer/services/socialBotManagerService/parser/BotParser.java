package i5.las2peer.services.socialBotManagerService.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;

import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.security.BotAgent;
import i5.las2peer.services.socialBotManagerService.model.Bot;
import i5.las2peer.services.socialBotManagerService.model.BotConfiguration;
import i5.las2peer.services.socialBotManagerService.model.ContentGenerator;
import i5.las2peer.services.socialBotManagerService.model.IfBlock;
import i5.las2peer.services.socialBotManagerService.model.IfThenBlock;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.ThenBlock;
import i5.las2peer.services.socialBotManagerService.model.VLE;
import i5.las2peer.services.socialBotManagerService.model.VLERoutine;
import i5.las2peer.services.socialBotManagerService.model.VLEUser;
import i5.las2peer.tools.CryptoException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class BotParser {
	private static BotParser instance = null;
	private static final String botPass = "actingAgent";
	private static final String classifierName = "i5.las2peer.services.tensorFlowClassifier.TensorFlowClassifier";
	private static final String textToTextName = "i5.las2peer.services.tensorFlowTextToText.TensorFlowTextToText";

	protected BotParser() {
	}

	public static BotParser getInstance() {
		if (instance == null) {
			instance = new BotParser();
		}
		return instance;
	}

	public void parseNodesAndEdges(BotConfiguration config, HashMap<String, BotAgent> botAgents, JSONObject nodes,
			JSONObject edges) throws ParseBotException {
		HashMap<String, VLE> vles = new HashMap<String, VLE>();
		HashMap<String, Bot> bots = new HashMap<String, Bot>();
		HashMap<String, VLEUser> users = new HashMap<String, VLEUser>();

		HashMap<String, VLERoutine> rlist = new HashMap<String, VLERoutine>();

		HashMap<String, ServiceFunction> bsfList = new HashMap<String, ServiceFunction>();
		HashMap<String, ServiceFunction> usfList = new HashMap<String, ServiceFunction>();

		HashMap<String, ServiceFunctionAttribute> sfaList = new HashMap<String, ServiceFunctionAttribute>();

		HashMap<String, IfBlock> ibList = new HashMap<String, IfBlock>();
		HashMap<String, ThenBlock> tbList = new HashMap<String, ThenBlock>();
		HashMap<String, IfThenBlock> itbList = new HashMap<String, IfThenBlock>();

		HashMap<String, ContentGenerator> gList = new HashMap<String, ContentGenerator>();

		int vleCount = 0;
		VLE vle = null;

		for (HashMap.Entry<String, Object> entry : nodes.entrySet()) {
			JSONObject elem = (JSONObject) entry.getValue();
			String nodeType = (String) elem.get("type");
			if (nodeType.equals("VLE Instance")) {
				vle = setVLEInstance(elem);
				config.addServiceConfiguration(vle.getName(), vle);
				vles.put(entry.getKey(), vle);
				vleCount++;
			} else if (nodeType.equals("VLE User")) {
				VLEUser u = addUser(elem);
				u.setId(entry.getKey());
				users.put(entry.getKey(), u);
			} else if (nodeType.equals("VLE Routine")) {
				VLERoutine routine = addRoutine(elem);
				rlist.put(entry.getKey(), routine);
			} else if (nodeType.equals("Bot Action")) {
				ServiceFunction sf = addAction(entry.getKey(), elem, config);
				bsfList.put(entry.getKey(), sf);
			} else if (nodeType.equals("User Action")) {
				ServiceFunction sf = addAction(entry.getKey(), elem, config);
				usfList.put(entry.getKey(), sf);
			} else if (nodeType.equals("Action Parameter")) {
				ServiceFunctionAttribute sfa = addActionParameter(entry.getKey(), elem);
				sfaList.put(entry.getKey(), sfa);
			} else if (nodeType.equals("Action Result")) {
				ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
				sfa.setId(entry.getKey());
				sfa.setName("result");
				sfa.setType("String");
				sfaList.put(entry.getKey(), sfa);
			} else if (nodeType.equals("Action Body")) {
				ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
				sfa.setId(entry.getKey());
				sfa.setName("body");
				sfa.setType("JSON");
				sfaList.put(entry.getKey(), sfa);
			} else if (nodeType.equals("If")) {
				IfBlock ib = addIfBlock(entry.getKey(), elem);
				ibList.put(entry.getKey(), ib);
			} else if (nodeType.equals("Then")) {
				ThenBlock tb = addThenBlock(entry.getKey(), elem);
				tbList.put(entry.getKey(), tb);
			} else if (nodeType.equals("TextToText") || nodeType.equals("Classifier")) {
				ContentGenerator g = new ContentGenerator();
				g.setId(entry.getKey());
				g.setName(nodeType);
				if (nodeType.equals("TextToText")) {
					g.setServiceName(textToTextName);
				} else if (nodeType.equals("Classifier")) {
					g.setServiceName(classifierName);
				}
				gList.put(entry.getKey(), g);
			}
		}

		if (vleCount != 1) {
			throw new ParseBotException("There must only be one VLE instance!");
		} else if (users.size() == 0) {
			throw new ParseBotException("Missing VLE User!");
		} else if (bsfList.size() == 0) {
			throw new ParseBotException("Missing Bot Action!");
		} else if (usfList.size() == 0 && rlist.size() == 0) {
			throw new ParseBotException("Missing User Action or VLE Routine!");
		}

		vle.setRoutines(rlist);

		// add bots
		for (HashMap.Entry<String, Object> entry : nodes.entrySet()) {
			JSONObject elem = (JSONObject) entry.getValue();
			String nodeType = (String) elem.get("type");
			if (nodeType.equals("Bot")) {
				Bot bot = addBot(elem, botAgents);
				bot.setVle(vle.getName());
				bots.put(entry.getKey(), bot);
				vle.addBot(bot.getName(), bot);
			}
		}

		if (bots.size() == 0) {
			throw new ParseBotException("Missing Bot!");
		}

		int checkGeneratorIns = 0;
		int checkGeneratorOuts = 0;

		HashSet<IfThenBlock> tempitbList = new HashSet<IfThenBlock>();

		for (HashMap.Entry<String, Object> entry : edges.entrySet()) {
			JSONObject elem = (JSONObject) entry.getValue();
			String type = (String) elem.get("type");
			String source = (String) elem.get("source");
			String target = (String) elem.get("target");
			if (type.equals("Action has Parameter")) {
				ServiceFunctionAttribute to = null;

				ServiceFunctionAttribute sfaListItem = sfaList.get(target);
				if (sfaListItem != null) {
					to = sfaListItem;
				}

				ServiceFunction bsfListItem = bsfList.get(source);
				ServiceFunction usfListItem = usfList.get(source);
				sfaListItem = sfaList.get(source);

				if (bsfListItem != null) {
					bsfListItem.addAttribute(to);
				} else if (usfListItem != null) {
					usfListItem.addAttribute(to);
				} else if (sfaListItem != null) {
					sfaListItem.addChildAttribute(to);
				}
				to.setFunctionId(source);
			} else if (type.equals("Bot performs Action")) {
				Bot b = bots.get(source);
				ServiceFunction bsfListItem = bsfList.get(target);
				if (bsfListItem != null) {
					b.addBotServiceFunction(bsfListItem.getId(), bsfListItem);
					bsfListItem.addBot(b.getName());
				} else {
					throw new ParseBotException("Function not found");
				}
			} else if (type.equals("Same Action Parameter as")) {
				sfaList.get(source).setSameAsTrigger(true);
				sfaList.get(source).setMappedTo(target);
				sfaList.get(target).setSameAsTrigger(true);
				sfaList.get(target).setMappedTo(source);
			} else if (type.equals("Action Parameter is Generator Input")) {
				checkGeneratorIns++;
				sfaList.get(source).setGeneratorId(target);
				ContentGenerator g = (ContentGenerator) (gList.get(target));
				g.setInput(source);
			} else if (type.equals("Generator has Ouput Parameter")) {
				checkGeneratorOuts++;
				sfaList.get(target).setGeneratorId(source);
				ContentGenerator g = (ContentGenerator) (gList.get(source));
				g.setOutput(target);
			} else if (type.equals("VLE consists of User")) {
				VLE v = vles.get(source);
				VLEUser u = users.get(target);
				v.addUser(u.getName(), u);
			} else if (type.equals("is If Input")) {
				if (ibList.containsKey(target)) {
					// if chain
					if (ibList.containsKey(source)) {
						//
						ibList.get(target).setPrev(ibList.get(source));
						ibList.get(source).setNext(ibList.get(target));
					} else {
						// start of chain
						ibList.get(target).setSource(source);
					}
				} else {
					// error
					System.out.println("Endpoint is not an if...");
				}
			} else if (type.equals("Then has Output")) {
				if (tbList.containsKey(source)) {
					// if chain
					if (tbList.containsKey(target)) {
						//
						tbList.get(source).setNext(tbList.get(target));
						tbList.get(target).setPrev(tbList.get(source));
					} else {
						// start of chain
						tbList.get(source).setOutput(target);
					}
				} else {
					// error
					System.out.println("Error: Does not start with a then...");

				}
			} else if (type.equals("If Then")) {
				IfThenBlock itb = new IfThenBlock();
				itb.setIb(source);
				itb.setTb(target);
				tempitbList.add(itb);
			}
		}

		if (checkGeneratorIns != checkGeneratorOuts) {
			throw new ParseBotException("Check the Content Generator connections! There are " + checkGeneratorIns
					+ " inputs and " + checkGeneratorOuts + " outputs.");
		}

		for (HashMap.Entry<String, Object> entry : edges.entrySet()) {
			JSONObject elem = (JSONObject) entry.getValue();
			String type = (String) elem.get("type");
			String source = (String) elem.get("source");
			String target = (String) elem.get("target");
			if (type.equals("User Action triggers Bot Action")) {
				ServiceFunction bf = bsfList.get(target);
				ServiceFunction uf = usfList.get(source);
				uf.setTf(bf);
				for (String botName : bf.getBots()) {
					System.out.println(botName);
					vle.addTrigger(uf.getFunctionName(), botName);
					vle.getBots().get(botName).addTrigger(uf.getFunctionName(), target);
				}
			} else if (type.equals("Routine triggers Action")) {
				ServiceFunction bf = bsfList.get(target);
				VLERoutine r = rlist.get(source);
				r.addTriggerFunction(bf);
			}
		}

		for (HashMap.Entry<String, Object> entry : edges.entrySet()) {
			JSONObject elem = (JSONObject) entry.getValue();
			String type = (String) elem.get("type");
			String source = (String) elem.get("source");
			String target = (String) elem.get("target");
			if (type.equals("User performs Action")) {
				VLEUser u = users.get(source);
				u.setFunctionId(target);
				ServiceFunction usfListItem = usfList.get(target);
				ServiceFunction bf = usfListItem.getTf();
				for (String botName : bf.getBots()) {
					vle.getBots().get(botName).addUserServiceFunction(usfListItem.getId(), usfListItem);
				}
			}
		}

		// create if then structure
		createIfThenStructure(tempitbList, ibList, tbList, itbList);

		// pass if then
		passIfThen(config, ibList, tbList, itbList, bsfList, usfList, sfaList);

		// pass attributes
		passAttributes(config, bsfList, usfList, sfaList);

		// pass content generators
		passContentGenerators(config, bsfList, usfList, sfaList, gList);

		JSONArray jaf = swaggerHelperFunction(config);

		JSONObject j = new JSONObject();
		j.put("triggerFunctions", jaf);
		JSONArray jarr = new JSONArray();
		for (BotAgent b : botAgents.values()) {
			jarr.add(b.getIdentifier());
		}
		j.put("botIds", jarr);
		Context.get().monitorEvent(MonitoringEvent.BOT_ADD_TO_MONITORING, j.toJSONString());
	}

	private VLE setVLEInstance(JSONObject elem) {
		VLE vle = new VLE();
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Addr")) {
					vle.setAddr((String) subVal.get("value"));
				} else if (subVal.get("name").equals("Name")) {
					vle.setName(subVal.getAsString("value"));
				} else if (subVal.get("name").equals("Environment Separator")) {
					String sep = (String) subVal.get("value");
					if (sep.equals("")) {
						// Single Environment
						vle.setSeparator("singleEnvironment");
						vle.setEnvironmentSeparator("singleEnvironment");
					} else {
						// normal setup
						vle.setSeparator(sep);
						vle.setEnvironmentSeparator(sep);
					}
				}
			}
		}
		return vle;
	}

	private Bot addBot(JSONObject elem, HashMap<String, BotAgent> botAgents) {
		Bot b = new Bot();
		b.setActive(false);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Name")) {
					String botName = (String) subVal.get("value");
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
							Context.getCurrent().storeAgent(botAgent);
						}
						botAgent.unlock(botPass);
						Context.getCurrent().registerReceiver(botAgent);
					} catch (AgentException | CryptoException e2) {
						// TODO Errorhandling
					}
					// runningAt = botAgent.getRunningAtNode();
					System.out.println("Bot " + botName + " registered at: " + botAgent.getRunningAtNode().getNodeId());

					// config.addBot(botAgent.getIdentifier(), botAgent.getLoginName());
					b.setId(botAgent.getIdentifier());
					b.setName(botAgent.getLoginName());
					botAgents.put(botName, botAgent);
				}
			}
		}
		return b;
	}

	private VLEUser addUser(JSONObject elem) {
		VLEUser u = new VLEUser();
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("role")) {
					String userType = (String) subVal.get("value");
					u.setName(userType);
				}
			}
		}
		return u;
	}

	private VLERoutine addRoutine(JSONObject elem) {
		VLERoutine r = new VLERoutine();
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("name")) {
					String routineName = (String) subVal.get("value");
					r.setName(routineName);
				} else if (subVal.get("name").equals("time")) {
					String routineTime = (String) subVal.get("value");
					r.setTime(routineTime);
				} else if (subVal.get("name").equals("interval")) {
					String routineInterval = (String) subVal.get("value");
					r.setInterval(routineInterval);
				}
			}
		}
		return r;
	}

	private ServiceFunction addAction(String key, JSONObject elem, BotConfiguration config) {

		ServiceFunction sf = new ServiceFunction();
		sf.setId(key);
		String actionType = "";
		String conversationType = "";
		String service = "";
		String sfName = "";
		String token = "";
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Name")) {
					sfName = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Service")) {
					service = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Action Type")) {
					actionType = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Conversation Type")) {
					conversationType = (String) subVal.get("value");
				} else if (subVal.get("name").equals("Token")) {
					token = (String) subVal.get("value");
				}
			}
		}

		if (actionType.equals("conversation")) {
			sf.setFunctionType(actionType);
			sf.setConversationType(conversationType);
			sf.setServiceName(service);
			sf.setToken(token);
		} else {
			// default case
			sf.setFunctionName(sfName);
			sf.setServiceName(service);
		}
		return sf;
	}

	private ServiceFunctionAttribute addActionParameter(String key, JSONObject elem) {

		ServiceFunctionAttribute sfa = new ServiceFunctionAttribute();
		sfa.setId(key);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("contentType")) {
					sfa.setType((String) subVal.get("value"));
				} else if (subVal.get("name").equals("name")) {
					sfa.setName((String) subVal.get("value"));
				} else if (subVal.get("name").equals("static")) {
					sfa.setStaticContent((boolean) subVal.get("value"));
				} else if (subVal.get("name").equals("content")) {
					sfa.setContent(subVal.getAsString("value"));
				} else if (subVal.get("name").equals("url")) {
					sfa.setContentURL(subVal.getAsString("value"));
				}
			}
		}
		return sfa;
	}

	private IfBlock addIfBlock(String key, JSONObject elem) {
		IfBlock ib = new IfBlock();
		ib.setId(key);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Condition Type")) {
					ib.setConditionType((String) subVal.get("value"));
				} else if (subVal.get("name").equals("value")) {
					ib.setValue((String) subVal.get("value"));
				}
			}
		}
		return ib;
	}

	private ThenBlock addThenBlock(String key, JSONObject elem) {
		ThenBlock ib = new ThenBlock();
		ib.setId(key);
		for (HashMap.Entry<String, Object> subEntry : ((JSONObject) elem.get("attributes")).entrySet()) {
			if (subEntry.getValue() instanceof JSONObject) {
				JSONObject subElem = (JSONObject) subEntry.getValue();
				JSONObject subVal = (JSONObject) subElem.get("value");
				if (subVal.get("name").equals("Manipulation Type")) {
					ib.setManipulationType((String) subVal.get("value"));
				} else if (subVal.get("name").equals("value")) {
					ib.setValue((String) subVal.get("value"));
				} else if (subVal.get("name").equals("value2")) {
					ib.setValue2((String) subVal.get("value"));
				}
			}
		}
		return ib;
	}

	private String getParent(String sfa, HashMap<String, ServiceFunctionAttribute> sfaList) {
		String parent = sfa;
		do {
			parent = sfaList.get(parent).getFunctionId();
		} while (sfaList.get(parent) != null);

		return parent;
	}

	private void createIfThenStructure(HashSet<IfThenBlock> itbs, HashMap<String, IfBlock> ibList,
			HashMap<String, ThenBlock> tbList, HashMap<String, IfThenBlock> itbList) {
		for (IfThenBlock itbIterator : itbs) {
			IfBlock ib = ibList.get(itbIterator.getIb());
			while (ib.getPrev() != null) {
				ib = ib.getPrev();
			}

			ThenBlock tb = tbList.get(itbIterator.getTb());
			ThenBlock tbIterator = tb;
			while (tbIterator.getNext() != null) {
				tbIterator = tbIterator.getNext();
			}
			IfThenBlock itb = new IfThenBlock();
			itb.setIb(ib.getSource());
			itb.setIb(ib.getId());
			itb.setTb(tb.getId());
			itbList.put(tbIterator.getOutput(), itb);

			itb.setSource(ib.getSource());
		}
	}

	private void passIfThen(BotConfiguration config, HashMap<String, IfBlock> ibList, HashMap<String, ThenBlock> tbList,
			HashMap<String, IfThenBlock> itbList, HashMap<String, ServiceFunction> bsfList,
			HashMap<String, ServiceFunction> usfList, HashMap<String, ServiceFunctionAttribute> sfaList)
			throws ParseBotException {
		for (HashMap.Entry<String, IfThenBlock> entry : itbList.entrySet()) {
			// If Block id
			String ibs = entry.getValue().getIb();
			IfBlock ib = ibList.get(ibs);
			VLE vle = null;
			ServiceFunction bf = null;

			String sfaParent = getParent(ib.getSource(), sfaList);

			if (bsfList.get(sfaList.get(sfaParent)) != null) {
				bf = bsfList.get(sfaList.get(sfaParent));
				String sname = bf.getServiceName();
				vle = config.getServiceConfiguration(sname);
			} else if (usfList.get(sfaParent) != null) {
				String sname = usfList.get(sfaParent).getServiceName();
				vle = config.getServiceConfiguration(sname);
			} else {
				throw new ParseBotException("Check your If input and Then Output!");
			}
			IfBlock tib = ib;

			String tbs = entry.getValue().getTb();
			ThenBlock tb = tbList.get(tbs);

			ThenBlock ttb = tb;
			for (Bot b : vle.getBots().values()) {
				b.addAttributeIfThen(entry.getKey(), entry.getValue());
				ib = tib;
				do {
					b.addAttributeIf(ib.getId(), ib);
					ib = ib.getNext();
				} while (ib != null);
				tb = ttb;
				do {
					b.addAttributeThen(tb.getId(), tb);
					tb = tb.getPrev();
				} while (tb != null);
			}

		}
	}

	private void passAttributes(BotConfiguration config, HashMap<String, ServiceFunction> bsfList,
			HashMap<String, ServiceFunction> usfList, HashMap<String, ServiceFunctionAttribute> sfaList)
			throws ParseBotException {
		for (HashMap.Entry<String, ServiceFunctionAttribute> entry : sfaList.entrySet()) {
			String parent = getParent(entry.getKey(), sfaList);
			if (bsfList.get(parent) != null) {
				ServiceFunction bf = bsfList.get(parent);
				String service = bf.getServiceName();
				VLE vle = config.getServiceConfiguration(service);
				for (String botName : bf.getBots()) {
					Bot b = vle.getBots().get(botName);
					b.addServiceFunctionsAttributes(entry.getKey(), entry.getValue());
				}
			} else if (usfList.get(parent) != null) {
				ServiceFunction uf = usfList.get(parent);
				String service = uf.getServiceName();
				VLE vle = config.getServiceConfiguration(service);
				ServiceFunction bf = uf.getTf();
				for (String botName : bf.getBots()) {
					Bot b = vle.getBots().get(botName);
					b.addServiceFunctionsAttributes(entry.getKey(), entry.getValue());
				}
			} else {
				throw new ParseBotException("Check the ServiceFunctionAttribute to ServiceFunction connection!");
			}
		}
	}

	private void passContentGenerators(BotConfiguration config, HashMap<String, ServiceFunction> bsfList,
			HashMap<String, ServiceFunction> usfList, HashMap<String, ServiceFunctionAttribute> sfaList,
			HashMap<String, ContentGenerator> gList) throws ParseBotException {
		for (HashMap.Entry<String, ContentGenerator> entry : gList.entrySet()) {
			String attributeString = entry.getValue().getInput();
			String parent = getParent(attributeString, sfaList);
			if (bsfList.get(parent) != null) {
				ServiceFunction bf = bsfList.get(parent);
				String service = bf.getServiceName();
				VLE vle = config.getServiceConfiguration(service);
				for (String botName : bf.getBots()) {
					Bot b = vle.getBots().get(botName);
					b.addGenerator(entry.getKey(), entry.getValue());
				}
			} else if (usfList.get(parent) != null) {
				ServiceFunction uf = usfList.get(parent);
				String service = uf.getServiceName();
				VLE vle = config.getServiceConfiguration(service);
				ServiceFunction bf = uf.getTf();
				for (String botName : bf.getBots()) {
					Bot b = vle.getBots().get(botName);
					b.addGenerator(entry.getKey(), entry.getValue());
				}
			} else {
				throw new ParseBotException("Check the content generator connections!");
			}
		}
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
					f.setPath(subEntry.getKey());
					f.setType(subsubEntry.getKey());
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

	private JSONArray swaggerHelperFunction(BotConfiguration config) {
		JSONArray jaf = new JSONArray();
		HashMap<String, ServiceFunction> allFunctions = new HashMap<String, ServiceFunction>();
		for (VLE vle : config.getVLEs().values()) {
			for (String s : vle.getTriggerList().keySet()) {
				jaf.add(s);
			}
			for (Bot b : vle.getBots().values()) {
				System.out.println(b.getTriggerList().keySet());
				jaf.addAll(b.getTriggerList().keySet());
				allFunctions.putAll(b.getUserServiceFunctions());
				allFunctions.putAll(b.getBotServiceFunctions());
			}
			for (ServiceFunction s : allFunctions.values()) {
				// try to get swagger information

				if (vle.getServiceInformation().get(s.getServiceName()) == null
						&& s.getFunctionType().equals("service")) {
					try {
						JSONObject j = readJsonFromUrl(vle.getAddr() + "/" + s.getServiceName() + "/swagger.json");
						vle.addServiceInformation(s.getServiceName(), j);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (vle.getServiceInformation().get(s.getServiceName()) != null && s.getFunctionName() != null) {
					addServiceInformation(s, vle.getServiceInformation().get(s.getServiceName()));
				}
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
