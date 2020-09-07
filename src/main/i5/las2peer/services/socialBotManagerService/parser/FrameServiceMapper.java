package i5.las2peer.services.socialBotManagerService.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.services.socialBotManagerService.model.ActionType;
import i5.las2peer.services.socialBotManagerService.model.Frame;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunction;
import i5.las2peer.services.socialBotManagerService.model.ServiceFunctionAttribute;
import i5.las2peer.services.socialBotManagerService.model.Slot;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import net.minidev.json.JSONObject;

public class FrameServiceMapper {

    public Frame retrieve(Frame frame, ServiceFunction action2) {

	System.out.println("retrieve");
	System.out.println(action2.getFunctionName());
	System.out.println(action2.getServiceName());
	
	ServiceFunction action = getServiceFunction("", action2.getFunctionName(), action2.getServiceName());

	frame.setServiceFunction(action);
	System.out.println("size: " + action.getAttributes().size());
	for (ServiceFunctionAttribute attr : action.getAttributes()) {
		System.out.println("add slot");
	    Slot slot = new Slot();
	    slot.setParameter(attr);
	    slot.setName("inform_" + attr.getName());

	    String message = "We need to know - Name: ".concat(attr.getName());
	    if (attr.getDescription() != null)
		message = message.concat(" Description: ").concat(attr.getDescription());
	    if (attr.getContentType() != null)
		message = message.concat("ContentType: ").concat(attr.getContentType());
	    if (attr.getExample() != null)
		message = message.concat("Example: ").concat(attr.getExample());
	    if (attr.getEnumList() != null && attr.getEnumList().size() > 0) {
		message = message.concat("possible answers: ");
		for (String enu : attr.getEnumList()) {
		    message = message.concat(", ").concat(enu);
		}
		slot.setEntity(attr.getName());
	    }
	    slot.setMessage(message);

	    for (ServiceFunctionAttribute subattr : attr.getChildAttributes()) {
		Slot childSlot = new Slot();
		childSlot.setParameter(subattr);
		childSlot.setName("inform_" + attr.getName() + "_" + subattr.getName());

		message = "We need to know - Name: ".concat(subattr.getName());
		if (subattr.getDescription() != null)
		    message = message.concat(" Description: ").concat(subattr.getDescription());
		if (subattr.getContentType() != null)
		    message = message.concat("ContentType: ").concat(subattr.getContentType());
		if (subattr.getExample() != null)
		    message = message.concat("Example: ").concat(subattr.getExample());
		if (subattr.getEnumList() != null && subattr.getEnumList().size() > 0) {
		    message = message.concat("possible answers: ");
		    for (String enu : subattr.getEnumList()) {
			message = message.concat(", ").concat(enu);
		    }
		    childSlot.setEntity(subattr.getName());
		}
		childSlot.setMessage(message);
		slot.addChildren(childSlot);
	    }

	}

	return frame;
    }

    public ServiceFunction getServiceFunction(String baseUrl, String functionPath, String httpMethod) {
	System.out.println("getServiceFUnction");
	String swaggerUrl = "https://petstore3.swagger.io/api/v3/openapi.json";
	System.out.println("parser start reading");
	
	OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerUrl);
	
	System.out.println("parser readed");
	Operation operation = getOperationByPath(openAPI, functionPath, httpMethod);
	ServiceFunction action = retrieveServiceFunction(openAPI, operation);
	action.setActionType(ActionType.REST);
	action.setHttpMethod(httpMethod);
	action.setFunctionPath(baseUrl + functionPath);
	action.setFunctionName(operation.getOperationId());

