package i5.las2peer.services.socialBotManagerService.parser.openapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;

public class OpenAPIConnectorTest {

    @Test
    public void ReadFunctionTest() {

	ServiceFunction action = new ServiceFunction();
	action.setFunctionName("addPet");
	action.setServiceName("https://petstore3.swagger.io");
	ServiceFunction result = OpenAPIConnector.readFunction(action);

	assertEquals("post", result.getHttpMethod());
	assertNotNull(result.getAttributes());

	assertEquals(1, result.getAttributes().size());
	Iterator<ServiceFunctionAttribute> iter = result.getAttributes().iterator();
	
	// pet object
	ServiceFunctionAttribute pet = iter.next();
	assertEquals(ParameterType.BODY, pet.getParameterType());	
	iter = pet.getChildAttributes().iterator();
	
	// pet id
	ServiceFunctionAttribute petId = iter.next();
	assertEquals("10", petId.getExample());
	assertEquals("integer", petId.getContentType());
	assertEquals(ParameterType.CHILD, petId.getParameterType());
	assertFalse(petId.isRequired());
	assertFalse(petId.isArray());
	
	// pet name
	ServiceFunctionAttribute petName = iter.next();
	assertEquals("doggie", petName.getExample());
	assertEquals("string", petName.getContentType());
	assertEquals(ParameterType.CHILD, petName.getParameterType());
	assertTrue(petName.isRequired());
	assertFalse(petName.isArray());
	
	//category
	ServiceFunctionAttribute category = iter.next();
	assertEquals("object", category.getContentType());
	
	//photoUrls
	ServiceFunctionAttribute photoUrls = iter.next();
	assertEquals("photoUrls", photoUrls.getName());
	assertEquals("string", photoUrls.getContentType());
	assertTrue(photoUrls.isArray());
	
	//tags
	ServiceFunctionAttribute tags = iter.next();
	assertTrue(tags.isArray());
	assertFalse(tags.isRequired());
	
	//status
	ServiceFunctionAttribute status = iter.next();
	assertEquals("status", status.getName());
	assertEquals("pet status in the store", status.getDescription());
	assertFalse(status.isRequired());
	assertFalse(status.isArray());
	assertEquals(ParameterType.CHILD, status.getParameterType());
	assertEquals("enum", status.getContentType());
	assertNotNull(status.getEnumList());
	List<Object> enumList = status.getEnumList();
	assertEquals(3, enumList.size());
	assertEquals("available", enumList.get(0));
	assertEquals("pending", enumList.get(1));
	assertEquals("sold", enumList.get(2));
	
	
    }

}
