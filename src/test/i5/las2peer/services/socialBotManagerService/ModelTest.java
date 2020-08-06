package i5.las2peer.services.socialBotManagerService;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
	
import i5.las2peer.connectors.webConnector.client.MiniClient;


import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import net.minidev.json.JSONStyle;

import java.util.HashMap;

public class ModelTest {

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static UserAgentImpl testAgent;
	private static final String testPass = "adamspass";

	private static final String mainPath = "SBFManager/";
	
	
	@Before
	public void startServer() throws Exception {
		// start node
		node = new LocalNodeManager().newNode();
		node.launch();

		// add agent to node
		testAgent = MockAgentFactory.getAdam();
		testAgent.unlock(testPass); // agents must be unlocked in order to be stored
		node.storeAgent(testAgent);

		// start service
		// during testing, the specified service version does not matter
		node.startService(new ServiceNameVersion(SocialBotManagerService.class.getName(), "1.0"), "a pass");

		// start connector
		connector = new WebConnector(true, 0, false, 0); // port 0 means use system defined port
		logStream = new ByteArrayOutputStream();
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
	}

	
	@After
	public void shutDownServer() throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (logStream != null) {
			System.out.println("Connector-Log:");
			System.out.println("--------------");
			System.out.println(logStream.toString());
			logStream = null;
		}
	}
	
	@Test
	public void storeModel() {
		try {
			String testJSONString =
	        				"{" + 
	        				"    \"attributes\": {" + 
	        				"        \"label\": {" + 
	        				"            \"id\": \"modelAttributes[label]\"," + 
	        				"            \"name\": \"Label\"," + 
	        				"            \"value\": {" + 
	        				"                \"id\": \"modelAttributes[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": \"Model Attributes\"" + 
	        				"            }" + 
	        				"        }," + 
	        				"        \"left\": 0," + 
	        				"        \"top\": 0," + 
	        				"        \"width\": 0," + 
	        				"        \"height\": 0," + 
	        				"        \"zIndex\": 0," + 
	        				"        \"type\": \"ModelAttributesNode\"," + 
	        				"        \"attributes\": {}" + 
	        				"    }," + 
	        				"    \"nodes\": {" + 
	        				"        \"f44c40de87defc07002f877d\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"f44c40de87defc07002f877d[name]\"," + 
	        				"                \"name\": \"Name\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"f44c40de87defc07002f877d[name]\"," + 
	        				"                    \"name\": \"Name\"," + 
	        				"                    \"value\": \"test\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"left\": 4500," + 
	        				"            \"top\": 4304," + 
	        				"            \"width\": 48," + 
	        				"            \"height\": 48," + 
	        				"            \"zIndex\": 16001," + 
	        				"            \"type\": \"Instance\"," + 
	        				"            \"attributes\": {" + 
	        				"                \"4bfaada53679f7e979bcc1e6\": {" + 
	        				"                    \"id\": \"f44c40de87defc07002f877d[name]\"," + 
	        				"                    \"name\": \"Name\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"f44c40de87defc07002f877d[name]\"," + 
	        				"                        \"name\": \"Name\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }," + 
	        				"                \"a7c8d03fd57a143943da1f48\": {" + 
	        				"                    \"id\": \"f44c40de87defc07002f877d[address]\"," + 
	        				"                    \"name\": \"Address\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"f44c40de87defc07002f877d[address]\"," + 
	        				"                        \"name\": \"Address\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }," + 
	        				"                \"3640924305c3d50b3a2de10d\": {" + 
	        				"                    \"id\": \"f44c40de87defc07002f877d[environment separator]\"," + 
	        				"                    \"name\": \"Environment Separator\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"f44c40de87defc07002f877d[environment separator]\"," + 
	        				"                        \"name\": \"Environment Separator\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }" + 
	        				"            }" + 
	        				"        }," + 
	        				"        \"ccbea5e25731d8e6537a1947\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"ccbea5e25731d8e6537a1947[name]\"," + 
	        				"                \"name\": \"Name\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"ccbea5e25731d8e6537a1947[name]\"," + 
	        				"                    \"name\": \"Name\"," + 
	        				"                    \"value\": \"test\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"left\": 4500," + 
	        				"            \"top\": 4402," + 
	        				"            \"width\": 48," + 
	        				"            \"height\": 48," + 
	        				"            \"zIndex\": 16002," + 
	        				"            \"type\": \"Bot\"," + 
	        				"            \"attributes\": {" + 
	        				"                \"80f33463988c54141b8c4dd8\": {" + 
	        				"                    \"id\": \"ccbea5e25731d8e6537a1947[name]\"," + 
	        				"                    \"name\": \"Name\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"ccbea5e25731d8e6537a1947[name]\"," + 
	        				"                        \"name\": \"Name\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }" + 
	        				"            }" + 
	        				"        }," + 
	        				"        \"d411d8ebc8120090fdb312ea\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"d411d8ebc8120090fdb312ea[name]\"," + 
	        				"                \"name\": \"Name\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"d411d8ebc8120090fdb312ea[name]\"," + 
	        				"                    \"name\": \"Name\"," + 
	        				"                    \"value\": \"test\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"left\": 4500," + 
	        				"            \"top\": 4500," + 
	        				"            \"width\": 48," + 
	        				"            \"height\": 48," + 
	        				"            \"zIndex\": 16003," + 
	        				"            \"type\": \"Messenger\"," + 
	        				"            \"attributes\": {" + 
	        				"                \"646c1466c4bf34e8267c4f26\": {" + 
	        				"                    \"id\": \"d411d8ebc8120090fdb312ea[messenger type]\"," + 
	        				"                    \"name\": \"Messenger Type\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"d411d8ebc8120090fdb312ea[messenger type]\"," + 
	        				"                        \"name\": \"Messenger Type\"," + 
	        				"                        \"value\": \"Rocket.Chat\"" + 
	        				"                    }," + 
	        				"                    \"option\": \"Rocket.Chat\"" + 
	        				"                }," + 
	        				"                \"fac7a2b850919e041f655919\": {" + 
	        				"                    \"id\": \"d411d8ebc8120090fdb312ea[name]\"," + 
	        				"                    \"name\": \"Name\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"d411d8ebc8120090fdb312ea[name]\"," + 
	        				"                        \"name\": \"Name\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }," + 
	        				"                \"4d07dc2fa7f97c4ef5834078\": {" + 
	        				"                    \"id\": \"d411d8ebc8120090fdb312ea[authentication token]\"," + 
	        				"                    \"name\": \"Authentication Token\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"d411d8ebc8120090fdb312ea[authentication token]\"," + 
	        				"                        \"name\": \"Authentication Token\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }," + 
	        				"                \"f7efda10fb976eff66861b7d\": {" + 
	        				"                    \"id\": \"d411d8ebc8120090fdb312ea[rasa nlu url]\"," + 
	        				"                    \"name\": \"Rasa NLU URL\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"d411d8ebc8120090fdb312ea[rasa nlu url]\"," + 
	        				"                        \"name\": \"Rasa NLU URL\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }" + 
	        				"            }" + 
	        				"        }," + 
	        				"        \"126ac8abe149e3d4198da02f\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"126ac8abe149e3d4198da02f[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"126ac8abe149e3d4198da02f[label]\"," + 
	        				"                    \"name\": \"Label\"," + 
	        				"                    \"value\": \"\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"left\": 4500," + 
	        				"            \"top\": 4598," + 
	        				"            \"width\": 48," + 
	        				"            \"height\": 48," + 
	        				"            \"zIndex\": 16004," + 
	        				"            \"type\": \"Incoming Message\"," + 
	        				"            \"attributes\": {" + 
	        				"                \"869849f65db9be737e99bd24\": {" + 
	        				"                    \"id\": \"126ac8abe149e3d4198da02f[intent keyword]\"," + 
	        				"                    \"name\": \"Intent Keyword\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"126ac8abe149e3d4198da02f[intent keyword]\"," + 
	        				"                        \"name\": \"Intent Keyword\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }" + 
	        				"            }" + 
	        				"        }," + 
	        				"        \"54b9ef831e9e6d8e0d9bf936\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"54b9ef831e9e6d8e0d9bf936[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"54b9ef831e9e6d8e0d9bf936[label]\"," + 
	        				"                    \"name\": \"Label\"," + 
	        				"                    \"value\": \"\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"left\": 4500," + 
	        				"            \"top\": 4696," + 
	        				"            \"width\": 48," + 
	        				"            \"height\": 48," + 
	        				"            \"zIndex\": 16005," + 
	        				"            \"type\": \"Chat Response\"," + 
	        				"            \"attributes\": {" + 
	        				"                \"a5844a8bde0637231a942b69\": {" + 
	        				"                    \"id\": \"54b9ef831e9e6d8e0d9bf936[message]\"," + 
	        				"                    \"name\": \"Message\"," + 
	        				"                    \"value\": {" + 
	        				"                        \"id\": \"54b9ef831e9e6d8e0d9bf936[message]\"," + 
	        				"                        \"name\": \"Message\"," + 
	        				"                        \"value\": \"test\"" + 
	        				"                    }" + 
	        				"                }" + 
	        				"            }" + 
	        				"        }" + 
	        				"    }," + 
	        				"    \"edges\": {" + 
	        				"        \"f69b4c865370ef6c80fff6f6\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"f69b4c865370ef6c80fff6f6[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"f69b4c865370ef6c80fff6f6[label]\"," + 
	        				"                    \"name\": \"Label\"," + 
	        				"                    \"value\": \"\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"source\": \"f44c40de87defc07002f877d\"," + 
	        				"            \"target\": \"ccbea5e25731d8e6537a1947\"," + 
	        				"            \"attributes\": {}," + 
	        				"            \"type\": \"has\"" + 
	        				"        }," + 
	        				"        \"37d23192979cc85722c6b969\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"37d23192979cc85722c6b969[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"37d23192979cc85722c6b969[label]\"," + 
	        				"                    \"name\": \"Label\"," + 
	        				"                    \"value\": \"\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"source\": \"ccbea5e25731d8e6537a1947\"," + 
	        				"            \"target\": \"d411d8ebc8120090fdb312ea\"," + 
	        				"            \"attributes\": {}," + 
	        				"            \"type\": \"has\"" + 
	        				"        }," + 
	        				"        \"3f1c74c2dbab055f129cf1d8\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"3f1c74c2dbab055f129cf1d8[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"3f1c74c2dbab055f129cf1d8[label]\"," + 
	        				"                    \"name\": \"Label\"," + 
	        				"                    \"value\": \"\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"source\": \"d411d8ebc8120090fdb312ea\"," + 
	        				"            \"target\": \"126ac8abe149e3d4198da02f\"," + 
	        				"            \"attributes\": {}," + 
	        				"            \"type\": \"generates\"" + 
	        				"        }," + 
	        				"        \"8120f9a5790fa5688b593db5\": {" + 
	        				"            \"label\": {" + 
	        				"                \"id\": \"8120f9a5790fa5688b593db5[label]\"," + 
	        				"                \"name\": \"Label\"," + 
	        				"                \"value\": {" + 
	        				"                    \"id\": \"8120f9a5790fa5688b593db5[label]\"," + 
	        				"                    \"name\": \"Label\"," + 
	        				"                    \"value\": \"\"" + 
	        				"                }" + 
	        				"            }," + 
	        				"            \"source\": \"126ac8abe149e3d4198da02f\"," + 
	        				"            \"target\": \"54b9ef831e9e6d8e0d9bf936\"," + 
	        				"            \"attributes\": {}," + 
	        				"            \"type\": \"triggers\"" + 
	        				"        }" + 
	        				"    }" + 
	        				"}";
	        
			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
	        JSONObject obj = (JSONObject) parser.parse(testJSONString);
	        
	        
	        // Connect to service
			MiniClient c = new MiniClient();
			c.setLogin(testAgent.getIdentifier(), testPass);
			c.setConnectorEndpoint(connector.getHttpEndpoint());
			
			// Store the model under 5 different names
			for (int i = 0; i < 5; i++) {
				ClientResponse response = c.sendRequest("POST", mainPath + "models/test" + i, obj.toJSONString(JSONStyle.NO_COMPRESS), "application/json;charset=UTF-8", "*/*", new HashMap<String, String>());
				System.out.println("Model " + i + ": " + response.getResponse());
				Assert.assertEquals("Model " + i + ": bad request", 200, response.getHttpCode());
			}
			
			// Check if fetched model names match the names of the previously stored models
			ClientResponse response = c.sendRequest("GET", mainPath + "models", "");
			Assert.assertEquals("bad request", 200, response.getHttpCode());
			System.out.println("Models: " + response.getResponse());
			
			// Check if models can be fetched successfully
			response = c.sendRequest("GET", mainPath + "models/test1", "");
			Assert.assertEquals("bad request", 200, response.getHttpCode());
			System.out.println("Models: " + response.getResponse());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