	return action;

    }

    public Operation getOperationByOperationId(OpenAPI openAPI, String operationId) {

	Operation operation = new Operation();
	for (PathItem pathItem : openAPI.getPaths().values()) {
	    if (pathItem.getGet() != null && pathItem.getGet().getOperationId().equals(operationId))
		return pathItem.getGet();
	    if (pathItem.getPost() != null && pathItem.getPost().getOperationId().equals(operationId))
		return pathItem.getPost();
	    if (pathItem.getPut() != null && pathItem.getPut().getOperationId().equals(operationId))
		return pathItem.getPut();
	    if (pathItem.getDelete() != null && pathItem.getDelete().getOperationId().equals(operationId))
		return pathItem.getDelete();
	}
	return null;
    }

    public Operation getOperationByPath(OpenAPI openAPI, String path, String httpMethod) {
	System.out.println("op by path");
	// Path
	PathItem pathItem = openAPI.getPaths().get(path);
	if (pathItem == null)
	    System.out.println("invalid path: " + path);

	// HTTP method
	Operation operation = null;
	switch (httpMethod) {
	case "get":
	    operation = openAPI.getPaths().get(path).getGet();
	    break;
	case "post":
	    System.out.println(openAPI.getPaths());
	    operation = openAPI.getPaths().get(path).getPost();
	    break;
	case "put":
	    operation = openAPI.getPaths().get(path).getPut();
	    break;
	case "delete":
	    operation = openAPI.getPaths().get(path).getDelete();
	    break;
	default:
	    System.out.println("invalid http method: " + httpMethod);
	}

	if (operation == null)
	    System.out.println("no operation found");
	return operation;

    }

    public ServiceFunction retrieveServiceFunction(OpenAPI openAPI, Operation operation) {
	ServiceFunction action = new ServiceFunction();
	String contentType = "application/json";
	System.out.println("retrieve service function");
	// Path Parameters
	List<Parameter> parameters = operation.getParameters();
	if (operation.getParameters() != null) {

	    for (Parameter parameter : operation.getParameters()) {

		ServiceFunctionAttribute attr = new ServiceFunctionAttribute();
		if (parameter.getName() != null)
		    attr.setName(parameter.getName());
		if (parameter.getDescription() != null)
		    attr.setDescription(parameter.getDescription());
		if (parameter.getRequired() != null)
		    attr.setRequired(parameter.getRequired());
		if (parameter.getIn() != null)
		    attr.setContentType(parameter.getIn());

		if (parameter.getSchema() != null) {
		    attr = createServiceFunctionAttribute(parameter.getSchema(), attr);
		}
		action.addAttribute(attr);
	    }
	}

	// Body
	if (operation.getRequestBody() != null) {

	    // Content MediaType
	    MediaType mediaType = operation.getRequestBody().getContent().get(contentType);
	    if (mediaType == null) {
		System.out.println("invalid media type: " + contentType);
	    } else {
		action.setConsumes(contentType);
	    }

	    // ContentBody
	    ServiceFunctionAttribute bodyAttribute = new ServiceFunctionAttribute();
	    Schema schema = mediaType.getSchema();
	    if (schema.get$ref() != null) {

		String prefix = "#/components/schemas/";
		if (schema.get$ref().startsWith(prefix))
		    bodyAttribute.setName(schema.get$ref().split(prefix)[1]);

		bodyAttribute = addChildrenByRef(openAPI, schema.get$ref(), bodyAttribute);
		bodyAttribute.setContentType("body");

	    }

	    action.addAttribute(bodyAttribute);

	}

	if (operation.getResponses().get("200").getContent().get(contentType) != null)
	    action.setProduces(contentType);

	return action;

    }

    public ServiceFunctionAttribute addChildrenByRef(OpenAPI openAPI, String ref, ServiceFunctionAttribute parentAttr) {
	return addChildrenByRef(openAPI, ref, parentAttr, 0);
    }

    public ServiceFunctionAttribute addChildrenByRef(OpenAPI openAPI, String ref, ServiceFunctionAttribute parentAttr,
	    int rec) {

	String prefix = "#/components/schemas/";
	if (ref != null && ref.startsWith(prefix))
	    ref = ref.split(prefix)[1];

	Map<String, Schema> properties = openAPI.getComponents().getSchemas().get(ref).getProperties();
	for (Map.Entry<String, Schema> pair : properties.entrySet()) {

	    String name = pair.getKey();
	    Schema schema = pair.getValue();

	    ServiceFunctionAttribute childAttr = new ServiceFunctionAttribute();
	    childAttr.setName(name);
	    childAttr.setContentType("child");

	    // nested schemas
	    if (schema.get$ref() != null) {
		if (rec > 8) {
		    System.out.println("to much nesting");
		} else {
		    String childRef = schema.get$ref();
		    childAttr = addChildrenByRef(openAPI, childRef, childAttr, rec + 1);
		}
		// values
	    } else {
		childAttr = createServiceFunctionAttribute(schema, childAttr);
	    }

	    parentAttr.addChildAttribute(childAttr);
	    childAttr.setParent(parentAttr);

	}
	return parentAttr;

    }

    private ServiceFunctionAttribute createServiceFunctionAttribute(Schema schema, ServiceFunctionAttribute attr) {

	switch (schema.getType()) {
	case "object":
	    break;
	case "integer":
	    attr.setContentType("integer");
	    break;
	case "string":
	    if (schema.getEnum() != null) {
		attr.setContentType("enum");
		attr.setEnumList(schema.getEnum());
		break;
	    }
	    attr.setContentType("string");
	    break;
	case "boolean":
	    attr.setContentType("boolean");
	    break;
	case "array":
	    attr.setContentType("array");
	    break;
	default:
	    System.out.println("unknown parameter content type");
	}

	// parameter description (optional)
	if (schema.getDescription() != null)
	    attr.setDescription(schema.getDescription());

	// parameter example value (optional)
	if (schema.getExample() != null)
	    attr.setExample(schema.getExample().toString());

	return attr;
    }

    private void performRESTAction(ServiceFunction action, JSONObject body) {

	// if (!sf.getActionType().equals(ActionType.SERVICE))
	// return;

	System.out.println("perform REST action");
	System.out.println(action.toString());

	MiniClient client = new MiniClient();
	client.setConnectorEndpoint(action.getServiceName());

	HashMap<String, String> headers = new HashMap<String, String>();

	ClientResponse r = client.sendRequest(action.getHttpMethod().toUpperCase(), action.getFunctionPath(),
		body.toJSONString(), action.getConsumes(), action.getProduces(), headers);

	System.out.println("Connect Success");
	System.out.println(r.getResponse());

    }

}
