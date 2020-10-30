package i5.las2peer.services.socialBotManagerService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;

public class TrainingDataTest {
	
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
	public void storeData() {
		String testString = "test";
		
		// Connect to service
		MiniClient c = new MiniClient();
		c.setLogin(testAgent.getIdentifier(), testPass);
		c.setConnectorEndpoint(connector.getHttpEndpoint());
		
		// Store test string
		ClientResponse response = c.sendRequest("POST", mainPath + "training/test", testString, new HashMap<String, String>());
		System.out.println(response.getResponse());
		Assert.assertEquals("bad request", 200, response.getHttpCode());
		
		// Check data can be fetched successfully
		response = c.sendRequest("GET", mainPath + "training/test", "");
		Assert.assertEquals("bad request", 200, response.getHttpCode());
		System.out.println(response.getResponse());
	}
}
