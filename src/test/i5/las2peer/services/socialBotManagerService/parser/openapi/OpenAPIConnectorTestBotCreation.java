package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;

public class OpenAPIConnectorTestBotCreation {

	//@Test
	public void ReadFunctionCreateBotV2Test() {

		ServiceFunction action = new ServiceFunction();
		action.setFunctionName("createBot");
		action.setServiceName("http://localhost:8080/sbfmanager/swagger.json");
		ServiceFunction result = OpenAPIConnector.readFunction(action);

		assertEquals("post", result.getHttpMethod());
		assertNotNull(result.getAttributes());

		assertEquals(1, result.getAttributes().size());
		Iterator<ServiceFunctionAttribute> iter = result.getAttributes().iterator();

		System.out.println(result);
		// bot object
		ServiceFunctionAttribute bot = iter.next();
		assertEquals("Bot", bot.getName());
		assertEquals(ParameterType.BODY, bot.getParameterType());
		assertNotNull(bot.getChildAttributes());
		assertEquals(4, bot.getChildAttributes().size());
		iter = bot.getChildAttributes().iterator();

		// bot name
		ServiceFunctionAttribute botName = iter.next();
		assertEquals("name", botName.getName());
		assertEquals("string", botName.getContentType());
		assertEquals(ParameterType.CHILD, botName.getParameterType());
		assertTrue(botName.isRequired());
		assertFalse(botName.isArray());

		// nlu module
		ServiceFunctionAttribute nluModule = iter.next();
		assertEquals("nluModule", nluModule.getName());

		// function
		ServiceFunctionAttribute function = iter.next();
		assertEquals("function", function.getName());
		iter = function.getChildAttributes().iterator();
		
		ServiceFunctionAttribute type = iter.next();
		assertEquals("type", type.getName());
		assertTrue(type.isRequired());
		assertFalse(type.isArray());
		assertEquals(ParameterType.DISCRIMINATOR, type.getParameterType());
		assertEquals("enum", type.getContentType());
		assertNotNull(type.getEnumList());
		List<String> enumList = type.getEnumList();
		assertEquals(3, enumList.size());
		assertEquals("AccessService", enumList.get(0));

		// acces service
		iter = function.getChildAttributes().iterator();

		
		// messenger
		ServiceFunctionAttribute messenger = iter.next();
		assertEquals("name", messenger.getName());
		assertEquals(ParameterType.CHILD, messenger.getParameterType());
		assertNotNull(messenger.getChildAttributes());
		assertEquals(3, messenger.getChildAttributes().size());
		assertTrue(messenger.isRequired());
		assertFalse(messenger.isArray());
		Iterator<ServiceFunctionAttribute> miter = messenger.getChildAttributes().iterator();

	}

	// @Test
	public void ReadFunctionCreateBotV2Test2() {

		ServiceFunction action = new ServiceFunction();
		action.setFunctionName("createBot");
		action.setServiceName("http://localhost:8080/sbfmanager/swagger.json");
		ServiceFunction result = OpenAPIConnector.readFunction(action);

		assertEquals("post", result.getHttpMethod());
		assertNotNull(result.getAttributes());

		assertEquals(1, result.getAttributes().size());
		Iterator<ServiceFunctionAttribute> iter = result.getAttributes().iterator();

		System.out.println(result);
		// Messenger
		ServiceFunctionAttribute messenger = iter.next();
		assertEquals("Messenger", messenger.getName());
		assertEquals(ParameterType.BODY, messenger.getParameterType());
		assertNotNull(messenger.getChildAttributes());
		assertEquals(4, messenger.getChildAttributes().size());
		iter = messenger.getChildAttributes().iterator();

		// type
		ServiceFunctionAttribute type = iter.next();
		assertEquals("type", type.getName());
		assertTrue(type.isRequired());
		assertFalse(type.isArray());
		assertEquals(ParameterType.DISCRIMINATOR, type.getParameterType());
		assertEquals("enum", type.getContentType());
		assertNotNull(type.getEnumList());
		List<String> enumList = type.getEnumList();
		assertEquals(2, enumList.size());
		assertEquals("Slack", enumList.get(0));
		assertEquals("Telegram", enumList.get(1));

		// token
		ServiceFunctionAttribute token = iter.next();
		assertEquals("token", token.getName());
		assertTrue(token.isRequired());
		assertFalse(token.isArray());
		assertEquals(ParameterType.CHILD, token.getParameterType());
		assertEquals("string", token.getContentType());
		assertEquals("Slack", token.getDiscriminator());

		// api name
		ServiceFunctionAttribute app = iter.next();
		assertEquals("appId", app.getName());
		assertTrue(app.isRequired());
		assertFalse(app.isArray());
		assertEquals(ParameterType.CHILD, app.getParameterType());
		assertEquals("string", app.getContentType());
		assertEquals("Slack", app.getDiscriminator());

		// token
		ServiceFunctionAttribute ttoken = iter.next();
		assertEquals("token", ttoken.getName());
		assertTrue(ttoken.isRequired());
		assertFalse(ttoken.isArray());
		assertEquals(ParameterType.CHILD, ttoken.getParameterType());
		assertEquals("string", ttoken.getContentType());
		assertEquals("Telegram", ttoken.getDiscriminator());

	}

}
